package com.nacho.firebase_appwrite_base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GrupoAdapter(
    private var grupoList: MutableList<Grupo>
) : RecyclerView.Adapter<GrupoAdapter.GrupoViewHolder>() {

    // ViewHolder para mantener la referencia de las vistas de cada ítem
    class GrupoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreGrupo: TextView = itemView.findViewById(R.id.nombre_grupo_creado)
        val numeroMiembros: TextView = itemView.findViewById(R.id.miembros_num)
        val avatarGrupo: ImageView = itemView.findViewById(R.id.avatar_grupo_creado)
        val editButton: ImageView = itemView.findViewById(R.id.edit_button)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrupoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grupos, parent, false)
        return GrupoViewHolder(view)
    }

    override fun onBindViewHolder(holder: GrupoViewHolder, position: Int) {
        val grupo = grupoList[position]

        // Asignar los valores del grupo a las vistas del diseño
        holder.nombreGrupo.text = grupo.nombre
        holder.numeroMiembros.text = grupo.numMiembros.toString()

        // Puedes asignar una imagen al avatar desde una URL o recurso local
        // Por simplicidad, usaremos un recurso local por ahora
        holder.avatarGrupo.setImageResource(R.drawable.ic_launcher_foreground)

        // Opcional: Configurar acciones para los botones (editar y eliminar)
        holder.editButton.setOnClickListener {
            // Lógica para editar el grupo
        }
        holder.deleteButton.setOnClickListener {
            // Lógica para eliminar el grupo
        }
    }

    override fun getItemCount(): Int = grupoList.size

    // Método para actualizar la lista de grupos (usado en el filtrado)
    fun updateList(newList: List<Grupo>) {
        grupoList.clear()
        grupoList.addAll(newList)
        notifyDataSetChanged()
    }
}
