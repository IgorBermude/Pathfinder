package com.example.pathfinder.ui.percorrerRota

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.pathfinder.R
import com.example.pathfinder.ui.components.MapaFragment
import com.example.pathfinder.data.models.Rota
import com.example.pathfinder.databinding.FragmentRouteBinding
import com.example.pathfinder.ui.home.HomeFragment
import com.example.pathfinder.ui.home.HomeViewModel
import com.example.pathfinder.util.NavigationViewUtils
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.api.matching.v5.MapboxMapMatching
import com.mapbox.api.matching.v5.models.MapMatchingResponse
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.bindgen.Expected
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.TimeFormat
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.core.formatter.MapboxDistanceFormatter
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.maneuver.api.MapboxManeuverApi
import com.mapbox.navigation.tripdata.progress.api.MapboxTripProgressApi
import com.mapbox.navigation.tripdata.progress.model.DistanceRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.EstimatedTimeToArrivalFormatter
import com.mapbox.navigation.tripdata.progress.model.PercentDistanceTraveledFormatter
import com.mapbox.navigation.tripdata.progress.model.TimeRemainingFormatter
import com.mapbox.navigation.tripdata.progress.model.TripProgressUpdateFormatter
import com.mapbox.navigation.tripdata.speedlimit.api.MapboxSpeedInfoApi
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer
import com.mapbox.navigation.ui.components.tripprogress.model.TripProgressViewOptions
import com.mapbox.navigation.ui.components.tripprogress.view.MapboxTripProgressView
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.voice.api.MapboxSpeechApi
import com.mapbox.navigation.voice.api.MapboxVoiceInstructionsPlayer
import com.mapbox.navigation.voice.model.SpeechAnnouncement
import com.mapbox.navigation.voice.model.SpeechError
import com.mapbox.navigation.voice.model.SpeechValue
import com.mapbox.navigation.voice.model.SpeechVolume
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck

class RouteFragment : Fragment() {
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var mapaFragment: MapaFragment
    private var rota: Rota? = null
    private var _binding: FragmentRouteBinding? = null
    private val binding get() = _binding!!

    private companion object {
        private const val BUTTON_ANIMATION_DURATION = 1500L
    }

    /**
     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
     * and remaining distance to the maneuver point.
     */
    private lateinit var maneuverApi: MapboxManeuverApi

    /**
     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
     */
    private lateinit var tripProgressApi: MapboxTripProgressApi

    /**
     * Gets notified with progress along the currently active route.
     */
    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
        // update the camera position to account for the progressed fragment of the route
        mapaFragment.viewportDataSource.onRouteProgressChanged(routeProgress)
        mapaFragment.viewportDataSource.evaluate()

        // draw the upcoming maneuver arrow on the map
        val style = mapaFragment.mapView.mapboxMap.style
        if (style != null) {
            val maneuverArrowResult = mapaFragment.routeArrowApi.addUpcomingManeuverArrow(routeProgress)
            mapaFragment.routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
        }

        // update top banner with maneuver instructions
        val maneuvers = maneuverApi.getManeuvers(routeProgress)
        maneuvers.fold(
            { error ->
                Toast.makeText(
                    requireContext(),
                    error.errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            },
            {
                binding.maneuverView.visibility = View.VISIBLE
                binding.maneuverView.renderManeuvers(maneuvers)
            }
        )

        // update bottom trip progress summary
        val routeInfoContainer = requireActivity().findViewById<ViewGroup>(R.id.route_info_container)
        val tripProgressView = routeInfoContainer.findViewById<MapboxTripProgressView>(R.id.tripProgressView)
        tripProgressView?.render(
            tripProgressApi.getTripProgress(routeProgress)
        )
    }


    /**
     * Extracts message that should be communicated to the driver about the upcoming maneuver.
     * When possible, downloads a synthesized audio file that can be played back to the driver.
     */
    private lateinit var speechApi: MapboxSpeechApi

