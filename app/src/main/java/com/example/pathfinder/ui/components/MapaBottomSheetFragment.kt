package com.example.pathfinder.ui.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pathfinder.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.widget.LinearLayout
import com.mapbox.maps.MapView

class MapaBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mapa_bottom_sheet, container, false)

        // Exemplo de listeners para cada tipo de mapa
        view.findViewById<LinearLayout>(R.id.map_type_traffic).setOnClickListener {
            changeMapStyle(StyleType.TRAFFIC)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.map_type_streets).setOnClickListener {
            changeMapStyle(StyleType.STREETS)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.map_type_satellite).setOnClickListener {
            changeMapStyle(StyleType.SATELLITE)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.map_type_dark).setOnClickListener {
            changeMapStyle(StyleType.DARK)
            dismiss()
        }
        view.findViewById<LinearLayout>(R.id.map_type_standard).setOnClickListener {
            changeMapStyle(StyleType.STANDARD)
            dismiss()
        }

        return view
    }

    private fun changeMapStyle(styleType: StyleType) {
        // Envie um resultado para o fragmento pai ou utilize um ViewModel compartilhado
        val mapManager = MapManeger
        val mapView: MapView? = mapManager.getMapView()

        mapView?.mapboxMap?.loadStyle(styleType.value)
    }

    enum class StyleType(val value: String) {
        TRAFFIC("mapbox://styles/mapbox/traffic-day-v2"),
        STREETS("mapbox://styles/mapbox/streets-v12"),
        SATELLITE("mapbox://styles/mapbox/standard-satellite"),
        DARK("mapbox://styles/mapbox/dark-v10"),
        STANDARD("mapbox://styles/mapbox-map-design/standard-experimental-ime")
    }
}
