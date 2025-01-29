package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nacho.firebase_appwrite_base.databinding.ActivityVerListaPeleasBinding

class VerListaPeleas : AppCompatActivity() {

    private lateinit var binding: ActivityVerListaPeleasBinding

    private lateinit var refBD: DatabaseReference
    private lateinit var peleaList: MutableList<Pelea>
    private lateinit var peleaAdapter: PeleaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerListaPeleasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refBD = FirebaseDatabase.getInstance().reference
        peleaList = mutableListOf()



        // Configuración del RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        peleaAdapter = PeleaAdapter(peleaList, binding.recyclerView)
        binding.recyclerView.adapter = peleaAdapter

        refBD.child("peleas").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                peleaList.clear() // Limpiar la lista antes de agregar nuevos datos
                for (pojo in snapshot.children) {
                    val pelea = pojo.getValue(Pelea::class.java)
                    pelea?.let { peleaList.add(it) }
                }
                peleaAdapter.notifyDataSetChanged() // Notificar cambios al adaptador
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VerListaPeleas, "Error al cargar las peleas", Toast.LENGTH_SHORT).show()
            }
        })

        // Botón para volver a la actividad principal
        binding.btnvolver.setOnClickListener {
            val intent = Intent(this, ActividadPrincipal::class.java)
            startActivity(intent)
        }

    }
}