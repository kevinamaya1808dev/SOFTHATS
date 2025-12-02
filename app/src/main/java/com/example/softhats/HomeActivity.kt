package com.example.softhats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.softhats.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Cargar el HomeFragment por defecto al abrir la app
        // (Solo si es la primera vez que se abre, para no recargar si rotas la pantalla)
        if (savedInstanceState == null) {
            cambiarFragmento(HomeFragment())
        }

        // 2. Configurar los clics de la barra inferior (Bottom Navigation)
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cambiarFragmento(HomeFragment())
                R.id.nav_catalogo -> cambiarFragmento(CatalogoFragment())
                R.id.nav_favoritos -> cambiarFragmento(FavoritosFragment())
                R.id.nav_perfil -> cambiarFragmento(PerfilFragment())
            }
            true
        }

        // 3. Configurar el botón del Carrito (el icono de arriba a la derecha)
        binding.btnCarritoTop.setOnClickListener {
            val intent = Intent(this, CarritoActivity::class.java)
            startActivity(intent)
        }

        //Boton de mapa
        binding.btnMapa.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

    }

    // Función auxiliar para cambiar el contenido de la pantalla
    private fun cambiarFragmento(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}