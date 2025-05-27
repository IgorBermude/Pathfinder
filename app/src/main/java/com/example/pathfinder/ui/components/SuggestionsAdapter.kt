package com.example.pathfinder.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.search.result.SearchSuggestion

class SuggestionsAdapter(
    private val onSuggestionClick: (SearchSuggestion) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    private val suggestions = mutableListOf<SearchSuggestion>()

    private val mapManager = MapManeger
    private val mapView: MapView? = mapManager.getMapView()

    private lateinit var mapMarkersManager: MapMarkersManager

    init {
        mapView?.let {
            mapMarkersManager = MapMarkersManager(it) // Inicializa o MapMarkersManager quando o mapView está disponível
        }
    }

    fun submitList(newSuggestions: List<SearchSuggestion>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val suggestion = suggestions[position]
        holder.bind(suggestion)
        holder.itemView.setOnClickListener { 
            // Lógica para lidar com o clique na sugestão

            // Não esta conseguindo passar a latitude e longitude para o MapMarkersManager
            suggestion.coordinate?.let { coordinate ->
                mapMarkersManager.showMarker(Point.fromLngLat(coordinate.latitude(), coordinate.longitude()))
            }
        }
    }

    override fun getItemCount(): Int = suggestions.size

    class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityName: TextView = itemView.findViewById(R.id.city_name)
        private val cityRegion: TextView = itemView.findViewById(R.id.city_region)
        private val cityDistance: TextView = itemView.findViewById(R.id.city_distance)

        fun bind(suggestion: SearchSuggestion) {
            cityName.text = suggestion.name
            cityRegion.text = suggestion.descriptionText ?: "Unknown Region"
            cityDistance.text = String.format("%.1f km", suggestion.distanceMeters ?: 0.0)
        }
    }
}
