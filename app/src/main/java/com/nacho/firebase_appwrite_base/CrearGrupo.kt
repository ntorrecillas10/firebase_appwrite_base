package com.nacho.firebase_appwrite_base

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nacho.firebase_appwrite_base.databinding.ActivityCrearGrupoBinding
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


private lateinit var refBD: DatabaseReference//referencia a la base de datos Firebase
private lateinit var identificador: String//identificador único para el objeto grupo
private lateinit var grupo_nuevo: Grupo//objeto grupo para crear


//appwrite
private lateinit var appWriteClient: Client//cliente de appwrite
private lateinit var storage: Storage//servicio de almacenamiento de appwrite
private lateinit var mi_bucket_id: String//id del bucket de appwrite
private lateinit var mi_proyecto_id: String//id del proyecto de appwrite
private lateinit var identificadorAppWrite: String//identificador único para el objeto grupo en appwrite

class CrearGrupo : AppCompatActivity() {
    private lateinit var binding: ActivityCrearGrupoBinding // Importamos el layout completo

    private lateinit var url: Uri // URL de la imagen seleccionada

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_grupo)

        // Inicializamos el binding
        binding = ActivityCrearGrupoBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //inicializamos las variables de appwrite
        mi_proyecto_id = "67586efe0025b764b95d"//aquí el id del proyecto, este el mío
        mi_bucket_id = "67586f44003e5355a3b7"//aquí el id del bucket, este el mío
        identificadorAppWrite = ""

        refBD = FirebaseDatabase.getInstance().reference

        //se inicializan las cosas de appwrite
        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1")//necesario para conectarse a la api de appwrite
            .setProject(mi_proyecto_id)
        storage = Storage(appWriteClient)


        // Listener para volver a la pantalla anterior
        binding.volver.setOnClickListener {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }

        // Listener para seleccionar una imagen para el avatar
        binding.avatarInput.setOnClickListener {
            urlGaleria.launch("image/*") // Abre la galería para seleccionar una imagen
        }

        grupo_nuevo = Grupo()

        var campos = false
        var imagen = false

        binding.confirmar.setOnClickListener {

            var lista_grupos = obtenerListaGrupos(refBD, this)

            //comprobamos que el nombre no este ya en la base de datos
            if (!existeGrupo(lista_grupos, binding.nombreGrupoTextInputEdit.text.toString())
            ) {

                if (binding.avatarInput.drawable != null) {
                    imagen = true
                }
                if (binding.nombreGrupoTextInputEdit.text.toString() != "" && binding.creacionGrupoTextInputEdit.text.toString() != "" && binding.worthTextInputEdit.text.toString().toInt() != 0) {
                    campos = true
                }
                if (campos && imagen) {

                    //key única para el grupo
                    identificador = refBD.child("grupos").push().key!!
                    //refBD.child("grupos").child(identificador).setValue(superheroe)

                    Log.d("ID", identificador)
                    Log.d("Date", grupo_nuevo.fecha)

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

                            grupo_nuevo = Grupo(
                                binding.nombreGrupoTextInputEdit.text.toString(),
                                binding.creacionGrupoTextInputEdit.text.toString(),
                                binding.worthTextInputEdit.text.toString().toInt(),
                                url_avatar,
                                identificador
                            )

                            Log.d("GRUPOS", grupo_nuevo.toString())

                            //subimos los datos a firebase
                            refBD.child("grupos").child(identificador).setValue(grupo_nuevo)


                        } catch (e: Exception) {
                            Log.e("UploadError", "Error al subir la imagen: ${e.message}")
                        }
                    }


                    Toast.makeText(
                        this,
                        "Grupo ${grupo_nuevo.nombre} creado con éxito",
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
                binding.nombreGrupoLayout.requestFocus()

            }
            else {
                Toast.makeText(this, "El nombre del grupo ya existe", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            //volvemos a la pantalla de inicio
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)

        }
    }

    // Lanzador de la galería para seleccionar una imagen
    private val urlGaleria =
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
                    binding.avatarInput.setImageURI(url) // Mostrar la imagen seleccionada en el ImageView
                }
            }
        }

    fun obtenerListaGrupos(db_ref: DatabaseReference, contexto: Context): MutableList<Grupo> {
        val lista_grupos = mutableListOf<Grupo>()

        db_ref.child("grupos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { grupo ->
                        val grupo_nuevo = grupo.getValue(Grupo::class.java)
                        lista_grupos.add(grupo_nuevo!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(contexto, "Error al obtener los grupos", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        return lista_grupos
    }

    fun existeGrupo(grupos: List<Grupo>, nombre: String): Boolean {
        return grupos.any { it.nombre.lowercase() == nombre.lowercase() }
    }
}