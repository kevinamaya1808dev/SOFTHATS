package com.example.softhats

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivitySetPasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetPasswordBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var showPassword = false
    private var showConfirm = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.btnTogglePassword.setOnClickListener { togglePassword() }
        binding.btnToggleConfirmPassword.setOnClickListener { toggleConfirm() }

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateStrength(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, i: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, i: Int, b: Int, c: Int) {}
        })

        binding.btnSavePassword.setOnClickListener { guardarPassword() }
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Debes crear una contraseña para continuar", Toast.LENGTH_SHORT).show()
    }

    private fun guardarPassword() {
        val password = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()

        if (password.length < 6) {
            toast("Mínimo 6 caracteres")
            return
        }

        if (password != confirm) {
            toast("Las contraseñas no coinciden")
            return
        }

        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val credential = EmailAuthProvider.getCredential(email, password)

        user.linkWithCredential(credential)
            .addOnSuccessListener {

                db.collection("usuarios").document(user.uid)
                    .set(
                        mapOf(
                            "email" to email,
                            "password_set" to true
                        ),
                        com.google.firebase.firestore.SetOptions.merge()
                    )

                Toast.makeText(this, "Contraseña creada", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                toast(it.message ?: "Error al crear contraseña")
            }
    }

    private fun togglePassword() {
        showPassword = !showPassword
        binding.etPassword.transformationMethod =
            if (showPassword)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
        binding.etPassword.setSelection(binding.etPassword.text.length)
    }

    private fun toggleConfirm() {
        showConfirm = !showConfirm
        binding.etConfirmPassword.transformationMethod =
            if (showConfirm)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text.length)
    }

    private fun updateStrength(password: String) {
        binding.tvPasswordStrength.text =
            when {
                password.length < 6 -> "Seguridad: débil"
                password.any { it.isUpperCase() } && password.any { it.isDigit() } -> "Seguridad: fuerte"
                else -> "Seguridad: media"
            }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