    /**
     * Plays the synthesized audio files with upcoming maneuver instructions
     * or uses an on-device Text-To-Speech engine to communicate the message to the driver.
     * NOTE: do not use lazy initialization for this class since it takes some time to initialize
     * the system services required for on-device speech synthesis. With lazy initialization
     * there is a high risk that said services will not be available when the first instruction
     * has to be played. [MapboxVoiceInstructionsPlayer] should be instantiated in
     * `Activity#onCreate`.
     */
    private lateinit var voiceInstructionsPlayer: MapboxVoiceInstructionsPlayer

    /**
     * When a synthesized audio file was downloaded, this callback cleans up the disk after it was played.
     */
    private val voiceInstructionsPlayerCallback =
        MapboxNavigationConsumer<SpeechAnnouncement> { value ->
            // remove already consumed file to free-up space
            speechApi.clean(value)
        }

    /**
     * Based on whether the synthesized audio file is available, the callback plays the file
     * or uses the fall back which is played back using the on-device Text-To-Speech engine.
     */
    private val speechCallback =
        MapboxNavigationConsumer<Expected<SpeechError, SpeechValue>> { expected ->
            expected.fold(
                { error ->
                    // play the instruction via fallback text-to-speech engine
                    voiceInstructionsPlayer.play(
                        error.fallback,
                        voiceInstructionsPlayerCallback
                    )
                },
                { value ->
                    // play the sound file from the external generator
                    voiceInstructionsPlayer.play(
                        value.announcement,
                        voiceInstructionsPlayerCallback
                    )
                }
            )
        }

