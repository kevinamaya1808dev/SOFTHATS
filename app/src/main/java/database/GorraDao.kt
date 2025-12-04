package com.example.softhats.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GorraDao {

    // 1. Para mostrar la lista cuando NO hay internet
    @Query("SELECT * FROM gorras")
    suspend fun obtenerTodas(): List<GorraEntity>

    // 2. Para la "Sincronización Espejo": Guardar la lista nueva que llegue de internet
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(gorras: List<GorraEntity>)

    // 3. Para borrar lo viejo antes de guardar lo nuevo
    @Query("DELETE FROM gorras")
    suspend fun borrarTodo()
}