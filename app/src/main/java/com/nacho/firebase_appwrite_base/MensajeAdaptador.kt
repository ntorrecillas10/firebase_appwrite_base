package com.nacho.firebase_appwrite_base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.nacho.firebase_appwrite_base.databinding.ItemChatBinding
import io.appwrite.Client
import io.appwrite.services.Storage
import layout.Mensaje

class MensajeAdaptador(
    originalList: List<Mensaje>,
) : RecyclerView.Adapter<MensajeAdaptador.MensajeViewHolder>() {

    private var displayedList: List<Mensaje> = originalList // Lista que se muestra actualmente

    inner class MensajeViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    // Appwrite
    private lateinit var appWriteClient: Client
    private lateinit var storage: Storage
    private lateinit var miBucketId: String
    private lateinit var miProyectoId: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MensajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val mensaje = displayedList[position]


        miProyectoId = "67586efe0025b764b95d" // ID del proyecto de Appwrite
        miBucketId = "67586f44003e5355a3b7" // ID del bucket de Appwrite

        appWriteClient = Client()
            .setEndpoint("https://cloud.appwrite.io/v1") // Conexión a Appwrite
            .setProject(miProyectoId)
        storage = Storage(appWriteClient)

        if(mensaje.id_emisor==mensaje.id_receptor){
            //ES MIO,ASIGNAR A LA DERECHA Y YO
            holder.binding.otro.text=""
            holder.binding.horaOtro.text=""
            holder.binding.imagenOtro.visibility=View.INVISIBLE
            holder.binding.imagenYo.visibility=View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(mensaje.imagen_emisor)
                .apply(opcionesGlide(holder.itemView.context))
                .into(holder.binding.imagenYo)
            holder.binding.horaYo.text=mensaje.fecha_hora
            holder.binding.yo.text=mensaje.contenido

        }else{
            //ES DE OTRO ASIGNAR A LA IZQUIERDA Y NOMBRE
            holder.binding.yo.text=""
            holder.binding.horaYo.text=""
            holder.binding.imagenYo.visibility=View.INVISIBLE
            holder.binding.imagenOtro.visibility=View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(mensaje.imagen_emisor)
                .apply(opcionesGlide(holder.itemView.context))
                .into(holder.binding.imagenOtro)
            holder.binding.horaOtro.text=mensaje.fecha_hora
            holder.binding.otro.text=mensaje.contenido
        }

    }

    override fun getItemCount(): Int = displayedList.size

    fun opcionesGlide(context: Context): RequestOptions {
        val options = RequestOptions()
            .placeholder(animacion_carga(context))
            .fallback(R.drawable.photo_negro)
            .error(R.drawable.photo_blanco)
        return options
    }
    fun animacion_carga(contexto: Context): CircularProgressDrawable {
        val animacion=CircularProgressDrawable(contexto)
        animacion.strokeWidth=5f
        animacion.centerRadius=30f
        animacion.start()

        return animacion
    }
}
