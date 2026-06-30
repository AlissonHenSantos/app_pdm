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
import android.app.AlertDialog;
import android.widget.EditText;

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
        Button btnEsqueciSenha = findViewById(R.id.btn_esqueci_senha);
        btnEsqueciSenha.setOnClickListener(v -> mostrarDialogRecuperarSenha());

        btnLogin.setOnClickListener(v -> fazerLogin());
        btnIrCadastro.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
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

    private void mostrarDialogRecuperarSenha() {
        final EditText input = new EditText(this);
        input.setHint("Digite seu e-mail");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // pré-preenche se o usuário já tiver digitado algo no campo de login
        String emailAtual = String.valueOf(editEmail.getText()).trim();
        if (!TextUtils.isEmpty(emailAtual)) {
            input.setText(emailAtual);
            input.setSelection(emailAtual.length());
        }

        new AlertDialog.Builder(this)
                .setTitle("Recuperar senha")
                .setMessage("Informe o e-mail cadastrado. Enviaremos um link para redefinir sua senha.")
                .setView(input)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(this, "Digite um e-mail válido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    enviarEmailRecuperacao(email);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarEmailRecuperacao(String email) {
        progress.setVisibility(android.view.View.VISIBLE);
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(android.view.View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "E-mail de recuperação enviado! Verifique sua caixa de entrada.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Erro ao enviar: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}