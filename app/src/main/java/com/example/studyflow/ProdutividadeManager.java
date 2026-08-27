package com.example.studyflow;

import android.content.Context;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.AtividadeLog;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ProdutividadeManager {

    /**
     * Registra uma ação concluída no histórico.
     */
    public static void registrarAtividade(Context context, String tipo, int referenciaId, int materiaId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AtividadeLog log = new AtividadeLog(tipo, referenciaId, System.currentTimeMillis(), materiaId);
            AppDatabase.getInstance(context).atividadeLogDao().inserir(log);
        });
    }

    /**
     * Calcula a sequência de dias ativos (Streak).
     */
    public static int calcularStreak(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        List<String> diasAtivos = db.atividadeLogDao().buscarDiasComAtividade();
        
        if (diasAtivos == null || diasAtivos.isEmpty()) return 0;

        int streak = 0;
        Calendar cal = Calendar.getInstance();
        String hoje = formatarData(cal);
        
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String ontem = formatarData(cal);

        // Se não houve atividade hoje nem ontem, o streak foi quebrado
        if (!diasAtivos.contains(hoje) && !diasAtivos.contains(ontem)) {
            return 0;
        }

        // Começa a verificar a partir do último dia ativo (hoje ou ontem)
        Calendar checker = Calendar.getInstance();
        if (!diasAtivos.contains(hoje)) {
            checker.add(Calendar.DAY_OF_YEAR, -1);
        }

        while (true) {
            String diaStr = formatarData(checker);
            if (diasAtivos.contains(diaStr)) {
                streak++;
                checker.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }

        return streak;
    }

    private static String formatarData(Calendar cal) {
        return String.format(Locale.US, "%04d-%02d-%02d", 
            cal.get(Calendar.YEAR), 
            cal.get(Calendar.MONTH) + 1, 
            cal.get(Calendar.DAY_OF_MONTH));
    }
}
