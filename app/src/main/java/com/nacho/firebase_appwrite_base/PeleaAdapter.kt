package com.nacho.firebase_appwrite_base

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.FirebaseDatabase
import com.nacho.firebase_appwrite_base.databinding.ItemPeleasBinding
import io.appwrite.Client
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PeleaAdapter(
    originalList: List<Pelea>, // Lista completa que no cambia
    private val recyclerPadre: RecyclerView,
) : RecyclerView.Adapter<PeleaAdapter.PeleaViewHolder>() {
    private lateinit var contexto: Context

    private var displayedList: List<Pelea> = originalList // Lista que se muestra actualmente

    inner class PeleaViewHolder(val binding: ItemPeleasBinding) : RecyclerView.ViewHolder(binding.root)

    // Appwrite
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeleaViewHolder {
        val binding = ItemPeleasBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        contexto = parent.context
        return PeleaViewHolder(binding)

    }

    override fun onBindViewHolder(holder: PeleaViewHolder, position: Int) {

        miProyectoId = "67586efe0025b764b95d" // ID del proyecto de Appwrite
        miBucketId = "67586f44003e5355a3b7" // ID del bucket de Appwrite

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // Conexión a Appwrite
            .setProject(miProyectoId)
        storage = Storage(appWriteClient)


        val pelea = displayedList[position]
        holder.binding.textEquipo1.text = pelea.nombre_equipo1
        holder.binding.textEquipo2.text = pelea.nombre_equipo2
        holder.binding.textFecha.text = pelea.fecha_pelea

        var URL: String? = when (pelea.url_equipo1) {
            "" -> null //Para que active imagen de fallback
            else -> pelea.url_equipo1
        }
        Glide.with(contexto)
            .load(URL)
            .apply(
                RequestOptions().fallback(R.drawable.photo_blanco)
                    .error(R.drawable.photo_negro)
            )
            .into(holder.binding.iconEquipo1)

        URL = when (pelea.url_equipo2) {
            "" -> null //Para que active imagen de fallback
            else -> pelea.url_equipo2
        }

        Glide.with(contexto)
            .load(URL)
            .apply(
                RequestOptions().fallback(R.drawable.photo_blanco)
                    .error(R.drawable.photo_negro)
            )
            .into(holder.binding.iconEquipo2)
        holder.binding.main.setOnClickListener{
            //damos visibilidad a sus botones de borrar y editar y quitamos la visibilidad a los demás botones de editar y borrar

            if (holder.binding.deleteButton.visibility == View.VISIBLE && holder.binding.editButton.visibility == View.VISIBLE){
                holder.binding.deleteButton.visibility = View.GONE
                holder.binding.editButton.visibility = View.GONE
            }else{

                //recorremos el listado del recycler view del contexto con  id @+id/recyclerView
                for(i in 0 until recyclerPadre.childCount){
                    val child = recyclerPadre.getChildAt(i)
                    val childViewHolder = recyclerPadre.getChildViewHolder(child) as PeleaAdapter.PeleaViewHolder
                    childViewHolder.binding.deleteButton.visibility = View.GONE
                    childViewHolder.binding.editButton.visibility = View.GONE
                }
                holder.binding.deleteButton.visibility = View.VISIBLE
                holder.binding.editButton.visibility = View.VISIBLE
            }

        }

        // Botón de borrar
        holder.binding.deleteButton.setOnClickListener {
            val refBD = FirebaseDatabase.getInstance().reference

            // Eliminar pelea de Firebase
            refBD.child("peleas").child(pelea.id!!).removeValue()

        }
        holder.binding.editButton.setOnClickListener {
            val intent = Intent(contexto, EditarPelea::class.java)
            intent.putExtra("peleaId", pelea.id)
            contexto.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = displayedList.size

}