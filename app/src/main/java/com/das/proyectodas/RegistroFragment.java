package com.das.proyectodas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegistroFragment extends Fragment {

    private EditText etUserReg, etEmailReg, etPassReg;
    private Button btnRegister;
    private TextView tvBackToLogin;

    // IP de mi instancia de Google Cloud
    private static final String URL_REGISTRO = "http://34.175.196.12:81/registro.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        etUserReg = view.findViewById(R.id.etUserReg);
        etEmailReg = view.findViewById(R.id.etEmailReg);
        etPassReg = view.findViewById(R.id.etPassReg);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        btnRegister.setOnClickListener(v -> registrarUsuario());

        tvBackToLogin.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_registro_to_login)
        );

        return view;
    }

    private void registrarUsuario() {
        String usuario = etUserReg.getText().toString().trim();
        String email = etEmailReg.getText().toString().trim();
        String password = etPassReg.getText().toString().trim();

        if (usuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTRO,
                response -> {
                    // El servidor debe devolver "SUCCESS" si el registro es correcto
                    if (response.trim().equalsIgnoreCase("SUCCESS")) {
                        Toast.makeText(getContext(), "Registro completado con éxito", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_registro_to_login);
                    } else {
                        Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", usuario);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }
}