package com.example.pathfinder.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class MapMarkersManager(private val context: Context, private val mapView: MapView) {

    private val mapboxMap = mapView.mapboxMap
    private val pointAnnotationManager = mapView.annotations.createPointAnnotationManager()
    private val markers = mutableMapOf<String, Point>()

    var onMarkersChangeListener: (() -> Unit)? = null

    val hasMarkers: Boolean
        get() = markers.isNotEmpty()

    fun clearMarkers() {
        markers.clear()
        pointAnnotationManager.deleteAll()
    }

    fun showMarker(coordinate: Point, iconResId: Int) {
        showMarkers(listOf(coordinate), iconResId)
    }

    fun showMarkers(coordinates: List<Point>, iconResId: Int) {
        clearMarkers()
        if (coordinates.isEmpty()) {
            onMarkersChangeListener?.invoke()
            return
        }

        // Obtenha o bitmap do recurso de imagem do ícone personalizado
        val pinBitmap = bitmapFromDrawableRes(context, iconResId)

        // Para cada coordenada, crie um marcador com o ícone customizado
        coordinates.forEach { coordinate ->
            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(coordinate)
                .withIconImage(pinBitmap)
            val annotation = pointAnnotationManager.create(pointAnnotationOptions)
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

    private fun bitmapFromDrawableRes(context: Context, resId: Int): Bitmap {
        val originalBitmap = BitmapFactory.decodeResource(context.resources, resId)
        val width = 100 // Tamanho padrão de largura
        val height = 100 // Tamanho padrão de altura
        return Bitmap.createScaledBitmap(originalBitmap, width, height, true)
    }

    private companion object {

        val MARKERS_EDGE_OFFSET = 64.0
        val PLACE_CARD_HEIGHT = 300.0

        val MARKERS_INSETS = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET
        )

        val MARKERS_INSETS_OPEN_CARD = EdgeInsets(
            MARKERS_EDGE_OFFSET, MARKERS_EDGE_OFFSET, PLACE_CARD_HEIGHT, MARKERS_EDGE_OFFSET
        )
    }
}
