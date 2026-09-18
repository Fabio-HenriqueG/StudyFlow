package com.example.studyflow;

import android.content.Context;
import android.content.Context;
import com.example.studyflow.data.AtividadeLog;
import com.example.studyflow.data.FirestoreService;
import java.util.Calendar;
import java.util.Locale;

public class ProdutividadeManager {

    /**
     * Registra uma ação concluída no histórico no Firestore.
     */
    public static void registrarAtividade(Context context, String tipo, String referenciaId, String materiaId) {
        AtividadeLog log = new AtividadeLog(tipo, referenciaId, System.currentTimeMillis(), materiaId);
        FirestoreService.getInstance().getUserDoc().collection("atividades").add(log);
    }

    /**
     * Calcula a sequência de dias ativos (Streak).
     * Nota: Para o Firestore, idealmente faríamos uma query para buscar os dias.
     */
    public static int calcularStreak(Context context) {
        // Por enquanto, manteremos uma lógica simplificada ou retornaremos 0
        // até implementarmos a busca de dias ativos no FirestoreService.
        return 0; 
    }

    private static String formatarData(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d", 
            cal.get(Calendar.YEAR), 
            cal.get(Calendar.MONTH) + 1, 
            cal.get(Calendar.DAY_OF_MONTH));
    }
}
