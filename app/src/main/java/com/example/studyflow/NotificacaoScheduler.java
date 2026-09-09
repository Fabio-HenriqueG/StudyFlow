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

        int intensidade = tarefa.insistencia; // 0=Focada, 1=Padrão, 2=Intensa

        // 1. Notificação Imediata de Confirmação (10 segundos depois)
        agendar(context, tarefa.id + 600, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                NotificacaoMensagens.TAREFA_CONFIRMACAO_TITULO, String.format(NotificacaoMensagens.TAREFA_CONFIRMACAO_MSG, tarefa.titulo), agora + 10000, tarefa.id);

        // Se intensidade for Focada (0), ignoramos os acompanhamentos de longo prazo
        if (intensidade >= 1) {
            // 2. Prazos Longos (> 14 dias)
            if (diferencaDias > 14) {
                // Ponto Médio
                long meioCaminho = agora + ((prazo - agora) / 2);
                agendar(context, tarefa.id, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        "Acompanhamento", String.format(NotificacaoMensagens.TAREFA_ACOMPANHAMENTO, tarefa.titulo), meioCaminho, tarefa.id);

                // 7 dias antes
                long seteDiasAntes = prazo - (7L * 24 * 60 * 60 * 1000);
                agendar(context, tarefa.id + 100, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        "Lembrete", String.format(NotificacaoMensagens.TAREFA_ATENCAO_7_DIAS, tarefa.titulo), seteDiasAntes, tarefa.id);
            }

            // 3. Prazos Médios (4 a 14 dias)
            else if (diferencaDias >= 4) {
                long meioCaminho = agora + ((prazo - agora) / 2);
                agendar(context, tarefa.id, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        "Acompanhamento", String.format(NotificacaoMensagens.TAREFA_ACOMPANHAMENTO, tarefa.titulo), meioCaminho, tarefa.id);
            }
        }

        // 4. Regras de Reta Final e Prioridade
        configurarRetaFinal(context, tarefa, intensidade);
    }

    private static void configurarRetaFinal(Context context, Tarefa tarefa, int intensidade) {
        long prazo = tarefa.dataLimite;
        long agora = System.currentTimeMillis();
        
        if (intensidade == 2) { // MODO INTENSA: Algoritmo de Pressão Crescente
            // 1. Acompanhamento antes do dia final
            long diaFinalMillis = obterInicioDoDia(prazo);
            long cursor = agora + (60 * 60 * 1000); // Começa daqui a 1 hora

            while (cursor < diaFinalMillis) {
                long horasRestantes = (diaFinalMillis - cursor) / (60 * 60 * 1000);
                long intervalo;

                if (horasRestantes > 72) intervalo = 24 * 60 * 60 * 1000L; // 1 por dia
                else if (horasRestantes > 24) intervalo = 6 * 60 * 60 * 1000L; // 1 a cada 6h
                else intervalo = 2 * 60 * 60 * 1000L; // 1 a cada 2h (na véspera)

                agendar(context, tarefa.id + (int)(cursor % 10000), "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        "Modo Intenso", "Não esqueça: '" + tarefa.titulo + "' precisa ser feita!", cursor, tarefa.id);
                
                cursor += intervalo;
            }

            // 2. O "Bombardeio" do Dia Final (A cada 30 min)
            long horaInicioBombardeio = Math.max(agora, diaFinalMillis + (9 * 60 * 60 * 1000)); // 9h da manhã ou agora
            cursor = horaInicioBombardeio;
            while (cursor < prazo - (15 * 60 * 1000)) { // Até 15 min antes do fim
                agendar(context, tarefa.id + (int)(cursor % 10000), "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_ALTA,
                        "FOCO TOTAL!", "Alerta de 30 min: Conclua '" + tarefa.titulo + "' agora!", cursor, tarefa.id);
                cursor += 30 * 60 * 1000; // 30 minutos
            }
        } 
        else {
            // Lógica Padrão/Focada (Já existente)
            if (intensidade >= 1) {
                Calendar manhaDia = Calendar.getInstance();
                manhaDia.setTimeInMillis(prazo);
                manhaDia.set(Calendar.HOUR_OF_DAY, 9);
                manhaDia.set(Calendar.MINUTE, 0);
                agendar(context, tarefa.id + 500, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        NotificacaoMensagens.TAREFA_DIA_HOJE_TITULO, String.format(NotificacaoMensagens.TAREFA_DIA_HOJE, tarefa.titulo), manhaDia.getTimeInMillis(), tarefa.id);
            }

            if (tarefa.prioridade == 2) {
                if (intensidade >= 1) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(prazo);
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    cal.set(Calendar.HOUR_OF_DAY, 20);
                    cal.set(Calendar.MINUTE, 0);
                    agendar(context, tarefa.id + 200, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_ALTA,
                            "Planejamento", String.format(NotificacaoMensagens.TAREFA_ALTA_PLANEJAMENTO, tarefa.titulo), cal.getTimeInMillis(), tarefa.id);
                }
                agendar(context, tarefa.id + 300, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_ALTA,
                        "URGENTE", String.format(NotificacaoMensagens.TAREFA_URGENTE_1H, tarefa.titulo), prazo - (60 * 60 * 1000), tarefa.id);
            } 
            else if (tarefa.prioridade == 1) {
                agendar(context, tarefa.id + 400, "TAREFA", NotificacaoHelper.CHANNEL_TAREFAS_GERAL,
                        "Lembrete", String.format(NotificacaoMensagens.TAREFA_URGENTE_2H, tarefa.titulo), prazo - (2L * 60 * 60 * 1000), tarefa.id);
            }
        }
    }

    private static long obterInicioDoDia(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static void agendar(Context context, int notificationId, String tipo, String channel, String titulo, String msg, long targetMillis, int referenciaId) {
        long delay = targetMillis - System.currentTimeMillis();
        if (delay <= 0) return;

        Data data = new Data.Builder()
                .putInt("id", notificationId)
                .putInt("referenciaId", referenciaId)
                .putString("tipo", tipo)
                .putString("channel", channel)
                .putString("titulo", titulo)
                .putString("mensagem", msg)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacaoWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("NOTIFICACAO_" + tipo + "_" + referenciaId)
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
                .putString("titulo", NotificacaoMensagens.META_TITULO_DIARIO)
                .putString("mensagem", NotificacaoMensagens.META_LEMBRETE_GERAL)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(NotificacaoWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag("LEMBRETE_DIARIO_METAS")
                .build();

        WorkManager.getInstance(context).enqueue(request);
    }
}
