package com.example.leitoracessivel.model.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.leitoracessivel.model.entities.Artigo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "leitor_acessivel.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_ARTIGOS = "artigos";
    public static final String COL_ID = "id";
    public static final String COL_TITULO = "titulo";
    public static final String COL_CONTEUDO = "conteudo";
    public static final String COL_DATA = "data_criacao";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_ARTIGOS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TITULO + " TEXT NOT NULL, " +
            COL_CONTEUDO + " TEXT NOT NULL, " +
            COL_DATA + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTIGOS);
        onCreate(db);
    }

    public long inserirArtigo(Artigo artigo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITULO, artigo.getTitulo());
        values.put(COL_CONTEUDO, artigo.getConteudo());
        long id = db.insert(TABLE_ARTIGOS, null, values);
        db.close();
        return id;
    }

    public List<Artigo> listarArtigos() {
        List<Artigo> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ARTIGOS, null, null, null,
                null, null, COL_DATA + " DESC");
        if (cursor.moveToFirst()) {
            do {
                Artigo a = new Artigo(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTEUDO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA))
                );
                lista.add(a);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    public Artigo buscarArtigo(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_ARTIGOS, null,
                COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
        Artigo artigo = null;
        if (cursor.moveToFirst()) {
            artigo = new Artigo(
                cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_TITULO)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTEUDO)),
                cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA))
            );
        }
        cursor.close();
        db.close();
        return artigo;
    }

    public int atualizarArtigo(Artigo artigo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITULO, artigo.getTitulo());
        values.put(COL_CONTEUDO, artigo.getConteudo());
        int rows = db.update(TABLE_ARTIGOS, values,
                COL_ID + "=?", new String[]{String.valueOf(artigo.getId())});
        db.close();
        return rows;
    }

    public void deletarArtigo(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ARTIGOS, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
    }
}
