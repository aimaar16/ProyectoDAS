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
    private Button btnEliminar;
    private List<Ropa> listaActualRopa = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        recyclerView = root.findViewById(R.id.recyclerArmario);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cargarDatos();

        btnAnadir = root.findViewById(R.id.btnAnadirRopa);
        btnAnadir.setOnClickListener(v -> {
            verificarPermisosYAbrirCamara();
        });

        btnEliminar = root.findViewById(R.id.btnEliminarRopa);
        btnEliminar.setOnClickListener(v -> {
            mostrarOpcionesEliminar();
        });

        return root;
    }

    private void cargarDatos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Ropa> listaDB = db.ropaDao().obtenerTodaLaRopa();

            if (listaDB.isEmpty()) {
                // Aquí podrías decidir si cargar los de prueba o no. 
                // Por ahora lo dejamos vacío si se ha borrado.
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
                    if (which == 0) {
                        mostrarDialogoElegirUna();
                    } else {
                        mostrarDialogoConfirmarTodo();
                    }
                })
                .show();
    }

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

    private void eliminarPrendaEspecifica(Ropa ropa) {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().eliminarPrenda(ropa);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Prenda eliminada: " + ropa.getNombre(), Toast.LENGTH_SHORT).show();
                    cargarDatos();
                });
            }
        }).start();
    }

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

    private void vaciarArmario() {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().eliminarTodaLaRopa();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Armario vaciado", Toast.LENGTH_SHORT).show();
                    cargarDatos();
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
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        cameraLauncher.launch(null);
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(getContext(), "Permiso de cámara denegado.", Toast.LENGTH_LONG).show();
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

        View viewInflated = LayoutInflater.from(requireContext()).inflate(R.layout.nueva_ropa, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.editNombre);
        final Spinner spinnerCat = viewInflated.findViewById(R.id.spinnerCategoria);
        final CheckBox checkFav = viewInflated.findViewById(R.id.checkFavorito);

        builder.setView(viewInflated);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString();
            String categoria = spinnerCat.getSelectedItem().toString();
            boolean esFavorito = checkFav.isChecked();
            int imagenPath = R.drawable.ic_launcher_background;
            guardarEnBaseDeDatos(new Ropa(nombre, categoria, imagenPath, esFavorito));
        });

        builder.setNegativeButton("Cancelar", (dialog, i) -> dialog.cancel());
        builder.show();
    }

    private void guardarEnBaseDeDatos(Ropa nuevaPrenda) {
        new Thread(() -> {
            AppDatabase.getDatabase(getContext()).ropaDao().insertarPrenda(nuevaPrenda);
            getActivity().runOnUiThread(this::cargarDatos);
        }).start();
    }
}