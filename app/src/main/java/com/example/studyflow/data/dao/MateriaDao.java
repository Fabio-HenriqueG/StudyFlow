package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studyflow.data.Materia;
import java.util.List;

@Dao
public interface MateriaDao {
    @Insert
    long inserir(Materia materia);

    @Update
    void atualizar(Materia materia);

    @Delete
    void excluir(Materia materia);

    @Query("SELECT * FROM Materias ORDER BY nome ASC")
    List<Materia> buscarTodas();
    
    @Query("SELECT * FROM Materias WHERE id = :id")
    Materia buscarPorId(int id);
}
