package com.example.pathfinder.ui.rotas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Rota

class RotaAdapter(
    private val rotas: List<Rota>,
    private val onRemove: (Rota) -> Unit
) : RecyclerView.Adapter<RotaAdapter.RotaViewHolder>() {

    inner class RotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.tv_nome)
        val tempo: TextView = itemView.findViewById(R.id.tv_tempo)
        val distancia: TextView = itemView.findViewById(R.id.tv_distancia)
        val btnRemover: Button = itemView.findViewById(R.id.btn_remover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rota, parent, false)
        return RotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotaViewHolder, position: Int) {
        val rota = rotas[position]
        holder.nome.text = rota.nomeRota
        holder.tempo.text = "Tempo: ${rota.tempoTotalRota}"
        holder.distancia.text = "Distância: ${rota.distanciaRota}"
        holder.btnRemover.setOnClickListener { onRemove(rota) }
    }

    override fun getItemCount(): Int = rotas.size
}
