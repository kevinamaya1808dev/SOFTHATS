package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton // Importante: Importamos el botón flotante
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

class HomeActivity : AppCompatActivity() {

    // --- 1. Declarar las Variables ---
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var gorraArrayList: ArrayList<Gorra>
    private lateinit var gorraAdapter: GorraAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // --- 2. Inicializar Variables y Vistas ---

        // Conectamos el RecyclerView del XML con nuestra variable
        recyclerView = findViewById(R.id.recyclerViewGorras)

        // Le decimos que use el GridLayout de 2 columnas
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Inicializamos la base de datos
        db = FirebaseFirestore.getInstance()

        // Inicializamos nuestra lista y adaptador
        gorraArrayList = ArrayList()
        gorraAdapter = GorraAdapter(this, gorraArrayList)

        // --- 3. CÓDIGO NUEVO: BOTÓN FLOTANTE DEL CARRITO ---
        // Buscamos el botón por su ID (el que pusimos en el XML)
        val fabCarrito = findViewById<FloatingActionButton>(R.id.fabVerCarrito)

        // Le damos vida al clic
        fabCarrito.setOnClickListener {
            // Al hacer clic, nos lleva a la pantalla del CarritoActivity
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }
        // --------------------------------------------------

        // --- 4. Define la acción de Clic en cada Gorra ---
        gorraAdapter.onItemClick = { gorraSeleccionada ->
            // Creamos un Intent para ir a DetalleGorraActivity
            val intent = Intent(this, DetalleGorraActivity::class.java)

            // Pasamos los datos de la gorra a la siguiente Activity
            intent.putExtra("EXTRA_NOMBRE", gorraSeleccionada.nombre)
            intent.putExtra("EXTRA_PRECIO", gorraSeleccionada.precio)
            intent.putExtra("EXTRA_DESCRIPCION", gorraSeleccionada.descripcion)
            intent.putExtra("EXTRA_IMAGEN", gorraSeleccionada.imagen_nombre)

            startActivity(intent)
        }
        // --- (Fin del bloque de clic) ---

        // Le decimos al RecyclerView que use nuestro adaptador
        recyclerView.adapter = gorraAdapter

        // --- 5. Llamar a la función que trae los datos ---
        getGorraData()
    }

    // --- 6. La Función Mágica (Leer de Firestore) ---
    private fun getGorraData() {

        // Apuntamos a nuestra colección "gorras" en Firestore
        db.collection("gorras")
            .get()
            .addOnSuccessListener { result ->

                // Limpiamos la lista
                gorraArrayList.clear()

                // Recorremos cada documento (gorra) que nos llegó
                for (document in result) {
                    // Convertimos el documento de Firebase en nuestro objeto "Gorra"
                    val gorra = document.toObject(Gorra::class.java)
                    gorraArrayList.add(gorra) // Añadimos la gorra a nuestra lista
                }

                // Le avisamos al adaptador que la lista cambió, para que redibuje
                gorraAdapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                // Si algo salió mal
                Log.e("HomeActivity", "Error al obtener datos: ", exception)
            }
    }
}