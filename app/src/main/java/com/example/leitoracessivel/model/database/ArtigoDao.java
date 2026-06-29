package com.example.leitoracessivel.model.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.leitoracessivel.model.entities.Artigo;

import java.util.List;

@Dao
public interface ArtigoDao {

    @Query("SELECT * FROM artigos ORDER BY data_criacao DESC")
    List<Artigo> listarArtigos();

    @Query("SELECT * FROM artigos WHERE id = :id")
    Artigo buscarArtigo(int id);

    @Insert
    long inserirArtigo(Artigo artigo);

    @Update
    int atualizarArtigo(Artigo artigo);

    @Delete
    void deletarArtigo(Artigo artigo);

    @Query("DELETE FROM artigos WHERE id = :id")
    void deletarArtigoPorId(int id);
}
