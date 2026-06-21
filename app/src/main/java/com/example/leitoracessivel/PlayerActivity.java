package com.example.leitoracessivel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import com.example.leitoracessivel.model.database.DatabaseHelper;
import com.example.leitoracessivel.model.entities.Artigo;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    private static final String UTTERANCE_ID_PLAY = "PLAY";
    private static final String UTTERANCE_ID_EXPORT = "EXPORT";

    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean reproduzindo = false;

    private Artigo artigoAtual;
    private DatabaseHelper dbHelper;

    private TextView tvTituloPlayer, tvConteudoPlayer, tvStatus;
    private Button btnPlay, btnStop, btnExportar;
    private SeekBar seekBarVelocidade;

    private float velocidade = 1.0f;
    private File arquivoExportado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvTituloPlayer = findViewById(R.id.tv_titulo_player);
        tvConteudoPlayer = findViewById(R.id.tv_conteudo_player);
        tvStatus = findViewById(R.id.tv_status);
        btnPlay = findViewById(R.id.btn_play);
        btnStop = findViewById(R.id.btn_stop);
        btnExportar = findViewById(R.id.btn_exportar);
        seekBarVelocidade = findViewById(R.id.seekbar_velocidade);

        dbHelper = new DatabaseHelper(this);

        int artigoId = getIntent().getIntExtra(MainActivity.EXTRA_ARTIGO_ID, -1);
        if (artigoId != -1) {
            artigoAtual = dbHelper.buscarArtigo(artigoId);
        }

        if (artigoAtual == null) {
            Toast.makeText(this, "Artigo não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTituloPlayer.setText(artigoAtual.getTitulo());
        tvConteudoPlayer.setText(artigoAtual.getConteudo());
        setTitle(artigoAtual.getTitulo());

        // Carrega velocidade do SharedPreferences
        velocidade = getSharedPreferences("config", MODE_PRIVATE)
                .getFloat("velocidade", 1.0f);
        int progresso = (int)((velocidade - 0.5f) / 1.5f * 100);
        seekBarVelocidade.setProgress(progresso);

        seekBarVelocidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean user) {
                velocidade = 0.5f + (progress / 100f) * 1.5f;
                tvStatus.setText(String.format("Velocidade: %.1fx", velocidade));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        inicializarTTS();
        configurarBotoes();
    }

    private void inicializarTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Tenta português do Brasil
                int result = tts.setLanguage(new Locale("pt", "BR"));
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.getDefault());
                    Toast.makeText(this, "Idioma PT-BR não disponível, usando padrão",
                            Toast.LENGTH_SHORT).show();
                }
                ttsReady = true;
                btnPlay.setEnabled(true);
                btnExportar.setEnabled(true);
                tvStatus.setText("Pronto para reproduzir");

                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        runOnUiThread(() -> {
                            reproduzindo = true;
                            btnPlay.setText("⏸ Pausar");
                            if (UTTERANCE_ID_EXPORT.equals(utteranceId)) {
                                tvStatus.setText("Exportando áudio…");
                            } else {
                                tvStatus.setText("Reproduzindo…");
                            }
                        });
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        runOnUiThread(() -> {
                            reproduzindo = false;
                            btnPlay.setText("▶ Reproduzir");
                            if (UTTERANCE_ID_EXPORT.equals(utteranceId)) {
                                tvStatus.setText("Áudio exportado!");
                                mostrarOpcaoCompartilhar();
                            } else {
                                tvStatus.setText("Reprodução concluída");
                            }
                        });
                    }

                    @Override
                    public void onError(String utteranceId) {
                        runOnUiThread(() -> {
                            reproduzindo = false;
                            btnPlay.setText("▶ Reproduzir");
                            tvStatus.setText("Erro na reprodução");
                            Toast.makeText(PlayerActivity.this,
                                    "Erro no TTS", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            } else {
                Toast.makeText(this, "Erro ao inicializar TTS", Toast.LENGTH_LONG).show();
                btnPlay.setEnabled(false);
                btnExportar.setEnabled(false);
            }
        });
    }

    private void configurarBotoes() {
        btnPlay.setEnabled(false);
        btnExportar.setEnabled(false);

        btnPlay.setOnClickListener(v -> {
            if (!ttsReady) return;
            if (reproduzindo) {
                tts.stop();
                reproduzindo = false;
                btnPlay.setText("▶ Reproduzir");
                tvStatus.setText("Parado");
            } else {
                reproduzir();
            }
        });

        btnStop.setOnClickListener(v -> {
            if (tts != null) tts.stop();
            reproduzindo = false;
            btnPlay.setText("▶ Reproduzir");
            tvStatus.setText("Parado");
        });

        btnExportar.setOnClickListener(v -> {
            if (!ttsReady) return;
            exportarAudio();
        });
    }

    private void reproduzir() {
        tts.setSpeechRate(velocidade);
        tts.setPitch(1.0f);
        String texto = artigoAtual.getTitulo() + ". " + artigoAtual.getConteudo();
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID_PLAY);
    }

    private void exportarAudio() {
        // ★ RECURSO EXTRA (item 10): synthesizeToFile — salva o TTS em arquivo .wav
        File dir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        if (dir == null) dir = getFilesDir();

        String nomeArquivo = artigoAtual.getTitulo()
                .replaceAll("[^a-zA-Z0-9_\\-]", "_")
                .substring(0, Math.min(artigoAtual.getTitulo().length(), 30));
        arquivoExportado = new File(dir, nomeArquivo + ".wav");

        tts.setSpeechRate(velocidade);
        String texto = artigoAtual.getTitulo() + ". " + artigoAtual.getConteudo();

        int result = tts.synthesizeToFile(texto, null, arquivoExportado, UTTERANCE_ID_EXPORT);
        if (result == TextToSpeech.SUCCESS) {
            tvStatus.setText("Iniciando exportação…");
            Snackbar.make(btnExportar, "Exportando áudio, aguarde…",
                    Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Erro ao exportar áudio", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarOpcaoCompartilhar() {
        if (arquivoExportado == null || !arquivoExportado.exists()) return;

        Snackbar.make(btnExportar, "Áudio salvo: " + arquivoExportado.getName(),
                Snackbar.LENGTH_LONG)
                .setAction("Compartilhar", v -> compartilharAudio())
                .show();
    }

    private void compartilharAudio() {
        if (arquivoExportado == null || !arquivoExportado.exists()) {
            Toast.makeText(this, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", arquivoExportado);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/wav");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, artigoAtual.getTitulo());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Compartilhar áudio via…"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.menu_compartilhar_texto) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, artigoAtual.getConteudo());
            startActivity(Intent.createChooser(intent, "Compartilhar texto via…"));
            return true;
        } else if (id == R.id.menu_compartilhar_audio) {
            compartilharAudio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
