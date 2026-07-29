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
                    NotificationManager.IMPORTANCE_HIGH // IMPORTANCE_HIGH faz a notificação aparecer no topo da tela
            );
            canal.setDescription("Notificações para prazos de tarefas do StudyFlow");
            canal.enableVibration(true); // Garante que o celular vibre se permitido

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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Usa o ícone padrão do app
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // PRIORIDADE ALTA para aparecer na tela
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Ativa som, luz e vibração padrão
                .setAutoCancel(true); // Remove a notificação quando o usuário clica nela

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // O ID permite que cada tarefa tenha sua própria notificação ativa
            manager.notify(id, builder.build());
        }
    }
}
