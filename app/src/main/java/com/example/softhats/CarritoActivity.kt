package com.example.softhats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ActivityCarritoBinding
import com.example.softhats.utils.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CarritoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarritoBinding
    private lateinit var database: AppDatabase
    private lateinit var adapter: CarritoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = AppDatabase.getDatabase(this)

        setupRecyclerView()
        observarCarrito()

        binding.btnPagar.setOnClickListener {
            confirmarEnvio()
        }
    }

    // ================== RECYCLER ==================
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
                    if (items.isEmpty()) View.VISIBLE else View.GONE
                binding.rvCarrito.visibility =
                    if (items.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun actualizarTotal(items: List<CarritoEntity>) {
        val total = items.sumOf { it.total }
        binding.tvGranTotal.text =
            "$ ${String.format(Locale.getDefault(), "%,.2f", total)}"
    }

    private fun actualizarCantidad(item: CarritoEntity, cambio: Int) {
        val nuevaCantidad = item.cantidad + cambio
        if (nuevaCantidad > 0) {
            lifecycleScope.launch(Dispatchers.IO) {
                database.carritoDao().insertarOActualizar(
                    item.copy(
                        cantidad = nuevaCantidad,
                        total = item.precioUnitario * nuevaCantidad
                    )
                )
            }
        } else eliminarItem(item)
    }

    private fun eliminarItem(item: CarritoEntity) {
        lifecycleScope.launch(Dispatchers.IO) {
            database.carritoDao().eliminarProducto(item.idProducto)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CarritoActivity,
                    "${item.nombre} eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ================== CONFIRMAR ENVÍO ==================
    private fun confirmarEnvio() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar pedido")
            .setMessage("¿Deseas enviar el pedido por WhatsApp?")
            .setPositiveButton("Sí") { _, _ -> procesarPedidoCompleto() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ================== PROCESAR PEDIDO ==================
    private fun procesarPedidoCompleto() {
        lifecycleScope.launch(Dispatchers.IO) {

            val carrito = database.carritoDao().obtenerCarrito().first()
            if (carrito.isEmpty()) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CarritoActivity,
                        "Agrega productos antes de pagar",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@launch
            }

            val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
            val compraId = SimpleDateFormat("ddMMyyyy_HHmmss", Locale.getDefault()).format(Date())
            val total = carrito.sumOf { it.total }

            val mensajeWhatsApp = buildString {
                append("*NUEVO PEDIDO SOFTHATS* 🧢\n")
                append("📅 Fecha: $fecha\n")
                append("----------------------------\n")
                carrito.forEach {
                    append("▪ ${it.cantidad}x ${it.nombre}\n")
                    append("  Subtotal: $${it.total}\n")
                }
                append("----------------------------\n")
                append("💰 TOTAL: $${total}\n")
            }

            // 📲 WhatsApp PRIMERO
            withContext(Dispatchers.Main) {
                abrirWhatsApp(mensajeWhatsApp)
            }

            // 📄 Generar PDF
            val pdfFile = PdfUtils.generarTicketPdf(
                this@CarritoActivity,
                compraId,
                carrito.map { it.nombre to it.total },
                total
            )

            database.carritoDao().vaciarCarrito()

            // 📄 Mostrar botón para abrir PDF
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@CarritoActivity)
                    .setTitle("Ticket generado")
                    .setMessage("¿Deseas ver tu ticket en PDF?")
                    .setPositiveButton("Ver ticket") { _, _ ->
                        abrirPdf(pdfFile)
                    }
                    .setNegativeButton("Cerrar") { _, _ ->
                        finish()
                    }
                    .show()
            }
        }
    }

    // ================== ABRIR PDF (FINAL Y SEGURO) ==================
    private fun abrirPdf(pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Abrir ticket PDF"))

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el PDF. Instala un lector de PDF.",
                Toast.LENGTH_LONG
            ).show()
            e.printStackTrace()
        }
    }

    // ================== WHATSAPP ==================
    private fun abrirWhatsApp(mensaje: String) {
        val numero = "525645119567"
        val uri = Uri.parse(
            "https://api.whatsapp.com/send?phone=$numero&text=${Uri.encode(mensaje)}"
        )
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
