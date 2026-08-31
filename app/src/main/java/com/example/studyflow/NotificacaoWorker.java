package com.example.studyflow;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.ChecklistItem;
import com.example.studyflow.data.Meta;
import com.example.studyflow.data.Tarefa;

import java.util.Calendar;
import java.util.List;

/**
 * Worker genérico para disparar notificações agendadas.
 */
public class NotificacaoWorker extends Worker {

    public NotificacaoWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        
        // 1. Verifica se as notificações estão habilitadas globalmente
        SharedPreferences prefs = context.getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("notifications_enabled", true)) {
            return Result.success();
        }

        // 2. Extrai os dados da notificação agendada
        int id = getInputData().getInt("id", -1);
        String tipo = getInputData().getString("tipo");
        String channel = getInputData().getString("channel");
        String titulo = getInputData().getString("titulo");
        String mensagem = getInputData().getString("mensagem");

        // Se houver um ID e Tipo, é uma notificação agendada
        if (id != -1 && tipo != null) {
            return processarNotificacaoAgendada(context, id, tipo, channel, titulo, mensagem);
        }

        return Result.success();
    }

    private Result processarNotificacaoAgendada(Context context, int id, String tipo, String channel, String titulo, String mensagem) {
        SharedPreferences prefs = context.getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        
        if ("TAREFA".equals(tipo)) {
            Tarefa t = AppDatabase.getInstance(context).tarefaDao().buscarPorId(id);
            if (t == null || t.concluida) return Result.success();
        } else if ("META".equals(tipo) && id == 999) {
            // Lembrete diário inteligente às 19:30 - Agenda para amanhã
            NotificacaoScheduler.agendarLembreteMetas(context);
            
            // Só envia se houver meta pendente
            AppDatabase db = AppDatabase.getInstance(context);
            List<Meta> metas = db.metaDao().buscarTodas();
            boolean temPendente = false;
            for (Meta m : metas) {
                if (!foiCumpridaNoPeriodo(prefs, m.ultimoCheckin)) {
                    temPendente = true; break;
                }
            }
            if (!temPendente) return Result.success();
        }

        enviar(context, id, tipo, channel, titulo, mensagem);
        return Result.success();
    }

    private void enviar(Context context, int id, String tipo, String channel, String titulo, String msg) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NavegarPara", tipo);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pi = PendingIntent.getActivity(context, id, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificacaoHelper.enviarNotificacao(context, id, channel, titulo, msg, pi);
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
}
