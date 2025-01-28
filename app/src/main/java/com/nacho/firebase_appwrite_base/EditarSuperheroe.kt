package com.nacho.firebase_appwrite_base

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ActivityEditarSuperheroeBinding
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EditarSuperheroe : AppCompatActivity() {

    private lateinit var binding: ActivityEditarSuperheroeBinding
    private lateinit var refBD: DatabaseReference
    private lateinit var superheroeId: String

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
        binding = ActivityEditarSuperheroeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeAppwrite()
        initializeUI()

        superheroeId = intent.getStringExtra("superheroeId") ?: ""
        val superheroeName = intent.getStringExtra("superheroeName") ?: ""
        val superheroeGroup = intent.getStringExtra("superheroeGrupo") ?: ""
        val superheroeRating = intent.getFloatExtra("superheroeRating", 0f)
        val superheroeAvatar = intent.getStringExtra("superheroeAvatar") ?: ""

        binding.nombreEdit.setText(superheroeName)
        binding.grupoEdit.setText(superheroeGroup)
        binding.ratingEdit.rating = superheroeRating

        Glide.with(this)
            .load(superheroeAvatar)
            .into(binding.avatarEdit)

        setUpListeners()

        // Listener para guardar los datos
        binding.guardarButton.setOnClickListener {
            val nombre = binding.nombreEdit.text.toString()
            val grupo = binding.grupoEdit.text.toString()
            val rating = binding.ratingEdit.rating

            if (nombre.isNotEmpty() && grupo.isNotEmpty() && rating != 0f) {
                val updatedSuperheroe = Superheroe(nombre, grupo, "", rating, superheroeId)

                // Actualizar en Firebase
                refBD.child("superheroes").child(superheroeId).setValue(updatedSuperheroe)
                    .addOnSuccessListener {
                        uploadImageToAppwrite()
                        scope.launch(Dispatchers.IO) {
                            storage.deleteFile(
                                bucketId = miBucketId,
                                fileId = superheroeAvatar.split("/")[8]
                            )
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar superheroe", Toast.LENGTH_SHORT).show()
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

    private fun uploadImageToAppwrite() {
        if (url != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val inputStream = contentResolver.openInputStream(url!!)
                    val file = inputStream?.let { input ->
                        val tempFile = File.createTempFile(superheroeId, null)
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
                        val updatedSuperheroe = Superheroe(
                            binding.nombreEdit.text.toString(),
                            binding.grupoEdit.text.toString(),
                            avatarUrl,
                            binding.ratingEdit.rating,
                            superheroeId
                        )

                        refBD.child("superheroes").child(superheroeId).setValue(updatedSuperheroe)

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditarSuperheroe, "Superheroe actualizado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditarSuperheroe, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditarSuperheroe, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                }
            }
        } else {
            Toast.makeText(this, "No se seleccionó una imagen", Toast.LENGTH_SHORT).show()
        }
    }
}
