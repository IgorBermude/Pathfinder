package com.example.pathfinder.data.models

import com.mapbox.geojson.Point

data class Destino(
    val nome: String,
    val ponto: Point,
    val distancia: Double? = null // em Km, pode ser nulo se não calculado ainda
)
