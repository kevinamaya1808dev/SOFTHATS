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