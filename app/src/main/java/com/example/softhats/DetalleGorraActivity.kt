package com.example.softhats

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ActivityDetalleGorraBinding
import kotlinx.coroutines.launch
import java.util.Locale

class DetalleGorraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleGorraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetalleGorraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Obtener datos del Intent
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: "Gorra Sin Nombre"
        val precio = intent.getDoubleExtra("EXTRA_PRECIO", 0.0)
        val descripcion = intent.getStringExtra("EXTRA_DESCRIPCION")
        val imagenNombre = intent.getStringExtra("EXTRA_IMAGEN")

        // 2. Mostrar la información
        displayGorraDetails(nombre, precio, descripcion, imagenNombre)

        // 3. Inicializar BD
        val db = AppDatabase.getDatabase(this)

        // CORRECCIÓN AQUÍ: Usamos el ID exacto que tienes en el XML (btnAddCarrito)
        binding.btnAddCarrito.setOnClickListener {
            agregarAlCarrito(db, nombre, precio)
        }
    }

    private fun agregarAlCarrito(db: AppDatabase, nombre: String, precio: Double) {
        val itemCarrito = CarritoEntity(
            idProducto = nombre.hashCode(),
            nombre = nombre,
            precioUnitario = precio,
            cantidad = 1,
            total = precio
        )

        lifecycleScope.launch {
            db.carritoDao().insertarOActualizar(itemCarrito)
            // Feedback visual (Toast)
            Toast.makeText(this@DetalleGorraActivity, "¡Añadido al carrito!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayGorraDetails(nombre: String?, precio: Double, descripcion: String?, imagenNombre: String?) {
        binding.tvNombreDetalle.text = nombre ?: "Gorra Desconocida"
        binding.tvDescripcion.text = descripcion ?: "No hay descripción disponible."
        binding.tvPrecioDetalle.text = "$ ${String.format(Locale.getDefault(), "%,.2f", precio)}"

        if (!imagenNombre.isNullOrEmpty()) {
            val resourceId = resources.getIdentifier(imagenNombre, "drawable", packageName)
            if (resourceId != 0) {
                binding.ivFotoDetalle.setImageResource(resourceId)
            }
        }
    }
}