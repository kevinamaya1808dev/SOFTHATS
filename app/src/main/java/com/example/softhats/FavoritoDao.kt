package com.example.softhats.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    // Guardar en favoritos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarFavorito(favorito: FavoritoEntity)

    // Quitar de favoritos
    @Query("DELETE FROM favoritos WHERE idProducto = :id")
    suspend fun eliminarFavorito(id: Int)

    // Verificar si ya es favorito (para pintar el corazón rojo o blanco)
    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE idProducto = :id)")
    suspend fun esFavorito(id: Int): Boolean

    // Obtener la lista completa (Para la pantalla de Favoritos)
    @Query("SELECT * FROM favoritos")
    fun obtenerTodos(): Flow<List<FavoritoEntity>>
}