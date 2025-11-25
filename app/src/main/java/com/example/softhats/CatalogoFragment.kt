package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.softhats.databinding.FragmentCatalogoBinding
import com.google.firebase.firestore.FirebaseFirestore

class CatalogoFragment : Fragment() {

    private lateinit var binding: FragmentCatalogoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var gorraArrayList: ArrayList<Gorra>
    private lateinit var gorraAdapter: GorraAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Conectamos con el diseño visual (XML)
        binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar el RecyclerView (Lista de 2 columnas)
        // NOTA IMPORTANTE: En fragmentos usamos 'requireContext()' en lugar de 'this'
        binding.recyclerViewGorras.layoutManager = GridLayoutManager(requireContext(), 2)

        gorraArrayList = ArrayList()
        // Aquí pasamos requireContext() porque el adaptador pide un Contexto
        gorraAdapter = GorraAdapter(requireContext(), gorraArrayList)

        // 2. Configurar el Clic en cada gorra (Ir a Detalle)
        gorraAdapter.onItemClick = { gorra ->
            val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
            // Pasamos los datos de la gorra seleccionada
            intent.putExtra("EXTRA_NOMBRE", gorra.nombre)
            intent.putExtra("EXTRA_PRECIO", gorra.precio)
            intent.putExtra("EXTRA_DESCRIPCION", gorra.descripcion)
            intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
            startActivity(intent)
        }

        binding.recyclerViewGorras.adapter = gorraAdapter

        // 3. Inicializar Firebase y llamar a la función de carga
        db = FirebaseFirestore.getInstance()
        cargarDatosDeFirebase()
    }

    private fun cargarDatosDeFirebase() {
        // Mostramos la barrita de carga para que el usuario sepa que está trabajando
        binding.progressBar.visibility = View.VISIBLE

        db.collection("gorras")
            .get()
            .addOnSuccessListener { result ->
                // Ya llegaron los datos, ocultamos la carga
                binding.progressBar.visibility = View.GONE
                gorraArrayList.clear()

                for (document in result) {
                    val gorra = document.toObject(Gorra::class.java)
                    gorraArrayList.add(gorra)
                }

                // Le avisamos a la lista que se actualice
                gorraAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.e("CatalogoFragment", "Error al traer gorras: ", exception)
            }
    }
}