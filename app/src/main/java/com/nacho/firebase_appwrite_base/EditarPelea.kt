package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.nacho.firebase_appwrite_base.databinding.ActivityCrearPeleaBinding
import com.nacho.firebase_appwrite_base.databinding.ActivityEditarPeleaBinding
import io.appwrite.Client
import io.appwrite.services.Storage

class EditarPelea : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPeleaBinding

    private lateinit var refBD: DatabaseReference
    private lateinit var peleaId: String
    private lateinit var pelea: Pelea // Objeto para editar

    // Appwrite variables
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String
    private lateinit var identificadorAppWrite: String

    // Cargar spinner con los grupos de Firebase
    private fun cargarSpinner(selector: Spinner) {
        val lista: MutableList<Grupo> = ArrayList()
        val adaptador = ArrayAdapter<Grupo>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, lista
        )
        selector.adapter = adaptador

        refBD.child("grupos")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lista.clear()
                    snapshot.children.forEach { hijo ->
                        val grupo = hijo.getValue(Grupo::class.java)
                        grupo?.let {
                            lista.add(it)
                        }
                    }
                    adaptador.notifyDataSetChanged()

                    // Aquí es donde seleccionamos los grupos correctos
                    if (selector == binding.spinner1Edit) {
                        val grupo1 = pelea.nombre_equipo1
                        val grupoSeleccionado1 = lista.find { it.nombre == grupo1 }
                        grupoSeleccionado1?.let {
                            val grupoPos1 = lista.indexOf(it)
                            selector.setSelection(grupoPos1)
                        }
                    } else if (selector == binding.spinner2Edit) {
                        val grupo2 = pelea.nombre_equipo2
                        val grupoSeleccionado2 = lista.find { it.nombre == grupo2 }
                        grupoSeleccionado2?.let {
                            val grupoPos2 = lista.indexOf(it)
                            selector.setSelection(grupoPos2)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditarPelea, "Error al cargar los grupos", Toast.LENGTH_SHORT).show()
                }
            })
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPeleaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Appwrite
        miProyectoId = "67586efe0025b764b95d"
        miBucketId = "67586f44003e5355a3b7"
        identificadorAppWrite = ""

        refBD = FirebaseDatabase.getInstance().reference

        // Inicialización de Appwrite
        appWriteClient = Client().setEndpoint("https://cloud.appwrite.io/v1").setProject(miProyectoId)
        storage = Storage(appWriteClient)

        peleaId = intent.getStringExtra("peleaId") ?: "" // ID de la pelea a editar

        // Cargar datos de la pelea
        cargarPelea()

        // Configuración de los Spinners
        cargarSpinner(binding.spinner1Edit)
        cargarSpinner(binding.spinner2Edit)

        // Listener para volver a la pantalla anterior
        binding.volver.setOnClickListener {
            val intent = Intent(this, VerListaPeleas::class.java)
            startActivity(intent)
        }

        // Confirmar y guardar la pelea modificada
        binding.confirmar.setOnClickListener {
            if (binding.fechaLayoutEdit.text.trim().isEmpty()) {
                Toast.makeText(applicationContext, "Poner fecha para la pelea", Toast.LENGTH_SHORT).show()
            } else if (binding.spinner1Edit.selectedItem == binding.spinner2Edit.selectedItem) {
                Toast.makeText(applicationContext, "Deben ser dos grupos distintos", Toast.LENGTH_SHORT).show()
            } else {
                // Guardar los cambios en Firebase
                actualizarPelea()
            }
        }
    }

    private fun cargarPelea() {
        refBD.child("peleas").child(peleaId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pelea = snapshot.getValue(Pelea::class.java) ?: return
                // Asignamos los datos de la pelea a la UI
                binding.fechaLayoutEdit.setText(pelea.fecha_pelea)

                // Esperamos a que los grupos se carguen antes de proceder
                cargarSpinner(binding.spinner1Edit) // Recarga el spinner 1
                cargarSpinner(binding.spinner2Edit) // Recarga el spinner 2
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditarPelea, "Error al cargar la pelea", Toast.LENGTH_SHORT).show()
            }
        })
    }



    private fun actualizarPelea() {
        // Utiliza el peleaId existente en lugar de crear un nuevo ID con push()
        // Al usar peleaId, actualizarás la pelea ya existente en Firebase
        val idEquipo1 = (binding.spinner1Edit.selectedItem as Grupo).key
        val idEquipo2 = (binding.spinner2Edit.selectedItem as Grupo).key
        val fecha = binding.fechaLayoutEdit.text.trim().toString()

        // Escribir los nuevos datos de la pelea en Firebase
        escribirPelea(
            refBD, peleaId, idEquipo1, idEquipo2, fecha,
            (binding.spinner1Edit.selectedItem as Grupo).avatarUrl,
            (binding.spinner2Edit.selectedItem as Grupo).avatarUrl,
            (binding.spinner1Edit.selectedItem as Grupo).nombre,
            (binding.spinner2Edit.selectedItem as Grupo).nombre
        )

        Toast.makeText(applicationContext, "Pelea actualizada con éxito", Toast.LENGTH_SHORT).show()
        val intent = Intent(applicationContext, VerListaPeleas::class.java)
        startActivity(intent)
    }


    private fun escribirPelea(
        dbRef: DatabaseReference, id: String, idEquipo1: String, idEquipo2: String, fecha: String,
        urlEquipo1: String? = null, urlEquipo2: String? = null,
        nombreEquipo1: String? = null, nombreEquipo2: String? = null
    ) {
        dbRef.child("peleas").child(id).setValue(
            Pelea(id, idEquipo1, idEquipo2, fecha, urlEquipo1, urlEquipo2, nombreEquipo1, nombreEquipo2)
        )
    }
}
