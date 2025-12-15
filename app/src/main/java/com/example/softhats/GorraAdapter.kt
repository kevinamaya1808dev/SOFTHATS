package com.example.softhats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
<<<<<<< HEAD
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.CarritoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
=======
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca

// DEFINICIÓN PRINCIPAL DEL ADAPTADOR (Módulo 2)
class GorraAdapter(private val context: Context, private val gorraList: ArrayList<Gorra>) :
    RecyclerView.Adapter<GorraAdapter.GorraViewHolder>() {

    // Declara una variable lambda para manejar el evento de clic fuera del adaptador
    var onItemClick: ((Gorra) -> Unit)? = null

<<<<<<< HEAD
=======
// -------------------------------------------------------------------------------------------------

    // 1. EL VIEWHOLDER (Debe existir solo una vez)
    // Controla los elementos visuales de CADA fila (item_gorra.xml)
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
    inner class GorraViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivGorra: ImageView = itemView.findViewById(R.id.ivGorra)
        val tvNombreGorra: TextView = itemView.findViewById(R.id.tvNombreGorra)
        val tvPrecioGorra: TextView = itemView.findViewById(R.id.tvPrecioGorra)

        // Inicializador para el evento de clic (Lógica de Módulo 2)
        init {
            // Clic en la foto o texto lleva al detalle
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick?.invoke(gorraList[position])
                }
            }
        }
    }
// -------------------------------------------------------------------------------------------------

<<<<<<< HEAD
=======
    // 2. ON CREATE VIEWHOLDER
    // Dibuja el XML de la fila (item_gorra.xml)
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GorraViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_gorra,
            parent, false
        )
        return GorraViewHolder(itemView)
    }

<<<<<<< HEAD
=======
    // 3. GET ITEM COUNT
    // Le dice al RecyclerView cuántos items (gorras) hay en la lista
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
    override fun getItemCount(): Int {
        return gorraList.size
    }

<<<<<<< HEAD
    override fun onBindViewHolder(holder: GorraViewHolder, position: Int) {
        val currentGorra = gorraList[position]

        // 1. Datos visuales
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // 2. Cargar Imagen
=======
    // 4. ON BIND VIEWHOLDER
    // Conecta los datos con el ViewHolder
    override fun onBindViewHolder(holder: GorraViewHolder, position: Int) {
        val currentGorra = gorraList[position]

        // Conecta los datos con las vistas
        holder.tvNombreGorra.text = currentGorra.nombre
        holder.tvPrecioGorra.text = "$ ${currentGorra.precio}"

        // Lógica para la imagen (de String a @drawable)
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
        val imageName = currentGorra.imagen_nombre
        val resourceId = context.resources.getIdentifier(
            imageName, "drawable", context.packageName
        )

        if (resourceId != 0) {
            holder.ivGorra.setImageResource(resourceId)
        } else {
<<<<<<< HEAD
            holder.ivGorra.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // ------------------------------------------------------------------
        // 3. LÓGICA "SUMA INTELIGENTE" EN EL BOTÓN DEL CARRITO
        // ------------------------------------------------------------------
        holder.btnCarrito.setOnClickListener {

            val cantidadAGregar = 1
            val precioFinal: Double = try {
                currentGorra.precio.toString().toDouble()
            } catch (e: NumberFormatException) { 0.0 }

            // Nombre seguro e ID
            val nombreSeguro = currentGorra.nombre ?: "Gorra Sin Nombre"
            val idProducto = nombreSeguro.hashCode()
            val imagenSegura = currentGorra.imagen_nombre ?: ""

            // Usamos Corrutinas para verificar y guardar
            val scope = (context as? LifecycleOwner)?.lifecycleScope ?: kotlinx.coroutines.GlobalScope

            scope.launch(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context)

                    // A. VERIFICAR SI YA EXISTE
                    val productoExistente = db.carritoDao().obtenerProducto(idProducto)

                    val cantidadFinal = if (productoExistente != null) {
                        // Si existe, sumamos
                        productoExistente.cantidad + cantidadAGregar
                    } else {
                        // Si es nuevo, es 1
                        cantidadAGregar
                    }

                    // B. CREAR OBJETO ACTUALIZADO
                    val productoParaCarrito = CarritoEntity(
                        idProducto = idProducto,
                        nombre = nombreSeguro,
                        precioUnitario = precioFinal,
                        cantidad = cantidadFinal,
                        total = precioFinal * cantidadFinal,
                        imagen = imagenSegura // ¡No olvidar la imagen!
                    )

                    // C. GUARDAR
                    db.carritoDao().insertarOActualizar(productoParaCarrito)

                    // D. FEEDBACK AL USUARIO
                    withContext(Dispatchers.Main) {
                        val mensaje = if (productoExistente != null) {
                            "Cantidad actualizada: $cantidadFinal"
                        } else {
                            "Agregado al carrito"
                        }
                        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("GorraAdapter", "Error al guardar: ${e.message}")
                    }
                }
            }
=======
            Log.w("GorraAdapter", "No se encontró la imagen: $imageName")
>>>>>>> 497abeaf50ca4bc9e9a857e51eebf62e007f7eca
        }
    }
}