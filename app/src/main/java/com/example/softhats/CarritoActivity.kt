package com.example.softhats

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ActivityCarritoBinding
import kotlinx.coroutines.launch
import java.util.Locale

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Inicializar Base de Datos
        database = AppDatabase.getDatabase(this)

        // 2. Configurar el RecyclerView (Lista)
        setupRecyclerView()

        // 3. Observar los datos del carrito en tiempo real
        observarCarrito()

        // 4. Botón Pagar (Simulado)
        binding.btnPagar.setOnClickListener {
            Toast.makeText(this, "Procesando compra... ¡Gracias!", Toast.LENGTH_LONG).show()
            // Aquí podrías borrar el carrito después de pagar
            lifecycleScope.launch { database.carritoDao().vaciarCarrito() }
        }
    }

    private fun setupRecyclerView() {
        // Inicializamos el adaptador con las acciones para cada botón
        adapter = CarritoAdapter(
            onSumarClick = { item -> actualizarCantidad(item, 1) },
            onRestarClick = { item -> actualizarCantidad(item, -1) },
            onEliminarClick = { item -> eliminarItem(item) }
        )

        binding.rvCarrito.layoutManager = LinearLayoutManager(this)
        binding.rvCarrito.adapter = adapter
    }

    private fun observarCarrito() {
        // Usamos Flow para recibir actualizaciones automáticas si algo cambia en la BD
        lifecycleScope.launch {
            database.carritoDao().obtenerCarrito().collect { items ->
                // Actualizamos la lista visual
                adapter.submitList(items)

                // Calculamos y mostramos el total
                actualizarTotal(items)

                // Mostrar u ocultar mensaje de "Vacío"
                binding.tvVacio.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCarrito.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun actualizarTotal(items: List<CarritoEntity>) {
        var granTotal = 0.0
        for (item in items) {
            granTotal += item.total
        }
        binding.tvGranTotal.text = "$ ${String.format(Locale.getDefault(), "%,.2f", granTotal)}"
    }

    // --- LÓGICA DE LOS BOTONES ---

    private fun actualizarCantidad(item: CarritoEntity, cambio: Int) {
        val nuevaCantidad = item.cantidad + cambio

        if (nuevaCantidad > 0) {
            // Actualizamos cantidad y recalcula el subtotal
            val itemActualizado = item.copy(
                cantidad = nuevaCantidad,
                total = item.precioUnitario * nuevaCantidad
            )
            lifecycleScope.launch {
                database.carritoDao().insertarOActualizar(itemActualizado)
            }
        } else {
            // Si la cantidad llega a 0, preguntamos o eliminamos directo (aquí eliminamos directo)
            eliminarItem(item)
        }
    }

    private fun eliminarItem(item: CarritoEntity) {
        lifecycleScope.launch {
            database.carritoDao().eliminarProducto(item.idProducto)
            Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
        }
    }
}