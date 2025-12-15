package com.example.softhats.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito")
data class CarritoEntity(
    @PrimaryKey val idProducto: Int,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val total: Double,
    val imagen: String // <--- ¡AQUÍ ESTÁ LA CLAVE PARA LAS FOTOS!
)