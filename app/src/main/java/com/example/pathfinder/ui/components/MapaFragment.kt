package com.example.pathfinder.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar

class MapaFragment : Fragment() {

    private lateinit var mapView: MapView

    private val permissionRequestCode = 1001
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

        if (hasLocationPermission()) {
            initializeMap()
        } else {
            requestLocationPermission()
        }

        return mapView
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
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            enableLocationComponent()
            restoreCameraState()
            trackCameraChanges()
            mapView.compass.enabled = false
            mapView.scalebar.enabled = false
        }
    }

    private fun enableLocationComponent() {
        mapView.location.updateSettings {
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

        mapView.getMapboxMap().setCamera(camera)
    }


    private fun trackCameraChanges() {
        mapView.getMapboxMap().addOnCameraChangeListener {
            saveCameraState(mapView.getMapboxMap().cameraState)
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
}
