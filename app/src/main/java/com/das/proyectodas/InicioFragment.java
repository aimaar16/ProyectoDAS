package com.das.proyectodas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

// Fragmento de la pantalla principal que gestiona el perfil del usuario
public class InicioFragment extends Fragment {

    private ImageView imgPerfil;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        imgPerfil = view.findViewById(R.id.imgPerfil);

        // Cargamos la foto actual del perfil desde el servidor
        cargarFotoDesdeServidor();

        // Configuracion del lanzador para capturar el resultado de la camara
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        // Obtenemos la miniatura de la foto y la mostramos
                        Bitmap foto = (Bitmap) result.getData().getExtras().get("data");
                        imgPerfil.setImageBitmap(foto);

                        // Enviamos la nueva imagen al servidor
                        subirFotoPerfil(foto);
                    }
                }
        );

        // Al pulsar en la imagen se intenta abrir la camara para cambiar la foto
        imgPerfil.setOnClickListener(v -> verificarPermisosYAbrirCamara());

        return view;
    }

    // Gestiona la respuesta a la solicitud de permisos de la camara
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirCamara();
                else Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            });

    // Comprueba si tenemos permiso para usar la camara antes de abrirla
    private void verificarPermisosYAbrirCamara() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    // Lanza el intent del sistema para capturar una foto
    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    // Convierte el bitmap a base64 y lo sube al servidor mediante Volley
    private void subirFotoPerfil(Bitmap bitmap) {

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(getContext(), "Error: username no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compresion de la imagen para el envio
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        String imagenBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);

        String url = "http://34.175.196.12:81/subirFotoPerfil.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Refrescamos la imagen evitando el cache de Glide
                    String urlSinCache = response + "?t=" + System.currentTimeMillis();

                    Glide.with(this)
                            .load(urlSinCache)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .circleCrop()
                            .into(imgPerfil);

                    // Guardamos la nueva ruta localmente
                    SharedPreferences prefsFoto = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
                    prefsFoto.edit().putString("url_foto_" + username, urlSinCache).apply();

                    Toast.makeText(getContext(), "Foto actualizada", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(getContext(), "Error subiendo foto", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("imagen", imagenBase64);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    // Recupera la URL de la foto de perfil y la carga con Glide
    private void cargarFotoDesdeServidor() {

        SharedPreferences prefsUser = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefsUser.getString("username", null);

        SharedPreferences prefsFoto = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
        String url = prefsFoto.getString("url_foto_" + username, null);

        // Si ya tenemos la URL guardada la cargamos directamente
        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(imgPerfil);
            return;
        }

        // Si no hay rastro de la foto, preguntamos al servidor
        StringRequest request = new StringRequest(Request.Method.POST,
                "http://34.175.196.12:81/obtenerFotoPerfil.php",
                response -> {
                    if (!response.equals("NO_FOTO")) {
                        String urlSinCache = response + "?t=" + System.currentTimeMillis();

                        Glide.with(this)
                                .load(urlSinCache)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .circleCrop()
                                .into(imgPerfil);

                        prefsFoto.edit().putString("url_foto_" + username, response).apply();
                    }
                },
                error -> {}
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                return params;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
}
