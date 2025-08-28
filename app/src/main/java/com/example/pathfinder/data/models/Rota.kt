package com.example.pathfinder.data.models

import com.mapbox.api.directions.v5.MapboxDirections
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.geojson.Point
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.firebase.Timestamp
import java.util.Date
import com.google.firebase.firestore.DocumentId

data class Rota(
    var idRota: String? = null,
    var origemRota: Point? = null,
    var destinosRota: List<Destino> = emptyList(), // Corrigido para destinosRota
    var criadorRotaId: String? = null,
    var distanciaRota: Double? = null,
    var tempoTotalRota: Long? = null,
    var dtModificacaoRota: Timestamp? = null,
    var nomeRota: String? = ""
){
    /*fun calcularDistanciaTotal(origin: Point, destination: Point) {
        val client = MapboxDirections.builder()
            .origin(origin)
            .destination(destination)
            .overview(DirectionsCriteria.OVERVIEW_FULL)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken("SEU_MAPBOX_ACCESS_TOKEN")
            .build()

        client.enqueueCall(object : Callback<DirectionsResponse> {
            override fun onResponse(
                call: Call<DirectionsResponse>,
                response: Response<DirectionsResponse>
            ) {
                val route = response.body()?.routes()?.firstOrNull()
                val distance = route?.distance() // Distância em metros
                println("Distância: $distance metros")
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                println("Erro ao buscar rota: ${t.message}")
            }
        })
    }*/
}
