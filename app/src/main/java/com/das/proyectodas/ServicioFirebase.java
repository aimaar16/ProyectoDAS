package com.das.proyectodas;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// Servicio encargado de recibir y gestionar las notificaciones push de Firebase
public class ServicioFirebase extends FirebaseMessagingService {

    private static final String TAG = "FCM";

    // Se ejecuta cuando llega un mensaje de Firebase estando la app en primer plano
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d(TAG, "Mensaje recibido de: " + remoteMessage.getFrom());

        // Si el mensaje contiene una notificacion, extraemos titulo y cuerpo
        if (remoteMessage.getNotification() != null) {
            String titulo = remoteMessage.getNotification().getTitle();
            String cuerpo = remoteMessage.getNotification().getBody();

            mostrarNotificacion(titulo, cuerpo);
        }
    }

    // Configura y lanza la notificacion local en el dispositivo
    private void mostrarNotificacion(String titulo, String cuerpo) {

        // Comprobacion obligatoria de permisos para Android 13+
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Construccion de la notificacion usando el canal definido en MainActivity
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_add)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Lanzamos la notificacion con un ID unico
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1001, builder.build());
    }

    // Se llama cuando el token de Firebase se genera o se renueva
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Nuevo token FCM: " + token);
        // Este token es el que se envia al servidor PHP para identificar al dispositivo
    }
}
