package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * Entidade que representa uma Anotação no banco de dados.
 * Usamos HTML para salvar o texto formatado (cores, negrito, etc).
 */
@Entity(tableName = "Anotacoes")
public class Anotacao implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulo;

    // Conteúdo da nota em formato HTML para preservar a estilização
    public String conteudoHtml;

    // Data da última vez que a nota foi salva
    public long dataUltimaEdicao;

    public Anotacao(String titulo, String conteudoHtml, long dataUltimaEdicao) {
        this.titulo = titulo;
        this.conteudoHtml = conteudoHtml;
        this.dataUltimaEdicao = dataUltimaEdicao;
    }
}
