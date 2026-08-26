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

    // Prioridade: 0 = Baixa (Verde), 1 = Média (Amarela), 2 = Alta (Vermelha)
    public int prioridade;
    
    // Perfil de insistência: 0=Discreto, 1=Equilibrado, 2=Não me deixe esquecer
    public int insistencia;
    
    // Status da tarefa
    public boolean concluida;
    public long dataConclusao; // Para controle de deleção automática

    //Construtor: É assim que o Java vai criar o objeto antes de mandar pro banco
    public Tarefa(String titulo, String descricao, long dataLimite, int frequencia, int prioridade, int insistencia){
        this.titulo = titulo;
        this.descricao = descricao;
        this.dataLimite = dataLimite;
        this.frequencia = frequencia;
        this.prioridade = prioridade;
        this.insistencia = insistencia;
        this.ultimoAlerta = 0; 
        this.concluida = false;
        this.dataConclusao = 0;
    }
}
