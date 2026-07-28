package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Tarefas")
public class Tarefa implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id; //igual o auto_increment do SQL
    public String titulo;
    public String descricao;
    public long dataLimite; // Guardaremos a data e hora em milissegundos (padrão do java)
    public int frequencia;
    
    // Armazena o timestamp (tempo) da última vez que o app mandou uma notificação para esta tarefa.
    // Isso evita que o app mande várias notificações ao mesmo tempo ou muito rápido.
    public long ultimoAlerta;

    //Construtor: É assim que o Java vai criar o objeto antes de mandar pro banco
    public Tarefa(String titulo, String descricao, long dataLimite, int frequencia){
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.frequencia = frequencia;
        this.ultimoAlerta = 0; // Começa como 0 pois nunca foi alertado
    }
}
