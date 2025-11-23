package com.example.softhats

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityDetalleGorraBinding
import java.util.Locale

class DetalleGorraActivity : AppCompatActivity() {

    // Declaración del ViewBinding
    private lateinit var binding: ActivityDetalleGorraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización del ViewBinding
        binding = ActivityDetalleGorraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Obtener los datos pasados por el Intent
        val nombre = intent.getStringExtra("EXTRA_NOMBRE")
        val precio = intent.getDoubleExtra("EXTRA_PRECIO", 0.0) // Usamos 0.0 como valor por defecto
        val descripcion = intent.getStringExtra("EXTRA_DESCRIPCION")
        val imagenNombre = intent.getStringExtra("EXTRA_IMAGEN")

        // 2. Llamar a la función para mostrar la información
        displayGorraDetails(nombre, precio, descripcion, imagenNombre)
    }

    private fun displayGorraDetails(nombre: String?, precio: Double, descripcion: String?, imagenNombre: String?) {

        // Usamos el operador Elvis (?:) para asegurar que no se caiga si el dato es nulo

        // 🔹 Mostrar Nombre y Descripción
        binding.tvNombreDetalle.text = nombre ?: "Gorra Desconocida"
        binding.tvDescripcion.text = descripcion ?: "No hay descripción disponible."

        // 🔹 Mostrar Precio con formato (ej: $1749.00)
        binding.tvPrecioDetalle.text = "$ ${String.format(Locale.getDefault(), "%,.2f", precio)}"

        // 🔹 Lógica para la Imagen (Igual que en el Adapter)
        if (!imagenNombre.isNullOrEmpty()) {
            val resourceId = resources.getIdentifier(
                imagenNombre, "drawable", packageName // Busca el nombre en la carpeta drawable
            )

            if (resourceId != 0) {
                binding.ivFotoDetalle.setImageResource(resourceId)
            } else {
                Log.e("DetalleGorra", "Error: Recurso drawable '$imagenNombre' no encontrado.")
            }
        }
    }
}