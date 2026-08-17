package com.example.studyflow;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigationrail.NavigationRailView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    NavigationBarView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Carrega o tema salvo antes de criar a tela
        SharedPreferences prefs = getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        int tema = prefs.getInt("app_theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(tema);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        navView = findViewById(R.id.nav_view);

        // 1. Criar o canal de notificações logo que o app abre
        NotificacaoHelper.criarCanalNotificacao(this);

        // 2. Pedir permissão para enviar notificações (apenas para Android 13 ou mais novo)
        pedirPermissaoNotificacao();

        // 3. Agendar o verificador de tarefas em segundo plano para rodar a cada 15 minutos
        agendarVerificadorTarefas();

        navView.setOnItemSelectedListener(item -> {
            // Reabilita a seleção visual quando um item é clicado
            item.setCheckable(true);
            
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_tarefas) {
                selectedFragment = new TarefasFragment();
            } else if (itemId == R.id.nav_anotacoes) {
                selectedFragment = new AnotacoesFragment();
            } else if (itemId == R.id.nav_metas) {
                selectedFragment = new MetasFragment();
            } else if (itemId == R.id.nav_checklist) {
                selectedFragment = new CheckListFragment();
            }

            //Lista adicionar
            if (item.getItemId() == R.id.nav_adicionar) { // O ID do seu botão de "+" no menu inferior
                // Cria e exibe a listinha deslizando de baixo para cima
                MenuMaisBottomSheet bottomSheet = new MenuMaisBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "MenuMaisBottomSheet");
                return true;
            }
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    /**
     * Mostra ou oculta o menu inferior/lateral para ganhar mais espaço na tela.
     */
    public void setBottomNavigationVisibility(int visibility) {
        if (navView != null) {
            navView.setVisibility(visibility);
        }
    }

    /**
     * Pede permissão ao usuário para mostrar notificações.
     * Necessário a partir do Android 13.
     */
    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                // Registra o pedido de permissão
                ActivityResultLauncher<String> requestPermissionLauncher =
                        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                            if (!isGranted) {
                                Toast.makeText(this, "Você não receberá alertas de tarefas.", Toast.LENGTH_LONG).show();
                            }
                        });

                // Abre a janelinha do Android perguntando se o usuário aceita
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Configura o WorkManager para rodar o nosso código de verificação
     * periodicamente, garantindo que as notificações sejam enviadas.
     */
    private void agendarVerificadorTarefas() {
        // Criamos um pedido de trabalho que se repete a cada 15 minutos (mínimo permitido pelo Android)
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                NotificacaoWorker.class, 
                15, TimeUnit.MINUTES
        ).build();

        // Mandamos o Android começar a executar esse trabalho.
        // KEEP garante que se já estiver rodando, ele não cria outro duplicado.
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "VerificadorPrazos",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
