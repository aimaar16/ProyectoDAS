package com.das.proyectodas;

import android.content.Context;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MiMapaEventsReceiver implements org.osmdroid.events.MapEventsReceiver {

    private Context context;
    private MapView map;
    private Marker marcadorActual = null;
    private MapaFragment fragment;


    public MiMapaEventsReceiver(Context context, MapView map, MapaFragment fragment) {
        this.context = context;
        this.map = map;
        this.fragment = fragment;
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        /*
        // 1. Borrar marcador anterior si existe
        if (marcadorActual != null) {
            map.getOverlays().remove(marcadorActual);
        }

        // 2. Crear nuevo marcador con nombre del usuario
        String username = fragment.getUsername();
        marcadorActual = new Marker(map);
        marcadorActual.setPosition(p);
        marcadorActual.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcadorActual.setTitle(username);
        map.getOverlays().add(marcadorActual);


        map.invalidate();

        // 3. Subir al servidor
        fragment.subirUbicacionAlServidor(p.getLatitude(), p.getLongitude());
        */
        return true;


    }
    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}