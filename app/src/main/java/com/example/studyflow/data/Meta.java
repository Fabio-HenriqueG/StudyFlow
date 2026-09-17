package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Metas")
public class Meta implements Serializable {

    @PrimaryKey
    @NonNull
    public String id = "";
    
    public String usuarioId;
    public String titulo;
    public long dataCriacao;
    public long ultimoCheckin;
    public long ultimoAlerta;

    public Meta() {
    }

    public Meta(String titulo, long dataCriacao) {
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.ultimoCheckin = 0;
        this.ultimoAlerta = 0;
    }

    public int getIntId() {
        return id != null ? id.hashCode() : 0;
    }
}
