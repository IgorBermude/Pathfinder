package com.example.pathfinder.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.pathfinder.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.location

object NavigationViewUtils {
    fun esconderBottomNavigationView(activity: FragmentActivity) {
        val navView = activity.findViewById<View>(R.id.nav_view)
        navView?.animate()
            ?.translationY(navView.height.toFloat())
            ?.alpha(0f)
            ?.setDuration(250)
            ?.withEndAction { navView.visibility = View.GONE }
            ?.start()
    }

    fun mostrarBottomNavigationView(activity: FragmentActivity) {
        val navView = activity.findViewById<View>(R.id.nav_view)
        navView?.apply {
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .start()
        }
    }

    fun toggleActionBarForScreen(activity: FragmentActivity, hide: Boolean) {
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        if (hide) {
            actionBar?.hide()
        } else {
            actionBar?.show()
        }
    }

    fun observeUserLocation(mapView: MapView, onLocationUpdate: (Point) -> Unit) {
        mapView.location.addOnIndicatorPositionChangedListener { point ->
            onLocationUpdate(point)
        }
    }
}