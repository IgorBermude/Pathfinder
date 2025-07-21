package com.example.pathfinder.data.models

import java.util.Date
import com.google.firebase.firestore.DocumentId
import com.mapbox.geojson.Point

data class Rota(
    var idRota: String? = null,
    var origemRota: Point? = null,
    var destinosRota: List<Destino> = emptyList(), // Corrigido para destinosRota
    var criadorRotaId: String? = null,
    var distanciaRota: Double? = null,
    var tempoTotalRota: Long? = null,
    var dtModificacaoRota: Date? = null,
    var nomeRota: String? = ""
)
