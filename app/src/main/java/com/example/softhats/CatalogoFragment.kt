package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.softhats.databinding.FragmentCatalogoBinding // Asegúrate que este nombre coincida con tu XML
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class CatalogoFragment : Fragment() {

    private var _binding: FragmentCatalogoBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore

    // Usamos dos listas: una para mostrar y otra de respaldo para las búsquedas
    private lateinit var gorraArrayList: ArrayList<Gorra>
    private lateinit var listaOriginal: ArrayList<Gorra>

    private lateinit var gorraAdapter: GorraAdapter

    // Variable para saber si ordenamos de menor a mayor o al revés
    private var esPrecioAscendente = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Configurar Listas
        gorraArrayList = ArrayList()
        listaOriginal = ArrayList() // Inicializamos la lista de respaldo

        // 2. Configurar RecyclerView (Grid de 2 columnas)
        binding.rvGorras.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvGorras.setHasFixedSize(true)

        gorraAdapter = GorraAdapter(requireContext(), gorraArrayList)
        binding.rvGorras.adapter = gorraAdapter

        // 3. Configurar Clic en Gorra (Ir a Detalle)
        gorraAdapter.onItemClick = { gorra ->
            val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
            intent.putExtra("EXTRA_NOMBRE", gorra.nombre)
            intent.putExtra("EXTRA_PRECIO", gorra.precio)
            intent.putExtra("EXTRA_DESCRIPCION", gorra.descripcion)
            intent.putExtra("EXTRA_IMAGEN", gorra.imagen_nombre)
            startActivity(intent)
        }

        // 4. Configurar el Buscador (Search Bar)
        setupBuscador()

        // 5. Configurar Botón de Ordenar (Flechas)
        binding.btnOrdenar.setOnClickListener {
            ordenarPorPrecio()
        }

        // 6. Cargar datos de Firebase
        db = FirebaseFirestore.getInstance()
        cargarDatosDeFirebase()
    }

    private fun cargarDatosDeFirebase() {
        // Nota: Si en el nuevo XML no pusiste ProgressBar, borra las líneas de binding.progressBar
        // binding.progressBar.visibility = View.VISIBLE

        db.collection("gorras")
            .get()
            .addOnSuccessListener { result ->
                // binding.progressBar.visibility = View.GONE
                gorraArrayList.clear()
                listaOriginal.clear()

                for (document in result) {
                    // Convertimos el documento a objeto Gorra
                    val gorra = document.toObject(Gorra::class.java)

                    // Agregamos a AMBAS listas
                    gorraArrayList.add(gorra)
                    listaOriginal.add(gorra)
                }

                gorraAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // binding.progressBar.visibility = View.GONE
                Log.e("CatalogoFragment", "Error: ", exception)
                Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarLista(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filtrarLista(texto: String) {
        val textoBusqueda = texto.lowercase(Locale.ROOT)
        gorraArrayList.clear()

        if (textoBusqueda.isEmpty()) {
            // Si no hay texto, mostramos TODAS las gorras originales
            gorraArrayList.addAll(listaOriginal)
        } else {
            // Si hay texto, filtramos
            for (item in listaOriginal) {
                if (item.nombre?.lowercase(Locale.ROOT)?.contains(textoBusqueda) == true) {
                    gorraArrayList.add(item)
                }
            }
        }
        gorraAdapter.notifyDataSetChanged()
    }

    private fun ordenarPorPrecio() {
        if (esPrecioAscendente) {
            // Ordenar de Mayor a Menor (Descendente)
            gorraArrayList.sortByDescending { it.precio }
            Toast.makeText(context, "Precio: Mayor a Menor", Toast.LENGTH_SHORT).show()
        } else {
            // Ordenar de Menor a Mayor (Ascendente)
            gorraArrayList.sortBy { it.precio }
            Toast.makeText(context, "Precio: Menor a Mayor", Toast.LENGTH_SHORT).show()
        }

        // Invertimos la variable para la próxima vez que toque el botón
        esPrecioAscendente = !esPrecioAscendente
        gorraAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}