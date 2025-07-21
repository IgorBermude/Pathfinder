package com.example.pathfinder.data.models

import com.mapbox.geojson.Point

data class Destino(
    val nomeDestino: String = "",
    val localDestino: Point = Point.fromLngLat(0.0, 0.0),
    val distancia: Double? = null,
)
