package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.studyflow.data.Tarefa;

import java.util.List;

@Dao
public interface TarefaDao {

    @Insert
    void inserir(Tarefa tarefa); // Esse método vai salvar a tarefa que você enviar

    @Query("SELECT * FROM tarefas ORDER BY dataLimite ASC")
    List<Tarefa> buscarTodas(); // Esse método vai puxar todas as tarefas salvas para mostrar na tela inicial
}
