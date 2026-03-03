package com.das.proyectodas.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RopaDao {

    @Insert
    void insertarPrenda(Ropa ropa);

    @Query("SELECT * FROM tabla_ropa")
    List<Ropa> obtenerTodaLaRopa();

    // Esta es la consulta clave para tu FavoritosFragment
    @Query("SELECT * FROM tabla_ropa WHERE esFavorito = 1")
    List<Ropa> obtenerFavoritos();

    @Update
    void actualizarPrenda(Ropa ropa);
}