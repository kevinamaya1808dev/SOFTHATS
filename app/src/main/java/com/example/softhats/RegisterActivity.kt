package com.example.softhats

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.softhats.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var prefs: SharedPreferences

    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        // =======================
        // SPINNER DE LADAS
        // =======================
        val ladas = resources.getStringArray(R.array.ladas)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ladas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spLada.adapter = adapter

        binding.spLada.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val maxLength = when (position) {
                    0 -> 10 // 🇲🇽 +52
                    1 -> 10 // 🇺🇸 +1
                    2 -> 9  // 🇨🇱 +56
                    3 -> 10 // 🇦🇷 +54
                    else -> 10
                }

                binding.etTelefono.filters = arrayOf(InputFilter.LengthFilter(maxLength))
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // =======================
        // MOSTRAR / OCULTAR CONTRASEÑAS
        // =======================
        binding.btnTogglePassword.setOnClickListener { togglePasswordVisibility() }
        binding.btnToggleConfirmPassword.setOnClickListener { toggleConfirmPasswordVisibility() }

        // =======================
        // BARRA DE SEGURIDAD
        // =======================
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateStrength(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, i: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, i: Int, b: Int, c: Int) {}
        })

        // =======================
        // BOTÓN REGISTRAR
        // =======================
        binding.btnRegister.setOnClickListener { registerUser() }
    }

    // =======================
    // FUNCIÓN MOSTRAR / OCULTAR CONTRASEÑA
    // =======================
    private fun togglePasswordVisibility() {
        val edit = binding.etPassword
        if (isPasswordVisible) {
            edit.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            edit.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility)
        }
        edit.setSelection(edit.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun toggleConfirmPasswordVisibility() {
        val edit = binding.etConfirmPassword
        if (isConfirmPasswordVisible) {
            edit.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off)
        } else {
            edit.transformationMethod = HideReturnsTransformationMethod.getInstance()
            binding.btnToggleConfirmPassword.setImageResource(R.drawable.ic_visibility)
        }
        edit.setSelection(edit.text.length)
        isConfirmPasswordVisible = !isConfirmPasswordVisible
    }

    // =======================
    // CALCULAR SEGURIDAD DE CONTRASEÑA
    // =======================
    private fun updateStrength(password: String) {
        var score = 0

        if (password.length >= 6) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        when (score) {
            0, 1 -> {
                binding.tvPasswordStrength.text = "Seguridad: débil"
                binding.tvPasswordStrength.setTextColor(0xFFFF4444.toInt())
            }
            2 -> {
                binding.tvPasswordStrength.text = "Seguridad: media"
                binding.tvPasswordStrength.setTextColor(0xFFFFC107.toInt())
            }
            else -> {
                binding.tvPasswordStrength.text = "Seguridad: fuerte"
                binding.tvPasswordStrength.setTextColor(0xFF4CAF50.toInt())
            }
        }
    }

    // =======================
    // REGISTRAR USUARIO + GUARDAR EN FIRESTORE
    // =======================
    private fun registerUser() {

        val nombre = binding.etNombre.text.toString().trim()
        val apP = binding.etApellidoPaterno.text.toString().trim()
        val apM = binding.etApellidoMaterno.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString().trim()
        val confirm = binding.etConfirmPassword.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        // EXTRAER LADA SIN BANDERA → solo +52
        val lada = binding.spLada.selectedItem.toString().substringAfter(" ")

        // Validaciones
        if (nombre.isEmpty() || apP.isEmpty() || apM.isEmpty() ||
            email.isEmpty() || pass.isEmpty() || confirm.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (pass != confirm) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }

        // =======================
        // CREAR USUARIO EN AUTH
        // =======================
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->

                val uid = result.user!!.uid

                val userData = hashMapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "apellido_paterno" to apP,
                    "apellido_materno" to apM,
                    "email" to email,
                    "lada" to lada,
                    "telefono" to telefono
                )

                db.collection("usuarios")
                    .document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar en Firestore", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
}
