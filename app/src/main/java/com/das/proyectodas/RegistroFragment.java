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

// Fragmento encargado del registro de nuevos usuarios en el sistema
public class RegistroFragment extends Fragment {

    private EditText etUserReg, etEmailReg, etPassReg;
    private Button btnRegister;
    private TextView tvBackToLogin;

    // URL del script de registro alojado en el servidor remoto
    private static final String URL_REGISTRO = "http://34.175.196.12:81/registro.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        // Inicializacion de las vistas del formulario
        etUserReg = view.findViewById(R.id.etUserReg);
        etEmailReg = view.findViewById(R.id.etEmailReg);
        etPassReg = view.findViewById(R.id.etPassReg);
        btnRegister = view.findViewById(R.id.btnRegister);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        // Listener para procesar el alta del usuario
        btnRegister.setOnClickListener(v -> registrarUsuario());

        // Permite volver a la pantalla de login si ya se tiene cuenta
        tvBackToLogin.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_registro_to_login)
        );

        return view;
    }

    // Gestiona el envío de datos de registro al servidor mediante una peticion POST
    private void registrarUsuario() {
        String usuario = etUserReg.getText().toString().trim();
        String email = etEmailReg.getText().toString().trim();
        String password = etPassReg.getText().toString().trim();

        // Comprobacion de seguridad basica para evitar campos vacios
        if (usuario.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configuracion de la peticion de red con Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGISTRO,
                response -> {
                    // Si el servidor confirma el registro, volvemos al login
                    if (response.trim().equalsIgnoreCase("SUCCESS")) {
                        Toast.makeText(getContext(), "Registro completado con éxito", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_registro_to_login);
                    } else {
                        // Mostramos el error devuelto por el servidor (p. ej. usuario duplicado)
                        Toast.makeText(getContext(), "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de red: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            
            // Definicion de los parametros que se envian en el cuerpo del POST
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", usuario);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        // Añadimos la peticion a la cola de ejecucion
        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }
}
