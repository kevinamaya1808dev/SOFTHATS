package com.example.softhats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.FavoritoEntity
import com.example.softhats.databinding.ItemGorraBinding
import java.util.Locale

class FavoritosAdapter : ListAdapter<FavoritoEntity, FavoritosAdapter.FavoritoViewHolder>(DiffCallback) {

    // 1. Variable para guardar la acción de clic (El "Cable")
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
            // Datos visuales
            binding.tvNombreGorra.text = favorito.nombre
            binding.tvPrecioGorra.text = "$ ${String.format(Locale.getDefault(), "%,.2f", favorito.precio)}"

            if (favorito.imagenNombre.isNotEmpty()) {
                val context = binding.root.context
                val resourceId = context.resources.getIdentifier(favorito.imagenNombre, "drawable", context.packageName)
                if (resourceId != 0) {
                    binding.ivGorra.setImageResource(resourceId)
                }
            }

            // 2. Configurar el clic: Cuando toquen la tarjeta, avisamos
            itemView.setOnClickListener {
                clickListener?.invoke(favorito)
            }
<<<<<<< HEAD

            // ---------------------------------------------------------------
            // 3. Clic en el botón del carrito (btnCarrito)
            // ---------------------------------------------------------------
            binding.btnCarrito.setOnClickListener {
                val context = binding.root.context
                val cantidadInicial = 1

                // Creamos el objeto para el carrito basándonos en el favorito
                val productoParaCarrito = CarritoEntity(
                    // Usamos hashcode del nombre para que coincida con el catálogo si es la misma gorra
                    idProducto = favorito.nombre.hashCode(),
                    nombre = favorito.nombre,
                    precioUnitario = favorito.precio,
                    cantidad = cantidadInicial,
                    total = favorito.precio * cantidadInicial,
                    // 🟢 AQUÍ ESTABA EL ERROR: Faltaba pasar la imagen
                    imagen = favorito.imagenNombre
                )

                // Usamos Corrutinas para guardar en BD
                val scope = (context as? LifecycleOwner)?.lifecycleScope
                    ?: kotlinx.coroutines.GlobalScope

                scope.launch(Dispatchers.IO) {
                    try {
                        // Conexión a la BD
                        val db = AppDatabase.getDatabase(context)

                        // Guardar
                        db.carritoDao().insertarOActualizar(productoParaCarrito)

                        // Avisar al usuario
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Agregado desde Favoritos: ${favorito.nombre}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("FavoritosAdapter", "Error DB: ${e.message}")
                        }
                    }
                }
            }
=======
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val binding = ItemGorraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        val favorito = getItem(position)
        // 3. Pasamos el listener al ViewHolder
        holder.bind(favorito, onItemClick)
    }
}