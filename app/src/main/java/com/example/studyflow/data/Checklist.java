package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "Checklists")
public class Checklist implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String titulo;

    public Checklist(String titulo) {
        this.titulo = titulo;
    }
}
