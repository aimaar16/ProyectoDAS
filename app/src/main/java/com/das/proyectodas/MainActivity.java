package com.das.proyectodas;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {
    private long tiempoClick = 0;
    private NavController navController;
    public static final String CHANNEL_ID = "OUTFIT_NOTIFICATIONS";
    private static final int PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("usuario", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("username", "");

        // 1. Configuración de navegación inicial
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // SI YA ESTÁ LOGUEADO, SALTAMOS EL LOGIN
            if (!nombreUsuario.isEmpty()) {
                navController.navigate(R.id.fragment_inicio);
                
                // SI VENIMOS DEL WIDGET, VAMOS A FAVORITOS
                if (getIntent().getBooleanExtra("desde_widget", false)) {
                    navController.navigate(R.id.fragment_favoritos);
                }
            }
        }

        // Resto de inicializaciones (FCM, Notificaciones, UI...)
        initUI();
        initFCM(nombreUsuario);
    }

    private void initUI() {
        createNotificationChannel();
        pedirPermisos();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.barra);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, android.R.string.ok, android.R.string.cancel);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.fragment_login) {
                cerrarSesion();
            } else {
                NavigationUI.onNavDestinationSelected(item, navController);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (tiempoClick + 1500 > System.currentTimeMillis()) {
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Pulsa otra vez para salir", Toast.LENGTH_SHORT).show();
                    tiempoClick = System.currentTimeMillis();
                }
            }
        });
    }

    private void initFCM(String nombreUsuario) {
        if (nombreUsuario.isEmpty()) return;
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            StringRequest request = new StringRequest(Request.Method.POST, "http://34.175.196.12:81/guardarToken.php",
                    r -> Log.d("FCM", "Token guardado"), e -> Log.e("FCM", "Error token")) {
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
    }

    private void pedirPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_CODE);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Outfit Notify", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_language) {
            mostrarDialogoIdioma();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void mostrarDialogoIdioma() {
        String[] idiomas = {getString(R.string.lang_es), getString(R.string.lang_en)};
        new AlertDialog.Builder(this).setTitle(R.string.change_language).setItems(idiomas, (d, w) -> cambiarIdioma(w == 0 ? "es" : "en")).show();
    }

    private void cambiarIdioma(String codigoIdioma) {
        Locale locale = new Locale(codigoIdioma);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        recreate();
    }

    private void cerrarSesion() {
        getSharedPreferences("usuario", MODE_PRIVATE).edit().clear().apply();
        getSharedPreferences("perfil", MODE_PRIVATE).edit().clear().apply();
        navController.navigate(R.id.fragment_login);
    }
}
