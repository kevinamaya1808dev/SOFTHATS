package com.example.softhats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

// LA DEFINICIÓN COMPLETA con el constructor y la herencia de RecyclerView.Adapter
class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    // 1. EL VIEWHOLDER
    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)
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

        // Conecta los datos con las vistas
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // Lógica para la imagen (se ha simplificado aquí para evitar más errores)
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