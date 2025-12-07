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

        val gorraService = GorraService(this)
        gorraService.obtenerYGuardarGorras(
            onSuccess = { lista: List<GorraEntity> ->
                Toast.makeText(
                    this,
                    " Se obtuvieron ${lista.size} gorras del servidor.",
                    Toast.LENGTH_LONG
                ).show()

                val db = AppDatabase.getDatabase(this)
                lifecycleScope.launch {
                    db.gorraDao().borrarTodo()
                    db.gorraDao().insertarTodas(lista)
                }
            },
            onError = { error: String ->
                Toast.makeText(this, " Error al obtener datos: $error", Toast.LENGTH_LONG).show()
            }
        )

        // ✅ 3. Cargar el HomeFragment por defecto (solo la primera vez)
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment())
        }

        // ✅ 4. Configurar la barra inferior (Bottom Navigation)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cambiarFragmento(HomeFragment())
                R.id.nav_catalogo -> cambiarFragmento(CatalogoFragment())
                R.id.nav_favoritos -> cambiarFragmento(FavoritosFragment())
                R.id.nav_perfil -> cambiarFragmento(PerfilFragment())
            }
            true
        }

        // ✅ 5. Botón del carrito (abre la actividad Carrito)
        binding.btnCarritoTop.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        // ✅ 6. Botón del mapa (abre la actividad Maps)
        binding.btnMapa.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    // 🔄 Función auxiliar para cambiar fragmentos dinámicamente
    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
