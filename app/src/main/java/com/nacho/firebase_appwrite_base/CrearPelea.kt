package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nacho.firebase_appwrite_base.databinding.ActivityCrearGrupoBinding
import com.nacho.firebase_appwrite_base.databinding.ActivityCrearPeleaBinding
import io.appwrite.Client
import io.appwrite.services.Storage


private lateinit var refBD: DatabaseReference//referencia a la base de datos Firebase
private lateinit var identificador: String//identificador único para el objeto pelea
private lateinit var pelea_nueva: Pelea//objeto grupo para crear


//appwrite
private lateinit var appWriteClient: Client//cliente de appwrite
private lateinit var storage: Storage//servicio de almacenamiento de appwrite
private lateinit var mi_bucket_id: String//id del bucket de appwrite
private lateinit var mi_proyecto_id: String//id del proyecto de appwrite
private lateinit var identificadorAppWrite: String//identificador único para el objeto grupo en appwrite


class CrearPelea : AppCompatActivity() {

    private lateinit var binding: ActivityCrearPeleaBinding

    private fun cargarSpinner(selector: Spinner) {
        val lista: MutableList<Grupo>
        val adaptador: ArrayAdapter<Grupo>
        lista = ArrayList()
        adaptador = ArrayAdapter<Grupo>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista
        )
        selector.adapter = adaptador



        refBD.child("grupos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { hijo: DataSnapshot? ->
                        val pojo_grupo = hijo?.getValue(Grupo::class.java)
                        lista.add(pojo_grupo!!)
                    }
                    adaptador.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrearPeleaBinding.inflate(layoutInflater)
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

        pelea_nueva = Pelea()


        cargarSpinner(binding.spinner1)
        cargarSpinner(binding.spinner2)

        binding.confirmar.setOnClickListener {

            if (binding.fechaLayout.text.trim().toString() == "") {
                Toast.makeText(
                    applicationContext,
                    "Poner fecha para la pelea",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (binding.spinner1.selectedItem == binding.spinner2.selectedItem) {
                Toast.makeText(
                    applicationContext,
                    "Deben ser dos grupos distintos",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //AÑADIMOS LOS DATOS COMO ESTAMOS ACOSTUMBRADOS
                // EN ESTE CASO HAY DOS CLAVES EXTERNAS
                val id_generado = refBD.child("peleas").push().key

                //AUNQUE EL OBJETO TENGA MAS PROPIEDADES DICHAS PROPIEDADES SON SOLO PARA
                //EL ADAPTADOR POR LO QUE SOLO RELLENAMOS ESTAS

                escribirPelea(
                    refBD, id_generado!!, (binding.spinner1.selectedItem as Grupo).key!!,

                    (binding.spinner2.selectedItem as Grupo).key!!,
                    binding.fechaLayout.text.trim().toString(), (binding.spinner1.selectedItem as Grupo).avatarUrl, (binding.spinner2.selectedItem as Grupo).avatarUrl,
                    (binding.spinner1.selectedItem as Grupo).nombre, (binding.spinner2.selectedItem as Grupo).nombre
                )

                Toast.makeText(
                    applicationContext,
                    "Pelea programada con éxito",
                    Toast.LENGTH_SHORT
                ).show()
                val actividad = Intent(applicationContext, ActividadPrincipal::class.java)
                startActivity(actividad)
            }
        }
    }

    fun escribirPelea(db_ref: DatabaseReference, id: String, id_equipo1: String, id_equipo2: String, fecha: String, url_equipo1: String? = null, url_equipo2: String? = null, nombre_equipo1: String? = null, nombre_equipo2: String? = null) =
        db_ref.child("peleas").child(id).setValue(
            Pelea(
                id,
                id_equipo1,
                id_equipo2,
                fecha,
                url_equipo1,
                url_equipo2,
                nombre_equipo1,
                nombre_equipo2
            )
        )
}
