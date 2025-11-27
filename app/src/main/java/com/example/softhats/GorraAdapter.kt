package com.example.softhats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Log

class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    // --- NUEVO: COPIA DE SEGURIDAD PARA LA BÚSQUEDA ---
    // Aquí guardamos TODAS las gorras para recuperarlas cuando borres el texto
    private val gorraListOriginal = ArrayList<Gorra>()

    // Función para guardar los datos originales la primera vez
    fun actualizarDatosOriginales(nuevaLista: ArrayList<Gorra>) {
        gorraListOriginal.clear()
        gorraListOriginal.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    // --- NUEVO: FUNCIÓN DE FILTRADO ---
    fun filtrar(texto: String) {
        val textoBusqueda = texto.lowercase()
        gorraList.clear()

        if (textoBusqueda.isEmpty()) {
            gorraList.addAll(gorraListOriginal)
        } else {
            for (gorra in gorraListOriginal) {
                // --- AQUÍ ESTABA EL ERROR ---
                // Agregamos (gorra.nombre ?: "") para evitar el null
                if ((gorra.nombre ?: "").lowercase().contains(textoBusqueda)) {
                    gorraList.add(gorra)
                }
            }
        }
        notifyDataSetChanged()
    }

    // --- EVENTOS DE CLIC (Lambdas) ---
    var onItemClick: ((Gorra) -> Unit)? = null
    var onFavoriteClick: ((Gorra) -> Unit)? = null
    var onCameraClick: ((Gorra) -> Unit)? = null
    var onCartClick: ((Gorra) -> Unit)? = null

// -------------------------------------------------------------------------------------------------

    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)
        val tvMarcaGorra: TextView = itemView.findViewById(R.id.tvMarcaGorra)

        val btnFavorite: ImageButton = itemView.findViewById(R.id.btnFavoriteItem)
        val btnCamera: ImageButton = itemView.findViewById(R.id.btnArCamera)
        val btnCart: ImageButton = itemView.findViewById(R.id.btnQuickAddCart)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(gorraList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GorraViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_gorra,
            parent, false
        )
        return GorraViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return gorraList.size
    }

    override fun onBindViewHolder(holder: GorraViewHolder, position: Int) {
        val currentGorra = gorraList[position]

        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"
        holder.tvMarcaGorra.text = "ThirtyOneHats"

        val imageName = currentGorra.imagen_nombre
        val resourceId = context.resources.getIdentifier(
            imageName, "drawable", context.packageName
        )

        if (resourceId != 0) {
            holder.ivGorra.setImageResource(resourceId)
        } else {
            // holder.ivGorra.setImageResource(R.drawable.placeholder_gorra)
        }

        holder.btnFavorite.setOnClickListener { onFavoriteClick?.invoke(currentGorra) }
        holder.btnCamera.setOnClickListener { onCameraClick?.invoke(currentGorra) }
        holder.btnCart.setOnClickListener { onCartClick?.invoke(currentGorra) }
    }
}