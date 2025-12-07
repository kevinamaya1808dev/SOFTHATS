package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.softhats.database.GorraEntity
import com.example.softhats.databinding.ActivityHomeBinding
import com.example.softhats.network.GorraService

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ 1. Llamar al backend para obtener las gorras
        val gorraService = GorraService(this)
        gorraService.obtenerGorras(
            onSuccess = { lista ->
                Toast.makeText(
                    this,
                    "Se obtuvieron ${lista.size} gorras del servidor.",
                    Toast.LENGTH_LONG
                ).show()

                // ⚙️ Si quieres guardarlas en Room (opcional)
                // val db = AppDatabase.getDatabase(this)
                // lifecycleScope.launch {
                //     db.gorraDao().borrarTodo()
                //     db.gorraDao().insertarTodas(lista)
                // }

            },
            onError = { error ->
                Toast.makeText(this, "Error al obtener datos: $error", Toast.LENGTH_LONG).show()
            }
        )

        // 2. Cargar el HomeFragment por defecto
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment())
        }

        // 3. Configurar la barra inferior (Bottom Navigation)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cambiarFragmento(HomeFragment())
                R.id.nav_catalogo -> cambiarFragmento(CatalogoFragment())
                R.id.nav_favoritos -> cambiarFragmento(FavoritosFragment())
                R.id.nav_perfil -> cambiarFragmento(PerfilFragment())
            }
            true
        }

        // 4. Botón del carrito
        binding.btnCarritoTop.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        // 5. Botón del mapa
        binding.btnMapa.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    // 🔄 Función auxiliar para cambiar el contenido de la pantalla
    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
