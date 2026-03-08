package com.das.proyectodas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.das.proyectodas.db.AppDatabase;
import com.das.proyectodas.db.Ropa;

import java.util.ArrayList;
import java.util.List;

public class OutfitFragment extends Fragment {

    private RecyclerView recyclerView;
    private Spinner spinnerDia;
    private RopaAdapter adapter;
    private List<Ropa> listaOutfit = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_outfit, container, false);

        recyclerView = root.findViewById(R.id.recyclerOutfit);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        spinnerDia = root.findViewById(R.id.spinnerDiaOutfit);
        
        // Configurar el Spinner con los días de la semana
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.dias_semana, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDia.setAdapter(spinnerAdapter);

        spinnerDia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String dia = parent.getItemAtPosition(position).toString();
                cargarRopaDelDia(dia);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return root;
    }

    private void cargarRopaDelDia(String dia) {
        new Thread(() -> {
            List<Ropa> ropaDia = AppDatabase.getDatabase(getContext()).ropaDao().obtenerRopaPorDia(dia);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter = new RopaAdapter(ropaDia);
                    recyclerView.setAdapter(adapter);
                });
            }
        }).start();
    }
}