package com.example.pathfinder.data.repositories

import com.example.pathfinder.data.models.Destino
import com.example.pathfinder.data.models.Rota
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point

class RotaRepository {
    private val db = FirebaseFirestore.getInstance()

    fun salvarRota(
        rota: Rota,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val rotaMap = hashMapOf(
            "origemRota" to rota.origemRota?.let { mapOf("latitude" to it.latitude(), "longitude" to it.longitude()) },
            "destinosRota" to rota.destinosRota.map { destino ->
                mapOf(
                    "nomeDestino" to destino.nomeDestino,
                    "localDestino" to destino.localDestino.let { mapOf("latitude" to it.latitude(), "longitude" to it.longitude()) },
                    "distanciaDestino" to destino.distancia
                )
            },
            "criadorRotaId" to rota.criadorRotaId,
            "distanciaRota" to rota.distanciaRota,
            "tempoTotalRota" to rota.tempoTotalRota,
            "dtModificacaoRota" to rota.dtModificacaoRota,
            "nomeRota" to rota.nomeRota
        )

        db.collection("rotas")
            .add(rotaMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun buscarRotasPorUsuario(
        usuarioId: String,
        onSuccess: (List<Rota>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("rotas")
            .whereEqualTo("criadorRotaId", usuarioId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rotas = querySnapshot.documents.mapNotNull { document ->
                    try {
                        val data = document.data ?: return@mapNotNull null
                        // Origem
                        val origemMap = data["origemRota"] as? Map<*, *>
                        val origemLatitude = origemMap?.get("latitude") as? Double
                        val origemLongitude = origemMap?.get("longitude") as? Double
                        val origemPoint = if (origemLatitude != null && origemLongitude != null) {
                            com.mapbox.geojson.Point.fromLngLat(origemLongitude, origemLatitude)
                        } else null
                        // Destinos
                        val destinosList = data["destinosRota"] as? List<*>
                        val destinos = destinosList?.mapNotNull { destinoObj ->
                            val destinoMap = destinoObj as? Map<*, *> ?: return@mapNotNull null
                            val nomeDestino = destinoMap["nomeDestino"] as? String ?: ""
                            val localDestinoMap = destinoMap["localDestino"] as? Map<*, *>
                            val lat = localDestinoMap?.get("latitude") as? Double ?: 0.0
                            val lng = localDestinoMap?.get("longitude") as? Double ?: 0.0
                            val distancia = destinoMap["distanciaDestino"] as? Double
                            Destino(
                                nomeDestino = nomeDestino,
                                localDestino = Point.fromLngLat(lng, lat),
                                distancia = distancia
                            )
                        } ?: emptyList()
                        Rota(
                            idRota = document.id,
                            origemRota = origemPoint,
                            destinosRota = destinos,
                            criadorRotaId = data["criadorRotaId"] as? String,
                            distanciaRota = data["distanciaRota"] as? Double,
                            tempoTotalRota = (data["tempoTotalRota"] as? Number)?.toLong(),
                            dtModificacaoRota = data["dtModificacaoRota"] as? java.util.Date,
                            nomeRota = data["nomeRota"] as? String
                        )
                    } catch (e: Exception) {
                        throw RuntimeException("Erro ao mapear rota: ${e.message}", e)
                    }
                }
                onSuccess(rotas)
            }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}