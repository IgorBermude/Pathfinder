package com.example.pathfinder.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Destino
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.extensions.applyLanguageAndVoiceUnitOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.navigation.base.route.NavigationRouterCallback
import com.mapbox.navigation.base.route.RouterFailure
import com.mapbox.navigation.core.directions.session.RoutesObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.NavigationRouteLine
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineViewOptions
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineApiOptions
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineColorResources
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
import com.mapbox.common.location.toAndroidLocation
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.common.location.Location
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxDelicateApi
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.base.formatter.DistanceFormatterOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.replay.route.ReplayProgressObserver
import com.mapbox.navigation.core.replay.route.ReplayRouteMapper
import com.mapbox.navigation.core.trip.session.OffRouteObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.core.trip.session.VoiceInstructionsObserver
import com.mapbox.navigation.tripdata.speedlimit.api.MapboxSpeedInfoApi
import com.mapbox.navigation.tripdata.speedlimit.model.SpeedInfoValue
import com.mapbox.navigation.ui.maps.camera.NavigationCamera
import com.mapbox.navigation.ui.maps.camera.data.MapboxNavigationViewportDataSource
import com.mapbox.navigation.ui.maps.camera.lifecycle.NavigationBasicGesturesHandler
import com.mapbox.navigation.ui.maps.camera.state.NavigationCameraState
import com.mapbox.navigation.ui.maps.camera.transition.NavigationCameraTransitionOptions
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowApi
import com.mapbox.navigation.ui.maps.route.arrow.api.MapboxRouteArrowView
import com.mapbox.navigation.ui.maps.route.arrow.model.RouteArrowOptions
import java.util.Date

class MapaFragment : Fragment() {

    lateinit var mapView: MapView
    private val mapManager = MapManeger // Use o MapManager para gerenciar a instância do mapa
    private var instance: MapaFragment? = null
    private lateinit var mapMarkersManager: MapMarkersManager
    private val permissionRequestCode = 1001
    private val SEARCH_REQUEST_CODE = 1001
    private val preferences by lazy {
        requireContext().getSharedPreferences("map_state", Context.MODE_PRIVATE)
    }
    // Navigation UI helpers
    val navigationLocationProvider = NavigationLocationProvider()
    private lateinit var routeLineApi: MapboxRouteLineApi
    private lateinit var routeLineView: MapboxRouteLineView

    var limitInfo: SpeedInfoValue? = null

    /**
     * Gets notified with location updates.
     *
     * Exposes raw updates coming directly from the location services
     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
     */
    private val locationObserver = object : LocationObserver {
        var firstLocationUpdateReceived = false

        override fun onNewRawLocation(rawLocation: Location) {
            // not handled
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            // update location puck's position on the map
            navigationLocationProvider.changePosition(
                location = enhancedLocation,
                keyPoints = locationMatcherResult.keyPoints,
            )

            // update camera position to account for new location
            viewportDataSource.onLocationChanged(enhancedLocation)
            viewportDataSource.evaluate()

            // if this is the first location update the activity has received,
            // it's best to immediately move the camera to the current user location
            if (!firstLocationUpdateReceived) {
                firstLocationUpdateReceived = true
                cameraSeguir()
            }

            limitInfo = speedInfoApi.updatePostedAndCurrentSpeed(
                locationMatcherResult,
                distanceFormatterOptions
            )
        }
    }

    /**
     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
     */
    val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()

    /**
     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
     */
    private lateinit var replayProgressObserver: ReplayProgressObserver

    /**
     * Debug object that converts a route into events that can be replayed to navigate a route.
     */
    private val replayRouteMapper = ReplayRouteMapper()

    // Delegate para o MapboxNavigation conforme recomendado
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerRoutesObserver(routesObserver)
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.startTripSession()
                mapboxNavigation.registerOffRouteObserver(offRouteObserver)

