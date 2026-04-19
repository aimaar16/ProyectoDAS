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
        return true;


    }
    @Override
    public boolean longPressHelper(GeoPoint p) {
        return false;
    }
}