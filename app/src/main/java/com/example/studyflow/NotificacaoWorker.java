package com.example.studyflow;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.ListenableWorker.Result;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.util.List;

/**
 * Esta classe é como um "empregado" que o Android chama de tempos em tempos para trabalhar
 * em segundo plano, mesmo se o app estiver fechado.
 */
public class NotificacaoWorker extends Worker {

    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Onde o trabalho real acontece.
     */
    @NonNull
    @Override
    public Result doWork() {
        Log.d("NotificacaoWorker", "Verificando tarefas para notificações...");

        // 1. Pega o banco de dados e a lista de todas as tarefas
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Tarefa> tarefas = db.tarefaDao().buscarTodas();

        long tempoAtual = System.currentTimeMillis();

        // 2. Passa por cada tarefa para ver se precisa avisar o usuário
        for (Tarefa tarefa : tarefas) {
            
            // Se a tarefa já passou do prazo, não precisamos mais notificar aqui (poderia ter um aviso de atraso)
            if (tarefa.dataLimite < tempoAtual) continue;

            // Calcula quanto tempo falta para o prazo (em milissegundos)
            long tempoRestante = tarefa.dataLimite - tempoAtual;
            
            // Calcula quanto tempo passou desde a última notificação que enviamos
            long tempoDesdeUltimoAlerta = tempoAtual - tarefa.ultimoAlerta;

            // Lógica de FREQUÊNCIA: quanto mais perto, mais avisos.
            boolean deveNotificar = false;
            String urgencia = "";

            // --- NOVIDADE: SE A TAREFA FOR PARA HOJE ---
            if (DateUtils.isToday(tarefa.dataLimite)) {
                // Para tarefas de hoje, avisar a cada 30 minutos.
                // Usamos o tempo absoluto desde o último alerta.
                if (tempoDesdeUltimoAlerta >= (30 * 60 * 1000)) {
                    deveNotificar = true;
                    urgencia = "Lembrete: Esta tarefa vence HOJE!";
                }
            }
            // --- Lógica anterior para outros prazos ---
            // Caso 1: Falta menos de 2 horas (Urgência máxima) -> Avisar a cada 30 minutos
            else if (tempoRestante < (2 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta > (30 * 60 * 1000)) {
                    deveNotificar = true;
                    urgencia = "URGENTE: Falta pouco tempo!";
                }
            } 
            // Caso 2: Falta entre 2h e 12h -> Avisar a cada 2 horas
            else if (tempoRestante < (12 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta > (2 * 60 * 60 * 1000)) {
                    deveNotificar = true;
                    urgencia = "Atenção: O prazo está chegando.";
                }
            }
            // Caso 3: Falta entre 12h e 24h -> Avisar a cada 6 horas
            else if (tempoRestante < (24 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta > (6 * 60 * 60 * 1000)) {
                    deveNotificar = true;
                    urgencia = "Lembrete: Sua tarefa vence em breve.";
                }
            }
            // Caso 4: Falta mais de 24h -> Avisar uma vez por dia (24h)
            else {
                if (tempoDesdeUltimoAlerta > (24 * 60 * 60 * 1000)) {
                    deveNotificar = true;
                    urgencia = "Lembrete de tarefa programada.";
                }
            }

            // 3. Se a lógica decidiu que deve notificar, envia o alerta!
            if (deveNotificar) {
                NotificacaoHelper.enviarNotificacao(
                        getApplicationContext(),
                        tarefa.id, // ID da tarefa garante que não vai sobrepor outras tarefas
                        urgencia,
                        tarefa.titulo + ": " + tarefa.descricao
                );

                // 4. Atualiza o banco dizendo que acabamos de avisar agora
                tarefa.ultimoAlerta = tempoAtual;
                db.tarefaDao().atualizar(tarefa);
            }
        }

        return Result.success(); // Trabalho concluído com sucesso
    }
}
