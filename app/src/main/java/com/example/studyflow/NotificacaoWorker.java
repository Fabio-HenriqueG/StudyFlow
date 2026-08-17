package com.example.studyflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.ChecklistItem;
import com.example.studyflow.data.Meta;
import com.example.studyflow.data.Tarefa;

import java.util.List;
import java.util.Calendar;

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

        // 0. Verifica se as notificações estão habilitadas nas configurações
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("notifications_enabled", true);
        if (!enabled) {
            Log.d("NotificacaoWorker", "Notificações desativadas pelo usuário.");
            return Result.success();
        }

        // Verifica Horário de Silêncio
        if (estaNoHorarioDeSilencio(prefs)) {
            Log.d("NotificacaoWorker", "No horário de silêncio. Notificação cancelada.");
            return Result.success();
        }

        // Pega o nível de frequência escolhido (0=Baixa, 1=Média, 2=Alta)
        int freqLevel = prefs.getInt("notification_frequency", 2);

        // 1. Pega o banco de dados e a lista de todas as tarefas
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        List<Tarefa> tarefas = db.tarefaDao().buscarTodas();

        long tempoAtual = System.currentTimeMillis();

        // 2. Passa por cada tarefa para ver se precisa avisar o usuário
        for (Tarefa tarefa : tarefas) {
            
            // Se a tarefa já passou do prazo, não precisamos mais notificar aqui
            if (tarefa.dataLimite < tempoAtual) continue;

            // Calcula quanto tempo falta para o prazo (em milissegundos)
            long tempoRestante = tarefa.dataLimite - tempoAtual;
            
            // Calcula quanto tempo passou desde a última notificação que enviamos
            long tempoDesdeUltimoAlerta = tempoAtual - tarefa.ultimoAlerta;

            // Lógica de FREQUÊNCIA
            boolean deveNotificar = false;
            String urgencia = "";

            long[][] intervalos = {
                {120 * 60 * 1000, 360 * 60 * 1000, 720 * 60 * 1000, 1440 * 60 * 1000}, // Baixa
                {60 * 60 * 1000, 240 * 60 * 1000, 480 * 60 * 1000, 720 * 60 * 1000},  // Média
                {30 * 60 * 1000, 120 * 60 * 1000, 360 * 60 * 1000, 1440 * 60 * 1000} // Alta
            };

            long[] configAtual = intervalos[freqLevel];

            if (DateUtils.isToday(tarefa.dataLimite)) {
                if (tempoDesdeUltimoAlerta >= configAtual[0]) {
                    deveNotificar = true;
                    urgencia = "Lembrete: Esta tarefa vence HOJE!";
                }
            }
            else if (tempoRestante < (2 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta >= configAtual[0]) {
                    deveNotificar = true;
                    urgencia = "URGENTE: Falta pouco tempo!";
                }
            } 
            else if (tempoRestante < (12 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta >= configAtual[1]) {
                    deveNotificar = true;
                    urgencia = "Atenção: O prazo está chegando.";
                }
            }
            else if (tempoRestante < (24 * 60 * 60 * 1000)) {
                if (tempoDesdeUltimoAlerta >= configAtual[2]) {
                    deveNotificar = true;
                    urgencia = "Lembrete: Sua tarefa vence em breve.";
                }
            }
            else {
                if (tempoDesdeUltimoAlerta >= configAtual[3]) {
                    deveNotificar = true;
                    urgencia = "Lembrete de tarefa programada.";
                }
            }

            if (deveNotificar) {
                NotificacaoHelper.enviarNotificacao(
                        getApplicationContext(),
                        tarefa.id,
                        urgencia,
                        tarefa.titulo + ": " + tarefa.descricao
                );

                tarefa.ultimoAlerta = tempoAtual;
                db.tarefaDao().atualizar(tarefa);
            }
        }

        // --- NOVIDADE: VERIFICAÇÃO DE METAS ---
        processarNotificacoesMetas(prefs, db, tempoAtual);

        // --- NOVIDADE: VERIFICAÇÃO DE CHECKLISTS ---
        processarNotificacoesChecklists(prefs, db, tempoAtual);

        return Result.success();
    }

    private void processarNotificacoesChecklists(SharedPreferences prefs, AppDatabase db, long tempoAtual) {
        int numAlertasPorDia = prefs.getInt("checklist_notification_frequency", 4);
        long intervaloEntreAlertas = (24 * 60 * 60 * 1000) / numAlertasPorDia;

        List<Checklist> checklists = db.checklistDao().buscarTodas();

        for (Checklist checklist : checklists) {
            // Verifica se a lista tem itens pendentes
            List<ChecklistItem> itens = db.checklistDao().buscarItensPorChecklist(checklist.id);
            boolean temItemPendente = false;
            for (ChecklistItem item : itens) {
                if (!item.isChecked) {
                    temItemPendente = true;
                    break;
                }
            }

            // Se tem itens pendentes e já passou o tempo do último alerta
            if (temItemPendente) {
                // Alerta 1: Frequência normal
                if (tempoAtual - checklist.ultimoAlerta >= intervaloEntreAlertas) {
                    NotificacaoHelper.enviarNotificacao(
                            getApplicationContext(),
                            checklist.id + 20000,
                            "Lembrete de Checklist",
                            "Sua lista '" + checklist.titulo + "' ainda tem itens pendentes!"
                    );

                    checklist.ultimoAlerta = tempoAtual;
                    db.checklistDao().atualizar(checklist);
                }

                // Alerta 2: Data de Validade (Novo)
                if (checklist.dataValidade > 0 && DateUtils.isToday(checklist.dataValidade)) {
                    // Envia um alerta especial apenas uma vez no dia da validade (ou conforme lógica)
                    // Aqui usamos um ID diferente para não colidir
                    NotificacaoHelper.enviarNotificacao(
                            getApplicationContext(),
                            checklist.id + 30000,
                            "Prazo de Checklist",
                            "Hoje é o dia final para concluir a lista: " + checklist.titulo
                    );
                }
            }
        }
    }

    private void processarNotificacoesMetas(SharedPreferences prefs, AppDatabase db, long tempoAtual) {
        int numAlertasPorDia = prefs.getInt("goal_notification_frequency", 4);
        long intervaloEntreAlertas = (24 * 60 * 60 * 1000) / numAlertasPorDia;

        List<Meta> metas = db.metaDao().buscarTodas();
        
        for (Meta meta : metas) {
            // Verifica se a meta já foi cumprida no período atual (usando a lógica de reset)
            if (!foiCumpridaNoPeriodo(prefs, meta.ultimoCheckin)) {
                
                // Verifica se já passou o tempo necessário desde o último lembrete
                if (tempoAtual - meta.ultimoAlerta >= intervaloEntreAlertas) {
                    
                    NotificacaoHelper.enviarNotificacao(
                            getApplicationContext(),
                            meta.id + 10000, // Offset para não colidir com IDs de tarefas
                            "Lembrete de Meta Diária",
                            "Você ainda não confirmou a meta: " + meta.titulo
                    );

                    meta.ultimoAlerta = tempoAtual;
                    db.metaDao().atualizar(meta);
                }
            }
        }
    }

    private boolean foiCumpridaNoPeriodo(SharedPreferences prefs, long ultimoCheckin) {
        if (ultimoCheckin == 0) return false;

        String resetTime = prefs.getString("goal_reset_time", "00:00");
        String[] parts = resetTime.split(":");
        int resetHour = Integer.parseInt(parts[0]);
        int resetMin = Integer.parseInt(parts[1]);

        Calendar agora = Calendar.getInstance();
        Calendar limiteReset = Calendar.getInstance();
        limiteReset.set(Calendar.HOUR_OF_DAY, resetHour);
        limiteReset.set(Calendar.MINUTE, resetMin);
        limiteReset.set(Calendar.SECOND, 0);
        limiteReset.set(Calendar.MILLISECOND, 0);

        if (agora.before(limiteReset)) {
            limiteReset.add(Calendar.DAY_OF_YEAR, -1);
        }

        return ultimoCheckin > limiteReset.getTimeInMillis();
    }

    private boolean estaNoHorarioDeSilencio(SharedPreferences prefs) {
        String inicio = prefs.getString("silence_start", "22:00");
        String fim = prefs.getString("silence_end", "07:00");

        try {
            Calendar cal = Calendar.getInstance();
            int agoraHora = cal.get(Calendar.HOUR_OF_DAY);
            int agoraMinuto = cal.get(Calendar.MINUTE);
            int agoraTotal = agoraHora * 60 + agoraMinuto;

            String[] partesInicio = inicio.split(":");
            int inicioTotal = Integer.parseInt(partesInicio[0]) * 60 + Integer.parseInt(partesInicio[1]);

            String[] partesFim = fim.split(":");
            int fimTotal = Integer.parseInt(partesFim[0]) * 60 + Integer.parseInt(partesFim[1]);

            if (inicioTotal < fimTotal) {
                return agoraTotal >= inicioTotal && agoraTotal <= fimTotal;
            } else {
                return agoraTotal >= inicioTotal || agoraTotal <= fimTotal;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
