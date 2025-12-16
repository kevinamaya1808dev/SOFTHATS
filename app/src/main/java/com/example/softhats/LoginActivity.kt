package com.example.softhats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient
    private val db = FirebaseFirestore.getInstance()

    private var passwordVisible = false

    // ===================== SharedPreferences =====================
    private fun guardarUltimoLogin() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("ultimo_login", System.currentTimeMillis()).apply()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // ===================== GOOGLE CONFIG =====================
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        // ===================== PASSWORD TOGGLE =====================
        binding.btnTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etContrasena.inputType =
                if (passwordVisible)
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.btnTogglePassword.setImageResource(
                if (passwordVisible) R.drawable.ic_visibility
                else R.drawable.ic_visibility_off
            )
            binding.etContrasena.setSelection(binding.etContrasena.text.length)
        }

        // ===================== LOGIN EMAIL =====================
        binding.btnLogin.setOnClickListener { loginConEmail() }

        binding.tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // ===================== GOOGLE LOGIN =====================
        binding.btnGoogleSignIn.setOnClickListener {
            auth.signOut()
            googleClient.revokeAccess().addOnCompleteListener {
                launcher.launch(googleClient.signInIntent)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // ❌ NO AUTO LOGIN
    }

    // ===================== LOGIN EMAIL =====================
    private fun loginConEmail() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etContrasena.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                guardarUltimoLogin()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    // ===================== RESULTADO GOOGLE =====================
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                if (account != null) firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al iniciar con Google", Toast.LENGTH_SHORT).show()
            }
        }

    // ===================== GOOGLE AUTH (CONTROL TOTAL) =====================
    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {

        val email = account.email ?: return

        val googleCredential =
            GoogleAuthProvider.getCredential(account.idToken, null)

        // 🔎 Verificar si el correo ya tiene PASSWORD
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->

                val methods = result.signInMethods ?: emptyList()

                // ===================== YA EXISTE PASSWORD =====================
                if (methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                    pedirPasswordYVincular(account, googleCredential)
                    return@addOnSuccessListener
                }

                // ===================== PRIMERA VEZ =====================
                auth.signInWithCredential(googleCredential)
                    .addOnSuccessListener {
                        startActivity(Intent(this, SetPasswordActivity::class.java))
                        finish()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            }
    }

    // ===================== PEDIR CONTRASEÑA =====================
    private fun pedirPasswordYVincular(
        account: GoogleSignInAccount,
        googleCredential: AuthCredential
    ) {
        val input = EditText(this)
        input.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = "Contraseña"

        AlertDialog.Builder(this)
            .setTitle("Verificación requerida")
            .setMessage("Ingresa la contraseña con la que te registraste")
            .setView(input)
            .setCancelable(false)
            .setPositiveButton("Continuar") { _, _ ->

                val password = input.text.toString().trim()
                val email = account.email ?: return@setPositiveButton

                if (password.isEmpty()) {
                    Toast.makeText(this, "Contraseña obligatoria", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {

                        auth.currentUser
                            ?.linkWithCredential(googleCredential)
                            ?.addOnSuccessListener {

                                guardarUltimoLogin()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
