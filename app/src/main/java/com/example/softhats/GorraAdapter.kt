package com.example.softhats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

// DEFINICIÓN PRINCIPAL DEL ADAPTADOR (Módulo 2)
class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    // Declara una variable lambda para manejar el evento de clic fuera del adaptador
    var onItemClick: ((Gorra) -> Unit)? = null

// -------------------------------------------------------------------------------------------------

    // 1. EL VIEWHOLDER (Debe existir solo una vez)
    // Controla los elementos visuales de CADA fila (item_gorra.xml)
    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)

        // Inicializador para el evento de clic (Lógica de Módulo 2)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(gorraList[position])
                }
            }
        }
    }
// -------------------------------------------------------------------------------------------------

    // 2. ON CREATE VIEWHOLDER
    // Dibuja el XML de la fila (item_gorra.xml)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GorraViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_gorra,
            parent, false
        )
        return GorraViewHolder(itemView)
    }

    // 3. GET ITEM COUNT
    // Le dice al RecyclerView cuántos items (gorras) hay en la lista
    override fun getItemCount(): Int {
        return gorraList.size
    }

    // 4. ON BIND VIEWHOLDER
    // Conecta los datos con el ViewHolder
    override fun onBindViewHolder(holder: GorraViewHolder, position: Int) {
        val currentGorra = gorraList[position]

        // Conecta los datos con las vistas
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // Lógica para la imagen (de String a @drawable)
        val imageName = currentGorra.imagen_nombre
        val resourceId = context.resources.getIdentifier(
            imageName, "drawable", context.packageName
        )

        if (resourceId != 0) {
            holder.ivGorra.setImageResource(resourceId)
        } else {
            Log.w("GorraAdapter", "No se encontró la imagen: $imageName")
        }
    }
}