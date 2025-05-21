package com.example.pathfinder.ui.components

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.pathfinder.R

class SearchBarAdapter(private val rootView: View) {

    private val searchBar: CardView = rootView.findViewById(R.id.search_bar)
    private val searchContainer: LinearLayout = rootView.findViewById(R.id.search_container)
    private val searchInput: TextView = rootView.findViewById(R.id.search_input)

    // Função para alterar a cor do fundo
    fun setBackgroundColor(color: Int) {
        searchContainer.setBackgroundColor(color)
    }

    // Função para alterar o texto do hint
    fun setHint(hint: String) {
        searchInput.hint = hint
    }

    // Função para alterar a cor do texto
    fun setTextColor(color: Int) {
        searchInput.setTextColor(color)
    }

    // Função para alterar a cor do hint
    fun setHintTextColor(color: Int) {
        searchInput.setHintTextColor(color)
    }

    // Função para adicionar um clique ao container
    fun setOnClickListener(listener: View.OnClickListener) {
        searchContainer.setOnClickListener(listener)
    }
}
