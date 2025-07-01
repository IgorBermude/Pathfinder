package com.example.pathfinder.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Destino

class DestinoAdapter(
    private var destinos: List<Destino>,
    private val onDeleteClick: (Destino) -> Unit
) : RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome: TextView = view.findViewById(R.id.text_nome)
        val distancia: TextView = view.findViewById(R.id.text_distancia)
        val delete: ImageView = view.findViewById(R.id.icon_delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = destinos[position]
        holder.nome.text = destino.nome
        holder.distancia.text = "Distancia: ${destino.distancia} Km"
        holder.delete.setOnClickListener { onDeleteClick(destino) }
    }

    fun update(novos: List<Destino>) {
        this.destinos = novos
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = destinos.size
}
