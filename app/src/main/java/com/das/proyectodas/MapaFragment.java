package com.das.proyectodas;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.HashMap;
import java.util.Map;

public class MapaFragment extends Fragment {

    private MapView map;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Context ctx = requireContext().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        View view = inflater.inflate(R.layout.fragment_mapa, container, false);

        map = view.findViewById(R.id.map);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        map.setMultiTouchControls(true);

        MyLocationNewOverlay myLocationOverlay =
                new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), map);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        map.getOverlays().add(myLocationOverlay);

        // Zoom por defecto
        map.getController().setZoom(15.0);

        // Listener para clics manuales
        MiMapaEventsReceiver mapEventsReceiver = new MiMapaEventsReceiver(ctx, map, this);
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        map.getOverlays().add(mapEventsOverlay);

        // 1. Forzar ubicación y subirla
        solicitarUbicacionReal();

        // 2. Cargar ubicaciones de otros
        cargarUbicaciones();

        return view;
    }

    public void subirUbicacionAlServidor(double lat, double lon) {
        String username = getUsername();
        String url = "http://34.175.196.12:81/insertar_ubicaciones.php";

        Log.d("MAPA_SYNC", "Intentando subir: user=" + username + ", lat=" + lat + ", lon=" + lon);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("MAPA_SYNC", "Respuesta servidor: " + response);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Ubicación sincronizada", Toast.LENGTH_SHORT).show();
                        cargarUbicaciones();
                    }
                },
                error -> {
                    Log.e("MAPA_SYNC", "Error Volley: " + error.toString());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error de conexión al servidor", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", username);
                params.put("lat", String.valueOf(lat));
                params.put("lon", String.valueOf(lon));
                params.put("descripcion", "GPS Automatico");
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void solicitarUbicacionReal() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        map.getController().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
                        subirUbicacionAlServidor(location.getLatitude(), location.getLongitude());
                    }
                });
    }

    private void cargarUbicaciones() {
        String url = "http://34.175.196.12:81/obtener_ubicaciones.php";
        String miUsuario = getUsername();

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        map.getOverlays().removeIf(o -> o instanceof Marker);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            double lat = obj.getDouble("lat");
                            double lon = obj.getDouble("lon");
                            String userRes = obj.getString("username");

                            if (!userRes.equalsIgnoreCase(miUsuario)) {
                                Marker marker = new Marker(map);
                                marker.setPosition(new GeoPoint(lat, lon));
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                marker.setTitle(userRes);
                                map.getOverlays().add(marker);
                            }
                        }
                        map.invalidate();
                    } catch (Exception e) {
                        Log.e("MAPA_SYNC", "Error JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("MAPA_SYNC", "Error cargando: " + error.getMessage()));

        Volley.newRequestQueue(requireContext()).add(request);
    }

    public String getUsername() {
        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", MODE_PRIVATE);
        String name = prefs.getString("username", "Anonimo");
        Log.d("MAPA_SYNC", "Usuario recuperado: " + name);
        return name;
    }
}
