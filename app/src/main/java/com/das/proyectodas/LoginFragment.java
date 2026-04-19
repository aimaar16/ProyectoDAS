package com.das.proyectodas;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

// Fragmento encargado de la autenticacion del usuario
public class LoginFragment extends Fragment {

    private EditText Usuario, Contraseña;
    private Button btnLogin;
    private TextView tvGoToRegister;

    // Ruta del script PHP en el servidor remoto
    private static final String URL_LOGIN = "http://34.175.196.12:81/login.php";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Usuario = view.findViewById(R.id.etUser);
        Contraseña = view.findViewById(R.id.etPass);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvGoToRegister = view.findViewById(R.id.tvGoToRegister);

        // Click para intentar loguearse
        btnLogin.setOnClickListener(v -> loginUsuario());

        // Click para ir a la pantalla de registro
        tvGoToRegister.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_registro)
        );

        return view;
    }

    // Gestiona el proceso de login comprobando internet y credenciales
    private void loginUsuario() {

        String usuario = Usuario.getText().toString().trim();
        String password = Contraseña.getText().toString().trim();

        // Validacion simple de campos vacios
        if (usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences("usuario", getContext().MODE_PRIVATE);
        String usuarioGuardado = prefs.getString("username", null);

        // Si no hay internet, permitimos acceso si el usuario ya habia entrado antes
        if (!hayInternet(getContext())) {
            if (usuarioGuardado != null && usuarioGuardado.equals(usuario)) {
                Toast.makeText(getContext(), "Entrando en modo offline", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_login_to_inicio);
                return;
            } else {
                Toast.makeText(getContext(), "Sin conexión. No se puede iniciar sesión.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Peticion al servidor para validar credenciales
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                response -> {
                    if (response.trim().equalsIgnoreCase("OK")) {
                        // Guardamos el usuario localmente para el modo offline
                        prefs.edit().putString("username", usuario).apply();
                        Toast.makeText(getContext(), "Bienvenido " + usuario, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigate(R.id.action_login_to_inicio);
                    } else {
                        Toast.makeText(getContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Error de conexión: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", usuario);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(requireContext());
        requestQueue.add(stringRequest);
    }

    // Comprueba si el dispositivo tiene conexion activa a internet
    public static boolean hayInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
