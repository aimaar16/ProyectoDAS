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

public class InicioFragment extends Fragment {

    private ImageView imgPerfil;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        imgPerfil = view.findViewById(R.id.imgPerfil);

        // Cargar foto desde tu servidor
        cargarFotoDesdeServidor();

        // Inicializar cámara
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap foto = (Bitmap) result.getData().getExtras().get("data");
                        imgPerfil.setImageBitmap(foto);

                        subirFotoPerfil(foto);
                    }
                }
        );

        imgPerfil.setOnClickListener(v -> verificarPermisosYAbrirCamara());

        return view;
    }
    // Permisos para la cámara como en el Armario Fragment
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) abrirCamara();
                else Toast.makeText(getContext(), "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            });
    private void verificarPermisosYAbrirCamara() {
        if (requireContext().checkSelfPermission(android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    private void subirFotoPerfil(Bitmap bitmap) {

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", null);

        Log.d("FOTO", "Username enviado: " + username);

        if (username == null) {
            Toast.makeText(getContext(), "Error: username no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        String imagenBase64 = android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT);

        String url = "http://34.175.196.12:81/subirFotoPerfil.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    String urlSinCache = response + "?t=" + System.currentTimeMillis();

                    Glide.with(this)
                            .load(urlSinCache)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .circleCrop()
                            .into(imgPerfil);

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


    private void cargarFotoDesdeServidor() {

        SharedPreferences prefsUser = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefsUser.getString("username", null);

        SharedPreferences prefsFoto = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
        String url = prefsFoto.getString("url_foto_" + username, null);

        if (url != null) {
            Glide.with(this)
                    .load(url)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .into(imgPerfil);
            return;
        }

        // Si no hay foto guardada, pedirla al servidor
        StringRequest request = new StringRequest(Request.Method.POST,
                "http://34.175.196.12:81/obtenerFotoPerfil.php",
                response -> {
                    if (!response.equals("NO_FOTO")) {
                        String urlSinCache = response + "?t=" + System.currentTimeMillis();

                        Glide.with(this)
                                .load(urlSinCache)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
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
