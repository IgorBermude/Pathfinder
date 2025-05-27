package com.example.pathfinder.ui.components

import com.mapbox.android.gestures.Utils.dpToPx
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager

class MapMarkersManager(mapView: MapView) {

    private val mapboxMap = mapView.mapboxMap
    private val circleAnnotationManager = mapView.annotations.createCircleAnnotationManager(null)
    private val markers = mutableMapOf<String, Point>()

    var onMarkersChangeListener: (() -> Unit)? = null

    val hasMarkers: Boolean
        get() = markers.isNotEmpty()

    fun clearMarkers() {
        markers.clear()
        circleAnnotationManager.deleteAll()
    }

    fun showMarker(coordinate: Point) {
        showMarkers(listOf(coordinate))
    }

    fun showMarkers(coordinates: List<Point>) {
        clearMarkers()
        if (coordinates.isEmpty()) {
            onMarkersChangeListener?.invoke()
            return
        }

        coordinates.forEach { coordinate ->
            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                .withPoint(coordinate)
                .withCircleRadius(8.0)
                .withCircleColor("#ee4e8b")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")

            val annotation = circleAnnotationManager.create(circleAnnotationOptions)
            markers[annotation.id] = coordinate
        }

        val onOptionsReadyCallback: (CameraOptions) -> Unit = {
            mapboxMap.setCamera(it)
            onMarkersChangeListener?.invoke()
        }

        if (coordinates.size == 1) {
            val options = CameraOptions.Builder()
                .center(coordinates.first())
                .padding(MARKERS_INSETS_OPEN_CARD)
                .zoom(14.0)
                .build()
            onOptionsReadyCallback(options)
        } else {
            mapboxMap.cameraForCoordinates(
                coordinates,
                CameraOptions.Builder().build(),
                MARKERS_INSETS,
                null,
                null,
                onOptionsReadyCallback,
            )
        }
    }

    private companion object {

        val MARKERS_EDGE_OFFSET = dpToPx(64f).toDouble()
        val PLACE_CARD_HEIGHT = dpToPx(300f).toDouble()

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )

        const val PERMISSIONS_REQUEST_LOCATION = 0
    }
}
