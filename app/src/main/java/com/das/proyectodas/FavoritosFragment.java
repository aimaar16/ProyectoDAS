package com.das.proyectodas;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.util.List;

// Esta pantalla es para ver solo la ropa que hemos marcado con la estrella (favoritos)
public class FavoritosFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtVacio;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_favoritos, container, false);

        recyclerView = root.findViewById(R.id.recyclerFavoritos);
        txtVacio = root.findViewById(R.id.txtVacio);

        // Ponemos la lista en dos columnas igual que en el armario
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Cargamos la ropa favorita
        cargarFavoritos();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refrescamos la lista cada vez que entramos por si hemos quitado algún favorito
        cargarFavoritos();
    }

    // Aquí buscamos en la base de datos solo lo que tiene la estrella puesta
    private void cargarFavoritos() {
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            List<Ropa> listaFavoritos = db.ropaDao().obtenerFavoritos();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Si no hay nada favorito, enseñamos un texto avisando
                    if (listaFavoritos.isEmpty()) {
                        txtVacio.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        // Si hay cosas, las mostramos en la lista
                        txtVacio.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        RopaAdapter adapter = new RopaAdapter(listaFavoritos);
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        }).start();
    }
}