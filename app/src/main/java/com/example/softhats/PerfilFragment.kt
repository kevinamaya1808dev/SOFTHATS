package com.example.softhats

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.softhats.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class PerfilFragment : Fragment() {

    private lateinit var binding: FragmentPerfilBinding
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val PICK_IMAGE = 200
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // 🟣 Registrar último inicio
        registrarUltimoIngreso()

        // 🟣 Mostrar datos del usuario
        if (user != null) {
            val email = user.email ?: "Correo no disponible"
            val nombre = user.displayName ?: ""

            // Si inició con Google y falta completar datos
            if (nombre.isNotEmpty()) {
                binding.tvUserEmail.text = nombre
            } else {
                binding.tvUserEmail.text = email
            }

            cargarFotoUsuario(user.uid)
        }

        // 🟣 Elegir nueva foto de perfil
        binding.cardProfile.setOnClickListener {
            seleccionarFoto()
        }

        // 🟣 Botón EDITAR PERFIL → abre ProfileActivity
        binding.btnEditUser.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        // 🟣 Botón Cerrar Sesión
        binding.btnLogout.setOnClickListener {
            cerrarSesion()
        }
    }

    // --------------------------------------------------------------------
    // 🟣 Seleccionar foto desde galería
    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    // --------------------------------------------------------------------
    // 🟣 Resultado de selección de foto
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data

            binding.ivUserProfile.setImageURI(imageUri)

            auth.currentUser?.let {
                subirFotoAFirebase(it.uid)
            }
        }
    }

    // --------------------------------------------------------------------
    // 🟣 Subir foto al Storage y guardar URL en Firestore
    private fun subirFotoAFirebase(uid: String) {
        val ref = storage.reference.child("usuarios/$uid/perfil.jpg")

        imageUri?.let { uri ->
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        db.collection("usuarios").document(uid)
                            .update("foto", url.toString())
                    }
                }
        }
    }

    // --------------------------------------------------------------------
    // 🟣 Cargar foto desde Firestore
    private fun cargarFotoUsuario(uid: String) {
        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                val url = doc.getString("foto")

                if (!url.isNullOrEmpty()) {
                    Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.ic_user_placeholder)
                        .into(binding.ivUserProfile)
                }
            }
    }

    // --------------------------------------------------------------------
    // 🟣 Registrar último ingreso
    private fun registrarUltimoIngreso() {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("ultimo_ingreso", System.currentTimeMillis()).apply()
    }

    // --------------------------------------------------------------------
    // 🟣 Cerrar sesión
    private fun cerrarSesion() {
        auth.signOut()

        // Limpia SharedPreferences
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Redirige a Login
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
