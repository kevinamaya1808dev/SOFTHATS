package com.example.softhats

import android.graphics.Color // IMPORTANTE: Agregado para manejar los colores
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.database.FavoritoEntity
import com.example.softhats.databinding.ActivityDetalleGorraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class DetalleGorraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleGorraBinding
    private lateinit var db: AppDatabase
    private var cantidadSeleccionada = 1
    private var esFavorito = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleGorraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recibimos los datos de la gorra seleccionada
        val nombre = intent.getStringExtra("EXTRA_NOMBRE") ?: "Gorra"
        val precio = intent.getDoubleExtra("EXTRA_PRECIO", 0.0)
        val descripcion = intent.getStringExtra("EXTRA_DESCRIPCION") ?: ""
        val imagenNombre = intent.getStringExtra("EXTRA_IMAGEN") ?: ""

        displayGorraDetails(nombre, precio, descripcion, imagenNombre)

        // Inicializamos la Base de Datos
        db = AppDatabase.getDatabase(this)

        // --- LÓGICA FAVORITOS ---
        verificarFavorito(nombre.hashCode())

        binding.fabFavorito.setOnClickListener {
            toggleFavorito(nombre, precio, descripcion, imagenNombre)
        }

        // --- LÓGICA CARRITO ---

        binding.btnSumarDetalle.setOnClickListener {
            cantidadSeleccionada++
            binding.tvCantidadDetalle.text = cantidadSeleccionada.toString()
        }

        binding.btnRestarDetalle.setOnClickListener {
            if (cantidadSeleccionada > 1) {
                cantidadSeleccionada--
                binding.tvCantidadDetalle.text = cantidadSeleccionada.toString()
            }
        }

        // Botón agregar: Llama a la función que suma cantidades
        binding.btnAddCarrito.setOnClickListener {
            agregarAlCarrito(nombre, precio, cantidadSeleccionada, imagenNombre)
        }
    }

    private fun verificarFavorito(id: Int) {
        lifecycleScope.launch {
            esFavorito = db.favoritoDao().esFavorito(id)
            actualizarIconoFavorito()
        }
    }

    private fun toggleFavorito(nombre: String, precio: Double, desc: String, img: String) {
        lifecycleScope.launch {
            val id = nombre.hashCode()
            if (esFavorito) {
                db.favoritoDao().eliminarFavorito(id)
                esFavorito = false
                Toast.makeText(this@DetalleGorraActivity, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                val fav = FavoritoEntity(id, nombre, precio, desc, img)
                db.favoritoDao().agregarFavorito(fav)
                esFavorito = true
                Toast.makeText(this@DetalleGorraActivity, "¡Añadido a favoritos!", Toast.LENGTH_SHORT).show()
            }
            actualizarIconoFavorito()
        }
    }

    // 🟡 FUNCIÓN MODIFICADA: Controla el color EXACTO de la estrella
    private fun actualizarIconoFavorito() {
        if (esFavorito) {
            // Caso SI es favorito: Icono lleno + Color AMARILLO DORADO
            binding.fabFavorito.setImageResource(android.R.drawable.btn_star_big_on)
            binding.fabFavorito.setColorFilter(Color.parseColor("#FFC107"))
        } else {
            // Caso NO es favorito: Icono vacío + Color GRIS
            binding.fabFavorito.setImageResource(android.R.drawable.btn_star_big_off)
            binding.fabFavorito.setColorFilter(Color.parseColor("#9E9E9E"))
        }
    }

    // 🟢 FUNCIÓN MEJORADA: Suma cantidades si ya existe
    private fun agregarAlCarrito(nombre: String, precio: Double, cantidadNueva: Int, imagen: String) {
        val id = nombre.hashCode()

        lifecycleScope.launch(Dispatchers.IO) {
            // 1. Preguntar: ¿Ya existe esta gorra en el carrito?
            val productoExistente = db.carritoDao().obtenerProducto(id)

            val cantidadFinal = if (productoExistente != null) {
                // Si existe, sumamos lo que había + lo nuevo
                productoExistente.cantidad + cantidadNueva
            } else {
                // Si no existe, usamos solo lo nuevo
                cantidadNueva
            }

            // 2. Creamos el objeto con la cantidad actualizada
            val itemCarrito = CarritoEntity(
                idProducto = id,
                nombre = nombre,
                precioUnitario = precio,
                cantidad = cantidadFinal,
                total = precio * cantidadFinal, // Recalculamos el total
                imagen = imagen // Incluimos la imagen
            )

            // 3. Guardamos
            db.carritoDao().insertarOActualizar(itemCarrito)

            // 4. Avisamos en el hilo principal
            withContext(Dispatchers.Main) {
                val mensaje = if (productoExistente != null) {
                    "Se actualizó la cantidad a $cantidadFinal"
                } else {
                    "Agregado al carrito"
                }
                Toast.makeText(this@DetalleGorraActivity, mensaje, Toast.LENGTH_SHORT).show()
                finish() // Cierra la ventana y vuelve al catálogo
            }
        }
    }

    private fun displayGorraDetails(nombre: String, precio: Double, descripcion: String, imagenNombre: String) {
        binding.tvNombreDetalle.text = nombre
        binding.tvDescripcion.text = descripcion
        binding.tvPrecioDetalle.text = "$ ${String.format(Locale.getDefault(), "%,.2f", precio)}"

        if (imagenNombre.isNotEmpty()) {
            val resourceId = resources.getIdentifier(imagenNombre, "drawable", packageName)
            if (resourceId != 0) binding.ivFotoDetalle.setImageResource(resourceId)
        }
    }
}