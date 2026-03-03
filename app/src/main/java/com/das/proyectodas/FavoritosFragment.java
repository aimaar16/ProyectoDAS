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


        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        cargarFavoritos();

        return root;
    }
    @Override
    public void onResume() {
        super.onResume();
        cargarFavoritos(); //Refresca la lista cada vez que la pantalla vuelve a ser visible
    }
    private void cargarFavoritos() {
        // Ejecutamos en un hilo secundario para Room
        new Thread(() -> {
            AppDatabase db = AppDatabase.getDatabase(getContext());
            // Usamos el metodo que definimos en el DAO para filtrar
            List<Ropa> listaFavoritos = db.ropaDao().obtenerFavoritos();

            // Volvemos al hilo principal para actualizar la UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (listaFavoritos.isEmpty()) {
                        txtVacio.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
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