package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ItemSuperheroeBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.List

class SuperheroeAdapter(
    originalList: List<Superheroe>, // Lista completa que no cambia
    private val recyclerPadre: RecyclerView,
    private val accion: String? = "",
    private val id_grupo: String? = "",
    private val nombre_grupo: String? = ""
) : RecyclerView.Adapter<SuperheroeAdapter.SuperheroeViewHolder>() {

    private var displayedList: List<Superheroe> = originalList // Lista que se muestra actualmente

    inner class SuperheroeViewHolder(val binding: ItemSuperheroeBinding) : RecyclerView.ViewHolder(binding.root)

    // Appwrite
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String
    private lateinit var refBD: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuperheroeViewHolder {
        val binding = ItemSuperheroeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SuperheroeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuperheroeViewHolder, position: Int) {
        val superheroe = displayedList[position]
        holder.binding.nombreCreado.text = superheroe.nombre
        holder.binding.ratingCreado.rating = superheroe.rating
        holder.binding.estadoCreado.text = superheroe.nombre_grupo

        miProyectoId = "67586efe0025b764b95d" // ID del proyecto de Appwrite
        miBucketId = "67586f44003e5355a3b7" // ID del bucket de Appwrite

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // Conexión a Appwrite
            .setProject(miProyectoId)
        storage = Storage(appWriteClient)

        Glide.with(holder.itemView.context)
            .load(superheroe.avatar)
            .into(holder.binding.avatarCreado)


        initializeUI()

        if (accion == "todos") {
            holder.binding.transferirBoton.visibility = View.GONE
        }

        var nuevo_id = "libre"
        var nombre_grupoo = ""
        holder.binding.transferirBoton.setOnClickListener {
            if (accion == "plantilla") { //Es para hecharle
                Toast.makeText(this.recyclerPadre.context, "Jugador despedido", Toast.LENGTH_SHORT).show()
            } else if (accion == "fichar") {//Es para ficharle por el id grupo
                nuevo_id = id_grupo!!
                nombre_grupoo = nombre_grupo!!
                Toast.makeText(this.recyclerPadre.context, "Jugador fichado", Toast.LENGTH_SHORT).show()
            }
            refBD.child("superheroes").child(superheroe.key).child("id_grupo").setValue(nuevo_id)
            refBD.child("superheroes").child(superheroe.key).child("nombre_grupo").setValue(nombre_grupoo)

        }



        holder.binding.main.setOnClickListener{
            //damos visibilidad a sus botones de borrar y editar y quitamos la visibilidad a los demás botones de editar y borrar

            if (holder.binding.editButton.visibility == View.VISIBLE && holder.binding.deleteButton.visibility == View.VISIBLE){
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
            }else{

                //recorremos el listado del recycler view del contexto con  id @+id/recyclerView
                for(i in 0 until recyclerPadre.childCount){
                    val child = recyclerPadre.getChildAt(i)
                    val childViewHolder = recyclerPadre.getChildViewHolder(child) as SuperheroeAdapter.SuperheroeViewHolder
                    childViewHolder.binding.editButton.visibility = View.INVISIBLE
                    childViewHolder.binding.deleteButton.visibility = View.INVISIBLE
                }

                holder.binding.editButton.visibility = View.VISIBLE
                holder.binding.deleteButton.visibility = View.VISIBLE
            }



        }
        // Botón de editar
        holder.binding.editButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditarSuperheroe::class.java).apply {
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
                putExtra("superheroeId", superheroe.key)
                putExtra("superheroeName", superheroe.nombre)
                putExtra("superheroeRating", superheroe.rating)
                putExtra("superheroeAvatar", superheroe.avatar)
            }
            holder.itemView.context.startActivity(intent)
        }

        // Botón de borrar
        holder.binding.deleteButton.setOnClickListener {
            val refBD = FirebaseDatabase.getInstance().reference

            // Eliminar superheroe de Firebase
            refBD.child("superheroes").child(superheroe.key).removeValue()
                .addOnSuccessListener {
                    // superheroe eliminado, ahora borrar la imagen de Appwrite
                    val imageId = superheroe.avatar.split("/")[8] // Extrae el ID de la imagen de la URL
                    Log.d("Firebase", "Image ID: $imageId")
                    // Ajusta según la lógica de tu clave
                    GlobalScope.launch(Dispatchers.IO) {
                        try {
                            Log.d("Appwrite", "Image ID: $imageId")
                            storage.deleteFile(miBucketId, imageId)
                        } catch (e: AppwriteException) {
                            Log.e("Appwrite", "Error deleting image: ${e.message}")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error deleting superheroe: ${e.message}")
                }
        }

        Log.d("accion", "accion: $accion, id_grupo: $id_grupo")
    }

    override fun getItemCount(): Int = displayedList.size


    fun updateList(newList: List<Superheroe>) {
        displayedList = newList
        notifyDataSetChanged()
    }
    private fun initializeUI() {
        refBD = FirebaseDatabase.getInstance().reference
    }
}
