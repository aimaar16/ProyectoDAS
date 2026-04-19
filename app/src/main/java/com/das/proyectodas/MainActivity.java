package com.das.proyectodas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// Esta es la clase principal donde se carga menu y navegacion
public class MainActivity extends AppCompatActivity {
    private long tiempoClick = 0; // Para saber cuándo se pulsa para salir
    private NavController navController;
    public static final String CHANNEL_ID = "OUTFIT_NOTIFICATIONS"; // ID para las notificaciones
    private static final int PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("username", "");


        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    Log.d("FCM", "Token: " + token);

                    // Enviar token al servidor
                    StringRequest request = new StringRequest(Request.Method.POST,
                            "http://34.175.196.12:81/guardarToken.php",
                            response -> Log.d("FCM", "Token guardado"),
                            error -> Log.e("FCM", "Error guardando token"))
                    {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("usuario", nombreUsuario);
                            params.put("token", token);
                            return params;
                        }
                    };

                    Volley.newRequestQueue(this).add(request);
                });


        // Llamamos a la función para crear el canal de notificaciones
        createNotificationChannel();

        // Pedimos permiso para las notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_CODE);
            }
        }
        // Permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1001);
        }

        // Configuramos la barra de arriba y el menú lateral
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.barra);
        setSupportActionBar(toolbar);

        // Ponemos el botón de las tres rayas para abrir el menú
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                android.R.string.ok,
                android.R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configuramos el sistema de navegación por fragmentos
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setItemIconTintList(null); // Para que los iconos se vean con sus colores
            NavigationUI.setupWithNavController(navigationView, navController);
            navigationView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.fragment_login) {
                    cerrarSesion();
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    drawer.closeDrawers();
                    return true;
                }

                // Para los demás items, dejar que NavigationUI lo gestione
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawers();

                return handled;
            });
        }

        // Controlamos el botón de atrás del móvil
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout elmenudesplegable = findViewById(R.id.drawer_layout);
                // Si el menú está abierto, lo cerramos
                if (elmenudesplegable.isDrawerOpen(GravityCompat.START)) {
                    elmenudesplegable.closeDrawer(GravityCompat.START);
                } else {
                    // Si pulsas dos veces rápido, sales de la app
                    if (tiempoClick + 1500 > System.currentTimeMillis()) {
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Pulsa otra vez para salir", Toast.LENGTH_SHORT).show();
                        tiempoClick = System.currentTimeMillis();
                    }
                }
            }
        });
    }

    // Función para que Android nos deje enviar notificaciones
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de Outfit";
            String description = "Avisos al añadir ropa al outfit diario";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // Cargamos el menú de la barra de arriba (el del idioma)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    // Qué pasa cuando pulsas una opción del menú de arriba
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            mostrarDialogoIdioma();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Ventana para elegir si quieres la app en Español o Inglés
    private void mostrarDialogoIdioma() {
        String[] idiomas = {getString(R.string.lang_es), getString(R.string.lang_en)};
        new AlertDialog.Builder(this)
                .setTitle(R.string.change_language)
                .setItems(idiomas, (dialog, which) -> {
                    if (which == 0) {
                        cambiarIdioma("es");
                    } else {
                        cambiarIdioma("en");
                    }
                })
                .show();
    }

    // Aquí cambiamos el idioma y reiniciamos la pantalla para que cambien los textos
    private void cambiarIdioma(String codigoIdioma) {
        Locale locale = new Locale(codigoIdioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate(); // Recargamos la actividad
    }

    private void cerrarSesion() {
        getSharedPreferences("usuario", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("perfil", MODE_PRIVATE).edit().clear().apply();

        navController.navigate(R.id.fragment_login);
    }

}
