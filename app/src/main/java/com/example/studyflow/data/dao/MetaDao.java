package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studyflow.data.Meta;
import java.util.List;

@Dao
public interface MetaDao {

    @Insert
    void inserir(Meta meta);

    @Update
    void atualizar(Meta meta);

    @Delete
    void excluir(Meta meta);

    @Query("SELECT * FROM metas ORDER BY id DESC")
    List<Meta> buscarTodas();

    @Query("SELECT COUNT(*) FROM metas")
    int contarTodas();
}
