package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Materias")
public class Materia implements Serializable {
    @PrimaryKey
    @NonNull
    public String id = "";
    
    public String nome;
    public int cor;

    // Construtor vazio para o Firebase
    public Materia() {
    }

    public Materia(String nome, int cor) {
        this.nome = nome;
        this.cor = cor;
    }

    @Override
    public String toString() {
        return nome;
    }
}
