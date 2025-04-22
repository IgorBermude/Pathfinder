package com.example.pathfinder.data.models

import java.util.Date

data class Rota(var idRota: Int? = null,
                var origemRota: Int? = null, // ID do Endereço de origem
                var criadorRota: Usuario? = null,
                var distanciaRota: Float? = null,
                var tempoTotalRota: String? = null, // formato "HH:mm:ss"
                var dtModificacaoRota: Date? = null // ou use com Timestamp do Firebase
)
