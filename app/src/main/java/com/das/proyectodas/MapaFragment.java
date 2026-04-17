package com.das.proyectodas;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;

import java.util.HashMap;
import java.util.Map;


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
        MyLocationNewOverlay myLocationOverlay =
                new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);

        myLocationOverlay.enableMyLocation(); // Activa GPS
        myLocationOverlay.enableFollowLocation(); // La cámara sigue al usuario

        map.getOverlays().add(myLocationOverlay);

        GeoPoint startPoint = new GeoPoint(42.8467, -2.6731);
        map.getController().setZoom(12.0);
        map.getController().setCenter(startPoint);

        Marker startMarker = new Marker(map);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
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
        MiMapaEventsReceiver mapEventsReceiver = new MiMapaEventsReceiver(ctx, map,this);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        map.getOverlays().add(mapEventsOverlay);

        // Cargar las ubicaciones del servidor en el mapa al iniciar la actividad
        cargarUbicaciones(map);
        return view;
    }

    public void subirUbicacionAlServidor(double lat, double lon) {

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", MODE_PRIVATE);
        String username = prefs.getString("username", null);

        String url = "http://34.175.196.12:81/insertar_ubicaciones.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(getContext(), "Ubicación guardada", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Toast.makeText(getContext(), "Error al subir ubicación", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("user", username);
                params.put("lat", String.valueOf(lat));
                params.put("lon", String.valueOf(lon));
                params.put("descripcion", "Ubicación seleccionada");

                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void cargarUbicaciones(MapView map) {

        String url = "http://34.175.196.12:81/obtener_ubicaciones.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {

                    try {
                        JSONArray array = new JSONArray(response);

                        for (int i = 0; i < array.length(); i++) {

                            JSONObject obj = array.getJSONObject(i);

                            double lat = obj.getDouble("lat");
                            double lon = obj.getDouble("lon");
                            String username = obj.getString("username");

                            Marker marker = new Marker(map);
                            marker.setPosition(new GeoPoint(lat, lon));
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            marker.setTitle(username);
                            map.getOverlays().add(marker);
                        }

                        map.invalidate();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Error parseando ubicaciones", Toast.LENGTH_SHORT).show();
                    }

                },
                error -> {
                    Toast.makeText(getContext(), "Error al cargar ubicaciones", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(requireContext()).add(request);
    }
    // Obtenemos nombre del usuario para mostrarlo en el mapa
    public String getUsername() {
        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", MODE_PRIVATE);
        return prefs.getString("username", "Usuario");
    }

}
