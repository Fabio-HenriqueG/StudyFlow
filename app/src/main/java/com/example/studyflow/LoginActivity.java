package com.example.studyflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private boolean isModoCadastro = false;

    private TextView txtHeader;
    private TextInputLayout layoutNome;
    private TextInputEditText editNome, editEmail, editSenha;
    private Button btnAcao, btnConvidado;
    private TextView btnAlternar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        // Se o usuário já está logado (e não for anônimo, ou se já tiver usuário ativo)
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            abrirMain();
            return;
        }

        setContentView(R.layout.activity_login);

        txtHeader = findViewById(R.id.txtLoginHeader);
        layoutNome = findViewById(R.id.inputLayoutNome);
        editNome = findViewById(R.id.editLoginNome);
        editEmail = findViewById(R.id.editLoginEmail);
        editSenha = findViewById(R.id.editLoginSenha);
        btnAcao = findViewById(R.id.btnAcaoLogin);
        btnConvidado = findViewById(R.id.btnEntrarConvidado);
        btnAlternar = findViewById(R.id.btnAlternarModo);
        progressBar = findViewById(R.id.progressLogin);

        btnAlternar.setOnClickListener(v -> alternarModo());
        btnAcao.setOnClickListener(v -> processarAutenticacao());
        btnConvidado.setOnClickListener(v -> entrarComoConvidado());
    }

    private void alternarModo() {
        isModoCadastro = !isModoCadastro;
        if (isModoCadastro) {
            txtHeader.setText("Criar uma Conta");
            layoutNome.setVisibility(View.VISIBLE);
            btnAcao.setText("Cadastrar");
            btnAlternar.setText("Já possui conta? Faça Login");
        } else {
            txtHeader.setText("Entrar na sua Conta");
            layoutNome.setVisibility(View.GONE);
            btnAcao.setText("Entrar");
            btnAlternar.setText("Não tem conta? Cadastre-se aqui");
        }
    }

    private void processarAutenticacao() {
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        String senha = editSenha.getText() != null ? editSenha.getText().toString().trim() : "";

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o e-mail e a senha.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (isModoCadastro) {
            String nome = editNome.getText() != null ? editNome.getText().toString().trim() : "";
            auth.createUserWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null && !nome.isEmpty()) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nome)
                                    .build();
                            user.updateProfile(profileUpdates);
                        }
                        setLoading(false);
                        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                        abrirMain();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Erro no cadastro: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            auth.signInWithEmailAndPassword(email, senha)
                    .addOnSuccessListener(authResult -> {
                        setLoading(false);
                        Toast.makeText(this, "Bem-vindo ao StudyFlow!", Toast.LENGTH_SHORT).show();
                        abrirMain();
                    })
                    .addOnFailureListener(e -> {
                        setLoading(false);
                        Toast.makeText(this, "Erro no login: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void entrarComoConvidado() {
        setLoading(true);
        if (auth.getCurrentUser() != null) {
            setLoading(false);
            abrirMain();
            return;
        }

        auth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    setLoading(false);
                    Toast.makeText(this, "Entrou em modo Convidado.", Toast.LENGTH_SHORT).show();
                    abrirMain();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Erro ao entrar como convidado: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAcao.setEnabled(!loading);
        btnConvidado.setEnabled(!loading);
        btnAlternar.setEnabled(!loading);
    }

    private void abrirMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
