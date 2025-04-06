package com.example.pathfinder.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Essa é a pagina inicial e pagina do mapa"
    }
    val text: LiveData<String> = _text
}