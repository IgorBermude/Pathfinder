package com.example.pathfinder.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfinder.data.models.Destino
import com.example.pathfinder.data.models.Rota
import com.mapbox.geojson.Point
import java.util.Date

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Essa é a pagina inicial e pagina do mapa"
    }
    val text: LiveData<String> = _text

    // Lista de rotas criadas pelo usuário
    private val _rotas = MutableLiveData<List<Rota>>(emptyList())
    val rotas: LiveData<List<Rota>> = _rotas

    // Obtém a última rota existente, se houver
    fun obterUltimaRota(): Rota? {
        val rotasAtuais = _rotas.value ?: emptyList()
        return rotasAtuais.lastOrNull()
    }

    // Cria uma nova rota com origem e primeiro destino
    fun criarNovaRota(origin: Point, destination: Destino, nomeRota: String?) {
        val novaRota = Rota(
            origemRota = origin,
            destinosRota = listOf(destination),
            criadorRota = null, // Preencha com o usuário logado se necessário
            distanciaRota = null,
            tempoTotalRota = null,
            dtModificacaoRota = Date(),
            nomeRota = nomeRota
        )
        adicionarRota(novaRota)
    }

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

    // Adiciona um destino à rota existente
    fun adicionarDestinoARotaExistente(destination: Destino) {
        val rotaAtual = obterUltimaRota()
        if (rotaAtual != null) {
            val novaRota = rotaAtual.copy(
                destinosRota = rotaAtual.destinosRota + destination,
                dtModificacaoRota = Date(),
            )
            atualizarUltimaRota(novaRota)
        }
    }

    // Remove um destino da rota existente
    fun removerDestinoDaRota(destino: Destino) {
        val rotaAtual = obterUltimaRota()
        if (rotaAtual != null) {
            val novaLista = rotaAtual.destinosRota.filter { it != destino }
            val novaRota = rotaAtual.copy(
                destinosRota = novaLista,
                dtModificacaoRota = Date()
            )
            atualizarUltimaRota(novaRota)
        }
    }
}