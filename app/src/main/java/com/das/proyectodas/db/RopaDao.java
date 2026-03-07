package com.das.proyectodas.db;

import androidx.room.Dao;
import androidx.room.Delete;
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


    @Query("SELECT * FROM tabla_ropa WHERE esFavorito = 1")
    List<Ropa> obtenerFavoritos();

    @Update
    void actualizarPrenda(Ropa ropa);

    @Query("DELETE FROM tabla_ropa")
    void eliminarTodaLaRopa();

    @Delete
    void eliminarPrenda(Ropa ropa);
}