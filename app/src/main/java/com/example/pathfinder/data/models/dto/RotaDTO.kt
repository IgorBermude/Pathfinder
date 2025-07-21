package com.example.pathfinder.data.models.dto

import java.util.Date

// DTO serializável para Rota

data class RotaDTO(
    var idRota: String? = null,
    var origemLatitude: Double? = null,
    var origemLongitude: Double? = null,
    var destinosRota: List<DestinoDTO> = emptyList(),
    var criadorRotaId: String? = null,
    var distanciaRota: Double? = null,
    var tempoTotalRota: Long? = null,
    var dtModificacaoRota: Date? = null,
    var nomeRota: String? = ""
)
