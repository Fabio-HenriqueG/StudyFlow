package com.example.studyflow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * Classe responsável por facilitar a criação e exibição de notificações.
 */
public class NotificacaoHelper {

    // Canais de Notificação
    public static final String CHANNEL_TAREFAS_ALTA = "channel_tarefas_alta";
    public static final String CHANNEL_TAREFAS_GERAL = "channel_tarefas_geral";
    public static final String CHANNEL_METAS = "channel_metas";
    public static final String CHANNEL_CHECKLISTS = "channel_checklists";
    public static final String CHANNEL_FLASHCARDS = "channel_flashcards";

    /**
     * Cria os canais de notificações exigidos a partir do Android 8.0.
     */
    public static void criarCanaisNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 1. Tarefas Alta Importância (Som e Vibração)
            NotificationChannel alta = new NotificationChannel(
                    CHANNEL_TAREFAS_ALTA,
                    "Tarefas Urgentes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alta.setDescription("Lembretes críticos de prazos curtos");
            
            // 2. Tarefas Geral
            NotificationChannel geral = new NotificationChannel(
                    CHANNEL_TAREFAS_GERAL,
                    "Lembretes de Tarefas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            
            // 3. Metas
            NotificationChannel metas = new NotificationChannel(
                    CHANNEL_METAS,
                    "Confirmação de Metas",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            // 4. Checklists
            NotificationChannel checklists = new NotificationChannel(
                    CHANNEL_CHECKLISTS,
                    "Itens Pendentes",
                    NotificationManager.IMPORTANCE_LOW
            );

            // 5. Flashcards
            NotificationChannel flashcards = new NotificationChannel(
                    CHANNEL_FLASHCARDS,
                    "Revisão de Flashcards",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager.createNotificationChannel(alta);
            manager.createNotificationChannel(geral);
            manager.createNotificationChannel(metas);
            manager.createNotificationChannel(checklists);
            manager.createNotificationChannel(flashcards);
        }
    }

    /**
     * Mostra uma notificação na tela com suporte a canais e intenções.
     */
    public static void enviarNotificacao(Context context, int id, String channelId, String titulo, String mensagem, PendingIntent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Ícone padrão do sistema
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setAutoCancel(true)
                .setPriority(getPriority(channelId));

        if (intent != null) {
            builder.setContentIntent(intent);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }

    private static int getPriority(String channelId) {
        if (CHANNEL_TAREFAS_ALTA.equals(channelId)) return NotificationCompat.PRIORITY_HIGH;
        if (CHANNEL_CHECKLISTS.equals(channelId)) return NotificationCompat.PRIORITY_LOW;
        return NotificationCompat.PRIORITY_DEFAULT;
    }
}
