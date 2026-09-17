package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Tarefas")
public class Tarefa implements Serializable {

    @PrimaryKey
    @NonNull
    public String id = ""; // ID principal (Firestore)
    
    public String titulo;
    public String descricao;
    public long dataLimite;
    public long ultimoAlerta;
    public int prioridade;
    public int insistencia;
    public boolean concluida;
    public long dataConclusao;

    // Construtor vazio para o Firebase
    public Tarefa() {
    }

    public Tarefa(String titulo, String descricao, long dataLimite, int prioridade, int insistencia){
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.prioridade = prioridade;
        this.insistencia = insistencia;
        this.ultimoAlerta = 0; 
        this.concluida = false;
        this.dataConclusao = 0;
    }

    // Helper para notificações e outras partes que precisam de um ID numérico
    public int getIntId() {
        return id != null ? id.hashCode() : 0;
    }
}
