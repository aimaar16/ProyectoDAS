package com.das.proyectodas;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.io.File;
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
        Context context = holder.itemView.getContext();
        holder.nombre.setText(item.getNombre());

        // 1. Prioridad: Foto de la cámara (URI)
        if (item.getImagenUri() != null && !item.getImagenUri().isEmpty()) {
            File imgFile = new File(item.getImagenUri());
            if (imgFile.exists()) {
                holder.imagen.setImageURI(Uri.fromFile(imgFile));
            } else {
                holder.imagen.setImageResource(R.drawable.ic_launcher_background);
            }
        } 
        // 2. Segunda prioridad: Nombre del recurso drawable (del JSON)
        else if (item.getImagenNombre() != null && !item.getImagenNombre().isEmpty()) {
            int resId = context.getResources().getIdentifier(item.getImagenNombre(), "drawable", context.getPackageName());
            if (resId != 0) {
                holder.imagen.setImageResource(resId);
            } else {
                holder.imagen.setImageResource(R.drawable.ic_launcher_background);
            }
        }
        // 3. Fallback: imagenResId o defecto
        else {
            int idImagen = item.getImagenResId();
            if (idImagen != 0) {
                holder.imagen.setImageResource(idImagen);
            } else {
                holder.imagen.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        // --- Lógica de favoritos ---
        if (item.isEsFavorito()) {
            holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.btnFavorito.setOnClickListener(v -> {
            boolean nuevoEstado = !item.isEsFavorito();
            item.setEsFavorito(nuevoEstado);

            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(v.getContext());
                db.ropaDao().actualizarPrenda(item);

                holder.itemView.post(() -> {
                    notifyItemChanged(holder.getAdapterPosition());
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