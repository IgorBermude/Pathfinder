package com.example.pathfinder.ui.home

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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


class SearchActivity : AppCompatActivity() {
    private lateinit var searchInput: EditText
    private lateinit var suggestionsRecyclerView: RecyclerView
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private lateinit var searchBarLayout: CardView
    private lateinit var searchIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        supportActionBar?.hide()

        searchBarLayout = findViewById(R.id.search_bar)
        //searchBarLayout.setBackgroundColor(resources.getColor(R.color.gray_light))

        searchIcon = findViewById(R.id.search_icon)
        searchIcon.setImageResource(R.drawable.ic_arrow_back)

        searchInput = findViewById(R.id.search_input)
        suggestionsRecyclerView = findViewById(R.id.suggestions_recycler_view)

        // Configurar RecyclerView para exibir sugestões
        suggestionsAdapter = SuggestionsAdapter { suggestion ->
            handleSuggestionClick(suggestion)
        }
        suggestionsRecyclerView.layoutManager = LinearLayoutManager(this)
        suggestionsRecyclerView.adapter = suggestionsAdapter

        val placeAutocomplete = PlaceAutocomplete.create()

        // Adicionar a pesquisa ao local
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // lógica aqui
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    /*private fun performSearch(query: String, placeAutocomplete: PlaceAutocomplete) {
        lifecycleScope.launch {
            try {
                val response = placeAutocomplete.suggestions(query)
                response.onValue {
                    suggestionsAdapter.submitList(it)
                }.onError {
                    Toast.makeText(this@SearchActivity, "Erro ao buscar sugestões", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SearchActivity, "Erro ao buscar sugestões", Toast.LENGTH_SHORT).show()
            }
        }
    }*/

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
