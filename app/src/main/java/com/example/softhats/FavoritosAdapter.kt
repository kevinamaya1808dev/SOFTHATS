package com.example.softhats

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import com.example.softhats.database.FavoritoEntity
import com.example.softhats.databinding.ItemGorraBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class FavoritosAdapter : ListAdapter<FavoritoEntity, FavoritosAdapter.FavoritoViewHolder>(DiffCallback) {

    var onItemClick: ((FavoritoEntity) -> Unit)? = null

    companion object DiffCallback : DiffUtil.ItemCallback<FavoritoEntity>() {
        override fun areItemsTheSame(oldItem: FavoritoEntity, newItem: FavoritoEntity): Boolean {
            return oldItem.idProducto == newItem.idProducto
        }

        override fun areContentsTheSame(oldItem: FavoritoEntity, newItem: FavoritoEntity): Boolean {
            return oldItem == newItem
        }
    }

    class FavoritoViewHolder(private val binding: ItemGorraBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorito: FavoritoEntity, clickListener: ((FavoritoEntity) -> Unit)?) {
            // 1. Datos visuales
            binding.tvNombreGorra.text = favorito.nombre
            binding.tvPrecioGorra.text = "$ ${String.format(Locale.getDefault(), "%,.2f", favorito.precio)}"

            // Cargar Imagen
            if (favorito.imagenNombre.isNotEmpty()) {
                val context = binding.root.context
                val resourceId = context.resources.getIdentifier(favorito.imagenNombre, "drawable", context.packageName)
                if (resourceId != 0) {
                    binding.ivGorra.setImageResource(resourceId)
                } else {
                    binding.ivGorra.setImageResource(R.drawable.ic_launcher_foreground)
                }
            }

            // 2. Clic en la tarjeta (Ir a detalles)
            itemView.setOnClickListener {
                clickListener?.invoke(favorito)
            }

            // ===============================================================
            // 🌟 3. LÓGICA DE FAVORITOS (NUEVO)
            // ===============================================================

            // A. Como estamos en la pantalla de Favoritos, la estrella SIEMPRE es amarilla
            binding.btnFavorito.setImageResource(android.R.drawable.btn_star_big_on)

            // B. Al darle clic, ELIMINAMOS de favoritos
            binding.btnFavorito.setOnClickListener {
                val context = binding.root.context
                val scope = (context as? LifecycleOwner)?.lifecycleScope ?: kotlinx.coroutines.GlobalScope

                scope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    db.favoritoDao().eliminarFavorito(favorito.idProducto)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // ===============================================================
            // 🛒 4. LÓGICA DEL CARRITO (CON SUMA INTELIGENTE)
            // ===============================================================
            binding.btnCarrito.setOnClickListener {
                val context = binding.root.context
                val cantidadAGregar = 1
                val idProducto = favorito.nombre.hashCode() // Usamos el mismo ID hash

                val scope = (context as? LifecycleOwner)?.lifecycleScope ?: kotlinx.coroutines.GlobalScope

                scope.launch(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getDatabase(context)

                        // A. Verificar si ya existe
                        val productoExistente = db.carritoDao().obtenerProducto(idProducto)

                        val cantidadFinal = if (productoExistente != null) {
                            productoExistente.cantidad + cantidadAGregar
                        } else {
                            cantidadAGregar
                        }

                        // B. Crear objeto actualizado (incluyendo la imagen)
                        val productoParaCarrito = CarritoEntity(
                            idProducto = idProducto,
                            nombre = favorito.nombre,
                            precioUnitario = favorito.precio,
                            cantidad = cantidadFinal,
                            total = favorito.precio * cantidadFinal,
                            imagen = favorito.imagenNombre
                        )

                        // C. Guardar
                        db.carritoDao().insertarOActualizar(productoParaCarrito)

                        withContext(Dispatchers.Main) {
                            val mensaje = if (productoExistente != null) "Cantidad actualizada: $cantidadFinal" else "Agregado al carrito"
                            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val binding = ItemGorraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val favorito = getItem(position)
        holder.bind(favorito, onItemClick)
    }
}