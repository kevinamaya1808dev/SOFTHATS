package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.softhats.database.AppDatabase
import com.example.softhats.databinding.FragmentFavoritosBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class FavoritosFragment : Fragment() {

    private lateinit var binding: FragmentFavoritosBinding
    private lateinit var favoritosAdapter: FavoritosAdapter // Declaramos el adaptador
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar Base de Datos
        db = AppDatabase.getDatabase(requireContext())

        // 2. Inicializar y Configurar el RecyclerView (Lista)
        favoritosAdapter = FavoritosAdapter() // ¡Importante! Creamos la instancia aquí

        // --- AQUÍ ESTÁ LA CONFIGURACIÓN DEL CLIC ---
        favoritosAdapter.onItemClick = { favorito ->
            // Al hacer clic en una gorra favorita, abrimos el detalle
            val intent = Intent(requireContext(), DetalleGorraActivity::class.java)
            intent.putExtra("EXTRA_NOMBRE", favorito.nombre)
            intent.putExtra("EXTRA_PRECIO", favorito.precio)
            intent.putExtra("EXTRA_DESCRIPCION", favorito.descripcion)
            intent.putExtra("EXTRA_IMAGEN", favorito.imagenNombre)
            startActivity(intent)
        }
        // ------------------------------------------

        binding.rvFavoritos.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // 2 columnas
            adapter = favoritosAdapter // Conectamos el adaptador a la lista
        }

        // 3. OBSERVAR DATOS DE LA BD EN TIEMPO REAL
        lifecycleScope.launch {
            // Usamos el Flow del DAO para recibir actualizaciones automáticas
            // Si agregas o quitas un favorito, esta lista se actualiza sola
            db.favoritoDao().obtenerTodos().collect { listaFavoritos ->

                // Le pasamos la lista nueva al adaptador
                favoritosAdapter.submitList(listaFavoritos)

                // Lógica visual: ¿Mostramos lista o mensaje de vacío?
                if (listaFavoritos.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE // Muestra "No tienes favoritos"
                    binding.rvFavoritos.visibility = View.GONE    // Oculta la lista vacía
                } else {
                    binding.layoutEmpty.visibility = View.GONE    // Oculta el mensaje
                    binding.rvFavoritos.visibility = View.VISIBLE // Muestra la lista con gorras
                }
            }
        }

        // 4. Configurar el botón "Explorar Catálogo"
        // (Para cuando la lista está vacía y el usuario quiere ir a buscar gorras)
        binding.btnIrCatalogo.setOnClickListener {
            val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
            bottomNav.selectedItemId = R.id.nav_catalogo
        }
    }
}