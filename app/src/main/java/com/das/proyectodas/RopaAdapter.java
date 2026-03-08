package com.das.proyectodas;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        // Cargar imagen
        if (item.getImagenUri() != null && !item.getImagenUri().isEmpty()) {
            File imgFile = new File(item.getImagenUri());
            if (imgFile.exists()) {
                holder.imagen.setImageURI(Uri.fromFile(imgFile));
            } else {
                holder.imagen.setImageResource(R.drawable.ic_launcher_background);
            }
        } else if (item.getImagenNombre() != null && !item.getImagenNombre().isEmpty()) {
            int resId = context.getResources().getIdentifier(item.getImagenNombre(), "drawable", context.getPackageName());
            holder.imagen.setImageResource(resId != 0 ? resId : R.drawable.ic_launcher_background);
        } else {
            int idImagen = item.getImagenResId();
            holder.imagen.setImageResource(idImagen != 0 ? idImagen : R.drawable.ic_launcher_background);
        }

        // Favoritos
        holder.btnFavorito.setImageResource(item.isEsFavorito() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        holder.btnFavorito.setOnClickListener(v -> {
            item.setEsFavorito(!item.isEsFavorito());
            new Thread(() -> {
                AppDatabase.getDatabase(context).ropaDao().actualizarPrenda(item);
                holder.itemView.post(() -> notifyItemChanged(holder.getAdapterPosition()));
            }).start();
        });

        // Botón Añadir al Outfit
        holder.btnOutfit.setOnClickListener(v -> {
            mostrarDialogoSeleccionarDia(context, item);
        });
    }

    private void mostrarDialogoSeleccionarDia(Context context, Ropa item) {
        String[] dias = context.getResources().getStringArray(R.array.dias_semana);
        new AlertDialog.Builder(context)
                .setTitle(R.string.select_day)
                .setItems(dias, (dialog, which) -> {
                    String diaSeleccionado = dias[which];
                    item.setDiaSemana(diaSeleccionado);
                    
                    new Thread(() -> {
                        AppDatabase.getDatabase(context).ropaDao().actualizarPrenda(item);
                        // Usamos el context para mostrar el Toast en el hilo principal
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, item.getNombre() + " añadido al " + diaSeleccionado, Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public int getItemCount() { return listaRopa.size(); }

    public static class RopaViewHolder extends RecyclerView.ViewHolder {
        ImageView imagen;
        TextView nombre;
        ImageButton btnFavorito, btnOutfit;

        public RopaViewHolder(@NonNull View v) {
            super(v);
            imagen = v.findViewById(R.id.imgPrenda);
            nombre = v.findViewById(R.id.txtNombrePrenda);
            btnFavorito = v.findViewById(R.id.btnFavorito);
            btnOutfit = v.findViewById(R.id.btnAddToOutfit);
        }
    }
}