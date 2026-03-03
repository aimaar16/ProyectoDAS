package com.das.proyectodas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ArmarioFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerArmario);

        // Usamos GridLayoutManager para ver 2 prendas por fila
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Datos de prueba (aquí irían tus fotos reales)
        List<Ropa> misPrendas = new ArrayList<>();
        misPrendas.add(new Ropa("Chaqueta Cuero", "Abrigo", R.drawable.ic_launcher_background));
        misPrendas.add(new Ropa("Pantalón Vaquero", "Pantalón", R.drawable.ic_launcher_background));
        misPrendas.add(new Ropa("Sudadera Azul", "Abrigo", R.drawable.ic_launcher_background));

        RopaAdapter adapter = new RopaAdapter(misPrendas);
        recyclerView.setAdapter(adapter);

        return root;
    }
}