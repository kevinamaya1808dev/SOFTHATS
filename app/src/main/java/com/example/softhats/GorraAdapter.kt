package com.example.softhats

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
// --- IMPORTACIONES ---
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    var onItemClick: ((Gorra) -> Unit)? = null

    // 1. EL VIEWHOLDER
    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)
        val btnCarrito: View = itemView.findViewById(R.id.btnCarrito)

        init {
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

        // Datos visuales
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // Lógica de imagen
        val imageName = currentGorra.imagen_nombre
        val resourceId = context.resources.getIdentifier(
            imageName, "drawable", context.packageName
        )

        if (resourceId != 0) {
            holder.ivGorra.setImageResource(resourceId)
        } else {
            holder.ivGorra.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // ------------------------------------------------------------------
        // LÓGICA CORREGIDA: BOTÓN AÑADIR AL CARRITO
        // ------------------------------------------------------------------
        holder.btnCarrito.setOnClickListener {

            val cantidadInicial = 1

            // 1. Convertir precio de forma segura
            val precioFinal: Double = try {
                currentGorra.precio.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }

            // 2. Crear objeto (CORREGIDO EL ERROR DE STRING?)
            val productoParaCarrito = CarritoEntity(
                // Si el nombre es nulo, usamos 0 como ID. Si no, usamos su hash.
                idProducto = currentGorra.nombre?.hashCode() ?: 0,

                // Si el nombre es nulo, ponemos un texto por defecto
                nombre = currentGorra.nombre ?: "Gorra Sin Nombre",

                precioUnitario = precioFinal,
                cantidad = cantidadInicial,
                total = precioFinal * cantidadInicial
            )

            // 3. Guardar en BD (Usando Corrutinas de forma segura)
            // Intentamos usar el ciclo de vida del contexto (si es una Activity)
            // Si falla, usamos GlobalScope como respaldo rápido.
            val scope = (context as? LifecycleOwner)?.lifecycleScope
                ?: kotlinx.coroutines.GlobalScope

            scope.launch(Dispatchers.IO) {
                try {
                    // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE DE LA BASE DE DATOS ---
                    // No uses .build() aquí adentro. Usa la instancia Singleton.
                    val db = AppDatabase.getDatabase(context)

                    // Asegúrate que tu DAO tenga el método 'insert' (o 'insertarOActualizar' si así lo llamaste)
                    db.carritoDao().insertarOActualizar(productoParaCarrito)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Agregado: ${currentGorra.nombre}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                        Log.e("GorraAdapter", "Error: ${e.message}")
                    }
                }
            }
        }
    }
}