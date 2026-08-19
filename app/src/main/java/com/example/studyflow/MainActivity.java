package com.example.studyflow;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.studyflow.data.AppDatabase;
import com.google.android.material.navigation.NavigationBarView;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NavigationBarView navView;

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

        // Inicialização segura do menu
        if (navView != null) {
            configurarNavegacao();
            // Desmarca qualquer item ao iniciar (Home é o padrão)
            navView.post(this::desmarcarMenu);
        }

        configurarBotaoVoltar();

        // Vigia o ciclo de vida dos fragmentos para sincronizar o menu automaticamente
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof HomeFragment) {
                    desmarcarMenu();
                }
            }
        }, false);

        // Inicializações de suporte
        NotificacaoHelper.criarCanalNotificacao(this);
        pedirPermissaoNotificacao();
        agendarVerificadorTarefas();
    }

    private void configurarNavegacao() {
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_adicionar) {
                new MenuMaisBottomSheet().show(getSupportFragmentManager(), "MenuMaisBottomSheet");
                return false; // Não marca o botão "+"
            }

            verificarDadosENavegar(itemId);
            return true;
        });
    }

    private void configurarBotaoVoltar() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment atual = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (atual != null && !(atual instanceof HomeFragment)) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new HomeFragment())
                            .commit();
                    desmarcarMenu();
                } else {
                    setEnabled(false);
                    onBackPressed();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void verificarDadosENavegar(int itemId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            Fragment destino = null;
            boolean vazio = false;

            try {
                if (itemId == R.id.nav_tarefas) {
                    vazio = db.tarefaDao().contarTodas() == 0;
                    destino = vazio ? new CriaTarefaFragment() : new TarefasFragment();
                } else if (itemId == R.id.nav_anotacoes) {
                    vazio = db.anotacaoDao().contarTodas() == 0;
                    destino = vazio ? new EditorAnotacaoFragment() : new AnotacoesFragment();
                } else if (itemId == R.id.nav_metas) {
                    vazio = db.metaDao().contarTodas() == 0;
                    destino = vazio ? new CriaMetaFragment() : new MetasFragment();
                } else if (itemId == R.id.nav_checklist) {
                    vazio = db.checklistDao().contarTodas() == 0;
                    destino = vazio ? new CriaChecklistFragment() : new CheckListFragment();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (destino == null) return;

            final boolean isVazio = vazio;
            final Fragment finalDestino = destino;

            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    if (isVazio) {
                        Toast.makeText(this, "Nenhum item criado.\nVamos criar!", Toast.LENGTH_SHORT).show();
                    }
                    getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, finalDestino)
                            .commit();
                }
            });
        });
    }

    /**
     * Técnica para desmarcar todos os itens do menu inferior.
     * Agora pública para que fragmentos possam chamar se necessário.
     */
    public void desmarcarMenu() {
        if (navView != null) {
            Menu menu = navView.getMenu();
            // Usa o ID do grupo definido no XML para desativar a exclusividade temporariamente
            menu.setGroupCheckable(R.id.group_main, true, false);
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setChecked(false);
            }
            // Restaura a exclusividade para que os próximos cliques funcionem corretamente
            menu.setGroupCheckable(R.id.group_main, true, true);
        }
    }

    public void setBottomNavigationVisibility(int visibility) {
        if (navView != null) navView.setVisibility(visibility);
    }

    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {}).launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void agendarVerificadorTarefas() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificacaoWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("VerificadorPrazos", ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }
}
