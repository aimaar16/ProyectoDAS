package com.das.proyectodas;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArmarioFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAnadir, btnEliminar;
    private List<Ropa> listaActualRopa = new ArrayList<>();

    // Cámara
    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bitmap miniatura = (Bitmap) result.getData().getExtras().get("data");
                    mostrarDialogoNuevaRopa(miniatura);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        recyclerView = root.findViewById(R.id.recyclerArmario);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cargarDatos();

        btnAnadir = root.findViewById(R.id.btnAnadirRopa);
        btnAnadir.setOnClickListener(v -> verificarPermisosYAbrirCamara());

        btnEliminar = root.findViewById(R.id.btnEliminarRopa);
        btnEliminar.setOnClickListener(v -> mostrarOpcionesEliminar());

        return root;
    }

    // -------------------------
    // CARGAR DATOS
    // -------------------------
    private void cargarDatos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Ropa> listaDB = db.ropaDao().obtenerTodaLaRopa();

            if (listaDB.isEmpty()) {
                List<Ropa> listaJson = cargarRopaDesdeJSON();
                for (Ropa r : listaJson) db.ropaDao().insertarPrenda(r);
                listaDB = db.ropaDao().obtenerTodaLaRopa();
            }

            listaActualRopa = listaDB;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    RopaAdapter adapter = new RopaAdapter(listaActualRopa);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }

    private List<Ropa> cargarRopaDesdeJSON() {
        try {
            InputStream is = requireContext().getAssets().open("ropa_prueba.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            return new Gson().fromJson(json, new TypeToken<List<Ropa>>() {}.getType());
        } catch (IOException ex) {
            return new ArrayList<>();
        }
    }

    // -------------------------
    // ELIMINAR ROPA
    // -------------------------
    private void mostrarOpcionesEliminar() {
        String[] opciones = {"Borrar una prenda", "Vaciar armario completo"};

        new AlertDialog.Builder(requireContext())
                .setTitle("¿Qué quieres eliminar?")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) mostrarDialogoElegirUna();
                    else mostrarDialogoConfirmarTodo();
                })
                .show();
    }

    private void mostrarDialogoElegirUna() {
        if (listaActualRopa.isEmpty()) {
            Toast.makeText(getContext(), "No hay prendas para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[listaActualRopa.size()];
        for (int i = 0; i < listaActualRopa.size(); i++)
            nombres[i] = listaActualRopa.get(i).getNombre();

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona la prenda a eliminar")
                .setItems(nombres, (dialog, which) -> eliminarPrendaEspecifica(listaActualRopa.get(which)))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarPrendaEspecifica(Ropa ropa) {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().eliminarPrenda(ropa);
            if (getActivity() != null)
                getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }

    private void mostrarDialogoConfirmarTodo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar toda la ropa")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Eliminar Todo", (dialog, which) -> vaciarArmario())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void vaciarArmario() {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().eliminarTodaLaRopa();
            if (getActivity() != null)
                getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }

    // -------------------------
    // AÑADIR ROPA
    // -------------------------
    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
            abrirCamara();
        else
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirCamara();
            });

    private void mostrarDialogoNuevaRopa(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva Prenda");
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.nueva_ropa, null);

        final EditText inputNombre = view.findViewById(R.id.editNombre);
        final Spinner spinnerCat = view.findViewById(R.id.spinnerCategoria);
        final CheckBox checkFav = view.findViewById(R.id.checkFavorito);

        builder.setView(view);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String categoria = spinnerCat.getSelectedItem().toString();
            boolean esFav = checkFav.isChecked();

            subirRopaAlServidor(bitmap, nombre, categoria, esFav);
        });

        builder.setNegativeButton("Cancelar", null).show();
    }

    // -------------------------
    // SUBIR ROPA AL SERVIDOR PHP
    // -------------------------
    private void subirRopaAlServidor(Bitmap foto, String nombre, String categoria, boolean esFav) {

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(getContext(), "Error: usuario no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convertir imagen a Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        String imagenBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);

        String url = "http://34.175.220.9:81/subirRopa.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {

                    Ropa r = new Ropa(nombre, categoria, 0, esFav);
                    r.setImagenUri(response);

                    new Thread(() -> {
                        AppDatabase.getDatabase(getContext()).ropaDao().insertarPrenda(r);
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Prenda subida correctamente", Toast.LENGTH_SHORT).show();
                            cargarDatos();
                        });
                    }).start();
                },
                error -> Toast.makeText(getContext(), "Error subiendo prenda", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("nombre", nombre);
                params.put("categoria", categoria);
                params.put("imagenNombre", nombre);
                params.put("esFavorito", esFav ? "1" : "0");
                params.put("diaSemana", "");
                params.put("imagen", imagenBase64);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
