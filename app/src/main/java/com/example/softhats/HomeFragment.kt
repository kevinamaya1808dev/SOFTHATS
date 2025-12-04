package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.softhats.databinding.FragmentHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var gorraArrayList: ArrayList<Gorra>
    private lateinit var gorraAdapter: GorraAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar el botón del Banner (Ver Ahora -> Ir a Catálogo)
        binding.btnVerAhora.setOnClickListener {
            // Buscamos la barra de navegación de la actividad principal y cambiamos la selección
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_catalogo
        }

        // 2. Configurar el RecyclerView de TENDENCIAS (Horizontal)
        // Usamos LinearLayoutManager HORIZONTAL para que se deslice de lado
        binding.rvTendencias.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        gorraArrayList = ArrayList()
        gorraAdapter = GorraAdapter(requireContext(), gorraArrayList)

        // Al hacer clic en una gorra de tendencias, vamos al detalle igual que en el catálogo
        gorraAdapter.onItemClick = { gorra ->
            val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
            intent.putExtra("EXTRA_NOMBRE", gorra.nombre)
            intent.putExtra("EXTRA_PRECIO", gorra.precio)
            intent.putExtra("EXTRA_DESCRIPCION", gorra.descripcion)
            intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
            startActivity(intent)
        }

        binding.rvTendencias.adapter = gorraAdapter

        // 3. Cargar datos de Firebase (Limitado a 5 para simular "Destacados")
        db = FirebaseFirestore.getInstance()
        cargarTendencias()
    }

    private fun cargarTendencias() {
        // Mostrar carga
        binding.progressTendencias.visibility = View.VISIBLE

        // Traemos solo 5 gorras para no saturar el inicio
        db.collection("gorras")
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                binding.progressTendencias.visibility = View.GONE
                gorraArrayList.clear()
                for (document in result) {
                    val gorra = document.toObject(Gorra::class.java)
                    gorraArrayList.add(gorra)
                }
                gorraAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                binding.progressTendencias.visibility = View.GONE
                Log.e("HomeFragment", "Error cargando tendencias", exception)
            }
    }
}