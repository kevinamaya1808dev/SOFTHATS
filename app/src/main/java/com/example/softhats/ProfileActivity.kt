package com.example.softhats
import com.example.softhats.ui.HistorialTicketsActivity


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarDatosUsuario()

        // GUARDAR PERFIL
        binding.btnGuardar.setOnClickListener {
            guardarCambios()
        }

        // 👉 ABRIR HISTORIAL DE TICKETS
        binding.btnHistorialTickets.setOnClickListener {
            startActivity(
                Intent(this, HistorialTicketsActivity::class.java)
            )
        }
    }

    // ================== CARGAR DATOS ==================

    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: return

        // Correo siempre desde Auth
        binding.etEmail.setText(user.email ?: "")

        db.collection("usuarios").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val nombre = doc.getString("nombre")
                    val apP = doc.getString("apellido_paterno")
                    val apM = doc.getString("apellido_materno")
                    val telefono = doc.getString("telefono")

                    binding.etNombre.setText(nombre ?: "")
                    binding.etApellidoP.setText(apP ?: "")
                    binding.etApellidoM.setText(apM ?: "")
                    binding.etTelefono.setText(telefono ?: "")

                    // 🔔 Señalamiento si es Google y faltan datos
                    if (esUsuarioGoogle() && (nombre.isNullOrEmpty() || telefono.isNullOrEmpty())) {
                        Toast.makeText(
                            this,
                            "Completa tu registro para continuar",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    // Documento NO existe (caso Google nuevo)
                    if (esUsuarioGoogle()) {
                        Toast.makeText(
                            this,
                            "Completa tu registro para continuar",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    // ================== GUARDAR ==================

    private fun guardarCambios() {
        val user = auth.currentUser ?: return

        val nombre = binding.etNombre.text.toString().trim()
        val apellidoP = binding.etApellidoP.text.toString().trim()
        val apellidoM = binding.etApellidoM.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        if (nombre.isEmpty() || apellidoP.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(
                this,
                "Completa los campos obligatorios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val datos = hashMapOf(
            "uid" to user.uid,
            "nombre" to nombre,
            "apellido_paterno" to apellidoP,
            "apellido_materno" to apellidoM,
            "telefono" to telefono,
            "email" to email
        )

        db.collection("usuarios").document(user.uid)
            .set(datos, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Perfil actualizado correctamente",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al guardar datos",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ================== GOOGLE ==================

    private fun esUsuarioGoogle(): Boolean {
        val user = auth.currentUser ?: return false
        return user.providerData.any { it.providerId == "google.com" }
    }
}
