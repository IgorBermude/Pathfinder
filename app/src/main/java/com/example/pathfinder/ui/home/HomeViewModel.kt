package com.example.pathfinder.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfinder.data.models.Rota

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Essa é a pagina inicial e pagina do mapa"
    }
    val text: LiveData<String> = _text

    // Lista de rotas criadas pelo usuário
    private val _rotas = MutableLiveData<List<Rota>>(emptyList())
    val rotas: LiveData<List<Rota>> = _rotas

    // Adiciona uma nova rota à lista
    fun adicionarRota(rota: Rota) {
        val listaAtual = _rotas.value?.toMutableList() ?: mutableListOf()
        listaAtual.add(rota)
        _rotas.value = listaAtual
    }

    // Atualiza a última rota da lista
    fun atualizarUltimaRota(novaRota: Rota) {
        val listaAtual = _rotas.value?.toMutableList() ?: mutableListOf()
        if (listaAtual.isNotEmpty()) {
            listaAtual[listaAtual.lastIndex] = novaRota
            _rotas.value = listaAtual
        }
    }
}