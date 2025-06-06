package com.example.pathfinder.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.pathfinder.R
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.vectorSource
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar

class MapaFragment : Fragment() {

    private lateinit var mapView: MapView
    private val mapManager = MapManeger // Use o MapManager para gerenciar a instância do mapa
    private var instance: MapaFragment? = null
    private lateinit var mapMarkersManager: MapMarkersManager

    private val permissionRequestCode = 1001
    private val SEARCH_REQUEST_CODE = 1001
    private val defaultCamera = CameraOptions.Builder()
        .zoom(2.0)
        .center(Point.fromLngLat(-98.0, 39.5))
        .pitch(0.0)
        .bearing(0.0)
        .build()

    private val preferences by lazy {
        requireContext().getSharedPreferences("map_state", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mapView = MapView(requireContext())
        mapManager.initialize(mapView) // Inicialize o MapManager com o MapView

        if (hasLocationPermission()) {
            initializeMap()
        } else {
            requestLocationPermission()
        }

        return mapView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

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
        mapManager.getMapView()?.getMapboxMap()?.loadStyleUri(Style.STANDARD_EXPERIMENTAL) { style ->
            enableLocationComponent()
            restoreCameraState()
            trackCameraChanges()
            mapManager.getMapView()?.compass?.enabled = false
            mapManager.getMapView()?.scalebar?.enabled = false

            addTrafficPoints()
        }
    }

    private fun addTrafficPoints() {
        val annotationManager = mapManager.getMapView()?.annotations?.createPointAnnotationManager()

        val trafficPoints = listOf(
            Triple("Semáforo", -98.0, 39.5),
            Triple("Radar", -98.2, 39.7),
            Triple("Obra", -98.1, 39.6)
        )

        for ((label, lng, lat) in trafficPoints) {
            annotationManager?.create(
                PointAnnotationOptions()
                    .withPoint(Point.fromLngLat(lng, lat))
                    .withTextField(label)
            )
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

}
