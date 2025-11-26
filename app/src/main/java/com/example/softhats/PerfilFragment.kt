package com.example.softhats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.softhats.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth

class PerfilFragment : Fragment() {

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inicializar Firebase Auth para saber quién está conectado
        auth = FirebaseAuth.getInstance()

        // 2. Obtener al usuario actual y mostrar su correo
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.tvUserEmail.text = currentUser.email
        } else {
            binding.tvUserEmail.text = "Invitado"
        }

        // 3. Configurar el botón rojo de Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            // A) Cerrar sesión en Firebase
            auth.signOut()

            // B) LIMPIAR SHAREDPREFERENCES
            // Esto es vital para que el Módulo 1 sepa que ya no debe recordar al usuario
            // CAMBIA "nombre_de_tu_preferencia" POR EL NOMBRE REAL QUE USASTE EN LOGINACTIVITY
            val sharedPref = requireActivity().getSharedPreferences("nombre_de_tu_preferencia", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                clear() // Borra todos los datos guardados (token, booleano, etc.)
                apply() // Aplica los cambios
            }

            // C) Regresar al usuario a la pantalla de Login
            val intent = Intent(requireContext(), LoginActivity::class.java)
            // Estas banderas borran el historial para que no pueda regresar con la flecha "Atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}