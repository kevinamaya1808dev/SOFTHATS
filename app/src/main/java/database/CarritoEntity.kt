package com.example.softhats.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carrito")
data class CarritoEntity(
    @PrimaryKey val idProducto: Int, // Usamos el ID del producto como clave para que no se repita el mismo producto dos veces
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val total: Double // Esto será precioUnitario * cantidad
)