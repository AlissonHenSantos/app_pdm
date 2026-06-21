package com.example.leitoracessivel;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.leitoracessivel.model.database.DatabaseHelper;
import com.example.leitoracessivel.model.entities.Artigo;
import com.google.android.material.snackbar.Snackbar;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditorActivity extends AppCompatActivity {

    private EditText etTitulo, etConteudo;
    private DatabaseHelper dbHelper;
    private Artigo artigoAtual;
    private boolean modoEdicao = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Aceita TXT, PDF e DOCX
    private final ActivityResultLauncher<String[]> abrirArquivo =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) processarArquivo(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Inicializa PDFBox (necessário uma vez)
        PDFBoxResourceLoader.init(getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etTitulo = findViewById(R.id.et_titulo);
        etConteudo = findViewById(R.id.et_conteudo);
        dbHelper = new DatabaseHelper(this);

        String modo = getIntent().getStringExtra(MainActivity.EXTRA_MODO);
        int artigoId = getIntent().getIntExtra(MainActivity.EXTRA_ARTIGO_ID, -1);

        if ("editar".equals(modo) && artigoId != -1) {
            modoEdicao = true;
            artigoAtual = dbHelper.buscarArtigo(artigoId);
            if (artigoAtual != null) {
                etTitulo.setText(artigoAtual.getTitulo());
                etConteudo.setText(artigoAtual.getConteudo());
            }
            setTitle("Editar artigo");
        } else {
            setTitle("Novo artigo");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_salvar) {
            salvar();
            return true;
        } else if (id == R.id.menu_abrir_arquivo) {
            // Abre seletor aceitando TXT, PDF e DOCX
            abrirArquivo.launch(new String[]{
                "text/plain",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/msword"
            });
            return true;
        } else if (id == R.id.menu_limpar) {
            etTitulo.setText("");
            etConteudo.setText("");
            Toast.makeText(this, "Campos limpos", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Detecta o tipo do arquivo e chama o leitor certo */
    private void processarArquivo(Uri uri) {
        String tipo = getContentResolver().getType(uri);
        String nome = obterNomeArquivo(uri);

        // Sugestão de título pelo nome do arquivo
        if (etTitulo.getText().toString().trim().isEmpty() && nome != null) {
            etTitulo.setText(nome);
        }

        Snackbar.make(etConteudo, "Lendo arquivo, aguarde…", Snackbar.LENGTH_INDEFINITE).show();

        // Processamento em background (PDF/DOCX podem ser pesados)
        executor.execute(() -> {
            String texto = "";
            String erro = null;

            try {
                if (tipo != null && tipo.contains("pdf")) {
                    texto = lerPDF(uri);
                } else if (tipo != null && (tipo.contains("wordprocessingml") || tipo.contains("msword"))) {
                    texto = lerDOCX(uri);
                } else {
                    texto = lerTXT(uri);
                }
            } catch (Exception e) {
                erro = e.getMessage();
            }

            final String textoFinal = texto;
            final String erroFinal = erro;

            handler.post(() -> {
                if (erroFinal != null) {
                    Toast.makeText(this, "Erro ao ler arquivo: " + erroFinal, Toast.LENGTH_LONG).show();
                } else if (textoFinal.isEmpty()) {
                    Toast.makeText(this, "Arquivo vazio ou sem texto extraível", Toast.LENGTH_LONG).show();
                } else {
                    etConteudo.setText(textoFinal);
                    Snackbar.make(etConteudo,
                            "✅ Arquivo carregado! (" + textoFinal.length() + " caracteres)",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    /** Lê arquivo .txt */
    private String lerTXT(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = reader.readLine()) != null) {
            sb.append(linha).append("\n");
        }
        reader.close();
        return sb.toString().trim();
    }

    /** Lê arquivo .pdf usando PDFBox Android */
    private String lerPDF(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        PDDocument document = PDDocument.load(is);
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(document);
        document.close();
        is.close();
        return texto.trim();
    }

    /** Lê arquivo .docx usando Apache POI */
    private String lerDOCX(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        XWPFDocument document = new XWPFDocument(is);
        List<XWPFParagraph> paragrafos = document.getParagraphs();
        StringBuilder sb = new StringBuilder();
        for (XWPFParagraph p : paragrafos) {
            String texto = p.getText();
            if (texto != null && !texto.trim().isEmpty()) {
                sb.append(texto).append("\n");
            }
        }
        document.close();
        is.close();
        return sb.toString().trim();
    }

    /** Extrai nome limpo do arquivo a partir da URI */
    private String obterNomeArquivo(Uri uri) {
        String nome = uri.getLastPathSegment();
        if (nome == null) return null;
        if (nome.contains("/")) nome = nome.substring(nome.lastIndexOf("/") + 1);
        // Remove extensão
        int dot = nome.lastIndexOf(".");
        if (dot > 0) nome = nome.substring(0, dot);
        return nome;
    }

    private void salvar() {
        String titulo = etTitulo.getText().toString().trim();
        String conteudo = etConteudo.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Informe um título");
            etTitulo.requestFocus();
            return;
        }
        if (conteudo.isEmpty()) {
            etConteudo.setError("Informe o conteúdo");
            etConteudo.requestFocus();
            return;
        }

        if (modoEdicao && artigoAtual != null) {
            artigoAtual.setTitulo(titulo);
            artigoAtual.setConteudo(conteudo);
            dbHelper.atualizarArtigo(artigoAtual);
            Toast.makeText(this, "Artigo atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            Artigo novo = new Artigo(titulo, conteudo);
            long id = dbHelper.inserirArtigo(novo);
            if (id > 0) {
                Toast.makeText(this, "Artigo salvo!", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
