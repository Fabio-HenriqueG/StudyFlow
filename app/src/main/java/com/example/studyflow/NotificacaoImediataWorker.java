package com.example.studyflow;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

/**
 * Este Worker é especializado em mandar UMA notificação rápida.
 * Ele é usado para o alerta de 10 segundos após a criação de uma tarefa.
 */
public class NotificacaoImediataWorker extends Worker {

    public NotificacaoImediataWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Pega o ID da tarefa que foi enviado junto com o pedido
        int tarefaId = getInputData().getInt("tarefa_id", -1);

        if (tarefaId != -1) {
            // Busca a tarefa no banco para pegar o título e descrição
            Tarefa tarefa = AppDatabase.getInstance(getApplicationContext()).tarefaDao().buscarPorId(tarefaId);
            
            if (tarefa != null) {
                // Envia a notificação imediata
                NotificacaoHelper.enviarNotificacao(
                        getApplicationContext(),
                        tarefa.id,
                        "Início de Acompanhamento",
                        "Você começou a tarefa: " + tarefa.titulo + ". Estarei te lembrando!"
                );
            }
        }

        return Result.success();
    }
}
