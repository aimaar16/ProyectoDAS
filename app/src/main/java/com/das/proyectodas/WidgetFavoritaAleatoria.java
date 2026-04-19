package com.das.proyectodas;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.util.List;
import java.util.Random;

public class WidgetFavoritaAleatoria extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void actualizarWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_favorita_aleatoria);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            List<Ropa> favoritas = db.ropaDao().obtenerFavoritos();

            String nombre = "(sin favoritas)";
            if (favoritas != null && !favoritas.isEmpty()) {
                Random r = new Random();
                Ropa elegida = favoritas.get(r.nextInt(favoritas.size()));
                nombre = elegida.getNombre();
            }

            views.setTextViewText(R.id.txtNombrePrenda, nombre);

            // Al pulsar el widget → abrir la app y avisar que venimos del widget
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("desde_widget", true); // <--- ESTO ES LA PISTA
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );
            
            // Hacemos que todo el widget sea clickable
            views.setOnClickPendingIntent(R.id.txtNombrePrenda, pendingIntent);
            // También al layout principal (si tuviera ID) o al título
            views.setOnClickPendingIntent(R.id.txtTitulo, pendingIntent);

            manager.updateAppWidget(widgetId, views);
        }).start();
    }

    public static void forzarActualizacion(Context context) {
        Intent intent = new Intent(context, WidgetFavoritaAleatoria.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, WidgetFavoritaAleatoria.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }
}
