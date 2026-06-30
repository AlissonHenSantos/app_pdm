package com.example.leitoracessivel;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText editEmail, editSenha, editConfirmar;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.edit_email_cadastro);
        editSenha = findViewById(R.id.edit_senha_cadastro);
        editConfirmar = findViewById(R.id.edit_senha_confirmar);
        progress = findViewById(R.id.progress_cadastro);

        Button btnCadastrar = findViewById(R.id.btn_cadastrar);
        btnCadastrar.setOnClickListener(v -> cadastrar());
    }

    private void cadastrar() {
        String email = String.valueOf(editEmail.getText()).trim();
        String senha = String.valueOf(editSenha.getText()).trim();
        String confirmar = String.valueOf(editConfirmar.getText()).trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter ao menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!senha.equals(confirmar)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(android.view.View.VISIBLE);
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(android.view.View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Conta criada com sucesso! Faça login.",
                                Toast.LENGTH_LONG).show();
                        finish(); // volta para LoginActivity
                    } else {
                        Toast.makeText(this,
                                "Falha no cadastro: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}