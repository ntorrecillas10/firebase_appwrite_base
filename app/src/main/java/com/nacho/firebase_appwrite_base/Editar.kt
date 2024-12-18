package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ActivityEditarBinding
import io.appwrite.Client
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import java.util.Timer
import java.util.TimerTask

class EditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarBinding
    private lateinit var refBD: DatabaseReference
    private lateinit var userId: String

    //appwrite
    private lateinit var appWriteClient: Client //cliente de appwrite
    private lateinit var storage: Storage //servicio de almacenamiento de appwrite
    private lateinit var mi_bucket_id: String //id del bucket de appwrite
    private lateinit var mi_proyecto_id: String //id del proyecto de appwrite
    private lateinit var identificadorAppWrite: String //identificador único para el objeto usuario en appwrite

    private var url: Uri? = null // Variable para almacenar la URL de la imagen seleccionada

    // Declaración de ActivityResultLauncher fuera de cualquier setOnClickListener
    private val url_galeria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        when (uri) {
            null -> Toast.makeText(this, "No has seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            else -> {
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
                url = uri
                binding.avatarEdit.setImageURI(url) // Establecer la imagen en el ImageView
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializamos las variables de appwrite
        mi_proyecto_id = "67586efe0025b764b95d" //aquí el id del proyecto, este el mío
        mi_bucket_id = "67586f44003e5355a3b7" //aquí el id del bucket, este el mío
        identificadorAppWrite = ""

        // Se inicializan las cosas de appwrite
        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // necesario para conectarse a la api de appwrite
            .setProject(mi_proyecto_id)
        storage = Storage(appWriteClient)

        userId = intent.getStringExtra("userId") ?: ""
        val userName = intent.getStringExtra("userName") ?: ""
        val userGrupo = intent.getStringExtra("userGrupo") ?: ""
        val userRating = intent.getFloatExtra("userRating", 0f)
        val userAvatar = intent.getStringExtra("userAvatar") ?: ""

        binding.nombreEdit.setText(userName)
        binding.grupoEdit.setText(userGrupo)
        binding.ratingEdit.rating = userRating
        Glide.with(this)
            .load(userAvatar)
            .into(binding.avatarEdit)

        refBD = FirebaseDatabase.getInstance().reference

        binding.volver.setOnClickListener{
            finish()
        }

        // Aquí, movemos la lógica de abrir la galería al onCreate, fuera de setOnClickListener
        binding.avatarEdit.setOnClickListener {
            url_galeria.launch("image/*") // Lanzar la galería cuando el usuario haga clic en el avatar
            //Seteamos la nueva imagen en el userAvatar
            binding.avatarEdit.setImageURI(url)
        }

        val colors = arrayOf(R.color.green_dark, R.color.green_medium, R.color.green_light)

        //Damos una animación de colores graduales al texto superior que cambian cada 2 segundos y se repite
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val colorIndex = (Math.random() * colors.size).toInt()
                    val color = colors[colorIndex]
                    binding.textoSuperior.setTextColor(getColor(color))
                }
            }
        }, 0, 2000)

        binding.guardarButton.setOnClickListener {
            val nombre = binding.nombreEdit.text.toString()
            val grupo = binding.grupoEdit.text.toString()
            val rating = binding.ratingEdit.rating
            val userAvatar = url.toString() // Obtener la URL de la imagen seleccionada

            if (nombre.isNotEmpty() && grupo.isNotEmpty()) {
                val updatedUser = Usuario(nombre, grupo, userAvatar, rating, userId)
                refBD.child("usuarios").child(userId).setValue(updatedUser)
                    .addOnSuccessListener {
                        //Guardamos la url de la imagen en una SharedPreference
                        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("userAvatar", userAvatar)
                        editor.apply()
                        Toast.makeText(this, "Usuario actualizado correctamente", Toast.LENGTH_SHORT).show()
                        finish() // Volver a la actividad anterior
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al actualizar usuario", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
