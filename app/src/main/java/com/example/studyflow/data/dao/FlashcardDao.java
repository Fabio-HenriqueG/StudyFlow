package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studyflow.data.Flashcard;
import java.util.List;

@Dao
public interface FlashcardDao {
    @Insert
    void inserir(Flashcard flashcard);

    @Update
    void atualizar(Flashcard flashcard);

    @Delete
    void excluir(Flashcard flashcard);

    @Query("SELECT * FROM Flashcards ORDER BY dataProximaRevisao ASC")
    List<Flashcard> buscarTodos();

    @Query("SELECT * FROM Flashcards WHERE dataProximaRevisao <= :hoje ORDER BY dataProximaRevisao ASC")
    List<Flashcard> buscarParaRevisarHoje(long hoje);

    @Query("SELECT * FROM Flashcards WHERE materiaId = :materiaId ORDER BY dataProximaRevisao ASC")
    List<Flashcard> buscarPorMateria(int materiaId);

    @Query("SELECT COUNT(*) FROM Flashcards")
    int contarTotal();

    @Query("SELECT COUNT(*) FROM Flashcards WHERE nivelDominio >= 5")
    int contarDominados();

    @Query("SELECT COUNT(*) FROM Flashcards WHERE nivelDominio > 0 AND nivelDominio < 5")
    int contarAprendendo();
    
    @Query("SELECT COUNT(*) FROM Flashcards WHERE materiaId = :materiaId AND nivelDominio >= 5")
    int contarDominadosPorMateria(int materiaId);

    @Query("SELECT COUNT(*) FROM Flashcards WHERE dataProximaRevisao BETWEEN :inicio AND :fim")
    int contarPorIntervalo(long inicio, long fim);

    @Query("SELECT COUNT(*) FROM Flashcards WHERE dataProximaRevisao <= :hoje")
    int contarParaRevisarHoje(long hoje);
}
