package com.das.proyectodas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class InicioFragment extends Fragment {

    private ImageView imgPerfil;
    private ActivityResultLauncher<Intent> takePictureLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        imgPerfil = view.findViewById(R.id.imgPerfil);

        // Cargar foto desde Firebase si existe
        cargarFotoDesdeFirebase();

        // Inicializar cámara
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap foto = (Bitmap) result.getData().getExtras().get("data");
                        imgPerfil.setImageBitmap(foto);

                        guardarFotoLocal(foto);
                        subirFotoFirebase(foto);
                    }
                }
        );

        imgPerfil.setOnClickListener(v -> abrirCamara());

        return view;
    }

    private void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureLauncher.launch(intent);
    }

    private void guardarFotoLocal(Bitmap bitmap) {
        File file = new File(requireContext().getFilesDir(), "foto_perfil.jpg");

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error guardando foto local", Toast.LENGTH_SHORT).show();
        }
    }

    private void subirFotoFirebase(Bitmap bitmap) {

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefs.getString("username", "default");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference().child("perfiles/" + username + "/foto.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(task -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();

                    // Guardar URL en SharedPreferences
                    SharedPreferences prefs2 = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
                    prefs2.edit().putString("url_foto_" + username, url).apply();


                    Toast.makeText(getContext(), "Foto subida a Firebase", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error subiendo foto a Firebase", Toast.LENGTH_SHORT).show()
                );
    }

    private void cargarFotoDesdeFirebase() {
        SharedPreferences prefs = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
        SharedPreferences prefsUser = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String username = prefsUser.getString("username", null);

        SharedPreferences prefsFoto = requireContext().getSharedPreferences("perfil", getContext().MODE_PRIVATE);
        String url = prefsFoto.getString("url_foto_" + username, null);

        if (url != null) {
            new Thread(() -> {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new java.net.URL(url).openStream());
                    requireActivity().runOnUiThread(() -> imgPerfil.setImageBitmap(bitmap));
                } catch (Exception ignored) {}
            }).start();
        }
    }
}
