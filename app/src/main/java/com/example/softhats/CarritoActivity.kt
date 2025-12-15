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

<<<<<<< HEAD
=======
        // 4. Botón Pagar (Simulado)
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
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
<<<<<<< HEAD
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun procesarPedidoCompleto() {
        lifecycleScope.launch(Dispatchers.IO) {

            // 1. Obtener productos (Una sola vez para generar el ticket)
            val carrito = database.carritoDao().obtenerCarrito().first()

            if (carrito.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CarritoActivity, "Agrega productos antes de pagar", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 2. Preparar datos
            val fechaVisual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val formatoNombre = SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss", Locale.getDefault())
            val nombreArchivo = "Ticket_${formatoNombre.format(Date())}.txt"

            // 3. Generar Archivo Ticket
            val sbTicket = StringBuilder()
            sbTicket.appendLine("----- TICKET HATSGO -----")
            sbTicket.appendLine("Fecha: $fechaVisual")
            sbTicket.appendLine("-------------------------")
            var totalTicket = 0.0
            for (item in carrito) {
                sbTicket.appendLine("${item.cantidad}x ${item.nombre} - $${item.total}")
                totalTicket += item.total
            }
            sbTicket.appendLine("-------------------------")
            sbTicket.appendLine("TOTAL: $$totalTicket")

            try {
                val outputStream = openFileOutput(nombreArchivo, Context.MODE_PRIVATE)
                val writer = OutputStreamWriter(outputStream)
                writer.write(sbTicket.toString())
                writer.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 4. Generar Mensaje WhatsApp
            val sbWhatsApp = StringBuilder()
            sbWhatsApp.append("*NUEVO PEDIDO HATSGO* 🧢\n")
            sbWhatsApp.append("📅 Fecha: $fechaVisual\n")
            sbWhatsApp.append("----------------------------\n")
            for (item in carrito) {
                sbWhatsApp.append("▪️ ${item.cantidad}x *${item.nombre}*\n")
                sbWhatsApp.append("   Subtotal: $${item.total}\n")
            }
            sbWhatsApp.append("----------------------------\n")
            sbWhatsApp.append("💰 *TOTAL A PAGAR: $${totalTicket}*\n")
            sbWhatsApp.append("----------------------------\n")
            sbWhatsApp.append("EN BREVE UN VENDEDOR SE CONTACTARA CONTIGO .")

            // 5. ¡AQUÍ ESTÁ EL CAMBIO! -> VACIAR CARRITO
            // Borramos todo de la base de datos ANTES de ir a WhatsApp
            database.carritoDao().vaciarCarrito() // <--- Asegúrate que esta función exista en tu DAO

            // 6. Volver al hilo principal para abrir WhatsApp
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CarritoActivity, "Pedido procesado. Ticket guardado.", Toast.LENGTH_SHORT).show()
                abrirWhatsApp(sbWhatsApp.toString())

                // Opcional: Cerrar actividad para volver al Home vacio
                finish()
            }
        }
    }

    private fun abrirWhatsApp(mensaje: String) {
        val numeroVendedor = "525645119567" // Tu número
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$numeroVendedor&text=${Uri.encode(mensaje)}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
=======
            Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
        }
    }
}