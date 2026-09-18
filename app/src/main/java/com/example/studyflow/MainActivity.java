package com.example.studyflow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
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

import com.example.studyflow.data.FirestoreService;
import com.example.studyflow.data.Tarefa;
import com.example.studyflow.data.Meta;
import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.Checklist;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private NavigationBarView navView;

    // Launcher para pedido de permissão (deve ser declarado como campo da classe)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d("MainActivity", "Permissão de notificação concedida.");
                } else {
                    Toast.makeText(this, "As notificações estão desativadas. Você pode perder prazos importantes!", Toast.LENGTH_LONG).show();
                }
            });

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
        
        // Inicialização Imediata (mesmo sem login concluído)
        configurarNavegacao();
        configurarBotaoVoltar();
        configurarLifecycleCallbacks();

        // Firebase Auth - Login Anônimo em Background
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.signInAnonymously()
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        String uid = user.getUid();
                        Log.d("MainActivity", "Login anônimo efetuado: " + uid);
                        String displayId = uid.length() > 5 ? uid.substring(0, 5) : uid;
                        Toast.makeText(this, "Sincronizado com a nuvem (ID: " + displayId + "...)", Toast.LENGTH_SHORT).show();
                        recarregarFragmentoHome();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Falha no login anônimo", e);
                    Toast.makeText(this, "Modo Offline ativo. Verifique sua conexão.", Toast.LENGTH_LONG).show();
                });
        } else {
            Log.d("MainActivity", "Usuário já logado.");
        }

        // Suporte e Notificações
        NotificacaoHelper.criarCanaisNotificacao(this);
        pedirPermissaoNotificacao();
        tratarIntentNotificacao(getIntent());
        
        try {
            agendarVerificadorTarefas();
            NotificacaoScheduler.agendarLembreteMetas(this);
        } catch (Exception e) {
            Log.e("MainActivity", "Erro ao agendar tarefas", e);
        }
        
        processarLimpezaTarefas();
    }

    private void configurarLifecycleCallbacks() {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof HomeFragment) {
                    desmarcarMenu();
                }
            }
        }, false);
    }

    private void recarregarFragmentoHome() {
        // Recarrega a HomeFragment para que ela busque dados com o novo UID
        Fragment atual = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (atual instanceof HomeFragment) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commitAllowingStateLoss();
        }
    }

    private void processarLimpezaTarefas() {
        long agora = System.currentTimeMillis();
        FirestoreService fs = FirestoreService.getInstance();

        // Busca todas as tarefas para processar limpeza (Poderia ser otimizado com query)
        fs.buscarTarefasAtivas().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Tarefa> tarefas = queryDocumentSnapshots.toObjects(Tarefa.class);
            for (Tarefa t : tarefas) {
                if (t.dataLimite < agora) {
                    if (t.prioridade == 0) {
                        fs.excluirTarefa(t.id);
                    } else {
                        t.concluida = true;
                        t.dataConclusao = agora;
                        fs.salvarTarefa(t);
                    }
                }
            }
        });

        // Limpeza do histórico (Médias antigas)
        long seteDiasAtras = agora - (7L * 24 * 60 * 60 * 1000);
        fs.buscarTarefasConcluidas().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Tarefa> concluidas = queryDocumentSnapshots.toObjects(Tarefa.class);
            for (Tarefa t : concluidas) {
                if (t.prioridade == 1 && t.dataConclusao < seteDiasAtras) {
                    fs.excluirTarefa(t.id);
                }
            }
        });
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
                FragmentManager fm = getSupportFragmentManager();
                // Primeiro, verifica se há algo no BackStack para voltar (Ex: Flashcards -> Lista)
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    // Se não houver pilha, verifica se estamos fora da Home para voltar a ela
                    Fragment atual = fm.findFragmentById(R.id.fragment_container);
                    if (atual != null && !(atual instanceof HomeFragment)) {
                        fm.beginTransaction()
                                .replace(R.id.fragment_container, new HomeFragment())
                                .commit();
                        desmarcarMenu();
                    } else {
                        // Caso contrário, fecha o app normalmente
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void navegarPara(Fragment destino, boolean isVazio) {
        if (!isFinishing() && !isDestroyed()) {
            if (isVazio) {
                Toast.makeText(this, R.string.nenhum_item_criado, Toast.LENGTH_SHORT).show();
            }
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, destino)
                    .commit();
        }
    }

    private void verificarDadosENavegar(int itemId) {
        Fragment destino = null;
        
        if (itemId == R.id.nav_tarefas) {
            destino = new TarefasFragment();
        } else if (itemId == R.id.nav_anotacoes) {
            destino = new AnotacoesFragment();
        } else if (itemId == R.id.nav_metas) {
            destino = new MetasFragment();
        } else if (itemId == R.id.nav_checklist) {
            destino = new ChecklistFragment();
        }

        if (destino != null) {
            navegarPara(destino, false);
        }
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
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void agendarVerificadorTarefas() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificacaoWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("VerificadorPrazos", ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    private void tratarIntentNotificacao(Intent intent) {
        if (intent != null && intent.hasExtra("NavegarPara")) {
            String destino = intent.getStringExtra("NavegarPara");
            if ("TAREFA".equals(destino)) {
                verificarDadosENavegar(R.id.nav_tarefas);
            } else if ("META".equals(destino)) {
                verificarDadosENavegar(R.id.nav_metas);
            } else if ("CHECKLIST".equals(destino)) {
                verificarDadosENavegar(R.id.nav_checklist);
            }
        }
    }
}
