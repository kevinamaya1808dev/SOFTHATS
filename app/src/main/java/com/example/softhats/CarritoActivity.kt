package com.example.softhats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ActivityCarritoBinding
// import com.google.firebase.auth.FirebaseAuth     <-- Ya no son estrictamente necesarios para pagar
// import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: CarritoAdapter

    // Si deseas usar Auth para obtener el nombre, puedes descomentarlo,
    // pero para enviar el pedido rápido no es obligatorio.
    // private val firestore = FirebaseFirestore.getInstance()
    // private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout con ViewBinding
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la base de datos local Room
        database = AppDatabase.getDatabase(this)

        // Configurar el RecyclerView del carrito
        setupRecyclerView()

        // Observar los cambios de productos en Room (Para actualizar la lista en tiempo real)
        observarCarrito()

        // Botón: Solo genera el archivo TXT (Respaldo)
        binding.btnGenerarTicket.setOnClickListener {
            generarTicketLocal()
        }

        // Botón PRINCIPAL: Enviar Pedido por WhatsApp
        binding.btnPagar.setOnClickListener {
            enviarPedidoWhatsApp()
        }
    }

    // ------------------------------------------------------------
    // CONFIGURACIÓN DE LA LISTA (RECYCLERVIEW)
    // ------------------------------------------------------------
    private fun setupRecyclerView() {
        adapter = CarritoAdapter(
            onSumarClick = { actualizarCantidad(it, 1) },
            onRestarClick = { actualizarCantidad(it, -1) },
            onEliminarClick = { eliminarItem(it) }
        )

        binding.rvCarrito.layoutManager = LinearLayoutManager(this)
        binding.rvCarrito.adapter = adapter
    }

    private fun observarCarrito() {
        lifecycleScope.launch {
            // Recolectamos el Flow de la BD
            database.carritoDao().obtenerCarrito().collect { items ->
                adapter.submitList(items)
                actualizarTotal(items)

                // Mostrar mensaje de vacío si no hay items
                binding.tvVacio.visibility =
                    if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun actualizarTotal(items: List<CarritoEntity>) {
        val total = items.sumOf { it.total }
        binding.tvGranTotal.text = "$ ${String.format("%,.2f", total)}"
    }

    private fun actualizarCantidad(item: CarritoEntity, cambio: Int) {
        val nuevaCantidad = item.cantidad + cambio

        if (nuevaCantidad > 0) {
            val actualizado = item.copy(
                cantidad = nuevaCantidad,
                total = nuevaCantidad * item.precioUnitario
            )
            // Operaciones de BD en hilo IO
            lifecycleScope.launch(Dispatchers.IO) {
                database.carritoDao().insertarOActualizar(actualizado)
            }
        } else {
            eliminarItem(item)
        }
    }

    private fun eliminarItem(item: CarritoEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.carritoDao().eliminarProducto(item.idProducto)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ------------------------------------------------------------
    // 🟡 GENERAR SOLO ARCHIVO TXT (SIN ENVIAR)
    // ------------------------------------------------------------
    private fun generarTicketLocal() {
        lifecycleScope.launch(Dispatchers.IO) {
            val carrito = database.carritoDao().obtenerCarrito().first()

            if (carrito.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CarritoActivity, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val nombreArchivo = "ticket_respaldo_${fecha}.txt"

            // Construimos el texto simple
            val sb = StringBuilder()
            sb.appendLine("----- TICKET RESPALDO HATSGO -----")
            sb.appendLine("Fecha: $fecha")
            sb.appendLine("----------------------------------")
            var total = 0.0
            carrito.forEach {
                sb.appendLine("${it.cantidad}x ${it.nombre} - $${it.total}")
                total += it.total
            }
            sb.appendLine("----------------------------------")
            sb.appendLine("TOTAL: $$total")

            guardarArchivoInterno(nombreArchivo, sb.toString())

            withContext(Dispatchers.Main) {
                Toast.makeText(this@CarritoActivity, "Ticket guardado en: $nombreArchivo", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ------------------------------------------------------------
    // 🟢 ENVIAR PEDIDO A WHATSAPP (CORREGIDO: NO DEPENDE DE FIRESTORE)
    // ------------------------------------------------------------
    private fun enviarPedidoWhatsApp() {
        lifecycleScope.launch(Dispatchers.IO) {

            // 1. Obtener datos de Room (Seguro y Rápido)
            val carrito = database.carritoDao().obtenerCarrito().first()

            if (carrito.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CarritoActivity, "Agrega productos antes de pagar", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // 2. Construir el mensaje para WhatsApp
            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val sb = StringBuilder()

            sb.append("*NUEVO PEDIDO HATSGO* 🧢\n")
            sb.append("📅 Fecha: $fecha\n")
            sb.append("----------------------------\n")

            var totalFinal = 0.0

            for (item in carrito) {
                // Formato: 2x Gorra Nike ($1500)
                sb.append("▪️ ${item.cantidad}x *${item.nombre}*\n")
                sb.append("   Subtotal: $${item.total}\n")
                totalFinal += item.total
            }

            sb.append("----------------------------\n")
            sb.append("💰 *TOTAL A PAGAR: $${totalFinal}*\n")
            sb.append("----------------------------\n")
            sb.append("EN BREVE UN VENDEDOR SE CONTACTARA CONTIGO .")

            // 3. Enviar a WhatsApp
            withContext(Dispatchers.Main) {
                abrirWhatsApp(sb.toString())
            }
        }
    }

    private fun abrirWhatsApp(mensaje: String) {
        // ⚠️ REEMPLAZA ESTE NÚMERO POR EL DEL VENDEDOR REAL (con código de país)
        val numeroVendedor = "525645119567"

        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$numeroVendedor&text=${Uri.encode(mensaje)}")
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    // Función auxiliar para guardar en memoria interna (si lo necesitas para historial)
    private fun guardarArchivoInterno(nombre: String, contenido: String) {
        try {
            val outputStream = openFileOutput(nombre, MODE_PRIVATE)
            val writer = OutputStreamWriter(outputStream)
            writer.write(contenido)
            writer.close()
            Log.d("TICKET", "Archivo guardado: $nombre")
        } catch (e: Exception) {
            Log.e("TICKET", "Error guardando archivo: ${e.message}")
        }
    }
}