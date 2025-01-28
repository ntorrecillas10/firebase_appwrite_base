package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.nacho.firebase_appwrite_base.databinding.ActivityVerListaSuperheroesBinding

class VerListaSuperheroes<T> : AppCompatActivity() {

    private lateinit var binding: ActivityVerListaSuperheroesBinding

    private lateinit var refBD: DatabaseReference
    private lateinit var userList: MutableList<Superheroe>
    private lateinit var superheroeAdapter: SuperheroeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerListaSuperheroesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refBD = FirebaseDatabase.getInstance().reference
        userList = mutableListOf()

        binding.volver.setOnClickListener {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }
        // Configurar el Spinner
        val spinner = findViewById<Spinner>(R.id.filtrar)
        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("RatingA", "RatingD", "Grupo"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.textoSuperior.doOnTextChanged { text, _, _, _ ->
            val filteredList = if (text.isNullOrEmpty()) {
                userList // Restauramos la lista completa
            } else {
                userList.filter { superheroe ->
                    superheroe.nombre.contains(text.toString(), ignoreCase = true)
                }
            }
            superheroeAdapter.updateList(filteredList) // Actualizamos la lista mostrada
        }



        //evento de click en cada item del adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> userList.sortBy { it.rating }
                    1 -> userList.sortByDescending { it.rating }
                    2 -> userList.sortBy { it.grupo.lowercase() }

                }
                superheroeAdapter.notifyDataSetChanged() // Notify adapter of data change
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle case where nothing is selected (optional)
                userList.sortBy { it.fecha}
            }
        }


        // Configuración del RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        superheroeAdapter = SuperheroeAdapter(userList, recyclerView)
        recyclerView.adapter = superheroeAdapter

        // Obtener todos los Superheroe de Firebase
        refBD.child("superheroes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear() // Limpiar la lista antes de agregar los nuevos datos
                for (pojo in snapshot.children) {
                    val superheroe = pojo.getValue(Superheroe::class.java)
                    superheroe?.let { userList.add(it) }
                }
                superheroeAdapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VerListaSuperheroes, "Error al cargar los superheroes", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
