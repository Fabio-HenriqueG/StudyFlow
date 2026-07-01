package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Tarefas")
public class Tarefa {

    @PrimaryKey(autoGenerate = true)
    public int id; //igual o auto_increment do SQL
    public String titulo;
    public String descricao;
    public long dataLimite; // Guardaremos a data e hora em milissegundos (padrão do java)
    public int frequencia;

    //Construtor: É assim que o Java vai criar o objeto antes de mandar pro banco
    public Tarefa(String titulo, String descricao, long dataLimite, int frequencia){
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.frequencia = frequencia;
    }
}
