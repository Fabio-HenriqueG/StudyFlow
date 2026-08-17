package com.example.studyflow;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/**
 * Classe responsável por facilitar a criação e exibição de notificações.
 */
public class NotificacaoHelper {

    // ID do canal de notificações (exigido a partir do Android 8.0)
    public static final String CANAL_ID = "tarefas_alertas";
    public static final String CANAL_NOME = "Alertas de Tarefas";

    /**
     * Cria o canal de notificações. Deve ser chamado uma vez na inicialização do app.
     */
    public static void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    CANAL_NOME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription("Notificações para prazos de tarefas do StudyFlow");

            // Carrega configurações
            android.content.SharedPreferences prefs = context.getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
            boolean vibrate = prefs.getBoolean("notification_vibration", true);
            
            canal.enableVibration(vibrate);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    /**
     * Mostra uma notificação na tela.
     */
    public static void enviarNotificacao(Context context, int id, String titulo, String mensagem) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        boolean sound = prefs.getBoolean("notification_sound", true);
        boolean vibrate = prefs.getBoolean("notification_vibration", true);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int defaults = 0;
        if (sound) defaults |= NotificationCompat.DEFAULT_SOUND;
        if (vibrate) defaults |= NotificationCompat.DEFAULT_VIBRATE;
        builder.setDefaults(defaults);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }
}
