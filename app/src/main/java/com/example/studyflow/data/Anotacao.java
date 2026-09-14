package com.example.studyflow.data;

import java.io.Serializable;

/**
 * Entidade que representa uma Anotação, adaptada para o Firebase.
 */
public class Anotacao implements Serializable {

    public String id; // ID do documento no Firestore
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
