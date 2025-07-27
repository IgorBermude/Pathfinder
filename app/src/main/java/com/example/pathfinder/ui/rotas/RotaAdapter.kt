package com.example.pathfinder.ui.rotas

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Rota
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class RotaAdapter(
    private val rotas: MutableList<Rota>,
    private val onRemove: (Rota) -> Unit,
    private val onSelect: (Rota) -> Unit
) : RecyclerView.Adapter<RotaAdapter.RotaViewHolder>() {

    inner class RotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.tv_nome)
        //val tempo: TextView = itemView.findViewById(R.id.tv_tempo)
        val distancia: TextView = itemView.findViewById(R.id.tv_distancia)
        val dtModificacao: TextView = itemView.findViewById(R.id.tv_dt_modificacao)
        val btnRemover: Button = itemView.findViewById(R.id.btn_remover)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_rota, parent, false)
        return RotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RotaViewHolder, position: Int) {
        val rota = rotas[position]
        Log.e("RotaAdapter", "Binding rota: ${rota} at position $position")
        holder.nome.text = rota.nomeRota
        //holder.tempo.text = "Tempo: ${rota.tempoTotalRota}"
        holder.distancia.text = rota.distanciaRota
            ?.div(1000.0)
            ?.let { "Distância: %.1f km".format(it) }
            ?: "Distância: Desconhecida"
        // Formata a data se não for nula
        rota.dtModificacaoRota?.toDate()?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            holder.dtModificacao.text = "Modificada: ${sdf.format(it)}"
            holder.dtModificacao.visibility = View.VISIBLE
        } ?: run {
            holder.dtModificacao.visibility = View.GONE
        }
        holder.btnRemover.setOnClickListener {
            holder.btnRemover.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    holder.btnRemover.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .withEndAction {
                            onRemove(rota)
                        }
                        .start()
                }
                .start()
        }
        holder.itemView.setOnClickListener {
            // Animação de clique: reduz a escala e volta ao normal
            holder.itemView.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(80)
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(80)
                        .withEndAction {
                            onSelect(rota)
                        }
                        .start()
                }
                .start()
        }
    }

    override fun getItemCount(): Int = rotas.size
}
