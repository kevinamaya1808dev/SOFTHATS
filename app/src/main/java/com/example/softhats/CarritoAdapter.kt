package com.example.softhats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.CarritoEntity
import com.example.softhats.databinding.ItemCarritoBinding
import java.util.Locale

// Este adaptador recibe funciones (lambdas) para saber qué hacer cuando le pican a los botones
class CarritoAdapter(
    private val onSumarClick: (CarritoEntity) -> Unit,
    private val onRestarClick: (CarritoEntity) -> Unit,
    private val onEliminarClick: (CarritoEntity) -> Unit
) : ListAdapter<CarritoEntity, CarritoAdapter.CarritoViewHolder>(DiffCallback) {

    // Clase para comparar items y saber si actualizaron la lista
    companion object DiffCallback : DiffUtil.ItemCallback<CarritoEntity>() {
        override fun areItemsTheSame(oldItem: CarritoEntity, newItem: CarritoEntity): Boolean {
            return oldItem.idProducto == newItem.idProducto
        }

        override fun areContentsTheSame(oldItem: CarritoEntity, newItem: CarritoEntity): Boolean {
            return oldItem == newItem
        }
    }

    // ViewHolder: Mantiene las referencias a los controles de cada renglón
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

            // Configurar botones
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