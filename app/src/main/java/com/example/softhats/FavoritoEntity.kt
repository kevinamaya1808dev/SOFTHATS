package com.example.softhats.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val idProducto: Int, // Usaremos el mismo ID (hashcode del nombre)
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val imagenNombre: String
)