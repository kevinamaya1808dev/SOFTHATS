package com.example.softhats

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.softhats.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val PICK_IMAGE = 200
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        cargarDatosUsuario()

        binding.btnChangePhoto.setOnClickListener { seleccionarFoto() }

        binding.btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser ?: return

        db.collection("usuarios").document(user.uid)
            .get()
            .addOnSuccessListener { doc ->

                binding.etNombre.setText(doc.getString("nombre"))
                binding.etApellidoP.setText(doc.getString("apellido_paterno"))
                binding.etApellidoM.setText(doc.getString("apellido_materno"))
                binding.etEmail.setText(user.email)
                binding.etTelefono.setText(doc.getString("telefono"))

                val foto = doc.getString("foto")
                if (!foto.isNullOrEmpty()) {
                    Glide.with(this).load(foto).into(binding.ivUserProfile)
                }
            }
    }

    private fun seleccionarFoto() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)

        if (req == PICK_IMAGE && res == Activity.RESULT_OK) {
            imageUri = data?.data
            binding.ivUserProfile.setImageURI(imageUri)
        }
    }

    private fun guardarCambios() {
        val user = auth.currentUser ?: return

        val nombre = binding.etNombre.text.toString().trim()
        val apP = binding.etApellidoP.text.toString().trim()
        val apM = binding.etApellidoM.text.toString().trim()
        val emailNuevo = binding.etEmail.text.toString().trim()
        val telefono = binding.etTelefono.text.toString().trim()

        val datosActualizados = mapOf(
            "nombre" to nombre,
            "apellido_paterno" to apP,
            "apellido_materno" to apM,
            "telefono" to telefono
        )

        // Guardar datos en Firestore
        db.collection("usuarios").document(user.uid)
            .update(datosActualizados)
            .addOnSuccessListener {

                // Cambió correo → enviar email de verificación
                if (emailNuevo != user.email) {
                    user.updateEmail(emailNuevo).addOnSuccessListener {
                        user.sendEmailVerification()
                        Toast.makeText(this, "Correo actualizado. Revisa tu bandeja.", Toast.LENGTH_LONG).show()
                    }
                }

                // Guardar foto
                if (imageUri != null) subirFotoAStorage(user.uid)

                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun subirFotoAStorage(uid: String) {
        val ref = FirebaseStorage.getInstance().reference.child("usuarios/$uid/perfil.jpg")

        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    db.collection("usuarios").document(uid)
                        .update("foto", url.toString())
                }
            }
    }
}
