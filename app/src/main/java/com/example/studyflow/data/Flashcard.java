package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Flashcards")
public class Flashcard implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String pergunta;
    public String resposta;
    public String explicacao;
    public int materiaId; // Vincula ao ID da Seção/Matéria
    
    // Lógica de Repetição Espaçada (Simples - baseada no algoritmo SM-2)
    public int nivelDominio; // 0 a 5
    public int intervalo; // dias até a próxima revisão
    public int repeticoes; // quantas vezes foi revisado
    public float facilidade; // fator de facilidade (padrão 2.5)
    public long dataProximaRevisao;
    public long dataCriacao;

    public Flashcard(String pergunta, String resposta, String explicacao, int materiaId) {
        this.pergunta = pergunta;
        this.resposta = resposta;
        this.explicacao = explicacao;
        this.materiaId = materiaId;
        this.dataCriacao = System.currentTimeMillis();
        this.dataProximaRevisao = System.currentTimeMillis(); // Revisar hoje mesmo ao criar
        this.facilidade = 2.5f;
        this.intervalo = 0;
        this.repeticoes = 0;
        this.nivelDominio = 0;
    }
}
