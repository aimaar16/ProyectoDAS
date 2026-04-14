package com.das.proyectodas;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

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
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    
    // URL para subir la imagen al servidor en Google Cloud
    private static final String URL_SUBIR_IMAGEN = "http://34.175.220.9:81/subir_imagen.php";

    // Launcher para sacar la foto
    private ActivityResultLauncher<Intent> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap laminiatura = (Bitmap) bundle.get("data");
                    // Cuando tenemos la foto, abrimos el diálogo para guardar y subir
                    mostrarDialogoNuevaRopa(laminiatura);
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

    private void mostrarOpcionesEliminar() {
        String[] opciones = {"Borrar una prenda", "Vaciar armario completo"};
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Qué quieres eliminar?")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) mostrarDialogoElegirUna();
                    else mostrarDialogoConfirmarTodo();
                }).show();
    }

    private void mostrarDialogoElegirUna() {
        if (listaActualRopa.isEmpty()) return;
        String[] nombres = new String[listaActualRopa.size()];
        for (int i = 0; i < listaActualRopa.size(); i++) nombres[i] = listaActualRopa.get(i).getNombre();

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona la prenda a eliminar")
                .setItems(nombres, (dialog, which) -> eliminarPrendaEspecifica(listaActualRopa.get(which)))
                .setNegativeButton("Cancelar", null).show();
    }

    private void eliminarPrendaEspecifica(Ropa ropa) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.ropaDao().eliminarPrenda(ropa);
            if (ropa.getImagenUri() != null) {
                File file = new File(ropa.getImagenUri());
                if (file.exists()) file.delete();
            }
            if (getActivity() != null) getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }

    private void mostrarDialogoConfirmarTodo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar toda la ropa")
                .setMessage("¿Estás seguro?")
                .setPositiveButton("Eliminar Todo", (dialog, which) -> vaciarArmario())
                .setNegativeButton("Cancelar", null).show();
    }

    private void vaciarArmario() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            for (Ropa r : db.ropaDao().obtenerTodaLaRopa()) {
                if (r.getImagenUri() != null) {
                    File f = new File(r.getImagenUri());
                    if (f.exists()) f.delete();
                }
            }
            db.ropaDao().eliminarTodaLaRopa();
            if (getActivity() != null) getActivity().runOnUiThread(this::cargarDatos);
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
        } catch (IOException ex) { return new ArrayList<>(); }
    }

    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) abrirCamara();
        else requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void abrirCamara() {
        // Lanzar cámara mediante Intent según Diapositiva 6
        Intent elIntentFoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(elIntentFoto);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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
            
            // 1. Guardar localmente
            String uriLocal = guardarImagenEnAlmacenamiento(bitmap, nombre);
            
            // 2. Subir al servidor
            subirImagenAlServidor(bitmap, nombre);
            
            Ropa r = new Ropa(nombre, categoria, 0, esFav);
            r.setImagenUri(uriLocal);
            new Thread(() -> {
                AppDatabase.getDatabase(getContext()).ropaDao().insertarPrenda(r);
                getActivity().runOnUiThread(this::cargarDatos);
            }).start();
        });
        builder.setNegativeButton("Cancelar", null).show();
    }

    private String guardarImagenEnAlmacenamiento(Bitmap bitmap, String nombre) {
        File dir = new File(requireContext().getFilesDir(), "imagenes_ropa");
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, nombre + "_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (IOException e) { return null; }
    }

    // Metodo para subir imagen
    private void subirImagenAlServidor(Bitmap foto, String nombreArchivo) {
        // Transformar bitmap a string en Base64 usando PNG
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        foto.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] fototransformada = stream.toByteArray();
        final String fotoen64 = Base64.encodeToString(fototransformada, Base64.DEFAULT);

        // Generar un identificador único
        final String pid = String.valueOf(System.currentTimeMillis());

        StringRequest request = new StringRequest(Request.Method.POST, URL_SUBIR_IMAGEN,
                response -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Guardado en BD remota", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Parámetros
                params.put("identificador", pid);
                params.put("imagen", fotoen64);
                params.put("titulo", nombreArchivo);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}