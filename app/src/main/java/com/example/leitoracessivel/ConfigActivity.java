package com.example.leitoracessivel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

public class ConfigActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private SeekBar seekBarVelocidade;
    private TextView tvVelocidadeLabel;
    private RadioGroup rgIdioma;
    private Switch switchTema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configurações");
        }

        prefs = getSharedPreferences("config", MODE_PRIVATE);

        seekBarVelocidade = findViewById(R.id.seekbar_velocidade_config);
        tvVelocidadeLabel = findViewById(R.id.tv_velocidade_label);
        rgIdioma = findViewById(R.id.rg_idioma);
        switchTema = findViewById(R.id.switch_tema);

        carregarConfiguracoes();
        configurarListeners();
    }

    private void carregarConfiguracoes() {
        float velocidade = prefs.getFloat("velocidade", 1.0f);
        int progresso = (int)((velocidade - 0.5f) / 1.5f * 100);
        seekBarVelocidade.setProgress(progresso);
        tvVelocidadeLabel.setText(String.format("Velocidade: %.1fx", velocidade));

        String idioma = prefs.getString("idioma", "pt");
        if ("en".equals(idioma)) {
            ((RadioButton) rgIdioma.findViewById(R.id.rb_ingles)).setChecked(true);
        } else {
            ((RadioButton) rgIdioma.findViewById(R.id.rb_portugues)).setChecked(true);
        }

        boolean temaDark = prefs.getBoolean("tema_dark", false);
        switchTema.setChecked(temaDark);
    }

    private void configurarListeners() {
        seekBarVelocidade.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float vel = 0.5f + (progress / 100f) * 1.5f;
                tvVelocidadeLabel.setText(String.format("Velocidade: %.1fx", vel));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        switchTema.setOnCheckedChangeListener((btn, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
        });

        findViewById(R.id.btn_salvar_config).setOnClickListener(v -> salvarConfiguracoes());
    }

    private void salvarConfiguracoes() {
        float velocidade = 0.5f + (seekBarVelocidade.getProgress() / 100f) * 1.5f;
        String idioma = rgIdioma.getCheckedRadioButtonId() == R.id.rb_ingles ? "en" : "pt";
        boolean temaDark = switchTema.isChecked();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("velocidade", velocidade);
        editor.putString("idioma", idioma);
        editor.putBoolean("tema_dark", temaDark);
        editor.apply();


        Snackbar.make(seekBarVelocidade,
                "Configurações salvas com sucesso!",
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
