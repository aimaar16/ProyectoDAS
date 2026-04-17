package com.das.proyectodas;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1. Cargar configuración osmdroid
        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx,
                PreferenceManager.getDefaultSharedPreferences(ctx));

        // 2. Inflar layout
        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        // 3. Inicializar el mapa
        MapView map = view.findViewById(R.id.map);

        // 4. Configurar estilo del mapa
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        GeoPoint startPoint = new GeoPoint(42.8467, -2.6731);
        map.getController().setZoom(12.0);
        map.getController().setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Vitoria-Gasteiz");
        startMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                // Acción al hacer clic en el marcador
                Toast.makeText(ctx, "Marcador clicado: " + marker.getTitle(),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        map.getOverlays().add(startMarker);

        return view;
    }
}
