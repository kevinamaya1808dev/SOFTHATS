package com.example.softhats

// Este es tu "molde" para los datos de Firestore
data class Gorra(
    val nombre: String? = null,
    val precio: Double? = null,
    val descripcion: String? = null,
    val imagen_nombre: String? = null
)