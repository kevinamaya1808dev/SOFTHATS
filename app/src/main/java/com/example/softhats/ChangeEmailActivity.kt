package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityChangeEmailBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeEmailBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        if (user == null) {
            finish()
            return
        }

        // Mostrar correo actual
        binding.tvEmailActualValue.text = user.email ?: ""

        binding.btnBack.setOnClickListener { finish() }

        binding.btnSaveEmail.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()
            val newEmail = binding.etNewEmail.text.toString().trim()
            val confirmNewEmail = binding.etConfirmNewEmail.text.toString().trim()

            if (newEmail.isEmpty() || confirmNewEmail.isEmpty()) {
                toast("Ingresa y confirma el nuevo correo")
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                toast("Correo inválido")
                return@setOnClickListener
            }

            if (newEmail != confirmNewEmail) {
                toast("Los correos no coinciden")
                return@setOnClickListener
            }

            // Confirmación
            AlertDialog.Builder(this)
                .setTitle("Confirmar cambio")
                .setMessage(
                    "¿Estás seguro de cambiar tu correo?\n\n" +
                            "Te enviaremos un correo al nuevo email para confirmarlo y se cerrará tu sesión."
                )
                .setPositiveButton("Sí, cambiar") { _, _ ->
                    procesarCambioCorreo(password, newEmail)
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun procesarCambioCorreo(password: String, newEmail: String) {
        val user = auth.currentUser ?: return
        val emailActual = user.email ?: run {
            toast("No se encontró el correo actual")
            return
        }

        // Detectar proveedor
        val isGoogleUser = user.providerData.any { it.providerId == "google.com" }

        if (isGoogleUser) {
            // ✅ Para Google: verifyBeforeUpdateEmail (NO requiere contraseña de la app)
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnSuccessListener {
                    toast("Revisa tu correo NUEVO para confirmar el cambio.")
                    cerrarSesionYVolverLogin()
                }
                .addOnFailureListener {
                    toast(it.message ?: "No se pudo iniciar el cambio de correo")
                }
            return
        }

        // ✅ Para Email/Password: Reautenticar con contraseña
        if (password.isEmpty()) {
            toast("Ingresa tu contraseña")
            return
        }

        val credential = EmailAuthProvider.getCredential(emailActual, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Recomendado también aquí: verifyBeforeUpdateEmail (flujo seguro)
                user.verifyBeforeUpdateEmail(newEmail)
                    .addOnSuccessListener {

                        // (Opcional) Actualizar Firestore "pendiente" (no obligatorio)
                        db.collection("usuarios").document(user.uid)
                            .update(
                                mapOf(
                                    "email_pending" to newEmail
                                )
                            )

                        toast("Revisa tu correo NUEVO para confirmar el cambio.")
                        cerrarSesionYVolverLogin()
                    }
                    .addOnFailureListener {
                        toast(it.message ?: "No se pudo iniciar el cambio de correo")
                    }
            }
            .addOnFailureListener {
                toast("Contraseña incorrecta")
            }
    }

    private fun cerrarSesionYVolverLogin() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
