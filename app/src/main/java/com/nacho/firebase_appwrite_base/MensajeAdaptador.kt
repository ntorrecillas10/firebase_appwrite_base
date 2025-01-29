package com.nacho.firebase_appwrite_base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nacho.firebase_appwrite_base.databinding.ItemChatBinding
import layout.Mensaje

class MensajeAdaptador(private val lista_mensajes: List<Mensaje>, last_pos: Int) : RecyclerView.Adapter<MensajeAdaptador.MensajeViewHolder>() {
    private lateinit var contexto: Context
    private var last_pos = last_pos

    inner class MensajeViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MensajeViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MensajeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MensajeViewHolder, position: Int) {
        val mensaje = lista_mensajes[position]

        if(mensaje.id_emisor==mensaje.id_receptor){
            //ES MIO,ASIGNAR A LA DERECHA Y YO
            holder.otro.text=""
            holder.hora_otro.text=""
            holder.imagen_otro.visibility=View.INVISIBLE
            holder.imagen_yo.visibility=View.VISIBLE
            Glide.with(contexto)
                .load(mensaje.imagen_emisor)
                .apply(Utilidades.opcionesGlide(contexto))
                .transition(Utilidades.transicion)
                .into(holder.imagen_yo)
            holder.hora_yo.text=mensaje.fecha_hora
            holder.yo.text=mensaje.contenido

        }else{
            //ES DE OTRO ASIGNAR A LA IZQUIERDA Y NOMBRE
            holder.yo.text=""
            holder.hora_yo.text=""
            holder.imagen_yo.visibility=View.INVISIBLE
            holder.imagen_otro.visibility=View.VISIBLE
            Glide.with(contexto)
                .load(mensaje.imagen_emisor)
                .apply(Utilidades.opcionesGlide(contexto))
                .transition(Utilidades.transicion)
                .into(holder.imagen_otro)
            holder.hora_otro.text=mensaje.fecha_hora
            holder.otro.text=mensaje.contenido
        }




    }

    override fun getItemCount(): Int = lista_mensajes.size

}
