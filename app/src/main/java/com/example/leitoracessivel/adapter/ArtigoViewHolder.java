package com.example.leitoracessivel.adapter;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitoracessivel.R;

public class ArtigoViewHolder extends RecyclerView.ViewHolder
        implements View.OnCreateContextMenuListener {

    TextView tvTitulo, tvPreview, tvData;

    public ArtigoViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTitulo = itemView.findViewById(R.id.tv_titulo);
        tvPreview = itemView.findViewById(R.id.tv_preview);
        tvData = itemView.findViewById(R.id.tv_data);
        itemView.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Opções do artigo");
        menu.add(Menu.NONE, R.id.menu_context_ouvir, 0, "▶ Ouvir");
        menu.add(Menu.NONE, R.id.menu_context_editar, 1, "✏ Editar");
        menu.add(Menu.NONE, R.id.menu_context_compartilhar, 2, "📤 Compartilhar");
        menu.add(Menu.NONE, R.id.menu_context_deletar, 3, "🗑 Deletar");
    }
}