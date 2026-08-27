package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.studyflow.data.AtividadeLog;
import java.util.List;

@Dao
public interface AtividadeLogDao {
    @Insert
    void inserir(AtividadeLog log);

    @Query("SELECT * FROM AtividadeLog WHERE dataMillis BETWEEN :inicio AND :fim ORDER BY dataMillis DESC")
    List<AtividadeLog> buscarPorPeriodo(long inicio, long fim);

    @Query("SELECT COUNT(*) FROM AtividadeLog WHERE dataMillis BETWEEN :inicio AND :fim AND tipo = :tipo")
    int contarPorTipoEPeriodo(String tipo, long inicio, long fim);

    @Query("SELECT DISTINCT DATE(dataMillis / 1000, 'unixepoch', 'localtime') FROM AtividadeLog ORDER BY dataMillis DESC")
    List<String> buscarDiasComAtividade();

    @Query("SELECT COUNT(DISTINCT materiaId) FROM AtividadeLog WHERE dataMillis BETWEEN :inicio AND :fim")
    int contarMateriasEstudadas(long inicio, long fim);
}
