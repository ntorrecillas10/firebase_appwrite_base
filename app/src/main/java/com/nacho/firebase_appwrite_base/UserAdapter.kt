package com.nacho.firebase_appwrite_base

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ItemUserBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.List

class UserAdapter(
    private val originalList: List<Usuario>, // Lista completa que no cambia
    private val recyclerPadre: RecyclerView,
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var displayedList: List<Usuario> = originalList // Lista que se muestra actualmente

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    // Appwrite
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = displayedList[position]
        holder.binding.nombreCreado.text = user.nombre
        holder.binding.grupoCreado.text = user.grupo
        holder.binding.ratingCreado.rating = user.rating

        miProyectoId = "67586efe0025b764b95d" // ID del proyecto de Appwrite
        miBucketId = "67586f44003e5355a3b7" // ID del bucket de Appwrite

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // Conexión a Appwrite
            .setProject(miProyectoId)
        storage = Storage(appWriteClient)

        Glide.with(holder.itemView.context)
            .load(user.avatar)
            .into(holder.binding.avatarCreado)

        holder.binding.main.setOnClickListener{
            //damos visibilidad a sus botones de borrar y editar y quitamos la visibilidad a los demás botones de editar y borrar

            if (holder.binding.editButton.visibility == View.VISIBLE && holder.binding.deleteButton.visibility == View.VISIBLE){
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
            }else{

                //recorremos el listado del recycler view del contexto con  id @+id/recyclerView
                for(i in 0 until recyclerPadre.childCount){
                    val child = recyclerPadre.getChildAt(i)
                    val childViewHolder = recyclerPadre.getChildViewHolder(child) as UserAdapter.UserViewHolder
                    childViewHolder.binding.editButton.visibility = View.INVISIBLE
                    childViewHolder.binding.deleteButton.visibility = View.INVISIBLE
                }

                holder.binding.editButton.visibility = View.VISIBLE
                holder.binding.deleteButton.visibility = View.VISIBLE
            }



        }
        // Botón de editar
        holder.binding.editButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditUserActivity::class.java).apply {
                holder.binding.editButton.visibility = View.INVISIBLE
                holder.binding.deleteButton.visibility = View.INVISIBLE
                putExtra("userId", user.key)
                putExtra("userName", user.nombre)
                putExtra("userGrupo", user.grupo)
                putExtra("userRating", user.rating)
                putExtra("userAvatar", user.avatar)
            }
            holder.itemView.context.startActivity(intent)

        }

        // Botón de borrar
        holder.binding.deleteButton.setOnClickListener {
            val refBD = FirebaseDatabase.getInstance().reference

            // Eliminar usuario de Firebase
            refBD.child("usuarios").child(user.key).removeValue()
                .addOnSuccessListener {
                    // Usuario eliminado, ahora borrar la imagen de Appwrite
                    val imageId = user.key.substring(1, 20) // Ajusta según la lógica de tu clave
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
                    Log.e("Firebase", "Error deleting user: ${e.message}")
                }
        }
    }

    override fun getItemCount(): Int = displayedList.size


    fun updateList(newList: List<Usuario>) {
        displayedList = newList
        notifyDataSetChanged()
    }
}
