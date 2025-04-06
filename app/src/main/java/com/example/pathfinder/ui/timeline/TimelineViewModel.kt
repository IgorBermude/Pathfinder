package com.example.pathfinder.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimelineViewModel : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "Essa é a pagina de timeline"
    }
    val text: LiveData<String> = _text
}