    /**
     * Stores and updates the state of whether the voice instructions should be played as they come or muted.
     */
    private var isVoiceInstructionsMuted = false
        set(value) {
            field = value
            if (value) {
                binding.soundButton.muteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(0f))
            } else {
                binding.soundButton.unmuteAndExtend(BUTTON_ANIMATION_DURATION)
                voiceInstructionsPlayer.volume(SpeechVolume(1f))
            }
        }


    /**
     * Observes when a new voice instruction should be played.
     */
    private val voiceInstructionsObserver = VoiceInstructionsObserver { voiceInstructions ->
        speechApi.generate(voiceInstructions, speechCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    private lateinit var tripProgressCard: View

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteBinding.inflate(inflater, container, false)
        // Recupera a última rota do HomeViewModel
        rota = homeViewModel.obterUltimaRota()

        NavigationViewUtils.esconderBottomNavigationView(requireActivity())

        // Altere a cor da status bar (barra superior)
        //requireActivity().window.statusBarColor = resources.getColor(R.color.blue_gray, null)

        // Altere a cor da navigation bar (barra inferior)
        //requireActivity().window.navigationBarColor = resources.getColor(R.color.white, null)

        // Se precisar do mapa, recupere o fragmento já existente
        mapaFragment = (requireParentFragment()
            .childFragmentManager
            .findFragmentById(R.id.map_container) as? MapaFragment)!!

        // make sure to use the same DistanceFormatterOptions across different features
        val distanceFormatterOptions = DistanceFormatterOptions.Builder(requireContext()).build()

        // Infla o item_inforota no route_info_container
        val routeInfoContainer = requireActivity().findViewById<ViewGroup>(R.id.route_info_container)
        val inflater = LayoutInflater.from(requireContext())
        val infoView = inflater.inflate(R.layout.item_inforota, routeInfoContainer, false)

        // Referencie corretamente os elementos do item_inforota
        tripProgressCard = infoView.findViewById(R.id.tripProgressCard)
        val tripProgressView = infoView.findViewById<MapboxTripProgressView>(R.id.tripProgressView)
        val stopButton = infoView.findViewById<View>(R.id.stop)

        val options = TripProgressViewOptions.Builder()
            .backgroundColor(R.color.white_gray)
            .distanceRemainingTextAppearance(R.style.TripProgressTextCentered)
            // Adicione outras customizações aqui
            .build()

        tripProgressView.updateOptions(options)

        tripProgressCard.visibility = View.VISIBLE

        // initialize maneuver api that feeds the data to the top banner maneuver view
        maneuverApi = MapboxManeuverApi(
            MapboxDistanceFormatter(distanceFormatterOptions)
        )

        // initialize bottom progress view
        tripProgressApi = MapboxTripProgressApi(
            TripProgressUpdateFormatter.Builder(requireContext())
                .distanceRemainingFormatter(
                    DistanceRemainingFormatter(distanceFormatterOptions)
                )
                .timeRemainingFormatter(
                    TimeRemainingFormatter(requireContext())
                )
                .percentRouteTraveledFormatter(
                    PercentDistanceTraveledFormatter()
                )
                .estimatedTimeToArrivalFormatter(
                    EstimatedTimeToArrivalFormatter(requireContext(), TimeFormat.NONE_SPECIFIED)
                )
                .build()
        )

        // initialize voice instructions api and the voice instruction player
        speechApi = MapboxSpeechApi(
            requireContext(),
            "pt-BR"
        )
        voiceInstructionsPlayer = MapboxVoiceInstructionsPlayer(
            requireContext(),
            "pt-BR"
        )

        // initialize view interactions
        stopButton.setOnClickListener {
            encerrarFragmento()
        }
        binding.recenter.setOnClickListener {
            mapaFragment.navigationCamera.requestNavigationCameraToFollowing()
            binding.recenter.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.routeOverview.setOnClickListener {
            mapaFragment.navigationCamera.requestNavigationCameraToOverview()
            binding.routeOverview.showTextAndExtend(BUTTON_ANIMATION_DURATION)
        }
        binding.soundButton.setOnClickListener {
            // mute/unmute voice instructions
            isVoiceInstructionsMuted = !isVoiceInstructionsMuted
        }

        // set initial sounds button state
        binding.soundButton.unmute()

        routeInfoContainer.addView(infoView)

        // Registre o observer APÓS tudo estar pronto
        // Remova qualquer registro duplicado em outros lugares
        mapaFragment.setRouteProgressObserver(routeProgressObserver)

        mapaFragment.setVoiceInstructionsObserver(voiceInstructionsObserver)

        // initialize location puck
        mapaFragment.mapView.location.apply {
            setLocationProvider(mapaFragment.navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.Companion.from(
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            puckBearingEnabled = true
            enabled = true
        }

        // show UI elements
        binding.soundButton.visibility = View.VISIBLE
        binding.routeOverview.visibility = View.VISIBLE
        //binding.tripProgressCard.visibility = View.VISIBLE
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

        if (mapaFragment.limitInfo != null) {
            binding.speedLimitView.isVisible = true
            binding.speedLimitView.render(mapaFragment.limitInfo!!)
        } else {
            binding.speedLimitView.isVisible = false
        }

        mapaFragment.navigationCamera.requestNavigationCameraToFollowing()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun clearRouteAndStopNavigation() {
        // clear
        //mapaFragment.mapboxNavigation.setNavigationRoutes(listOf())

        // Limpa as setas
        mapaFragment.routeArrowApi.clearArrows()

        // stop simulation
        mapaFragment.stopSimulation()

        // hide UI elements
        binding.soundButton.visibility = View.INVISIBLE
        binding.maneuverView.visibility = View.INVISIBLE
        binding.routeOverview.visibility = View.INVISIBLE
        tripProgressCard.visibility = View.GONE
    }

    private fun encerrarFragmento(){
        // Limpa rota e para navegação
        clearRouteAndStopNavigation()

        // Reverta o navigation puck para o estado original
        mapaFragment.mapView.location.apply {
            setLocationProvider(mapaFragment.navigationLocationProvider)
            this.locationPuck = createDefault2DPuck()
            puckBearingEnabled = true
            enabled = true
        }

        // Mostra novamente os componentes da HomeFragment
        val homeFragment = requireParentFragment() as? HomeFragment
        homeFragment?.let {
            it.mostrarComponentes()
            NavigationViewUtils.mostrarBottomNavigationView(requireActivity())
        }

        // Remove RouteFragment
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remova o observer apenas aqui
        mapaFragment.removeRouteProgressObserver(routeProgressObserver)
        mapaFragment.removeVoiceInstructionsObserver(voiceInstructionsObserver)
        maneuverApi.cancel()
        speechApi.cancel()
        voiceInstructionsPlayer.shutdown()
        _binding = null
    }
}