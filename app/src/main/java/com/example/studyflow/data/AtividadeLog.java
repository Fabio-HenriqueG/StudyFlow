package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "AtividadeLog")
public class AtividadeLog {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String tipo; // TAREFA, META, CHECKLIST, FLASHCARD
    public int referenciaId;
    public long dataMillis;
    public int materiaId;

    public AtividadeLog(String tipo, int referenciaId, long dataMillis, int materiaId) {
        this.tipo = tipo;
        this.referenciaId = referenciaId;
        this.dataMillis = dataMillis;
        this.materiaId = materiaId;
    }
}
