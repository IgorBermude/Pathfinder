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
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener

class MapaFragment : Fragment() {

    private lateinit var mapView: MapView

    private val preferences by lazy {
        requireContext().getSharedPreferences("map_state", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mapView = MapView(requireContext())

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            initializeMapWithLocation()
        }

        return mapView
    }

    private fun initializeMapWithLocation() {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            enableUserLocation()
            restoreSavedCamera()
            setupCameraListener()
        }
    }

    private fun enableUserLocation() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            enabled = true
        }
    }

    private fun restoreSavedCamera() {
        val savedZoom = preferences.getFloat("zoom", 2.0f).toDouble()
        val savedLat = preferences.getFloat("latitude", 39.5f).toDouble()
        val savedLng = preferences.getFloat("longitude", -98.0f).toDouble()
        val savedPitch = preferences.getFloat("pitch", 0.0f).toDouble()
        val savedBearing = preferences.getFloat("bearing", 0.0f).toDouble()

        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(savedZoom)
                .center(Point.fromLngLat(savedLng, savedLat))
                .pitch(savedPitch)
                .bearing(savedBearing)
                .build()
        )
    }

    private fun setupCameraListener() {
        mapView.getMapboxMap().addOnCameraChangeListener {
            val cameraState = mapView.getMapboxMap().cameraState
            saveCameraState(cameraState)
        }
    }



    private fun saveCameraState(cameraState: CameraState) {
        preferences.edit().apply {
            putFloat("zoom", cameraState.zoom?.toFloat() ?: 2.0f)
            putFloat("latitude", cameraState.center?.latitude()?.toFloat() ?: 39.5f)
            putFloat("longitude", cameraState.center?.longitude()?.toFloat() ?: -98.0f)
            putFloat("pitch", cameraState.pitch?.toFloat() ?: 0.0f)
            putFloat("bearing", cameraState.bearing?.toFloat() ?: 0.0f)
            apply()
        }
    }
}
