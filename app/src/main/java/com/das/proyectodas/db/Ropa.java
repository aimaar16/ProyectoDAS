package com.das.proyectodas.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tabla_ropa")
public class Ropa {

    @PrimaryKey(autoGenerate = true)
    private int id; // Room lo usará para identificar cada prenda de forma única

    private String nombre;
    private String categoria;
    private int imagenResId;
    private boolean esFavorito; // <--- Aquí se guarda si es favorita o no

    // Constructor
    public Ropa(String nombre, String categoria, int imagenResId, boolean esFavorito) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.imagenResId = imagenResId;
        this.esFavorito = esFavorito;
    }

    // Getters y Setters necesarios para Room
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getImagenResId() { return imagenResId; }
    public void setImagenResId(int imagenResId) { this.imagenResId = imagenResId; }

    public boolean isEsFavorito() { return esFavorito; }
    public void setEsFavorito(boolean esFavorito) { this.esFavorito = esFavorito; }
}