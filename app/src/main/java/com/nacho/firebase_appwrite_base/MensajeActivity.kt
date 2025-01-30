package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.R
import com.google.firebase.database.ValueEventListener
import com.nacho.firebase_appwrite_base.databinding.ActivityEditarGrupoBinding
import com.nacho.firebase_appwrite_base.databinding.ActivityMensajeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import layout.Mensaje
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.CountDownLatch

class MensajeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMensajeBinding
    private lateinit var listaMensajes: MutableList<Mensaje>
    private lateinit var RefBD: DatabaseReference
    private lateinit var grupoActual: Grupo
    private lateinit var mensajesAdaptador: MensajeAdaptador


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMensajeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        grupoActual = intent.getParcelableExtra<Grupo>("Grupo")!!
        RefBD = FirebaseDatabase.getInstance().getReference()
        listaMensajes = mutableListOf()

        binding.btnvolver.setOnClickListener {
            val intent = Intent(this, VerListaGrupos::class.java)
            startActivity(intent)
        }

        mensajesAdaptador = MensajeAdaptador(listaMensajes)
        binding.rviewMensajes.layoutManager = LinearLayoutManager(applicationContext)
        binding.rviewMensajes.setHasFixedSize(true)

        binding.botonEnviar.setOnClickListener {
            val mensaje = binding.textoMensaje.text.toString().trim()

            if (mensaje.trim() != "") {
                val hoy: Calendar = Calendar.getInstance()
                val formateador: SimpleDateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
                val fecha_hora = formateador.format(hoy.getTime());

                val id_mensaje = RefBD.child("chat").child("mensajes").push().key!!
                val nuevo_mensaje = Mensaje(
                    id_mensaje,
                    grupoActual.key,
                    "",
                    "",
                    mensaje,
                    fecha_hora
                )
                RefBD.child("chat").child("mensajes").child(id_mensaje).setValue(nuevo_mensaje)
                binding.textoMensaje.setText("")
            } else {
                Toast.makeText(applicationContext, "Escribe algo", Toast.LENGTH_SHORT).show()
            }//Actualizamos la lista de mensajes
            mensajesAdaptador.notifyDataSetChanged()
        }

        RefBD.child("chat").child("mensajes").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                GlobalScope.launch(Dispatchers.IO) {
                    val pojo_mensaje = snapshot.getValue(Mensaje::class.java)
                    pojo_mensaje!!.id_receptor = grupoActual.key
                    if (pojo_mensaje.id_receptor == pojo_mensaje.id_emisor) {
                        pojo_mensaje.imagen_emisor = grupoActual.avatarUrl
                    } else {

                        var semaforo = CountDownLatch(1)


                        RefBD.child("grupos").child(pojo_mensaje.id_emisor!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val grupo = snapshot.getValue(Grupo::class.java)
                                    pojo_mensaje.imagen_emisor = grupo!!.avatarUrl
                                    semaforo.countDown()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    println(error.message)
                                }
                            })
                        semaforo.await()
                    }


                }

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
}
