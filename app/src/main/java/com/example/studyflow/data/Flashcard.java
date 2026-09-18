package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Flashcards")
public class Flashcard implements Serializable {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    public String pergunta;
    public String resposta;
    public String explicacao;
    public String materiaId; // Vincula ao ID da Seção/Matéria (String para Firebase)
    
    // Lógica de Repetição Espaçada
    public int nivelDominio; // 0 a 5
    public int intervalo; // dias até a próxima revisão
    public int repeticoes; // quantas vezes foi revisado
    public float facilidade; // fator de facilidade (padrão 2.5)
    public long dataProximaRevisao;
    public long dataCriacao;

    // Construtor vazio para o Firebase
    public Flashcard() {
    }

    public Flashcard(String pergunta, String resposta, String explicacao, String materiaId) {
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.explicacao = explicacao;
        this.materiaId = materiaId;
        this.dataCriacao = System.currentTimeMillis();
        this.dataProximaRevisao = System.currentTimeMillis();
        this.facilidade = 2.5f;
        this.intervalo = 0;
        this.repeticoes = 0;
        this.nivelDominio = 0;
    }

    public int getIntId() {
        return id != null ? id.hashCode() : 0;
    }
}
