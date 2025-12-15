package com.example.softhats.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    // Obtener todo el carrito (Usamos Flow para que se actualice solo si cambia algo)
    @Query("SELECT * FROM carrito")
    fun obtenerCarrito(): Flow<List<CarritoEntity>>

    // Agregar o actualizar un producto (Si ya existe, lo reemplaza con los nuevos datos)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(item: CarritoEntity)

    // 🟢 AGREGA ESTA NUEVA FUNCIÓN:
    @Query("SELECT * FROM carrito WHERE idProducto = :id LIMIT 1")
    suspend fun obtenerProducto(id: Int): CarritoEntity?

    // Eliminar un producto específico
    @Query("DELETE FROM carrito WHERE idProducto = :idProducto")
    suspend fun eliminarProducto(idProducto: Int)

    // Vaciar todo el carrito (ej. cuando ya compró)
    @Query("DELETE FROM carrito")
    suspend fun vaciarCarrito()
}