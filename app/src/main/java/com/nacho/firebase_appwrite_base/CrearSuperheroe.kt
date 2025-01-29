package com.nacho.firebase_appwrite_base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//binding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nacho.firebase_appwrite_base.databinding.ActivityCrearSuperheroeBinding
//appwrite
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

//scope
lateinit var scopeSuperheroe: CoroutineScope

//firebase
private lateinit var refBD: DatabaseReference//referencia a la base de datos Firebase
lateinit var url: Uri//url de la imagen
private lateinit var identificador: String//identificador único para el objeto superheroe
private lateinit var superheroe_nuevo: Superheroe//objeto superheroe para crear


//appwrite
private lateinit var appWriteClient: Client//cliente de appwrite
private lateinit var storage: Storage//servicio de almacenamiento de appwrite
private lateinit var mi_bucket_id: String//id del bucket de appwrite
private lateinit var mi_proyecto_id: String//id del proyecto de appwrite
private lateinit var identificadorAppWrite: String//identificador único para el objeto superheroe en appwrite


class CrearSuperheroe : AppCompatActivity() {
    private lateinit var binding: ActivityCrearSuperheroeBinding//importamos el layout al completo

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_superheroe)
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
        mi_proyecto_id = "67586efe0025b764b95d"//aquí el id del proyecto, este el mío
        mi_bucket_id = "67586f44003e5355a3b7"//aquí el id del bucket, este el mío
        identificadorAppWrite = ""


        //se inicializa el binding
        binding = ActivityCrearSuperheroeBinding.inflate(layoutInflater)
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

        binding.volver.setOnClickListener {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }

        //inicializamos el scope
        scopeSuperheroe = MainScope()

        //inicializamos el superheroe
        superheroe_nuevo = Superheroe()

        //creamos el objeto superheroe con los datos introducidos y lo subimos a firebase y a appwrite
        binding.confirmar.setOnClickListener {

            var listaSuperheroes = obtenerListaSuperheroes(refBD, this)

            //comprobamos que el nombre no este ya en la base de datos
            if (!existeSuperheroe(listaSuperheroes, binding.nombreTextInputEdit.text.toString())
            ) {

                if (binding.avatarInput.drawable != null) {
                    imagen = true
                }
                if (binding.nombreTextInputEdit.text.toString() != "" && binding.ratingBar.rating != 0f) {
                    campos = true
                }
                if (campos && imagen) {

                    //key única para el superheroe
                    identificador = refBD.child("superheroes").push().key!!
                    //refBD.child("superheroe").child(identificador).setValue(superheroe)

                    Log.d("ID", identificador)
                    Log.d("Date", superheroe_nuevo.fecha)

                    //subimos la imagen a appwrite storage y los datos a firebase
                    identificadorAppWrite = ID.unique() // coge el identificador y lo adapta a appwrite

                    //necesario para crear un archivo temporal con la imagen
                    val inputStream = this.contentResolver.openInputStream(url)

                    GlobalScope.launch(Dispatchers.IO) {//scope para las funciones de appwrite, pero ya aprovechamos y metemos el código de firebase
                        try {

                            val file = inputStream.use { input ->
                                val tempFile =
                                    kotlin.io.path.createTempFile(identificadorAppWrite).toFile()
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

                            val url_avatar =
                                "https://cloud.appwrite.io/v1/storage/buckets/$mi_bucket_id/files/$identificadorAppWrite/preview?project=$mi_proyecto_id"

                            superheroe_nuevo = Superheroe(
                                binding.nombreTextInputEdit.text.toString(),
                                url_avatar,
                                binding.ratingBar.rating,
                                identificador
                            )

                            Log.d("superheroe", superheroe_nuevo.toString())

                            //subimos los datos a firebase
                            refBD.child("superheroes").child(identificador).setValue(superheroe_nuevo)


                        } catch (e: Exception) {
                            Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                        }
                    }


                    Toast.makeText(
                        this,
                        "superheroe ${superheroe_nuevo.nombre} creado con éxito",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Rellena todos los campos y elige una imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                campos = false
                imagen = false
                //Ponemos el focus en el campo de nombre
                binding.nombreTextInputEdit.requestFocus()

            }
            else {
                Toast.makeText(this, "El nombre de superheroe ya existe", Toast.LENGTH_SHORT)
                    .show()
               return@setOnClickListener
            }
            //volvemos a la pantalla de inicio
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)



        }
        //listener del botón cargar
        binding.cargar.setOnClickListener {
            val intent = Intent(this, VerListaSuperheroes::class.java)
            startActivity(intent)
        }
    }


    private val url_galeria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            when (uri) {
                null -> Toast.makeText(
                    this,
                    "No has seleccionado ninguna imagen",
                    Toast.LENGTH_SHORT
                ).show()

                else -> {
                    Toast.makeText(this, "Imagen seleccionada", Toast.LENGTH_SHORT).show()
                    url = uri
                    binding.avatarInput.setImageURI(url)
                }
            }
        }

    fun existeSuperheroe(superheroes: List<Superheroe>, nombre: String): Boolean {
        return superheroes.any { it.nombre.lowercase() == nombre.lowercase() }
    }


    fun obtenerListaSuperheroes(db_ref: DatabaseReference, contexto: Context): MutableList<Superheroe> {
        val lista_superheroes = mutableListOf<Superheroe>()

        db_ref.child("superheroes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { superheroe ->
                        val superheroe_nuevo = superheroe.getValue(Superheroe::class.java)
                        lista_superheroes.add(superheroe_nuevo!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(contexto, "Error al obtener los superheroes", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        return lista_superheroes
    }


}