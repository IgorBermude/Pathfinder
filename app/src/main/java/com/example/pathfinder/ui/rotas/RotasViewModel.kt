package com.example.pathfinder.ui.rotas

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RotasViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Essa é a pagina para carregar as rotas"
    }
    val text: LiveData<String> = _text
}