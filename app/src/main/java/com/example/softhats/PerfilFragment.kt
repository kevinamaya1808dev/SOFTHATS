package com.example.softhats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.softhats.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private lateinit var binding: FragmentPerfilBinding
    private val auth = FirebaseAuth.getInstance()

    private val avatars = listOf(
        R.drawable.avatara,
        R.drawable.avatarb,
        R.drawable.avatarc,
        R.drawable.avatard,
        R.drawable.avatare,
        R.drawable.avatarf
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPerfilBinding.bind(view)

        val user = auth.currentUser

        if (user == null) {
            // ===== INVITADO =====
            binding.tvUserName.text = "Invitado"
            binding.ivUserProfile.setImageResource(R.drawable.avatara)

            binding.btnLoginRegister.visibility = View.VISIBLE
            binding.btnLogout.visibility = View.GONE
            binding.btnEditUser.visibility = View.GONE
            binding.btnResetPassword.visibility = View.GONE
            binding.btnChangeEmail.visibility = View.GONE
            binding.tvLastLogin.visibility = View.GONE

            binding.btnLoginRegister.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

        } else {
            // ===== USUARIO LOGUEADO =====
            binding.tvUserName.text = user.displayName ?: "Usuario"
            binding.tvUserEmail.text = user.email ?: ""

            binding.btnLoginRegister.visibility = View.GONE
            binding.btnLogout.visibility = View.VISIBLE
            binding.btnEditUser.visibility = View.VISIBLE
            binding.btnResetPassword.visibility = View.VISIBLE
            binding.btnChangeEmail.visibility = View.VISIBLE

            mostrarUltimoLogin()

            // Avatar aleatorio SOLO EN SESIÓN
            binding.ivUserProfile.setImageResource(avatars.random())

            binding.cardProfile.setOnClickListener {
                mostrarDialogoAvatares()
            }

            binding.btnLogout.setOnClickListener {
                cerrarSesion()
            }

            binding.btnResetPassword.setOnClickListener {
                startActivity(Intent(requireContext(), ForgotPasswordActivity::class.java))
            }

            binding.btnChangeEmail.setOnClickListener {
                startActivity(Intent(requireContext(), ChangeEmailActivity::class.java))
            }
        }
    }

    private fun mostrarDialogoAvatares() {
        val nombres = arrayOf("Avatar 1", "Avatar 2", "Avatar 3", "Avatar 4", "Avatar 5", "Avatar 6")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Elige tu avatar")
            .setItems(nombres) { _, which ->
                binding.ivUserProfile.setImageResource(avatars[which])
            }
            .show()
    }

    private fun mostrarUltimoLogin() {
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val time = prefs.getLong("ultimo_login", 0L)

        if (time > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvLastLogin.text = "Último inicio: ${sdf.format(Date(time))}"
            binding.tvLastLogin.visibility = View.VISIBLE
        }
    }

    private fun cerrarSesion() {
        auth.signOut()

        // Refrescar el fragment correctamente
        parentFragmentManager.beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
    }
}
