package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nacho.firebase_appwrite_base.R
import com.nacho.firebase_appwrite_base.Usuario
//binding
import com.nacho.firebase_appwrite_base.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
//appwrite
import io.appwrite.Client
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

//scope
lateinit var scopeUser: CoroutineScope

//firebase
private lateinit var refBD: DatabaseReference//referencia a la base de datos Firebase
lateinit var url: Uri//url de la imagen
private lateinit var identificador: String//identificador único para el objeto usuario
private lateinit var usuario_nuevo: Usuario//objeto usuario para crear


//appwrite
private lateinit var appWriteClient: Client//cliente de appwrite
private lateinit var storage: Storage//servicio de almacenamiento de appwrite
private lateinit var mi_bucket_id:String//id del bucket de appwrite
private lateinit var mi_proyecto_id:String//id del proyecto de appwrite
private lateinit var identificadorAppWrite:String//identificador único para el objeto usuario en appwrite




class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding//importamos el layout al completo

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var campos = false
        var imagen = false


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



        //inicializamos las variables de appwrite
        mi_proyecto_id ="67586efe0025b764b95d"//aquí el id del proyecto, este el mío
        mi_bucket_id ="67586f44003e5355a3b7"//aquí el id del bucket, este el mío
        identificadorAppWrite =""


        //se inicializa el binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //iniciamos la referencia a la base de datos de Firebase
        refBD = FirebaseDatabase.getInstance().reference

        //se inicializan las cosas de appwrite
        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1")//necesario para conectarse a la api de appwrite
            .setProject(mi_proyecto_id)
        storage = Storage(appWriteClient)

        //listener de la imagen
        binding.avatarInput.setOnClickListener {
            url_galeria.launch("image/*")//abre la galería
        }
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.ratingBar.rating = rating
            Log.d("RATING", rating.toString())
        }

        binding.volver.setOnClickListener{
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        //inicializamos el scope
        scopeUser = MainScope()

        //inicializamos el usuario
        usuario_nuevo = Usuario()

        //creamos el objeto usuario con los datos introducidos y lo subimos a firebase y a appwrite
        binding.confirmar.setOnClickListener {
            if(binding.avatarInput.drawable != null){
                imagen = true
            }
            if(binding.nombreTextInputEdit.text.toString() != "" && binding.grupoTextInputEdit.text.toString() != "" && binding.ratingBar.rating != 0f){
                campos = true
            }
            if(campos && imagen){

                //key única para el usuario
                identificador = refBD.child("usuarios").push().key!!
                //refBD.child("usuarios").child(identificador).setValue(user)

                Log.d("ID", identificador)
                Log.d("Date", usuario_nuevo.fecha)

                //subimos la imagen a appwrite storage y los datos a firebase
                identificadorAppWrite = identificador.substring(1, 20) // coge el identificador y lo adapta a appwrite

                //necesario para crear un archivo temporal con la imagen
                val inputStream = this.contentResolver.openInputStream(url)

                GlobalScope.launch(Dispatchers.IO) {//scope para las funciones de appwrite, pero ya aprovechamos y metemos el código de firebase
                    try{

                        val file = inputStream.use { input ->
                            val tempFile = kotlin.io.path.createTempFile(identificadorAppWrite).toFile()
                            if (input != null) {
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            InputFile.fromFile(tempFile) // tenemos un archivo temporal con la imagen
                        }


                            //se sube la imagen a appwrite
                            storage.createFile(
                                bucketId = mi_bucket_id,//id del bucket, este es el mío
                                fileId = identificadorAppWrite,
                                file = file
                            )


                        //plantilla de url a appwrite
                        //https://cloud.appwrite.io/v1/storage/buckets/[BUCKET_ID]/files/[FILE_ID]/preview?project=[PROJECT_ID]

                        val url_avatar = "https://cloud.appwrite.io/v1/storage/buckets/$mi_bucket_id/files/$identificadorAppWrite/preview?project=$mi_proyecto_id"

                        usuario_nuevo = Usuario(binding.nombreTextInputEdit.text.toString(), binding.grupoTextInputEdit.text.toString(), url_avatar, binding.ratingBar.rating, identificador)

                        Log.d("USUARIO", usuario_nuevo.toString())

                        //subimos los datos a firebase
                        refBD.child("usuarios").child(identificador).setValue(usuario_nuevo)


                    }catch (e: Exception){
                        Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                    }
                }


                Toast.makeText(this, "Usuario ${usuario_nuevo.nombre} creado con éxito", Toast.LENGTH_SHORT).show()
                binding.nombreTextInputEdit.text?.clear()
                binding.grupoTextInputEdit.text?.clear()
                binding.ratingBar.rating = 0f
                //Seteamos la imagen por defecto
                binding.avatarInput.setImageResource(R.drawable.photo_blanco)
            }else{
                Toast.makeText(this, "Rellena todos los campos y elige una imagen", Toast.LENGTH_SHORT).show()
            }
            campos = false
            imagen = false
            //Ponemos el focus en el campo de nombre
            binding.nombreTextInputEdit.requestFocus()

        }

        //listener del botón cargar
        binding.cargar.setOnClickListener {
            val intent = Intent(this, List::class.java)
            startActivity(intent)
        }


    }


    private val url_galeria = registerForActivityResult(ActivityResultContracts.GetContent()){
            uri: Uri? ->
        when(uri){
            null -> Toast.makeText(this, "No has seleccionado ninguna imagen", Toast.LENGTH_SHORT).show()
            else -> {
                Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
                url = uri
                binding.avatarInput.setImageURI(url)
            }
        }
    }
}