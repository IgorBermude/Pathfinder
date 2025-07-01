package com.example.pathfinder.data.models

import java.util.Date
import com.google.firebase.firestore.DocumentId
import com.mapbox.geojson.Point

data class Rota(
    @DocumentId
    var idRota: String? = null,
    var origemRota: Point? = null,
    var destinosRota: List<Destino> = emptyList(),
    var criadorRota: Usuario? = null,
    var distanciaRota: Float? = 0f,
    var tempoTotalRota: String? = "00:00:00",
    var dtModificacaoRota: Date? = null,
    var nomeRota: String? = ""
)
