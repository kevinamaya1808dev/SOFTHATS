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
            procesarPedidoCompleto()
        }
    }

    private fun setupRecyclerView() {
        adapter = CarritoAdapter(
            onSumarClick = { item -> actualizarCantidad(item, 1) },
            onRestarClick = { item -> actualizarCantidad(item, -1) },
            onEliminarClick = { item -> eliminarItem(item) }
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
        val granTotal = items.sumOf { it.total }
        binding.tvGranTotal.text =
            "$ ${String.format(Locale.getDefault(), "%,.2f", granTotal)}"
    }

    private fun actualizarCantidad(item: CarritoEntity, cambio: Int) {
        val nuevaCantidad = item.cantidad + cambio
        if (nuevaCantidad > 0) {
            val actualizado = item.copy(
                cantidad = nuevaCantidad,
                total = item.precioUnitario * nuevaCantidad
            )
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
                Toast.makeText(
                    this@CarritoActivity,
                    "${item.nombre} eliminado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // --------------------------------------------------
    // 🟢 PROCESAR PEDIDO → PDF → CONFIRMAR WHATSAPP
    // --------------------------------------------------
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

            val fechaVisual = SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            ).format(Date())

            val compraId = SimpleDateFormat(
                "ddMMyyyy_HHmmss",
                Locale.getDefault()
            ).format(Date())

            val itemsCompra = carrito.map { it.nombre to it.total }
            val totalCompra = carrito.sumOf { it.total }

            val pdfFile = PdfUtils.generarTicketPdf(
                this@CarritoActivity,
                compraId,
                itemsCompra,
                totalCompra
            )

            val mensajeWhatsApp = StringBuilder().apply {
                append("*NUEVO PEDIDO HATSGO* 🧢\n")
                append("📅 Fecha: $fechaVisual\n")
                append("----------------------------\n")
                carrito.forEach {
                    append("▪️ ${it.cantidad}x *${it.nombre}*\n")
                    append("   Subtotal: $${it.total}\n")
                }
                append("----------------------------\n")
                append("💰 *TOTAL A PAGAR: $${totalCompra}*\n")
                append("----------------------------\n")
                append("EN BREVE UN VENDEDOR SE CONTACTARÁ CONTIGO.")
            }.toString()

            database.carritoDao().vaciarCarrito()

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@CarritoActivity,
                    "Ticket PDF generado",
                    Toast.LENGTH_SHORT
                ).show()

                // 1️⃣ Abrir PDF
                abrirPdf(pdfFile)

                // 2️⃣ Confirmar WhatsApp
                AlertDialog.Builder(this@CarritoActivity)
                    .setTitle("Enviar pedido")
                    .setMessage("¿Deseas enviar el pedido por WhatsApp?")
                    .setPositiveButton("Sí") { _, _ ->
                        abrirWhatsApp(mensajeWhatsApp)
                        finish()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }
    }

    // --------------------------------------------------
    // 📄 ABRIR PDF (CORRECTO)
    // --------------------------------------------------
    private fun abrirPdf(pdfFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
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
                "No se pudo abrir el PDF",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
        }
    }

    // --------------------------------------------------
    // 📲 WHATSAPP
    // --------------------------------------------------
    private fun abrirWhatsApp(mensaje: String) {
        val numeroVendedor = "525645119567"
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(
                    "https://api.whatsapp.com/send?phone=$numeroVendedor&text=${
                        Uri.encode(mensaje)
                    }"
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir WhatsApp",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
