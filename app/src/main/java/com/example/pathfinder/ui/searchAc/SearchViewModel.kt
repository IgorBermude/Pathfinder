package com.example.pathfinder.ui.searchAc

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import com.mapbox.search.ApiType
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings

class SearchViewModel : ViewModel() {
    val pointLiveData = MutableLiveData<Point>()

    fun setPoint(point: Point) {
        pointLiveData.value = point
    }
}