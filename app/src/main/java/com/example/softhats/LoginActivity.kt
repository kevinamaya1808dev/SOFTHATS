package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 🔹 Configurar inicio de sesión con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        // 🔹 Siempre cerrar sesión previa para mostrar selector de cuenta
        googleClient.signOut()

        // 🔹 Botón de inicio con Google
        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleClient.signInIntent
            launcher.launch(signInIntent)
        }

        // 🔹 Inicio de sesión manual con correo y contraseña
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etContrasena.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java)) // <-- ASÍ
                            finish()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Ir al registro
        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    // 🔹 Controlador de resultado del intent de Google
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                Log.d("GOOGLE_AUTH", "Cuenta seleccionada: ${account.email}")
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            Log.e("GOOGLE_AUTH", "Error al seleccionar cuenta: ${e.message}")
            Toast.makeText(this, "Error al iniciar sesión con Google", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Autenticación con Firebase usando Google
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GOOGLE_AUTH", "Inicio de sesión correcto: ${account.displayName}")
                    val intent = Intent(this, HomeActivity::class.java) // <-- ASÍ
                    intent.putExtra("user_name", account.displayName)
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("GOOGLE_AUTH", "Error en Firebase: ${task.exception?.message}")
                    Toast.makeText(this, "Error en Firebase", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 🔹 Si el usuario ya está autenticado, se muestra su perfil
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("GOOGLE_AUTH", "Sesión previa detectada: ${currentUser.email}")
            val intent = Intent(this, HomeActivity::class.java) // <-- ASÍ
            intent.putExtra("user_name", currentUser.displayName ?: currentUser.email)
            startActivity(intent)
            finish()
        }
    }
}
