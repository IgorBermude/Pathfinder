package com.example.pathfinder.ui.rotas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfinder.data.models.Rota

class RotaSharedViewModel : ViewModel() {
    private val _rotaSelecionada = MutableLiveData<Rota?>()
    val rotaSelecionada: LiveData<Rota?> = _rotaSelecionada

    fun selecionarRota(rota: Rota) {
        _rotaSelecionada.value = rota
    }

    fun limparRota() {
        _rotaSelecionada.value = null
    }
}

