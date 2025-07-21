package com.example.pathfinder.util

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.pathfinder.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.locationcomponent.location
import android.app.AlertDialog
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext

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

    fun mostrarDialogNomeRota(onNomeConfirmado: (String) -> Unit, requireContext: FragmentActivity) {
        val editText = EditText(requireContext)
        editText.hint = "Digite o nome da rota"

        AlertDialog.Builder(requireContext)
            .setTitle("Salvar rota")
            .setView(editText)
            .setPositiveButton("Salvar") { _, _ ->
                val nomeRota = editText.text.toString().trim()
                if (nomeRota.isNotEmpty()) {
                    onNomeConfirmado(nomeRota)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}