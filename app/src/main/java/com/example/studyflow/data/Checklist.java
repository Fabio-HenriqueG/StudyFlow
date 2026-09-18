package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Checklists")
public class Checklist implements Serializable {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String titulo;
    public long ultimoAlerta;
    
    // Novo: Fixar no topo
    public boolean isPinned;
    
    // Novo: Data de Validade
    public long dataValidade;

    // Construtor vazio para o Firebase
    public Checklist() {
    }

    public Checklist(String titulo) {
        this.titulo = titulo;
        this.ultimoAlerta = 0;
        this.isPinned = false;
        this.dataValidade = 0;
    }
}
