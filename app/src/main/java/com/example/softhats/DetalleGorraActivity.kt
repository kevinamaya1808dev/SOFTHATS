package com.example.softhats

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.database.FavoritoEntity
import com.example.softhats.databinding.ActivityDetalleGorraBinding
import kotlinx.coroutines.launch
import java.util.Locale

class DetalleGorraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleGorraBinding
    private lateinit var db: AppDatabase
    private var cantidadSeleccionada = 1
    private var esFavorito = false // Variable para saber si ya le dio like

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

        // 1. Checar estado inicial: ¿Esta gorra ya es favorita?
        verificarFavorito(nombre.hashCode())

        // 2. Configurar clic en el botón flotante (Corazón/Estrella)
        binding.fabFavorito.setOnClickListener {
            toggleFavorito(nombre, precio, descripcion, imagenNombre)
        }

        // --- LÓGICA CARRITO (Suma, Resta y Añadir) ---

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

        binding.btnAddCarrito.setOnClickListener {
            agregarAlCarrito(nombre, precio, cantidadSeleccionada)
        }
    }

    // Función para consultar a la BD si ya existe esta gorra en favoritos
    private fun verificarFavorito(id: Int) {
        lifecycleScope.launch {
            esFavorito = db.favoritoDao().esFavorito(id)
            actualizarIconoFavorito()
        }
    }

    // Función para Guardar o Borrar de favoritos
    private fun toggleFavorito(nombre: String, precio: Double, desc: String, img: String) {
        lifecycleScope.launch {
            val id = nombre.hashCode() // Usamos el hash del nombre como ID único

            if (esFavorito) {
                // Si ya era favorito, lo borramos (Des-likear)
                db.favoritoDao().eliminarFavorito(id)
                esFavorito = false
                Toast.makeText(this@DetalleGorraActivity, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
            } else {
                // Si no era favorito, lo guardamos (Likear)
                val fav = FavoritoEntity(id, nombre, precio, desc, img)
                db.favoritoDao().agregarFavorito(fav)
                esFavorito = true
                Toast.makeText(this@DetalleGorraActivity, "¡Añadido a favoritos!", Toast.LENGTH_SHORT).show()
            }
            // Actualizamos el dibujo de la estrella
            actualizarIconoFavorito()
        }
    }

    // Función visual para pintar la estrella llena o vacía
    private fun actualizarIconoFavorito() {
        if (esFavorito) {
            binding.fabFavorito.setImageResource(android.R.drawable.btn_star_big_on) // Estrella amarilla
        } else {
            binding.fabFavorito.setImageResource(android.R.drawable.btn_star_big_off) // Estrella gris
        }
    }

    // --- Funciones de Carrito y Visualización ---
    private fun agregarAlCarrito(nombre: String, precio: Double, cantidad: Int) {
        val itemCarrito = CarritoEntity(
            idProducto = nombre.hashCode(),
            nombre = nombre,
            precioUnitario = precio,
            cantidad = cantidad,
            total = precio * cantidad
        )
        lifecycleScope.launch {
            db.carritoDao().insertarOActualizar(itemCarrito)
            Toast.makeText(this@DetalleGorraActivity, "Agregado al carrito", Toast.LENGTH_SHORT).show()
            finish()
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