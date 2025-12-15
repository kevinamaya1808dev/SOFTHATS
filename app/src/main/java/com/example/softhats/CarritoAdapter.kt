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
            binding.tvNombreCarrito.text = item.nombre
            binding.tvPrecioCarrito.text = "$ ${String.format(Locale.getDefault(), "%,.2f", item.precioUnitario)}"
            binding.tvCantidad.text = item.cantidad.toString()

            // --- LÓGICA NUEVA PARA LA IMAGEN ---
            val context = binding.root.context
            // Asumimos que en tu Entity tienes un campo llamado 'imagen' (ej. "gorra_murakami")
            val nombreImagen = item.imagen // <--- ASEGÚRATE QUE TU ENTITY TENGA ESTE CAMPO

            val resourceId = context.resources.getIdentifier(
                nombreImagen,
                "drawable",
                context.packageName
            )

            // Si encuentra la imagen, la pone. Si no, pone una por defecto (el launcher)
            if (resourceId != 0) {
                binding.ivImagenCarrito.setImageResource(resourceId)
            } else {
                binding.ivImagenCarrito.setImageResource(R.mipmap.ic_launcher)
            }
            // -----------------------------------

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