package com.example.leitoracessivel;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputEditText editEmail, editSenha;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.edit_email);
        editSenha = findViewById(R.id.edit_senha);
        progress = findViewById(R.id.progress_login);

        Button btnLogin = findViewById(R.id.btn_login);
        Button btnIrCadastro = findViewById(R.id.btn_ir_cadastro);

        btnLogin.setOnClickListener(v -> fazerLogin());
        btnIrCadastro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Se já houver usuário logado, pula direto para a MainActivity
        if (auth.getCurrentUser() != null) {
            irParaMain();
        }
    }

    private void fazerLogin() {
        String email = String.valueOf(editEmail.getText()).trim();
        String senha = String.valueOf(editSenha.getText()).trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha e-mail e senha", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(android.view.View.VISIBLE);
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(android.view.View.GONE);
                    if (task.isSuccessful()) {
                        irParaMain();
                    } else {
                        Toast.makeText(this,
                                "Falha no login: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irParaMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}