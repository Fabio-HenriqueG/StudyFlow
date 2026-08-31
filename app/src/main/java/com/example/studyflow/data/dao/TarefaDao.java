package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.studyflow.data.Tarefa;

import java.util.List;

@Dao
public interface TarefaDao {

    @Insert
    long inserir(Tarefa tarefa); // Esse método vai salvar a tarefa que você enviar

    @Update
    void atualizar(Tarefa tarefa); // Esse método vai atualizar uma tarefa existente

    @Delete
    void excluir(Tarefa tarefa); // Esse método vai excluir a tarefa

    @Query("SELECT * FROM tarefas WHERE concluida = 0 ORDER BY dataLimite ASC")
    List<Tarefa> buscarAtivas(); 

    @Query("SELECT * FROM tarefas WHERE concluida = 1 ORDER BY dataConclusao DESC")
    List<Tarefa> buscarNoHistorico();

    @Query("SELECT * FROM tarefas WHERE concluida = 0 AND dataLimite < :agora")
    List<Tarefa> buscarExpiradas(long agora);

    @Query("DELETE FROM tarefas WHERE concluida = 1 AND prioridade = 1 AND dataConclusao < :limite")
    void deletarMediasAntigas(long limite);

    @Query("SELECT * FROM tarefas ORDER BY dataLimite ASC")
    List<Tarefa> buscarTodas();

    @Query("SELECT * FROM tarefas WHERE id = :id")
    Tarefa buscarPorId(int id);

    @Query("SELECT COUNT(*) FROM tarefas")
    int contarTodas();
}
