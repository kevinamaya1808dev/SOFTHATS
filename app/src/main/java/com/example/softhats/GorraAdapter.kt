package com.example.softhats

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.database.FavoritoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    // Variable para el clic en la foto (ir a detalle)
    var onItemClick: ((Gorra) -> Unit)? = null

    // 1. EL VIEWHOLDER
    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)
        val btnCarrito: View = itemView.findViewById(R.id.btnCarrito)
        // 🟢 NUEVO: Declaramos el botón de favorito
        val btnFavorito: ImageButton = itemView.findViewById(R.id.btnFavorito)

        init {
            // Clic en la tarjeta lleva al detalle
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(gorraList[position])
                }
            }
        }
    }

    // 2. ON CREATE VIEWHOLDER
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GorraViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_gorra,
            parent, false
        )
        return GorraViewHolder(itemView)
    }

    // 3. GET ITEM COUNT
    override fun getItemCount(): Int {
        return gorraList.size
    }

    // 4. ON BIND VIEWHOLDER
    override fun onBindViewHolder(holder: GorraViewHolder, position: Int) {
        val currentGorra = gorraList[position]

        // --- A. Datos visuales ---
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // --- B. Cargar Imagen ---
        val imageName = currentGorra.imagen_nombre
        val resourceId = context.resources.getIdentifier(
            imageName, "drawable", context.packageName
        )

        if (resourceId != 0) {
            holder.ivGorra.setImageResource(resourceId)
        } else {
            holder.ivGorra.setImageResource(R.drawable.ic_launcher_foreground)
            Log.w("GorraAdapter", "No se encontró la imagen: $imageName")
        }

        // Preparar datos comunes para BD
        val nombreSeguro = currentGorra.nombre ?: "Gorra Sin Nombre"
        val idProducto = nombreSeguro.hashCode()
        val imagenSegura = currentGorra.imagen_nombre ?: ""

        // Scope para corrutinas
        val scope = (context as? LifecycleOwner)?.lifecycleScope ?: kotlinx.coroutines.GlobalScope
        val db = AppDatabase.getDatabase(context)

        // ==================================================================
        // 🌟 C. LÓGICA DE FAVORITOS (ESTRELLA) - NUEVO
        // ==================================================================

        // 1. Checar estado inicial (¿Está en favoritos?) para pintar la estrella
        scope.launch(Dispatchers.IO) {
            val esFavorito = db.favoritoDao().esFavorito(idProducto)

            withContext(Dispatchers.Main) {
                if (esFavorito) {
                    holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_on) // ⭐ Amarilla
                } else {
                    holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_off) // ☆ Gris
                }
            }
        }

        // 2. Clic en la Estrella (Guardar o Borrar)
        holder.btnFavorito.setOnClickListener {
            scope.launch(Dispatchers.IO) {
                val yaEsFavorito = db.favoritoDao().esFavorito(idProducto)

                if (yaEsFavorito) {
                    // Si ya existe -> Borrar
                    db.favoritoDao().eliminarFavorito(idProducto)
                    withContext(Dispatchers.Main) {
                        holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_off)
                        Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Si no existe -> Guardar
                    val nuevoFav = FavoritoEntity(
                        idProducto = idProducto,
                        nombre = nombreSeguro,
                        precio = try { currentGorra.precio.toString().toDouble() } catch (e: Exception) { 0.0 },
                        descripcion = currentGorra.descripcion ?: "Sin descripción",
                        imagenNombre = imagenSegura
                    )
                    db.favoritoDao().agregarFavorito(nuevoFav)
                    withContext(Dispatchers.Main) {
                        holder.btnFavorito.setImageResource(android.R.drawable.btn_star_big_on)
                        Toast.makeText(context, "¡Añadido a favoritos!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // ==================================================================
        // 🛒 D. LÓGICA DEL CARRITO (SUMA INTELIGENTE) - SE MANTIENE IGUAL
        // ==================================================================
        holder.btnCarrito.setOnClickListener {

            val cantidadAGregar = 1
            val precioFinal: Double = try {
                currentGorra.precio.toString().toDouble()
            } catch (e: NumberFormatException) { 0.0 }

            scope.launch(Dispatchers.IO) {
                try {
                    // 1. Verificar si ya existe en el carrito
                    val productoExistente = db.carritoDao().obtenerProducto(idProducto)

                    val cantidadFinal = if (productoExistente != null) {
                        productoExistente.cantidad + cantidadAGregar
                    } else {
                        cantidadAGregar
                    }

                    // 2. Crear objeto actualizado
                    val productoParaCarrito = CarritoEntity(
                        idProducto = idProducto,
                        nombre = nombreSeguro,
                        precioUnitario = precioFinal,
                        cantidad = cantidadFinal,
                        total = precioFinal * cantidadFinal,
                        imagen = imagenSegura
                    )

                    // 3. Guardar
                    db.carritoDao().insertarOActualizar(productoParaCarrito)

                    withContext(Dispatchers.Main) {
                        val mensaje = if (productoExistente != null) {
                            "Cantidad actualizada: $cantidadFinal"
                        } else {
                            "Agregado al carrito"
                        }
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("GorraAdapter", "Error al guardar: ${e.message}")
                    }
                }
            }
        }
    }
}