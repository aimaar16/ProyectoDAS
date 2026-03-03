package com.das.proyectodas;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Asegúrate de usar ImageButton o ImageView
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.util.List;

public class RopaAdapter extends RecyclerView.Adapter<RopaAdapter.RopaViewHolder> {
    private List<Ropa> listaRopa;

    public RopaAdapter(List<Ropa> listaRopa) {
        this.listaRopa = listaRopa;
    }

    @NonNull
    @Override
    public RopaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ropa, parent, false);
        return new RopaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RopaViewHolder holder, int position) {
        Ropa item = listaRopa.get(position);
        holder.nombre.setText(item.getNombre());
        holder.imagen.setImageResource(item.getImagenResId());

        // 1. Configurar el icono inicial
        if (item.isEsFavorito()) {
            holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // 2. Listener para el clic
        holder.btnFavorito.setOnClickListener(v -> {
            // Cambiamos el estado en el objeto local
            boolean nuevoEstado = !item.isEsFavorito();
            item.setEsFavorito(nuevoEstado);

            // Actualizamos en la Base de Datos (en hilo secundario)
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(v.getContext());
                db.ropaDao().actualizarPrenda(item);

                // Volvemos al hilo principal para refrescar solo este item
                holder.itemView.post(() -> {
                    notifyItemChanged(position);
                });
            }).start();
        });
    }

    @Override
    public int getItemCount() { return listaRopa.size(); }

    public static class RopaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        ImageView btnFavorito;

        public RopaViewHolder(@NonNull View v) {
            super(v);
            imagen = v.findViewById(R.id.imgPrenda);
            nombre = v.findViewById(R.id.txtNombrePrenda);

            btnFavorito = v.findViewById(R.id.btnFavorito);
        }
    }
}