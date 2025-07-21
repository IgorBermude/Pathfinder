package com.example.pathfinder.data.models.dto

// DTO serializável para Destino

data class DestinoDTO(
    val nomeDestino: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val distancia: Double? = null
)
