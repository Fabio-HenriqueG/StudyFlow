package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studyflow.data.Anotacao;
import java.util.List;

@Dao
public interface AnotacaoDao {

    @Insert
    long inserir(Anotacao anotacao);

    @Update
    void atualizar(Anotacao anotacao);

    @Delete
    void excluir(Anotacao anotacao);

    // Busca todas as notas ordenando pela data de edição mais recente primeiro
    @Query("SELECT * FROM anotacoes ORDER BY dataUltimaEdicao DESC")
    List<Anotacao> buscarTodas();

    @Query("SELECT * FROM anotacoes WHERE id = :id")
    Anotacao buscarPorId(int id);

    @Query("SELECT COUNT(*) FROM anotacoes")
    int contarTodas();
}
