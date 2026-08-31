package com.example.studyflow;

import android.content.Context;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.studyflow.data.Tarefa;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Responsável por calcular e agendar as notificações baseadas em regras de negócio.
 */
public class NotificacaoScheduler {

    /**
     * Agenda todas as notificações pertinentes a uma tarefa.
     */
    public static void agendarNotificacoesTarefa(Context context, Tarefa tarefa) {
        long agora = System.currentTimeMillis();
        long prazo = tarefa.dataLimite;
        long diferencaDias = (prazo - agora) / (24 * 60 * 60 * 1000);

        // 1. Prazos Longos (> 14 dias)
        if (diferencaDias > 14) {
            // Ponto Médio
            long meioCaminho = agora + ((prazo - agora) / 2);
            agendar(context, tarefa.id, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                    "Acompanhamento", String.format(NotificacaoMensagens.TAREFA_ACOMPANHAMENTO, tarefa.titulo), meioCaminho);

            // 7 dias antes
            long seteDiasAntes = prazo - (7L * 24 * 60 * 60 * 1000);
            agendar(context, tarefa.id + 100, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                    "Lembrete", String.format(NotificacaoMensagens.TAREFA_ATENCAO_7_DIAS, tarefa.titulo), seteDiasAntes);
        }

        // 2. Prazos Médios (4 a 14 dias)
        else if (diferencaDias >= 4) {
            long meioCaminho = agora + ((prazo - agora) / 2);
            agendar(context, tarefa.id, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                    "Acompanhamento", String.format(NotificacaoMensagens.TAREFA_ACOMPANHAMENTO, tarefa.titulo), meioCaminho);
        }

        // 3. Regras de Reta Final e Prioridade
        configurarRetaFinal(context, tarefa);
    }

    private static void configurarRetaFinal(Context context, Tarefa tarefa) {
        long prazo = tarefa.dataLimite;
        
        if (tarefa.prioridade == 2) { // ALTA
            // 1 dia antes às 20h
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(prazo);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            cal.set(Calendar.HOUR_OF_DAY, 20);
            cal.set(Calendar.MINUTE, 0);
            agendar(context, tarefa.id + 200, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_ALTA,
                    "Planejamento", String.format(NotificacaoMensagens.TAREFA_ALTA_PLANEJAMENTO, tarefa.titulo), cal.getTimeInMillis());

            // 1 hora antes
            agendar(context, tarefa.id + 300, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_ALTA,
                    "URGENTE", String.format(NotificacaoMensagens.TAREFA_URGENTE_1H, tarefa.titulo), prazo - (60 * 60 * 1000));
        } 
        else if (tarefa.prioridade == 1) { // MÉDIA
            // 2 horas antes
            agendar(context, tarefa.id + 400, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                    "Lembrete", String.format(NotificacaoMensagens.TAREFA_URGENTE_2H, tarefa.titulo), prazo - (2L * 60 * 60 * 1000));
        } 
        else { // BAIXA
            // 9h da manhã do dia do prazo
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(prazo);
            cal.set(Calendar.HOUR_OF_DAY, 9);
            cal.set(Calendar.MINUTE, 0);
            agendar(context, tarefa.id + 500, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                    "Lembrete", String.format(NotificacaoMensagens.TAREFA_DIA_HOJE, tarefa.titulo), cal.getTimeInMillis());
        }
    }

    private static void agendar(Context context, int id, String tipo, String channel, String titulo, String msg, long targetMillis) {
        long delay = targetMillis - System.currentTimeMillis();
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putInt("id", id)
                .putString("tipo", tipo)
                .putString("channel", channel)
                .putString("titulo", titulo)
                .putString("mensagem", msg)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacaoWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("NOTIFICACAO_" + tipo + "_" + id)
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }

    /**
     * Cancela todos os agendamentos de uma tarefa específica (útil ao excluir ou concluir).
     */
    public static void cancelarNotificacoesTarefa(Context context, int tarefaId) {
        WorkManager.getInstance(context).cancelAllWorkByTag("NOTIFICACAO_TAREFA_" + tarefaId);
    }

    /**
     * Agenda o lembrete diário de metas para as 19:30.
     */
    public static void agendarLembreteMetas(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 19);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);

        if (cal.getTimeInMillis() <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = cal.getTimeInMillis() - System.currentTimeMillis();

        Data data = new Data.Builder()
                .putInt("id", 999) // ID fixo para o lembrete diário
                .putString("tipo", "META")
                .putString("channel", NotificacaoHelper.CHANNEL_METAS)
                .putString("titulo", "Metas Diárias")
                .putString("mensagem", "Você já cumpriu suas metas hoje? Não esqueça de confirmar!")
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacaoWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("LEMBRETE_DIARIO_METAS")
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
