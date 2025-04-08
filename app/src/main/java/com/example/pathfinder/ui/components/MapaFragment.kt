package com.example.pathfinder.ui.components

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.CameraState
import com.mapbox.maps.MapView

class MapaFragment : Fragment() {

    private lateinit var mapView: MapView

    private val preferences by lazy {
        requireContext().getSharedPreferences("map_state", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        val savedZoom = preferences.getFloat("zoom", 2.0f).toDouble()
        val savedLat = preferences.getFloat("latitude", 39.5f).toDouble()
        val savedLng = preferences.getFloat("longitude", -98.0f).toDouble()
        val savedPitch = preferences.getFloat("pitch", 0.0f).toDouble()
        val savedBearing = preferences.getFloat("bearing", 0.0f).toDouble()

        mapView = MapView(requireContext())
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(savedZoom)
                .center(Point.fromLngLat(savedLng, savedLat))
                .pitch(savedPitch)
                .bearing(savedBearing)
                .build()
        )

        mapView.getMapboxMap().addOnCameraChangeListener {
            val cameraState = mapView.getMapboxMap().cameraState
            saveCameraState(cameraState)
        }

        return mapView
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
