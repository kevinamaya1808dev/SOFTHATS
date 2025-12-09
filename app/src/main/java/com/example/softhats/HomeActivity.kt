package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.softhats.database.AppDatabase
import com.example.softhats.database.GorraEntity
import com.example.softhats.databinding.ActivityHomeBinding
import com.example.softhats.network.GorraService
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- LÓGICA DE SINCRONIZACIÓN (OPCIONAL SI YA LA TIENES) ---
        // Si ya tienes la lógica de sincronización en otro lado o no la necesitas aquí, puedes quitar este bloque
        val gorraService = GorraService(this)
        gorraService.obtenerYGuardarGorras(
            onSuccess = { lista: List<GorraEntity> ->
                // Feedback visual opcional
                // Toast.makeText(this, "Sincronizado: ${lista.size} gorras", Toast.LENGTH_SHORT).show()

                val db = AppDatabase.getDatabase(this)
                lifecycleScope.launch {
                    // Lógica espejo: Borrar todo e insertar lo nuevo
                    db.gorraDao().borrarTodo()
                    db.gorraDao().insertarTodas(lista)
                }
            },
            onError = { error: String ->
                Toast.makeText(this, "Error de red: $error", Toast.LENGTH_LONG).show()
            }
        )
        // ------------------------------------------------------------

        // 1. Cargar el HomeFragment por defecto
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment())
        }

        // 2. Configurar la barra inferior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cambiarFragmento(HomeFragment())
                R.id.nav_catalogo -> cambiarFragmento(CatalogoFragment())
                R.id.nav_favoritos -> cambiarFragmento(FavoritosFragment())
                R.id.nav_perfil -> cambiarFragmento(PerfilFragment())
            }
            true
        }

        // 3. Botón del Carrito (Derecha)
        binding.btnCarritoTop.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        // 4. Botón del Mapa (Izquierda) - NUEVO
        binding.btnMapa.setOnClickListener {
            // Asegúrate de que MapsActivity exista (la bajaste con git pull)
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}