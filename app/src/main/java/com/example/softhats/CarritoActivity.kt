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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*


class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: CarritoAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout con ViewBinding
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar la base de datos local Room
        database = AppDatabase.getDatabase(this)

        // Configurar el RecyclerView del carrito
        setupRecyclerView()

        // Observar los cambios de productos en Room
        observarCarrito()

        // Botón que solo genera el archivo TXT del ticket
        binding.btnGenerarTicket.setOnClickListener {
            generarTicket()
        }

        // Botón que genera ticket + lo envía a Firestore + WhatsApp
        binding.btnPagar.setOnClickListener {
            generarTicketYEnviar()
        }
    }

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
            database.carritoDao().obtenerCarrito().collect { items ->
                adapter.submitList(items)
                actualizarTotal(items)

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
            lifecycleScope.launch {
                database.carritoDao().insertarOActualizar(actualizado)
            }
        } else eliminarItem(item)
    }

    private fun eliminarItem(item: CarritoEntity) {
        lifecycleScope.launch {
            database.carritoDao().eliminarProducto(item.idProducto)
            Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    // ------------------------------------------------------------
// 🟡 SOLO GENERA EL ARCHIVO TXT DEL TICKET (sin enviar)
// ------------------------------------------------------------
    private fun generarTicket() {
        lifecycleScope.launch {

            // 1️⃣ Obtener carrito desde Room
            val carrito = database.carritoDao().obtenerCarrito().first()
            if (carrito.isEmpty()) {
                Toast.makeText(this@CarritoActivity, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // 2️⃣ Obtener fecha para el nombre del archivo
            val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val nombreArchivo = "ticket_compra_${fecha}.txt"

            // 3️⃣ Construir texto del ticket (sin datos del usuario)
            val sb = StringBuilder()
            sb.appendLine("----- TICKET DE COMPRA HATSGO -----")
            sb.appendLine("Fecha: $fecha")
            sb.appendLine("------------------------------------")
            sb.appendLine("PRODUCTOS:")

            var total = 0.0
            carrito.forEach {
                sb.appendLine("${it.cantidad}x ${it.nombre} - $${it.total}")
                total += it.total
            }

            sb.appendLine("------------------------------------")
            sb.appendLine("TOTAL A PAGAR: $$total")
            sb.appendLine("------------------------------------")

            val contenido = sb.toString()

            // 4️⃣ Guardar archivo interno en la app
            guardarArchivoInterno(nombreArchivo, contenido)

            Toast.makeText(
                this@CarritoActivity,
                "Ticket generado: $nombreArchivo",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    // ------------------------------------------------------------
    // 🟢 GENERAR TICKET Y ENVIAR A WHATSAPP
    // ------------------------------------------------------------
    private fun generarTicketYEnviar() {
        lifecycleScope.launch {

            val carrito = database.carritoDao().obtenerCarrito().first()
            if (carrito.isEmpty()) {
                Toast.makeText(this@CarritoActivity, "El carrito está vacío", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val uid = auth.currentUser?.uid
            if (uid == null) {
                Toast.makeText(this@CarritoActivity, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            firestore.collection("usuarios").document(uid).get()
                .addOnSuccessListener { doc ->

                    if (!doc.exists()) {
                        Toast.makeText(this@CarritoActivity, "El documento no existe en Firestore", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val nombre = doc.getString("nombre") ?: "Cliente"
                    val telefono = doc.getString("telefono") ?: "N/A"

                    Log.d("FIRESTORE_DATA", "Nombre Firestore: $nombre")
                    Log.d("FIRESTORE_DATA", "Teléfono Firestore: $telefono")

                    val fecha = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                    val nombreArchivo = "ticket_compra_${fecha}.txt"

                    val textoTicket = construirTicketTexto(fecha, nombre, telefono, carrito)
                    guardarArchivoInterno(nombreArchivo, textoTicket)

                    enviarWhatsApp(textoTicket)
                }
                .addOnFailureListener {
                    Toast.makeText(this@CarritoActivity, "Error al obtener usuario Firestore", Toast.LENGTH_LONG).show()

                }


        }
    }

    private fun construirTicketTexto(
        fecha: String,
        nombre: String,
        telefono: String,
        carrito: List<CarritoEntity>
    ): String {

        val sb = StringBuilder()

        sb.appendLine("----- TICKET DE COMPRA HATSGO -----")
        sb.appendLine("Fecha: $fecha")
        sb.appendLine("Cliente: $nombre")
        sb.appendLine("Teléfono: $telefono")
        sb.appendLine("-----------------------------------")
        sb.appendLine("PRODUCTOS:")

        var total = 0.0

        carrito.forEach {
            sb.appendLine("${it.cantidad}x ${it.nombre} - $${it.total}")
            total += it.total
        }

        sb.appendLine("-----------------------------------")
        sb.appendLine("TOTAL A PAGAR: $$total")
        sb.appendLine("-----------------------------------")
        sb.appendLine("Estado: Pendiente de pago")
        sb.appendLine("-----------------------------------")

        return sb.toString()
    }

    private fun guardarArchivoInterno(nombre: String, contenido: String) {
        try {
            val outputStream = openFileOutput(nombre, MODE_PRIVATE)
            val writer = OutputStreamWriter(outputStream)
            writer.write(contenido)
            writer.close()

            Log.d("TICKET", "Archivo guardado: $filesDir/$nombre")

        } catch (e: Exception) {
            Toast.makeText(this, "Error guardando ticket", Toast.LENGTH_LONG).show()
        }
    }

    private fun enviarWhatsApp(ticket: String) {

        val numero = "525626350435"

        val uri = Uri.parse(
            "https://wa.me/$numero?text=" + Uri.encode(ticket)
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.whatsapp")

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    }
}




