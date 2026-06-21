package com.example.leitoracessivel.adapter;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitoracessivel.R;
import com.example.leitoracessivel.model.entities.Artigo;

import java.util.List;

public class ArtigoAdapter extends RecyclerView.Adapter<ArtigoViewHolder> {

    public interface OnArtigoClickListener {
        void onArtigoClick(Artigo artigo);
        void onArtigoEdit(Artigo artigo);
        void onArtigoDelete(Artigo artigo);
        void onArtigoShare(Artigo artigo);
    }

    private List<Artigo> artigos;
    private OnArtigoClickListener listener;
    private int posicaoSelecionada = -1;

    public ArtigoAdapter(List<Artigo> artigos, OnArtigoClickListener listener) {
        this.artigos = artigos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArtigoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artigo, parent, false);
        return new ArtigoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtigoViewHolder holder, int position) {
        Artigo artigo = artigos.get(position);
        holder.tvTitulo.setText(artigo.getTitulo());
        // Trunca preview do conteúdo
        String preview = artigo.getConteudo();
        if (preview.length() > 100) preview = preview.substring(0, 100) + "…";
        holder.tvPreview.setText(preview);
        holder.tvData.setText(artigo.getDataCriacao() != null
                ? artigo.getDataCriacao().substring(0, 10) : "");

        holder.itemView.setOnClickListener(v -> listener.onArtigoClick(artigo));

        holder.itemView.setOnLongClickListener(v -> {
            posicaoSelecionada = holder.getBindingAdapterPosition();
            return false; // deixa o sistema abrir o menu de contexto
        });
    }

    @Override
    public int getItemCount() {
        return artigos.size();
    }

    public Artigo getArtigo(int position) {
        return artigos.get(position);
    }

    public int getPosicaoSelecionada() {
        return posicaoSelecionada;
    }

    public void atualizarLista(List<Artigo> novaLista) {
        this.artigos = novaLista;
        notifyDataSetChanged();
    }

}
