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

        // USAMOS EL NOMBRE CORRECTO: holder.imagen (que es como está en tu ViewHolder)
        int idImagen = item.getImagenResId();

        try {
            if (idImagen != 0) {
                holder.imagen.setImageResource(idImagen);
            } else {
                holder.imagen.setImageResource(R.drawable.ic_launcher_background);
            }
        } catch (Exception e) {
            // Esto evita que si el ID del JSON es viejo, la app se cierre
            holder.imagen.setImageResource(R.drawable.ic_launcher_background);
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
                    notifyItemChanged(holder.getAdapterPosition()); // Mejor usar getAdapterPosition()
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