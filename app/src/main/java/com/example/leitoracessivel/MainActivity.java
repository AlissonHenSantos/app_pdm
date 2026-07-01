package com.example.leitoracessivel;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitoracessivel.adapter.ArtigoAdapter;
import com.example.leitoracessivel.model.database.AppDatabase;
import com.example.leitoracessivel.model.database.ArtigoDao;
import com.example.leitoracessivel.model.entities.Artigo;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements ArtigoAdapter.OnArtigoClickListener {

    public static final String EXTRA_ARTIGO_ID = "artigo_id";
    public static final String EXTRA_MODO = "modo";

    private RecyclerView recyclerView;
    private ArtigoAdapter adapter;
    private AppDatabase db;
    private ArtigoDao artigoDao;
    private List<Artigo> artigos;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<String> solicitarPermissao =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                solicitarPermissao.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getInstance(this);
        artigoDao = db.artigoDao();
        recyclerView = findViewById(R.id.recycler_artigos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_novo);
        fab.setOnClickListener(v -> abrirEditor(null));

        carregarArtigos();
        carregarArtigos();

        SharedPreferences prefs = getSharedPreferences("config", MODE_PRIVATE);

        if (prefs.getBoolean("lembrete", false)) {
            LembreteManager.agendar(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarArtigos();
    }

    private void carregarArtigos() {
        artigos = artigoDao.listarArtigos();
        if (adapter == null) {
            adapter = new ArtigoAdapter(artigos, this);
            recyclerView.setAdapter(adapter);
            registerForContextMenu(recyclerView);
        } else {
            adapter.atualizarLista(artigos);
        }
    }

    // ===== Menu AppBar =====
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_configuracoes) {
            startActivity(new Intent(this, ConfigActivity.class));
            return true;
        } else if (id == R.id.menu_sobre) {
            Snackbar.make(recyclerView,
                    "LeitorAcessível v1.0 — Tecnologia Assistiva",
                    Snackbar.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.menu_sair) {
        auth.signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        return true;
    }
        return super.onOptionsItemSelected(item);
    }

    // ===== Menu de Contexto =====
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int pos = adapter.getPosicaoSelecionada();
        if (pos < 0) return false;
        Artigo artigo = adapter.getArtigo(pos);

        int id = item.getItemId();
        if (id == R.id.menu_context_ouvir) {
            abrirPlayer(artigo);
            return true;
        } else if (id == R.id.menu_context_editar) {
            abrirEditor(artigo);
            return true;
        } else if (id == R.id.menu_context_compartilhar) {
            compartilhar(artigo);
            return true;
        } else if (id == R.id.menu_context_deletar) {
            deletarArtigo(artigo);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    // ===== Callbacks do Adapter =====
    @Override
    public void onArtigoClick(Artigo artigo) {
        abrirPlayer(artigo);
    }

    @Override
    public void onArtigoEdit(Artigo artigo) {
        abrirEditor(artigo);
    }

    @Override
    public void onArtigoDelete(Artigo artigo) {
        deletarArtigo(artigo);
    }

    @Override
    public void onArtigoShare(Artigo artigo) {
        compartilhar(artigo);
    }

    // ===== Ações =====
    private void abrirEditor(Artigo artigo) {
        Intent intent = new Intent(this, EditorActivity.class);
        if (artigo != null) {
            intent.putExtra(EXTRA_ARTIGO_ID, artigo.getId());
            intent.putExtra(EXTRA_MODO, "editar");
        } else {
            intent.putExtra(EXTRA_MODO, "novo");
        }
        startActivity(intent);
    }

    private void abrirPlayer(Artigo artigo) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(EXTRA_ARTIGO_ID, artigo.getId());
        startActivity(intent);
    }

    private void compartilhar(Artigo artigo) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, artigo.getTitulo());
        intent.putExtra(Intent.EXTRA_TEXT, artigo.getTitulo() + "\n\n" + artigo.getConteudo());
        startActivity(Intent.createChooser(intent, "Compartilhar artigo via…"));
    }

    private void deletarArtigo(Artigo artigo) {
        artigoDao.deletarArtigo(artigo);
        Toast.makeText(this, "Artigo deletado", Toast.LENGTH_SHORT).show();
        Snackbar.make(recyclerView, "\"" + artigo.getTitulo() + "\" removido",
                Snackbar.LENGTH_LONG)
                .setAction("Desfazer", v -> {
                    artigoDao.inserirArtigo(artigo);
                    carregarArtigos();
                }).show();
        carregarArtigos();
    }


}
