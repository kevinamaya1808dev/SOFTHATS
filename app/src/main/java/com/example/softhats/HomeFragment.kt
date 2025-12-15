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
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: FirebaseFirestore

    // Listas y Adaptadores
    private lateinit var novedadesList: ArrayList<Gorra>
    private lateinit var novedadesAdapter: GorraAdapter
    private lateinit var tendenciasList: ArrayList<Gorra>
    private lateinit var tendenciasAdapter: GorraAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        // 1. Botón del Banner
        binding.btnVerAhora.setOnClickListener {
            requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_catalogo
        }

        // 2. Botones de Scroll (Novedades y Tendencias)
        // NOTA: Como cambiamos a Cards en el XML, este código funcionará automáticamente al recompilar
        binding.btnFilterNovedades.setOnClickListener {
            binding.root.smoothScrollTo(0, binding.tvTitleNovedades.top)
        }

        binding.btnFilterTendencias.setOnClickListener {
            binding.root.smoothScrollTo(0, binding.tvTitleTendencias.top)
        }

        // 3. Configurar Listas
        setupRecyclerViews()

        // 4. Cargar datos
        cargarDatos()
    }

    private fun setupRecyclerViews() {
        // Lista Novedades
        binding.rvNovedades.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        novedadesList = ArrayList()
        novedadesAdapter = GorraAdapter(requireContext(), novedadesList)
        novedadesAdapter.onItemClick = { gorra -> abrirDetalle(gorra) }
        binding.rvNovedades.adapter = novedadesAdapter

        // Lista Tendencias
        binding.rvTendencias.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        tendenciasList = ArrayList()
        tendenciasAdapter = GorraAdapter(requireContext(), tendenciasList)
        tendenciasAdapter.onItemClick = { gorra -> abrirDetalle(gorra) }
        binding.rvTendencias.adapter = tendenciasAdapter
    }

    private fun cargarDatos() {
        binding.progressCarga.visibility = View.VISIBLE

        // Consulta 1: Novedades (Orden por precio descendente)
        db.collection("gorras")
            .orderBy("precio", Query.Direction.DESCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { result ->
                novedadesList.clear()
                for (document in result) {
                    novedadesList.add(document.toObject(Gorra::class.java))
                }
                novedadesAdapter.notifyDataSetChanged()
                cargarTendencias() // Encadenar la siguiente carga
            }
            .addOnFailureListener {
                binding.progressCarga.visibility = View.GONE
                Log.e("HomeFragment", "Error novedades")
            }
    }

    private fun cargarTendencias() {
        // Consulta 2: Tendencias (Orden por precio ascendente)
        db.collection("gorras")
            .orderBy("precio", Query.Direction.ASCENDING)
            .limit(6)
            .get()
            .addOnSuccessListener { result ->
                binding.progressCarga.visibility = View.GONE
                tendenciasList.clear()
                for (document in result) {
                    tendenciasList.add(document.toObject(Gorra::class.java))
                }
                tendenciasAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { binding.progressCarga.visibility = View.GONE }
    }

    private fun abrirDetalle(gorra: Gorra) {
        val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
        intent.putExtra("EXTRA_NOMBRE", gorra.nombre)
        intent.putExtra("EXTRA_PRECIO", gorra.precio)
        intent.putExtra("EXTRA_DESCRIPCION", gorra.descripcion)
        intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
        startActivity(intent)
    }
}