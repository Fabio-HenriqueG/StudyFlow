package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Materias")
public class Materia implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nome;
    public int cor;

    public Materia(String nome, int cor) {
        this.nome = nome;
        this.cor = cor;
    }

    @Override
    public String toString() {
        return nome;
    }
}