                replayProgressObserver = ReplayProgressObserver(mapboxNavigation.mapboxReplayer)
                mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
            }
            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterRoutesObserver(routesObserver)
                mapboxNavigation.unregisterLocationObserver(locationObserver)
                mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
                mapboxNavigation.unregisterOffRouteObserver(offRouteObserver)
            }
        }
    )

    /**
     * Draws maneuver arrows on the map based on the data [routeArrowApi].
     */
    lateinit var routeArrowView: MapboxRouteArrowView

    val offRouteObserver = object : OffRouteObserver {
        override fun onOffRouteStateChanged(offRoute: Boolean) {
        }
    }

    private val routesObserver = RoutesObserver { routeUpdateResult ->
        if (routeUpdateResult.navigationRoutes.isNotEmpty()) {
            // generate route geometries asynchronously and render them
            routeLineApi.setNavigationRoutes(
                routeUpdateResult.navigationRoutes
            ) { value ->
                mapView.getMapboxMap().getStyle()?.apply {
                    routeLineView.renderRouteDrawData(this, value)
                }
            }

            // update the camera position to account for the new route
            viewportDataSource.onRouteChanged(routeUpdateResult.navigationRoutes.first())
            viewportDataSource.evaluate()
        } else {
            // remove the route line and route arrow from the map
            val style = mapView.mapboxMap.style
            if (style != null) {
                routeLineApi.clearRouteLine { value ->
                    routeLineView.renderClearRouteLineValue(
                        style,
                        value
                    )
                }
                routeArrowView.render(style, routeArrowApi.clearArrows())
            }

            // remove the route reference from camera position evaluations
            viewportDataSource.clearRouteData()
            viewportDataSource.evaluate()
        }
    }


    lateinit var navigationCamera: NavigationCamera
    lateinit var viewportDataSource: MapboxNavigationViewportDataSource
    private val pixelDensity = Resources.getSystem().displayMetrics.density
    private val overviewPadding: EdgeInsets by lazy {
        EdgeInsets(
            140.0 * pixelDensity,
            40.0 * pixelDensity,
            120.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }
    private val followingPadding: EdgeInsets by lazy {
        EdgeInsets(
            180.0 * pixelDensity,
            40.0 * pixelDensity,
            150.0 * pixelDensity,
            40.0 * pixelDensity
        )
    }

    /**
     * API used for formatting speed limit related data.
     */
    private val speedInfoApi: MapboxSpeedInfoApi by lazy {
        MapboxSpeedInfoApi()
    }

    /**
     * Options used for formatting speed information, such as mph or km/h.
     * By default, the unit type will be determined based on the device's locale.
     */
    private val distanceFormatterOptions: DistanceFormatterOptions by lazy {
        DistanceFormatterOptions.Builder(requireContext()).build()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mapView = MapView(requireContext())
        mapManager.initialize(mapView) // Inicialize o MapManager com o MapView

        // Inicialize o mapMarkersManager após o mapView estar disponível
        mapMarkersManager = MapMarkersManager(requireContext(), mapView)

        // Inicialize RouteLineApi e RouteLineView
        val routeLineViewOptions = MapboxRouteLineViewOptions.Builder(requireContext())
            .routeLineColorResources(RouteLineColorResources.Builder().build())
            .routeLineBelowLayerId("road-label-navigation")
            .build()
        routeLineView = MapboxRouteLineView(routeLineViewOptions)
        routeLineApi = MapboxRouteLineApi(MapboxRouteLineApiOptions.Builder().build())

        // initialize maneuver arrow view to draw arrows on the map
        val routeArrowOptions = RouteArrowOptions.Builder(requireContext()).build()
        routeArrowView = MapboxRouteArrowView(routeArrowOptions)

        if (hasLocationPermission()) {
            initializeMap()
        } else {
            requestLocationPermission()
        }

        val mapboxMap = mapView.mapboxMap

        // initialize Navigation Camera
        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
        navigationCamera = NavigationCamera(
            mapboxMap,
            mapView.camera,
            viewportDataSource
        )

        // set the animations lifecycle listener to ensure the NavigationCamera stops
        // automatically following the user location when the map is interacted with
        mapView.camera.addCameraAnimationsLifecycleListener(
            NavigationBasicGesturesHandler(navigationCamera)
        )
        // set the padding values depending to correctly frame maneuvers and the puck
        viewportDataSource.overviewPadding = overviewPadding
        viewportDataSource.followingPadding = followingPadding

        return mapView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SEARCH_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val locationName = data?.getStringExtra("location_name")
            val latitude = data?.getDoubleExtra("latitude", 0.0)
            val longitude = data?.getDoubleExtra("longitude", 0.0)

            if (latitude != null && longitude != null) {
                markLocationOnMap(locationName, latitude, longitude)
            }
        }
    }

    private fun markLocationOnMap(locationName: String?, latitude: Double, longitude: Double) {
        mapManager.getMapView()?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(14.0)
                .build()
        )

        // Adicionar um marcador no mapa
        val annotationManager = mapManager.getMapView()?.annotations?.createPointAnnotationManager()
        annotationManager?.create(
            PointAnnotationOptions()
                .withPoint(Point.fromLngLat(longitude, latitude))
                .withTextField(locationName ?: "Local")
        )
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            permissionRequestCode
        )
    }

    private fun initializeMap() {
        mapManager.getMapView()?.getMapboxMap()?.loadStyleUri("mapbox://styles/mapbox/streets-v12") { style ->
            // Associe o LocationProvider do MapView ao navigationLocationProvider
            mapView.location.apply {
                setLocationProvider(navigationLocationProvider)
                enabled = true
            }
            enableLocationComponent()
            restoreCameraState()
            trackCameraChanges()
            mapManager.getMapView()?.compass?.enabled = false
            mapManager.getMapView()?.scalebar?.enabled = false
        }
    }

    private fun enableLocationComponent() {
        mapManager.getMapView()?.location?.updateSettings {
            enabled = true
        }
    }

    private fun restoreCameraState() {
        val zoom = preferences.getFloat("zoom", 2.0f).toDouble()
        val lat = preferences.getFloat("latitude", 39.5f).toDouble()
        val lng = preferences.getFloat("longitude", -98.0f).toDouble()
        val pitch = preferences.getFloat("pitch", 0.0f).toDouble()
        val bearing = preferences.getFloat("bearing", 0.0f).toDouble()

        val camera = CameraOptions.Builder()
            .zoom(zoom)
            .center(Point.fromLngLat(lng, lat))
            .pitch(pitch)
            .bearing(bearing)
            .build()

        mapManager.getMapView()?.getMapboxMap()?.setCamera(camera)
    }

    private fun trackCameraChanges() {
        mapManager.getMapView()?.getMapboxMap()?.addOnCameraChangeListener {
            saveCameraState(mapManager.getMapView()?.getMapboxMap()?.cameraState!!)
        }
    }

    private fun saveCameraState(state: CameraState) {
        preferences.edit().apply {
            putFloat("zoom", state.zoom.toFloat())
            putFloat("latitude", state.center.latitude().toFloat())
            putFloat("longitude", state.center.longitude().toFloat())
            putFloat("pitch", state.pitch.toFloat())
            putFloat("bearing", state.bearing.toFloat())
            apply()
        }
    }

    fun getInstance(): MapaFragment {
        if (instance == null) {
            instance = MapaFragment()
        }
        return instance!!
    }

    fun addMarker(latitude: Double, longitude: Double) {
        val point = Point.fromLngLat(longitude, latitude)
        mapMarkersManager.showMarker(point, R.drawable.location_pin) // Adiciona o marcador no mapa
    }

    fun removeLastMarker() {
        mapMarkersManager.removeLastMarker() // Remove o último marcador adicionado
    }

    fun centralizeUserLocation() {
        val oneTimeListener = OnIndicatorPositionChangedListener { point ->
            val cameraOptions = CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
            mapView.mapboxMap.flyTo(cameraOptions)
        }
        mapView.location.addOnIndicatorPositionChangedListener(oneTimeListener)
        mapView.location.removeOnIndicatorPositionChangedListener(oneTimeListener)
    }

    fun setupMapMoveListener(targetIcon: ImageView) {
        mapView.gestures.addOnMoveListener(
            object : com.mapbox.maps.plugin.gestures.OnMoveListener {
                override fun onMoveBegin(detector: com.mapbox.android.gestures.MoveGestureDetector) {
                    targetIcon.setImageResource(R.drawable.target)
                    targetIcon.setColorFilter(requireContext().getColor(R.color.black))
                }
                override fun onMove(detector: com.mapbox.android.gestures.MoveGestureDetector): Boolean = false
                override fun onMoveEnd(detector: com.mapbox.android.gestures.MoveGestureDetector) {}
            }
        )
    }

    fun getMapMarkersManager(): MapMarkersManager = mapMarkersManager

    fun getUserLocation(callback: (android.location.Location?) -> Unit) {
        val tempLocationObserver = object : LocationObserver {
            override fun onNewRawLocation(rawLocation: Location) {
                // Não utilizado aqui
            }

            override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
                // Converte de com.mapbox.common.location.Location para android.location.Location
                val androidLocation = locationMatcherResult.enhancedLocation.toAndroidLocation()
                callback(androidLocation)
                mapboxNavigation.unregisterLocationObserver(this)
            }
        }
        mapboxNavigation.registerLocationObserver(tempLocationObserver)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        routeLineApi.cancel()
        routeLineView.cancel()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    fun requestRoutes(
        origin: Point,
        destinos: List<Destino>,
        onRouteReady: (List<Destino>) -> Unit
    ) {
        val replayRouteMapper = ReplayRouteMapper()

        // A lista começa pelo origin e segue com todos os destinos (convertendo para Point)
        val points = listOf(origin) + destinos.map { it.ponto }

        val routeOptions = RouteOptions.builder()
            .applyDefaultNavigationOptions()
            .applyLanguageAndVoiceUnitOptions(requireContext())
            .coordinatesList(points)
            .alternatives(false)
            .language("pt")
            .build()

        mapboxNavigation.requestRoutes(
            routeOptions,
            object : NavigationRouterCallback {
                override fun onRoutesReady(
                    routes: List<NavigationRoute>,
                    routerOrigin: String
                ) {
                    mapboxNavigation.setNavigationRoutes(routes)

                    val routeCoordinates = routes.firstOrNull()
                        ?.directionsRoute
                        ?.geometry()
                        ?.let { LineString.fromPolyline(it, 6).coordinates() }
                        ?: emptyList()
                    // Mapeia os Points retornados para Destinos (mantendo nome e distância se possível)
                    val destinosAtualizados = destinos.mapIndexed { idx, destino ->
                        if (idx < routeCoordinates.size) destino.copy(ponto = routeCoordinates[idx]) else destino
                    }
                    onRouteReady(destinosAtualizados)
                }
                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: String) {}
                override fun onFailure(reasons: List<RouterFailure>, routeOptions: RouteOptions) {}
            }
        )
    }

    fun clearRoutes() {
        mapboxNavigation.setNavigationRoutes(emptyList())
        routeLineApi.clearRouteLine { value ->
            mapView.getMapboxMap().getStyle()?.let { style ->
                routeLineView.renderClearRouteLineValue(style, value)
            }
        }
    }

    @OptIn(MapboxDelicateApi::class)
    fun updateCamera(originPoint: Point, destinationPoint: Point) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        val overviewOption = mapView.mapboxMap.cameraForCoordinates(
            listOf(
                originPoint,
                destinationPoint
            ),
            CameraOptions.Builder()
                .padding(EdgeInsets(100.0, 100.0, 100.0, 100.0))
                .build(),
            null,
            null,
            null,
        )

        mapView.camera.easeTo(
            overviewOption,
            mapAnimationOptions
        )
    }

    fun cameraSeguir(){
        navigationCamera.requestNavigationCameraToOverview(
            stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
                .maxDuration(0)
                .build()
        )
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
     fun startSimulation(route: DirectionsRoute) {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
        val replayData = replayRouteMapper.mapDirectionsRouteGeometry(route)
        mapboxNavigation.mapboxReplayer.pushEvents(replayData)
        mapboxNavigation.mapboxReplayer.seekTo(replayData[0])
        mapboxNavigation.mapboxReplayer.play()
    }

    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
     fun stopSimulation() {
        mapboxNavigation.mapboxReplayer.stop()
        mapboxNavigation.mapboxReplayer.clearEvents()
    }


    fun setRouteProgressObserver(observer: RouteProgressObserver) {
        mapboxNavigation.registerRouteProgressObserver(observer)
    }

    fun removeRouteProgressObserver(observer: RouteProgressObserver) {
        mapboxNavigation.unregisterRouteProgressObserver(observer)
    }

    fun setVoiceInstructionsObserver(observer: VoiceInstructionsObserver) {
        mapboxNavigation.registerVoiceInstructionsObserver(observer)
    }

    fun removeVoiceInstructionsObserver(observer: VoiceInstructionsObserver) {
        mapboxNavigation.unregisterVoiceInstructionsObserver(observer)
    }

    fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(requireContext())
                .build()
        )

        // initialize location puck
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
            this.locationPuck = LocationPuck2D(
                bearingImage = ImageHolder.Companion.from(
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
            puckBearingEnabled = true
            enabled = true
        }
    }
}