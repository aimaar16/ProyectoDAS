package com.das.proyectodas;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

public class ArmarioFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAnadir;
    private Uri fotoUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        recyclerView = root.findViewById(R.id.recyclerArmario);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cargarDatos();

        btnAnadir = root.findViewById(R.id.btnAnadirRopa);
        btnAnadir.setOnClickListener(v -> {
            // CAMBIO AQUÍ: Primero comprobamos el permiso
            verificarPermisosYAbrirCamara();
        });

        return root;
    }

    private void cargarDatos() {
        // Room no permite trabajar en el hilo principal
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());

            // 1. Intentamos obtener lo que ya hay en la DB
            List<Ropa> listaDB = db.ropaDao().obtenerTodaLaRopa();

            // 2. Si es la primera vez, volcamos el JSON a Room
            if (listaDB.isEmpty()) {
                List<Ropa> listaJson = cargarRopaDesdeJSON();
                for (Ropa r : listaJson) {
                    db.ropaDao().insertarPrenda(r);
                }
                // Volvemos a leer para obtener los objetos con sus IDs generados
                listaDB = db.ropaDao().obtenerTodaLaRopa();
            }

            // 3. Pasamos la lista con IDs reales al adaptador en el hilo UI
            List<Ropa> listaFinal = listaDB;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    RopaAdapter adapter = new RopaAdapter(listaFinal);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }

    private List<Ropa> cargarRopaDesdeJSON() {
        String json = null;
        try {
            InputStream is = requireContext().getAssets().open("ropa_prueba.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        Type listType = new TypeToken<List<Ropa>>() {}.getType();
        return gson.fromJson(json, listType);
    }
    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Si ya tenemos permiso, abrimos la cámara
            abrirCamara();
        } else {
            // Si no, lo pedimos usando el launcher que ya definiste
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    private void abrirCamara() {
        cameraLauncher.launch(null);
    }
    // Registramos el lanzador para la cámara
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado. No puedes añadir fotos.", Toast.LENGTH_LONG).show();
                }
            });
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    mostrarDialogoNuevaRopa(bitmap);
                }
            }
    );
    private void mostrarDialogoNuevaRopa(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva Prenda");

        // Layout sencillo para el diálogo
        View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.nueva_ropa, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.editNombre);
        final Spinner spinnerCat = viewInflated.findViewById(R.id.spinnerCategoria);
        final CheckBox checkFav = viewInflated.findViewById(R.id.checkFavorito);

        builder.setView(viewInflated);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String categoria = spinnerCat.getSelectedItem().toString();
            boolean esFavorito = checkFav.isChecked();

            // Imagen por defecto
            int imagenPath = R.drawable.ic_launcher_background;

            guardarEnBaseDeDatos(new Ropa(nombre, categoria, imagenPath, esFavorito));
        });

        builder.setNegativeButton("Cancelar", (dialog, i) -> dialog.cancel());
        builder.show();
    }
    //Metodo de utilidad para pasar la imagen a la DB
    private String bitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private void guardarEnBaseDeDatos(Ropa nuevaPrenda) {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().insertarPrenda(nuevaPrenda);
            // Recargamos la lista en el hilo principal
            getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }
}