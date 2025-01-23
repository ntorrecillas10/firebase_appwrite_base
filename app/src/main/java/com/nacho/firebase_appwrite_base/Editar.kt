package com.nacho.firebase_appwrite_base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ActivityEditarBinding
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarBinding
    private lateinit var refBD: DatabaseReference
    private lateinit var userId: String
    private lateinit var userAvatar: String

    // Appwrite variables
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String
    private lateinit var identificadorAppWrite: String

    private var url: Uri? = null // Variable para almacenar la URL de la imagen seleccionada

    // Configuración Appwrite
    private val scope = MainScope()

    private val urlGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        handleImageSelection(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeAppwrite()
        initializeUI()


        userId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val userGroup = intent.getStringExtra("userGrupo") ?: ""
        val userRating = intent.getFloatExtra("userRating", 0f)
        val userAvatar = intent.getStringExtra("userAvatar") ?: ""

        binding.nombreEdit.setText(userName)
        binding.grupoEdit.setText(userGroup)
        binding.ratingEdit.rating = userRating

        Glide.with(this)
            .load(userAvatar)
            .into(binding.avatarEdit)

        setUpListeners()

        // Listener para guardar los datos
        binding.guardarButton.setOnClickListener {
            val nombre = binding.nombreEdit.text.toString()
            val grupo = binding.grupoEdit.text.toString()
            val rating = binding.ratingEdit.rating

            if (nombre.isNotEmpty() && grupo.isNotEmpty() && rating != 0f) {
                val updatedUser = Usuario(nombre, grupo, "", rating, userId)

                // Actualizar en Firebase
                refBD.child("usuarios").child(userId).setValue(updatedUser)
                    .addOnSuccessListener {
                        uploadImageToAppwrite()
                        scope.launch(Dispatchers.IO) {
                            storage.deleteFile(
                                bucketId = miBucketId,
                                fileId = userAvatar.split("/")[8]
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initializeAppwrite() {
        miProyectoId = "67586efe0025b764b95d" // ID del proyecto
        miBucketId = "67586f44003e5355a3b7" // ID del bucket

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1")
            .setProject(miProyectoId)

        storage = Storage(appWriteClient)
    }

    private fun initializeUI() {
        refBD = FirebaseDatabase.getInstance().reference
    }


    private fun setUpListeners() {
        binding.volver.setOnClickListener {
            finish()
        }

        // Listener para seleccionar imagen
        binding.avatarEdit.setOnClickListener {
            urlGaleria.launch("image/*")
        }

    }

    private fun handleImageSelection(uri: Uri?) {
        when (uri) {
            null -> Toast.makeText(this, "No has seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            else -> {
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
                url = uri
                binding.avatarEdit.setImageURI(url) // Establecer la imagen en el ImageView
            }
        }
    }

    private fun saveUserData() {

    }

    private fun uploadImageToAppwrite() {
        if (url != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(url!!)
                    val file = inputStream?.let { input ->
                        val tempFile = File.createTempFile(userId, null)
                        input.copyTo(tempFile.outputStream())
                        InputFile.fromFile(tempFile)
                    }

                    if (file != null) {
                        identificadorAppWrite = ID.unique() // Genera un identificador único para la imagen
                        storage.createFile(
                            bucketId = miBucketId,
                            fileId = identificadorAppWrite,
                            file = file
                        )

                        val avatarUrl = "https://cloud.appwrite.io/v1/storage/buckets/$miBucketId/files/$identificadorAppWrite/preview?project=$miProyectoId"
                        val updatedUser = Usuario(
                            binding.nombreEdit.text.toString(),
                            binding.grupoEdit.text.toString(),
                            avatarUrl,
                            binding.ratingEdit.rating,
                            userId
                        )

                        refBD.child("usuarios").child(userId).setValue(updatedUser)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditUserActivity, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditUserActivity, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditUserActivity, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                }
            }
        } else {
            Toast.makeText(this, "No se seleccionó una imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
