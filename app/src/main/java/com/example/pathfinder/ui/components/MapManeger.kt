package com.example.pathfinder.ui.components

import com.mapbox.maps.MapView

object MapManeger {
    private var mapView: MapView? = null

    fun initialize(mapView: MapView) {
        this.mapView = mapView
    }

    fun getMapView(): MapView? {
        return mapView
    }

    fun clear() {
        mapView = null // Limpa a instância do MapView, se necessário
    }
}
