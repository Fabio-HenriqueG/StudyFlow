package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

/**
 * Entidade que representa uma Anotação, adaptada para o Firebase.
 */
@Entity(tableName = "anotacoes")
public class Anotacao implements Serializable {

    @PrimaryKey
    @NonNull
    public String id = ""; // ID do documento no Firestore
    public String titulo;
    public String conteudoHtml;
    public long dataUltimaEdicao;

    // Construtor vazio necessário para o Firestore
    public Anotacao() {
    }

    public Anotacao(String titulo, String conteudoHtml, long dataUltimaEdicao) {
        this.titulo = titulo;
        this.conteudoHtml = conteudoHtml;
        this.dataUltimaEdicao = dataUltimaEdicao;
    }
}
