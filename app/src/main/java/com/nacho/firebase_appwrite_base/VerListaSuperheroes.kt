package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.nacho.firebase_appwrite_base.databinding.ActivityVerListaSuperheroesBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class VerListaSuperheroes<T> : AppCompatActivity() {

    private lateinit var binding: ActivityVerListaSuperheroesBinding

    private lateinit var refBD: DatabaseReference
    private lateinit var superheroeList: MutableList<Superheroe>
    private lateinit var superheroeAdapter: SuperheroeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerListaSuperheroesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refBD = FirebaseDatabase.getInstance().reference
        superheroeList = mutableListOf()

        val accion=intent.getStringExtra("accion")
        val id_grupo_accion=intent.getStringExtra("id_grupo_accion")
        val nombre_grupo_accion=intent.getStringExtra("nombre_grupo_accion")


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        superheroeAdapter = SuperheroeAdapter(superheroeList,recyclerView,accion,id_grupo_accion,nombre_grupo_accion)
        binding.recyclerView.adapter = superheroeAdapter
        recyclerView.setHasFixedSize(true)


        // Configurar el Spinner
        val spinner = findViewById<Spinner>(R.id.filtrar)
        // Configurar el adaptador del Spinner
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("RatingA", "RatingD", "Grupo")
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        binding.textoSuperior.doOnTextChanged { text, _, _, _ ->
            val filteredList = if (text.isNullOrEmpty()) {
                superheroeList // Restauramos la lista completa
            } else {
                superheroeList.filter { superheroe ->
                    superheroe.nombre.contains(text.toString(), ignoreCase = true)
                }
            }
            superheroeAdapter.updateList(filteredList) // Actualizamos la lista mostrada
        }


        //evento de click en cada item del adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> superheroeList.sortBy { it.rating }
                    1 -> superheroeList.sortByDescending { it.rating }
                    2 -> superheroeList.sortBy { it.id_grupo?.lowercase() }

                }
                superheroeAdapter.notifyDataSetChanged() // Notify adapter of data change
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                superheroeList.sortBy { it.fecha }
            }
        }


        var busqueda:String=when(accion){
            "fichar"->"libre"
            "grupo"->id_grupo_accion!!
            else->""
        }

        refBD.child("superheroes")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    superheroeList.clear()
                    GlobalScope.launch(Dispatchers.IO) {
                        snapshot.children.forEach { hijo: DataSnapshot? ->
                            val pojoSuperheroe = hijo?.getValue(Superheroe::class.java)
                            if(pojoSuperheroe!!.id_grupo!!.contains(busqueda as CharSequence)){
                                if(pojoSuperheroe!!.id_grupo=="libre"){
                                    pojoSuperheroe.nombre_grupo="libre";
                                }else{
                                    val semaforo = CountDownLatch(1)
                                    //Sacar el nombre del grupo
                                    refBD.child("grupos")
                                        .child(pojoSuperheroe!!.id_grupo!!)
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val pojo_grupo = snapshot.getValue(Grupo::class.java)
                                                pojoSuperheroe.nombre_grupo = pojo_grupo!!.nombre
                                                semaforo.countDown()
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                println(error.message)
                                            }
                                        })
                                    semaforo.await();

                                }
                                superheroeList.add(pojoSuperheroe!!)
                            }

                        }

                        runOnUiThread{
                            binding.recyclerView.adapter?.notifyDataSetChanged()
                        }
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    println(error.message)
                }
            })

        binding.volver.setOnClickListener {
            if (accion == "todos") {
                val intent = Intent(this, ActividadPrincipal::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, VerListaGrupos::class.java)
                startActivity(intent)
            }
        }


    }
}
