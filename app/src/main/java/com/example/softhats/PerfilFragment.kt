package com.example.softhats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.GridView
import androidx.fragment.app.Fragment
import com.example.softhats.databinding.FragmentPerfilBinding
import com.example.softhats.ui.profile.AvatarAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PerfilFragment : Fragment(R.layout.fragment_perfil) {

    private lateinit var binding: FragmentPerfilBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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

        // Cargar avatar (invitado / local / Firestore)
        cargarAvatar()

        if (user == null) {
            // ================= INVITADO =================
            binding.tvUserName.text = "Invitado"
            binding.tvUserEmail.text = ""

            binding.btnLoginRegister.visibility = View.VISIBLE
            binding.btnRegister.visibility = View.VISIBLE

            binding.btnLogout.visibility = View.GONE
            binding.btnEditUser.visibility = View.GONE
            binding.btnResetPassword.visibility = View.GONE
            binding.btnChangeEmail.visibility = View.GONE
            binding.tvLastLogin.visibility = View.GONE

            binding.btnLoginRegister.setOnClickListener {
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }

            binding.btnRegister.setOnClickListener {
                startActivity(Intent(requireContext(), RegisterActivity::class.java))
            }

        } else {
            // ================= USUARIO LOGUEADO =================
            binding.tvUserName.text = user.displayName ?: "Usuario"
            binding.tvUserEmail.text = user.email ?: ""

            binding.btnLoginRegister.visibility = View.GONE
            binding.btnRegister.visibility = View.GONE

            binding.btnLogout.visibility = View.VISIBLE
            binding.btnEditUser.visibility = View.VISIBLE
            binding.btnResetPassword.visibility = View.VISIBLE
            binding.btnChangeEmail.visibility = View.VISIBLE

            mostrarUltimoLogin()

            // Cambiar avatar
            binding.cardProfile.setOnClickListener {
                mostrarDialogoAvatares()
            }

            // 🔹 Editar perfil → ActivityProfile
            binding.btnEditUser.setOnClickListener {
                startActivity(
                    Intent(requireContext(), ProfileActivity::class.java)
                )
            }

            // 🔹 Restablecer contraseña → ForgotPasswordActivity
            binding.btnResetPassword.setOnClickListener {
                startActivity(
                    Intent(requireContext(), ForgotPasswordActivity::class.java)
                )
            }

            // 🔹 Cambiar correo electrónico → ChangeEmailActivity
            binding.btnChangeEmail.setOnClickListener {
                startActivity(
                    Intent(requireContext(), ChangeEmailActivity::class.java)
                )
            }

            // Cerrar sesión
            binding.btnLogout.setOnClickListener {
                cerrarSesion()
            }
        }
    }

    // ===================== AVATAR =====================

    private fun mostrarDialogoAvatares() {
        val gridView = GridView(requireContext())
        gridView.numColumns = 3
        gridView.adapter = AvatarAdapter(requireContext(), avatars)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Elige tu avatar")
            .setView(gridView)
            .create()

        gridView.setOnItemClickListener { _, _, position, _ ->
            val selectedAvatar = avatars[position]
            binding.ivUserProfile.setImageResource(selectedAvatar)
            guardarAvatar(selectedAvatar)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarAvatar(resId: Int) {
        val user = auth.currentUser ?: return
        val avatarName = resources.getResourceEntryName(resId)

        // Firestore (nube)
        firestore.collection("users")
            .document(user.uid)
            .set(mapOf("avatar" to avatarName))

        // Cache local
        val prefs = requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("avatar_${user.uid}", avatarName)
            .apply()
    }

    private fun cargarAvatar() {
        val user = auth.currentUser

        // Invitado
        if (user == null) {
            binding.ivUserProfile.setImageResource(R.drawable.avatarinvitado)
            return
        }

        val prefs = requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // Cache local
        val localAvatar = prefs.getString("avatar_${user.uid}", null)
        if (localAvatar != null) {
            val resId = resources.getIdentifier(
                localAvatar,
                "drawable",
                requireContext().packageName
            )
            if (resId != 0) binding.ivUserProfile.setImageResource(resId)
        }

        // Firestore (sincronización)
        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                val avatarName = doc.getString("avatar") ?: return@addOnSuccessListener
                val resId = resources.getIdentifier(
                    avatarName,
                    "drawable",
                    requireContext().packageName
                )
                if (resId != 0) {
                    binding.ivUserProfile.setImageResource(resId)
                    prefs.edit()
                        .putString("avatar_${user.uid}", avatarName)
                        .apply()
                }
            }
    }

    // ===================== ÚLTIMO LOGIN =====================

    private fun mostrarUltimoLogin() {
        val prefs = requireContext()
            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val time = prefs.getLong("ultimo_login", 0L)
        if (time > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvLastLogin.text = "Último inicio: ${sdf.format(Date(time))}"
            binding.tvLastLogin.visibility = View.VISIBLE
        }
    }

    // ===================== SESIÓN =====================

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(requireContext(), HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
