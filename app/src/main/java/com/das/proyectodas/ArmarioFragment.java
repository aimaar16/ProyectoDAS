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

import com.das.proyectodas.db.Ropa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ArmarioFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_armario, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recyclerArmario);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Cargar datos desde el JSON en assets
        List<Ropa> misPrendas = cargarRopaDesdeJSON();

        RopaAdapter adapter = new RopaAdapter(misPrendas);
        recyclerView.setAdapter(adapter);

        return root;
    }

    private List<Ropa> cargarRopaDesdeJSON() {
        String json = null;
        try {
            // Abrir el archivo desde la carpeta assets
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

        // Convertir el String JSON a una Lista de objetos Ropa usando GSON
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Ropa>>() {}.getType();
        return gson.fromJson(json, listType);
    }
}