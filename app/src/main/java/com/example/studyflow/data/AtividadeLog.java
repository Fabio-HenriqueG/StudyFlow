package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AtividadeLog")
public class AtividadeLog {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    public String tipo; // TAREFA, META, CHECKLIST, FLASHCARD
    public String referenciaId;
    public long dataMillis;
    public String materiaId;

    // Construtor vazio para o Firebase
    public AtividadeLog() {
    }

    public AtividadeLog(String tipo, String referenciaId, long dataMillis, String materiaId) {
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.dataMillis = dataMillis;
        this.materiaId = materiaId;
    }
}
