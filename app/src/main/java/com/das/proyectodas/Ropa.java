package com.das.proyectodas;
public class Ropa {
    private String nombre;
    private String categoria;
    private int imagenResId;

    public Ropa(String nombre, String categoria, int imagenResId) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.imagenResId = imagenResId;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public int getImagenResId() { return imagenResId; }
}