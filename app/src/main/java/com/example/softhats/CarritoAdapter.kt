package com.example.softhats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ItemCarritoBinding
import java.util.Locale

class CarritoAdapter(
    private val onSumarClick: (CarritoEntity) -> Unit,
    private val onRestarClick: (CarritoEntity) -> Unit,
    private val onEliminarClick: (CarritoEntity) -> Unit
) : ListAdapter<CarritoEntity, CarritoAdapter.CarritoViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<CarritoEntity>() {
        override fun areItemsTheSame(oldItem: CarritoEntity, newItem: CarritoEntity): Boolean {
            return oldItem.idProducto == newItem.idProducto
        }

        override fun areContentsTheSame(oldItem: CarritoEntity, newItem: CarritoEntity): Boolean {
            return oldItem == newItem
        }
    }

    class CarritoViewHolder(private val binding: ItemCarritoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: CarritoEntity,
            onSumar: (CarritoEntity) -> Unit,
            onRestar: (CarritoEntity) -> Unit,
            onEliminar: (CarritoEntity) -> Unit
        ) {
            // 1. Asignar datos de texto
            binding.tvNombreCarrito.text = item.nombre
            binding.tvPrecioCarrito.text = "$ ${String.format(Locale.getDefault(), "%,.2f", item.precioUnitario)}"
            binding.tvCantidad.text = item.cantidad.toString()

            // 2. Lógica de Imagen (MEJORADA Y SEGURA)
            val context = binding.root.context

            // Verificamos que el nombre de la imagen no sea nulo
            val nombreImagen = item.imagen

            if (!nombreImagen.isNullOrEmpty()) {
                val resourceId = context.resources.getIdentifier(
                    nombreImagen,
                    "drawable",
                    context.packageName
                )

                // Si encontramos la imagen en la carpeta drawable, la ponemos
                if (resourceId != 0) {
                    binding.ivImagenCarrito.setImageResource(resourceId)
                } else {
                    // Si el nombre existe pero el archivo no, ponemos icono por defecto
                    binding.ivImagenCarrito.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } else {
                // Si el campo imagen viene vacío, ponemos icono por defecto
                binding.ivImagenCarrito.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // 3. Asignar acciones a los botones
            binding.btnSumar.setOnClickListener { onSumar(item) }
            binding.btnRestar.setOnClickListener { onRestar(item) }
            binding.btnEliminar.setOnClickListener { onEliminar(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarritoViewHolder {
        val binding = ItemCarritoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CarritoViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onSumarClick, onRestarClick, onEliminarClick)
    }
}