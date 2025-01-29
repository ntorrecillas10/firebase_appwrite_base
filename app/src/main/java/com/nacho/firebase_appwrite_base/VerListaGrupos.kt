package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.nacho.firebase_appwrite_base.databinding.ActivityVerListaGruposBinding

class VerListaGrupos : AppCompatActivity() {

    private lateinit var binding: ActivityVerListaGruposBinding

    private lateinit var refBD: DatabaseReference
    private lateinit var grupoList: MutableList<Grupo>
    private lateinit var grupoAdapter: GrupoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerListaGruposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refBD = FirebaseDatabase.getInstance().reference
        grupoList = mutableListOf()

        // Botón para volver a la actividad principal
        binding.btnvolver.setOnClickListener {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }

        // Filtrado de búsqueda en tiempo real
        binding.textoSuperior.doOnTextChanged { text, _, _, _ ->
            val filteredList = if (text.isNullOrEmpty()) {
                grupoList // Mostrar la lista completa si no hay texto
            } else {
                grupoList.filter { grupo ->
                    grupo.nombre.contains(text.toString(), ignoreCase = true)
                }
            }
            grupoAdapter.updateList(filteredList) // Actualizar la lista mostrada
        }

        // Configuración del RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        grupoAdapter = GrupoAdapter(grupoList, binding.recyclerView)
        binding.recyclerView.adapter = grupoAdapter

        // Obtener todos los grupos de Firebase
        refBD.child("grupos").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                grupoList.clear() // Limpiar la lista antes de agregar nuevos datos
                for (pojo in snapshot.children) {
                    val grupo = pojo.getValue(Grupo::class.java)
                    grupo?.let { grupoList.add(it) }
                }
                grupoAdapter.notifyDataSetChanged() // Notificar cambios al adaptador
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VerListaGrupos, "Error al cargar los grupos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
