package com.das.proyectodas;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.io.File;
import java.util.List;

// Adaptador para mostrar cada prenda en la lista
public class RopaAdapter extends RecyclerView.Adapter<RopaAdapter.RopaViewHolder> {
    private List<Ropa> listaRopa;

    public RopaAdapter(List<Ropa> listaRopa) {
        this.listaRopa = listaRopa;
    }

    @NonNull
    @Override
    public RopaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflamos el diseño de cada tarjeta de ropa
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ropa, parent, false);
        return new RopaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RopaViewHolder holder, int position) {
        Ropa item = listaRopa.get(position);
        Context context = holder.itemView.getContext();
        holder.nombre.setText(item.getNombre());

        // 1. Cargar imagen REMOTA desde servidor
        if (item.getImagenUri() != null && !item.getImagenUri().isEmpty()) {
            Glide.with(context)
                    .load(item.getImagenUri())   // ✔ URL remota
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.imagen);
        }
        // 2. Cargar imagen desde drawable
        else if (item.getImagenNombre() != null && !item.getImagenNombre().isEmpty()) {
            int resId = context.getResources().getIdentifier(item.getImagenNombre(), "drawable", context.getPackageName());
            Glide.with(context)
                    .load(resId != 0 ? resId : R.drawable.ic_launcher_background)
                    .into(holder.imagen);
        }
        // 3. Fallback
        else {
            Glide.with(context)
                    .load(R.drawable.ic_launcher_background)
                    .into(holder.imagen);
        }

        // Favoritos
        holder.btnFavorito.setImageResource(item.isEsFavorito() ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        holder.btnFavorito.setOnClickListener(v -> {
            item.setEsFavorito(!item.isEsFavorito());
            new Thread(() -> {
                AppDatabase db = AppDatabase.getDatabase(context);
                db.ropaDao().actualizarPrenda(item);
                holder.itemView.post(() -> notifyItemChanged(holder.getAdapterPosition()));
            }).start();
        });

        // Día de la semana
        holder.btnOutfit.setOnClickListener(v -> mostrarDialogoSeleccionarDia(context, item));
    }


    // Ventana que sale para elegir el día de la semana
    private void mostrarDialogoSeleccionarDia(Context context, Ropa item) {
        String[] dias = context.getResources().getStringArray(R.array.dias_semana);
        new AlertDialog.Builder(context)
                .setTitle(R.string.select_day)
                .setItems(dias, (dialog, which) -> {
                    String diaSeleccionado = dias[which];
                    item.setDiaSemana(diaSeleccionado);

                    new Thread(() -> {
                        AppDatabase.getDatabase(context).ropaDao().actualizarPrenda(item);
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).runOnUiThread(() -> {
                                Toast.makeText(context, item.getNombre() + " añadido al " + diaSeleccionado, Toast.LENGTH_SHORT).show();
                                // Mandamos la notificación avisando de que se ha añadido
                                lanzarNotificacionOutfit(context, item, diaSeleccionado);
                            });
                        }
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    // Función para crear y lanzar la notificación del outfit
    private void lanzarNotificacionOutfit(Context context, Ropa item, String dia) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("notificacion_id", 1001);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, flags);

        // Configuramos cómo se va a ver la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_today)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText("Has añadido " + item.getNombre() + " para el " + dia)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                // Botón que sale en la propia notificación
                .addAction(android.R.drawable.ic_menu_view, "Ver Outfit", pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(1001, builder.build());
        }
    }

    @Override
    public int getItemCount() { return listaRopa.size(); }

    // Clase para guardar las vistas de cada elemento de la lista
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
