package com.example.leitoracessivel.model.entities;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "artigos")
public class Artigo {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String titulo;

    @NonNull
    private String conteudo;

    @ColumnInfo(name = "data_criacao", defaultValue = "CURRENT_TIMESTAMP")
    private String dataCriacao;

    public Artigo() {
        this.titulo = "";
        this.conteudo = "";
    }

    @Ignore
    public Artigo(int id, @NonNull String titulo, @NonNull String conteudo, String dataCriacao) {
        this.id = id;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.dataCriacao = dataCriacao;
    }

    @Ignore
    public Artigo(@NonNull String titulo, @NonNull String conteudo) {
        this.titulo = titulo;
        this.conteudo = conteudo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    @NonNull
    public String getTitulo() { return titulo; }
    public void setTitulo(@NonNull String titulo) { this.titulo = titulo; }

    @NonNull
    public String getConteudo() { return conteudo; }
    public void setConteudo(@NonNull String conteudo) { this.conteudo = conteudo; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        return titulo;
    }
}
