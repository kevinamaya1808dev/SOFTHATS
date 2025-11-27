package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.softhats.databinding.FragmentCatalogoBinding
import com.google.firebase.firestore.FirebaseFirestore
import android.text.Editable
import android.text.TextWatcher

class CatalogoFragment : Fragment() {

    private lateinit var binding: FragmentCatalogoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var gorraArrayList: ArrayList<Gorra>
    private lateinit var gorraAdapter: GorraAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar el RecyclerView (Lista de 2 columnas)
        binding.recyclerViewGorras.layoutManager = GridLayoutManager(requireContext(), 2)

        gorraArrayList = ArrayList()
        gorraAdapter = GorraAdapter(requireContext(), gorraArrayList)

        // =========================================================================
        // CONFIGURACIÓN DE LOS CLICS (Eventos del Adapter)
        // =========================================================================

        // A) Clic en toda la tarjeta -> Ir a Detalle
        gorraAdapter.onItemClick = { gorra ->
            val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
            intent.putExtra("EXTRA_NOMBRE", gorra.nombre)
            intent.putExtra("EXTRA_PRECIO", gorra.precio)
            intent.putExtra("EXTRA_DESCRIPCION", gorra.descripcion)
            intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
            startActivity(intent)
        }

        // B) Clic en Favorito
        gorraAdapter.onFavoriteClick = { gorra ->
            Toast.makeText(requireContext(), "❤️ ${gorra.nombre} añadido a Favoritos", Toast.LENGTH_SHORT).show()
        }

        // C) Clic en Cámara (AR)
        gorraAdapter.onCameraClick = { gorra ->
            Toast.makeText(requireContext(), "📸 Cargando probador para ${gorra.nombre}...", Toast.LENGTH_SHORT).show()
            // TODO: Descomentar esto cuando tengas tu Activity de AR creada
            // val intent = Intent(requireContext(), ArActivity::class.java)
            // intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
            // startActivity(intent)
        }

        // D) Clic en Carrito Rápido
        gorraAdapter.onCartClick = { gorra ->
            Toast.makeText(requireContext(), "🛒 ¡${gorra.nombre} agregado al carrito!", Toast.LENGTH_SHORT).show()
        }

        binding.recyclerViewGorras.adapter = gorraAdapter

        // =========================================================================
        // 5. LÓGICA DE BÚSQUEDA EN TIEMPO REAL (NUEVO)
        // =========================================================================
        binding.etSearchGorra.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Llamamos a la función filtrar que creamos en el Adapter
                gorraAdapter.filtrar(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 3. Inicializar Firebase y cargar
        db = FirebaseFirestore.getInstance()
        cargarDatosDeFirebase()

        // 4. Configurar botones de la Barra Superior
        configurarBarraSuperior()
    }

    private fun configurarBarraSuperior() {
        binding.btnFilter.setOnClickListener {
            Toast.makeText(requireContext(), "Abrir filtros...", Toast.LENGTH_SHORT).show()
        }

        binding.btnSort.setOnClickListener {
            Toast.makeText(requireContext(), "Ordenar lista...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarDatosDeFirebase() {
        binding.progressBar.visibility = View.VISIBLE

        db.collection("gorras")
            .get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE
                gorraArrayList.clear()

                for (document in result) {
                    val gorra = document.toObject(Gorra::class.java)
                    gorraArrayList.add(gorra)
                }

                // =========================================================
                // CAMBIO IMPORTANTE: Guardar copia original para la búsqueda
                // =========================================================
                // En lugar de solo notifyDataSetChanged(), usamos nuestra función especial
                gorraAdapter.actualizarDatosOriginales(gorraArrayList)
            }
            .addOnFailureListener { exception ->
                binding.progressBar.visibility = View.GONE
                Log.e("CatalogoFragment", "Error al traer gorras: ", exception)
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }
}