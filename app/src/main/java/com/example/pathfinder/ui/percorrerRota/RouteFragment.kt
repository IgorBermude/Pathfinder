package com.example.pathfinder.ui.percorrerRota

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pathfinder.R
import com.example.pathfinder.ui.components.MapaFragment
import com.example.pathfinder.data.models.Rota
import com.example.pathfinder.ui.home.HomeViewModel
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.api.directions.v5.DirectionsCriteria
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RouteFragment : Fragment() {
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var mapaFragment: MapaFragment
    private var rota: Rota? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Recupera a última rota do HomeViewModel
        rota = homeViewModel.obterUltimaRota()

        // Se precisar do mapa, recupere o fragmento já existente
        mapaFragment = (requireParentFragment()
            .childFragmentManager
            .findFragmentById(R.id.map_container) as? MapaFragment)!!



        mapaFragment.cameraSeguir()
        // Chame o método para obter DirectionsRoute e simular
        rota?.let { rotaObj ->
            val accessToken = getString(R.string.mapbox_access_token)
            solicitarDirectionsRouteViaMapMatching(rotaObj, accessToken) { directionsRoute ->
                directionsRoute?.let {
                    mapaFragment.startSimulation(it)
                }
            }
        }

        return inflater.inflate(R.layout.fragment_route, container, false)
    }

    /**
     * Solicita uma DirectionsRoute válida usando o Map Matching API.
     */
    private fun solicitarDirectionsRouteViaMapMatching(rota: Rota, accessToken: String, onResult: (DirectionsRoute?) -> Unit) {
        val pontos = listOf(rota.origemRota) + rota.destinosRota.map { it.ponto }

        val mapMatchingRequest = MapboxMapMatching.builder()
            .accessToken(accessToken)
            .coordinates(pontos)
            .steps(true)
            .voiceInstructions(true)
            .bannerInstructions(true)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .build()

        mapMatchingRequest.enqueueCall(object : Callback<MapMatchingResponse> {
            override fun onResponse(call: Call<MapMatchingResponse>, response: Response<MapMatchingResponse>) {
                if (response.isSuccessful) {
                    val directionsRoute = response.body()?.matchings()?.firstOrNull()?.toDirectionRoute()
                    onResult(directionsRoute)
                } else {
                    onResult(null)
                }
            }
            override fun onFailure(call: Call<MapMatchingResponse>, throwable: Throwable) {
                onResult(null)
            }
        })
    }

}