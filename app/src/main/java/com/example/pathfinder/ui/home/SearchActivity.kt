package com.example.pathfinder.ui.home

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.ui.components.SuggestionsAdapter
import com.mapbox.search.autocomplete.PlaceAutocomplete
import com.mapbox.search.result.SearchSuggestion
import kotlinx.coroutines.launch
import androidx.cardview.widget.CardView
import android.widget.EditText
import android.widget.TextView
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView


class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private lateinit var searchIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        supportActionBar?.hide()

        searchIcon = findViewById(R.id.back_icon)

        searchInput = findViewById(R.id.search_input)

        // Configurar RecyclerView para exibir sugestões
        suggestionsAdapter = SuggestionsAdapter { suggestion ->
            handleSuggestionClick(suggestion)
        }

        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view)
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionsRecyclerView.adapter = suggestionsAdapter

        searchIcon.setOnClickListener {
            searchIcon.animate()
                .alpha(0.5f) // Reduz a opacidade para 50%
                .setDuration(100) // Duração da animação de clique
                .withEndAction {
                    searchIcon.animate()
                        .alpha(1f) // Restaura a opacidade para 100%
                        .setDuration(100) // Duração da animação de retorno
                        .start()
                }
                .start()
            onBackPressed()
        }

        val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = SearchEngineSettings()
        )

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                // Quando o texto mudar, faça a busca
                searchEngine.search(
                    text.toString(),
                    SearchOptions.Builder().limit(5).build(),
                    object : SearchSelectionCallback {
                        override fun onSuggestions(suggestions: List<SearchSuggestion>, responseInfo: ResponseInfo) {
                            // Atualize o RecyclerView com as sugestões
                            suggestionsAdapter.submitList(suggestions)
                        }
                        override fun onResult(suggestion: SearchSuggestion, result: SearchResult, info: ResponseInfo) {
                            // Trate o resultado selecionado
                        }
                        override fun onResults(suggestion: SearchSuggestion, results: List<SearchResult>, responseInfo: ResponseInfo) {
                            // Trate resultados de categoria
                        }
                        override fun onError(e: Exception) {
                            System.out.println("Erro ao buscar: ${e.message}")
                        }
                    }
                )
            }
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })


    }

    private fun handleSuggestionClick(suggestion: SearchSuggestion) {
        // Salvar a sugestão selecionada
        saveLastSearchedLocation(suggestion)

        // Retornar a localização para o mapa
        val resultIntent = Intent().apply {
            putExtra("location_name", suggestion.name)
            putExtra("latitude", suggestion.coordinate?.latitude())
            putExtra("longitude", suggestion.coordinate?.longitude())
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun saveLastSearchedLocation(suggestion: SearchSuggestion) {
        val preferences = getSharedPreferences("search_preferences", MODE_PRIVATE)
        preferences.edit().apply {
            putString("last_location_name", suggestion.name)
            putFloat("last_lat", suggestion.coordinate?.latitude()?.toFloat() ?: 0f)
            putFloat("last_lng", suggestion.coordinate?.longitude()?.toFloat() ?: 0f)
            apply()
        }
    }


}
