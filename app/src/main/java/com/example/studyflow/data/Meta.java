package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Metas")
public class Meta implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String titulo;
    
    // Timestamp de quando a meta foi iniciada
    public long dataCriacao;
    
    // Timestamp da última vez que o usuário confirmou o cumprimento no dia
    public long ultimoCheckin;

    // Timestamp do último alerta enviado para esta meta
    public long ultimoAlerta;

    public Meta(String titulo, long dataCriacao) {
        this.titulo = titulo;
        this.dataCriacao = dataCriacao;
        this.ultimoCheckin = 0;
        this.ultimoAlerta = 0;
    }
}
