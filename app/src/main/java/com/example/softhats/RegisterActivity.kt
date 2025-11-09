package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // 🔹 ViewBinding para acceder a los elementos del XML
    private lateinit var binding: ActivityRegisterBinding

    // 🔹 Instancia de Firebase Authentication
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Acción del botón de registro
        binding.btnRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // 🔹 Validaciones básicas
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Validación de formato de correo electrónico
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "El correo no tiene un formato válido", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Validación de longitud de contraseña
        if (password.length < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔹 Crear usuario en Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuario creado correctamente
                    Toast.makeText(this, "✅ Registro exitoso", Toast.LENGTH_SHORT).show()

                    // Redirigir al login
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    // Mostrar error devuelto por Firebase
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    Toast.makeText(this, "❌ Error: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }
}
