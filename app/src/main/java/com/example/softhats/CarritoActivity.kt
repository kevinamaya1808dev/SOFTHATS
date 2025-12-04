package com.example.softhats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ActivityCarritoBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.URLEncoder
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

        // 4. Botón Pagar/Cerrar Venta (MODIFICADO para WhatsApp)
        binding.btnPagar.setOnClickListener {
            // Llama a la función para obtener el resumen y enviar a WhatsApp
            enviarPedidoWhatsAppConResumen()
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
            // Si la cantidad llega a 0, eliminamos directo
            eliminarItem(item)
        }
    }

    private fun eliminarItem(item: CarritoEntity) {
        lifecycleScope.launch {
            database.carritoDao().eliminarProducto(item.idProducto)
            Toast.makeText(this@CarritoActivity, "${item.nombre} eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    // --------------------------------------------------------------------------------
    // 🚩🚩 FUNCIÓN 1 DE 2: LECTURA DEL CARRITO Y ENVÍO A WHATSAPP 🚩🚩
    // --------------------------------------------------------------------------------

    private fun enviarPedidoWhatsAppConResumen() {
        lifecycleScope.launch {
            // 1. Obtener la lista actual de artículos del carrito usando .first()
            val items = database.carritoDao().obtenerCarrito().first()

            if (items.isEmpty()) {
                Toast.makeText(this@CarritoActivity, "El carrito está vacío para enviar el pedido.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            Toast.makeText(this@CarritoActivity, "Generando pedido y abriendo WhatsApp...", Toast.LENGTH_LONG).show()

            // 2. Procesar los artículos para generar el mensaje
            val mensajeArticulos = StringBuilder()
            var totalVenta = 0.0
            val nombreCliente = "Cliente SOFTHATS"

            for (item in items) {
                totalVenta += item.total

                // Construir la línea del artículo
                mensajeArticulos.append(
                    String.format(
                        Locale.getDefault(),
                        "- %d x %s ($%.2f c/u)\n",
                        item.cantidad,
                        item.nombre,
                        item.precioUnitario
                    )
                )
            }

            // 3. Llamar a la función que lanza WhatsApp
            lanzarWhatsApp(nombreCliente, mensajeArticulos.toString(), totalVenta)
        }
    }


    // --------------------------------------------------------------------------------
    // 🚩🚩 FUNCIÓN 2 DE 2: ENSAMBLAR MENSAJE Y ABRIR WHATSAPP 🚩🚩
    // --------------------------------------------------------------------------------

    private fun lanzarWhatsApp(
        nombreCliente: String,
        detalleArticulos: String,
        totalVenta: Double
    ) {
        // 🚨 AJUSTA ESTE NÚMERO POR EL TELÉFONO DE SOFTHATS (Código de país + número, sin '+') 🚨
        val numeroEmpresa = "525626350435"

        val plantillaMensaje = """
            *--- Pedido SOFTHATS (Cierre de Venta) ---*
            
            Hola, %s.
            
            Hemos generado tu resumen de pedido, por favor confírmalo:
            
            *Artículos:*
            %s
            
            *Total del Pedido: $%.2f Mxm*
            
            Para completar la compra, por favor responde indicando tu método de pago preferido (Transferencia o Deposito).
            ¡Gracias por elegir a SOFTHATS!
        """.trimIndent()

        val mensajeFinal = String.format(
            Locale.getDefault(),
            plantillaMensaje,
            nombreCliente,
            detalleArticulos,
            totalVenta
        )

        try {
            // Codificar el mensaje para la URL
            val mensajeCodificado = URLEncoder.encode(mensajeFinal, "UTF-8")
            val uri = Uri.parse("https://wa.me/$numeroEmpresa?text=$mensajeCodificado")

            // 1. Crear el Intent de vista (ACTION_VIEW)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // 2. 🔑 SOLUCIÓN: FORZAR EL INTENT A USAR EL PAQUETE DE WHATSAPP ESTÁNDAR 🔑
            intent.setPackage("com.whatsapp")

            // 3. Verificar y lanzar
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                // Si falla al forzar el paquete, intentar con el método genérico
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                if (fallbackIntent.resolveActivity(packageManager) != null) {
                    startActivity(fallbackIntent)
                } else {
                    Toast.makeText(this, "WhatsApp no está instalado.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ocurrió un error al intentar abrir la aplicación.", Toast.LENGTH_LONG).show()
        }
    }
    // --------------------------------------------------------------------------------
}