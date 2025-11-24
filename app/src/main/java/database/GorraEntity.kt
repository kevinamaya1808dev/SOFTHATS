package com.example.softhats.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gorras")
data class GorraEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val urlImagen: String
)