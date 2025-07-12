package com.example.pathfinder.ui.searchAc

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.ui.components.SuggestionsAdapter
import com.mapbox.search.result.SearchSuggestion
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.example.pathfinder.ui.components.LocationHelper
import com.example.pathfinder.util.NavigationViewUtils.observeUserLocation
import com.mapbox.search.ApiType
import com.mapbox.search.ResponseInfo
import com.mapbox.search.SearchEngine
import com.mapbox.search.SearchEngineSettings
import com.mapbox.search.SearchOptions
import com.mapbox.search.SearchSelectionCallback
import com.mapbox.search.common.IsoCountryCode
import com.mapbox.search.result.SearchResult
import com.mapbox.geojson.Point


class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: AppCompatEditText
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private lateinit var searchIcon: ImageView
    private lateinit var searchEngine: SearchEngine
    private val searchViewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        LocationHelper.initialize(this)
        LocationHelper.getCurrentLocation(this, this, object : LocationHelper.LocationCallback {
            override fun onLocationResult(location: Location?) {
                location?.let {
                    val point = Point.fromLngLat(location.longitude, location.latitude)
                    searchViewModel.setPoint(point)
                }
            }
        })
        searchViewModel.pointLiveData.observe(this, Observer { point ->
            println("Ponto recebido via ViewModel: $point")
        })

        supportActionBar?.hide()

        searchIcon = findViewById(R.id.back_icon)

        searchInput = findViewById(R.id.search_input)

        searchInput.requestFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

        searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
            apiType = ApiType.GEOCODING,
            settings = SearchEngineSettings()
        )

        // Configurar RecyclerView para exibir sugestões
        suggestionsAdapter = SuggestionsAdapter(this) { suggestion ->
            // Ao clicar, busque o SearchResult e ResponseInfo e retorne para o HomeFragment
            searchEngine.select(suggestion, object : SearchSelectionCallback {
                override fun onSuggestions(
                    suggestions: List<SearchSuggestion>,
                    responseInfo: ResponseInfo
                ) {
                    // Não utilizado aqui
                }

                override fun onResult(
                    suggestion: SearchSuggestion,
                    result: SearchResult,
                    info: ResponseInfo
                ) {
                    saveLastSearchedLocation(suggestion)
                    val resultIntent = Intent().apply {
                        putExtra("search_result", result)
                        putExtra("response_info", info)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onResults(
                    suggestion: SearchSuggestion,
                    results: List<SearchResult>,
                    responseInfo: ResponseInfo
                ) {
                    // Não utilizado aqui
                }

                override fun onError(e: Exception) {
                    Toast.makeText(this@SearchActivity, "Erro ao buscar resultado", Toast.LENGTH_SHORT).show()
                }
            })
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

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                // Use a localização do usuário do ViewModel, se disponível
                val userLocation = searchViewModel.pointLiveData.value
                System.out.println("Localização do usuário: $userLocation")
                searchEngine.search(
                    text.toString(),
                    SearchOptions.Builder()
                        .limit(10)
                        .countries(listOf(IsoCountryCode.BRAZIL))
                        .origin(userLocation!!)
                        .build(),
                    object : SearchSelectionCallback {
                        override fun onSuggestions(
                            suggestions: List<SearchSuggestion>,
                            responseInfo: ResponseInfo
                        ) {
                            // Atualize o RecyclerView com as sugestões
                            suggestionsAdapter.submitList(suggestions)
                        }

                        override fun onResult(
                            suggestion: SearchSuggestion,
                            result: SearchResult,
                            info: ResponseInfo
                        ) {
                            // Retornar o resultado selecionado para o HomeFragment
                            val resultIntent = Intent().apply {
                                putExtra("location_name", suggestion.name)
                                putExtra("latitude", suggestion.coordinate?.latitude())
                                putExtra("longitude", suggestion.coordinate?.longitude())
                                putExtra("search_result", result)
                                putExtra("response_info", info)
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }

                        override fun onResults(
                            suggestion: SearchSuggestion,
                            results: List<SearchResult>,
                            responseInfo: ResponseInfo
                        ) {
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