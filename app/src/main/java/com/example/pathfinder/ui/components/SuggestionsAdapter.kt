package com.example.pathfinder.ui.components

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.mapbox.search.result.SearchSuggestion

class SuggestionsAdapter(
    private val context: Context,
    private val onSuggestionClick: (SearchSuggestion) -> Unit
) : RecyclerView.Adapter<SuggestionsAdapter.SuggestionViewHolder>() {

    private val suggestions = mutableListOf<SearchSuggestion>()
    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences("search_preferences", Context.MODE_PRIVATE)
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
            onSuggestionClick(suggestion)
        }
    }

    override fun getItemCount(): Int = suggestions.size

    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cityName: TextView = itemView.findViewById(R.id.city_name)
        private val cityRegion: TextView = itemView.findViewById(R.id.city_region)
        private val cityDistance: TextView = itemView.findViewById(R.id.city_distance)
        private val locationIcon: ImageView = itemView.findViewById(R.id.location_icon)

        fun bind(suggestion: SearchSuggestion) {
            System.out.println("Sugestão recebida: $suggestion")
            cityName.text = suggestion.name
            cityRegion.text = suggestion.fullAddress
            cityDistance.text = suggestion.distanceMeters?.let { String.format("%.1f km", it / 1000) } ?: "Distancia desconhecida"

            // Verifica se o resultado está no histórico
            val lastName = preferences.getString("last_location_name", null)
            val lastLat = preferences.getFloat("last_lat", 0f)
            val lastLng = preferences.getFloat("last_lng", 0f)
            val isHistory = lastName == suggestion.name &&
                suggestion.coordinate?.latitude()?.toFloat() == lastLat &&
                suggestion.coordinate?.longitude()?.toFloat() == lastLng

            if (isHistory) {
                locationIcon.setImageResource(R.drawable.ic_history)
            } else {
                locationIcon.setImageResource(R.drawable.ic_location_marker)
            }
        }
    }
}
