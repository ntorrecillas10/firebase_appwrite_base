package com.nacho.firebase_appwrite_base

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ItemGruposBinding
import com.nacho.firebase_appwrite_base.databinding.ItemSuperheroeBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GrupoAdapter(
    originalList: List<Grupo>, // Lista completa que no cambia
    private val recyclerPadre: RecyclerView,
) : RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder>() {

    private var displayedList: List<Grupo> = originalList // Lista que se muestra actualmente

    inner class GrupoViewHolder(val binding: ItemGruposBinding) : RecyclerView.ViewHolder(binding.root)

    // Appwrite
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val binding = ItemGruposBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GrupoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        val grupo = displayedList[position]
        holder.binding.nombreGrupoCreado.text = grupo.nombre
        holder.binding.lugarGrupoCreado.text = grupo.lugarCreacion
        holder.binding.miembrosNum.text = grupo.numMiembros.toString()


        miProyectoId = "67586efe0025b764b95d" // ID del proyecto de Appwrite
        miBucketId = "67586f44003e5355a3b7" // ID del bucket de Appwrite

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // Conexión a Appwrite
            .setProject(miProyectoId)
        storage = Storage(appWriteClient)

        Glide.with(holder.itemView.context)
            .load(grupo.avatarUrl)
            .into(holder.binding.avatarGrupoCreado)

        holder.binding.main.setOnClickListener{
            //damos visibilidad a sus botones de borrar y editar y quitamos la visibilidad a los demás botones de editar y borrar

            if (holder.binding.editButton.visibility == View.VISIBLE && holder.binding.deleteButton.visibility == View.VISIBLE){
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
            }else{

                //recorremos el listado del recycler view del contexto con  id @+id/recyclerView
                for(i in 0 until recyclerPadre.childCount){
                    val child = recyclerPadre.getChildAt(i)
                    val childViewHolder = recyclerPadre.getChildViewHolder(child) as GrupoAdapter.GrupoViewHolder
                    childViewHolder.binding.editButton.visibility = View.INVISIBLE
                    childViewHolder.binding.deleteButton.visibility = View.INVISIBLE
                }

                holder.binding.editButton.visibility = View.VISIBLE
                holder.binding.deleteButton.visibility = View.VISIBLE
            }



        }
        // Botón de editar
        holder.binding.editButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditarGrupo::class.java).apply {
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
                putExtra("grupoId", grupo.key)
                putExtra("grupoName", grupo.nombre)
                putExtra("grupoLugar", grupo.lugarCreacion)
                putExtra("grupoAvatar", grupo.avatarUrl)
                putExtra("grupoMiembros", grupo.numMiembros)
            }
            holder.itemView.context.startActivity(intent)

        }

        // Botón de borrar
        holder.binding.deleteButton.setOnClickListener {
            val refBD = FirebaseDatabase.getInstance().reference

            // Eliminar superheroe de Firebase
            refBD.child("grupos").child(grupo.key).removeValue()
                .addOnSuccessListener {
                    // superheroe eliminado, ahora borrar la imagen de Appwrite
                    val imageId = grupo.avatarUrl.split("/")[8] // Extrae el ID de la imagen de la URL
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
                    Log.e("Firebase", "Error deleting grupo: ${e.message}")
                }
        }

        holder.binding.verSuperheroes.setOnClickListener {
            val intent = Intent(holder.itemView.context, VerListaSuperheroes::class.java).apply {
                putExtra("accion", "grupo")
                putExtra("grupo", grupo.nombre)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.binding.anadirSuperheroes.setOnClickListener {
            val intent = Intent(holder.itemView.context, VerListaSuperheroes::class.java).apply {
                putExtra("accion", "fichar")
            }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = displayedList.size


    fun updateList(newList: List<Grupo>) {
        displayedList = newList
        notifyDataSetChanged()
    }
}