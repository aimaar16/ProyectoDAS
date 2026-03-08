package com.das.proyectodas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Esta es la pantalla de inicio, la que sale al abrir la app
public class InicioFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el diseño del fragmento de inicio
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }
}