package com.example.pathfinder.ui.searchAc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point

class SearchViewModel : ViewModel() {
    val pointLiveData = MutableLiveData<Point>()

    fun setPoint(point: Point) {
        pointLiveData.value = point
    }
}