package com.example.softhats

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        // Puedes mantener enableEdgeToEdge() si lo deseas, pero no es necesario para el catálogo
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

        // Le decimos al RecyclerView que use nuestro adaptador
        recyclerView.adapter = gorraAdapter

        // --- 3. Llamar a la función que trae los datos ---
        getGorraData()
    }

    // --- 4. La Función Mágica (Leer de Firestore) ---
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