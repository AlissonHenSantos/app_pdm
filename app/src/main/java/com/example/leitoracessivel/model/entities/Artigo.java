package com.example.leitoracessivel.model.entities;

public class Artigo {
    private int id;
    private String titulo;
    private String conteudo;
    private String dataCriacao;

    public Artigo() {}

    public Artigo(int id, String titulo, String conteudo, String dataCriacao) {
        this.id = id;
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.dataCriacao = dataCriacao;
    }

    public Artigo(String titulo, String conteudo) {
        this.titulo = titulo;
        this.conteudo = conteudo;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    @Override
    public String toString() {
        return titulo;
    }
}
