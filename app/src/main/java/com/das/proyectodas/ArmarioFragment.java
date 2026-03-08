package com.das.proyectodas;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.io.File;
import java.io.FileOutputStream;
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

// Esta pantalla es donde sale toda la ropa del armario
public class ArmarioFragment extends Fragment {

    private RecyclerView recyclerView;
    private Button btnAnadir;
    private Button btnEliminar;
    private List<Ropa> listaActualRopa = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        // Ponemos la lista en dos columnas
        recyclerView = root.findViewById(R.id.recyclerArmario);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Cargamos la ropa que haya guardada
        cargarDatos();

        // Botón para añadir ropa nueva (abre la cámara)
        btnAnadir = root.findViewById(R.id.btnAnadirRopa);
        btnAnadir.setOnClickListener(v -> {
            verificarPermisosYAbrirCamara();
        });

        // Botón para borrar ropa
        btnEliminar = root.findViewById(R.id.btnEliminarRopa);
        btnEliminar.setOnClickListener(v -> {
            mostrarOpcionesEliminar();
        });

        return root;
    }

    // Aquí leemos la base de datos para mostrar la ropa
    private void cargarDatos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Ropa> listaDB = db.ropaDao().obtenerTodaLaRopa();

            // Si el armario está vacío, metemos la ropa de prueba del JSON
            if (listaDB.isEmpty()) {
                List<Ropa> listaJson = cargarRopaDesdeJSON();
                for (Ropa r : listaJson) {
                    db.ropaDao().insertarPrenda(r);
                }
                listaDB = db.ropaDao().obtenerTodaLaRopa();
            }

            listaActualRopa = listaDB;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Ponemos los datos en el adaptador para que se vean
                    RopaAdapter adapter = new RopaAdapter(listaActualRopa);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }

    // Ventana para elegir si borrar una cosa o el armario entero
    private void mostrarOpcionesEliminar() {
        String[] opciones = {"Borrar una prenda", "Vaciar armario completo"};
        
        new AlertDialog.Builder(requireContext())
                .setTitle("¿Qué quieres eliminar?")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoElegirUna(); // Borrar solo una
                    } else {
                        mostrarDialogoConfirmarTodo(); // Borrar por completo
                    }
                })
                .show();
    }

    // Lista para elegir qué prenda borrar
    private void mostrarDialogoElegirUna() {
        if (listaActualRopa.isEmpty()) {
            Toast.makeText(getContext(), "No hay ropa para eliminar", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] nombres = new String[listaActualRopa.size()];
        for (int i = 0; i < listaActualRopa.size(); i++) {
            nombres[i] = listaActualRopa.get(i).getNombre();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Selecciona la prenda a eliminar")
                .setItems(nombres, (dialog, which) -> {
                    eliminarPrendaEspecifica(listaActualRopa.get(which));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Borramos la prenda de la base de datos y su foto si tiene
    private void eliminarPrendaEspecifica(Ropa ropa) {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            db.ropaDao().eliminarPrenda(ropa);
            
            if (ropa.getImagenUri() != null) {
                File file = new File(ropa.getImagenUri());
                if (file.exists()) file.delete();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Prenda eliminada: " + ropa.getNombre(), Toast.LENGTH_SHORT).show();
                    cargarDatos(); // Refrescamos la lista
                });
            }
        }).start();
    }

    // Aviso para no borrar el armario sin querer
    private void mostrarDialogoConfirmarTodo() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar toda la ropa")
                .setMessage("¿Estás seguro de que quieres vaciar el armario? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar Todo", (dialog, which) -> {
                    vaciarArmario();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Borramos por completo
    private void vaciarArmario() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Ropa> todas = db.ropaDao().obtenerTodaLaRopa();
            
            for (Ropa r : todas) {
                if (r.getImagenUri() != null) {
                    File file = new File(r.getImagenUri());
                    if (file.exists()) file.delete();
                }
            }

            db.ropaDao().eliminarTodaLaRopa();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Armario vaciado", Toast.LENGTH_SHORT).show();
                    cargarDatos();
                });
            }
        }).start();
    }

    // Leemos el archivo JSON con la ropa de prueba
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

    // Miramos si tenemos permiso para usar la cámara
    private void verificarPermisosYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        cameraLauncher.launch(null);
    }

    // Si nos dan permiso, abrimos la cámara
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();
                }
            });

    // Cuando hacemos la foto, abrimos el diálogo para guardarla
    private final ActivityResultLauncher<Void> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicturePreview(),
            bitmap -> {
                if (bitmap != null) {
                    mostrarDialogoNuevaRopa(bitmap);
                }
            }
    );

    // Ventana para ponerle nombre y categoría a la ropa nueva
    private void mostrarDialogoNuevaRopa(Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Nueva Prenda");

        View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.nueva_ropa, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.editNombre);
        final Spinner spinnerCat = viewInflated.findViewById(R.id.spinnerCategoria);
        final CheckBox checkFav = viewInflated.findViewById(R.id.checkFavorito);

        builder.setView(viewInflated);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String categoria = spinnerCat.getSelectedItem().toString();
            boolean esFavorito = checkFav.isChecked();
            
            // Guardamos la foto en el móvil y nos quedamos con la ruta
            String uri = guardarImagenEnAlmacenamiento(bitmap, nombre);
            
            Ropa nuevaPrenda = new Ropa(nombre, categoria, 0, esFavorito);
            nuevaPrenda.setImagenUri(uri);
            
            guardarEnBaseDeDatos(nuevaPrenda);
        });

        builder.setNegativeButton("Cancelar", (dialog, i) -> dialog.cancel());
        builder.show();
    }

    // Guardamos la foto en una carpeta interna de la app
    private String guardarImagenEnAlmacenamiento(Bitmap bitmap, String nombre) {
        File directory = new File(requireContext().getFilesDir(), "imagenes_ropa");
        if (!directory.exists()) directory.mkdirs();

        File file = new File(directory, nombre + "_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Metemos la prenda en la base de datos de Room
    private void guardarEnBaseDeDatos(Ropa nuevaPrenda) {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().insertarPrenda(nuevaPrenda);
            getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }
}