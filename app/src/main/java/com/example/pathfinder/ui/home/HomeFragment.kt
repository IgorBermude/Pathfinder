package com.example.pathfinder.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Rota
import com.example.pathfinder.databinding.FragmentHomeBinding
import com.example.pathfinder.ui.MainActivity
import com.example.pathfinder.ui.components.MapaBottomSheetFragment
import com.example.pathfinder.ui.components.MapaFragment
import com.example.pathfinder.ui.searchAc.SearchActivity
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.place.SearchPlace
import com.mapbox.search.ui.view.place.SearchPlaceBottomSheetView
import java.util.Date

class HomeFragment : Fragment() {

    companion object {
        private const val REQUEST_CODE_SEARCH = 1001
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var targetIcon: ImageView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        targetIcon = binding.root.findViewById(R.id.ac_target)
        searchPlaceView = binding.root.findViewById(R.id.search_place_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
        //Desativa o search_input
        //root.findViewById<TextView>(R.id.search_input).isEnabled = false

        childFragmentManager.commit {
            replace(R.id.map_container, MapaFragment().getInstance())
        }

        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            toggleActionBarForScreen(destination.id == R.id.profileFragment)
        }

        // Configurar o clique no search_container do layout incluído
        binding.root.findViewById<View>(R.id.search_container).setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SEARCH)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.root.findViewById<View>(R.id.search_text).setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SEARCH)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.actionProfile.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_profile -> {
                        findNavController().navigate(R.id.profileFragment)
                        true
                    }
                    R.id.menu_fechar -> {
                        requireActivity().finish()
                        true
                    }
                    R.id.menu_sair -> {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        binding.root.findViewById<View>(R.id.ac_target).setOnClickListener {
            // Dentralize a localização do usuario no mapa
            targetIcon.setImageResource(R.drawable.target_variation)
            targetIcon.setColorFilter(resources.getColor(R.color.blue, null))

            val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as MapaFragment
            mapaFragment.centralizeUserLocation()
            mapaFragment.setupMapMoveListener(targetIcon)
        }

        binding.root.findViewById<View>(R.id.map_type_button).setOnClickListener {
            MapaBottomSheetFragment().show(parentFragmentManager, "RotaBottomSheet")
        }

        searchPlaceView.addOnCloseClickListener {
            val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
            mapaFragment?.removeLastMarker()
            fecharSearchPlaceView()
        }

        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            val destination = searchPlace.coordinate
            val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment

            mapaFragment?.getUserLocation { location ->
                if (location != null) {
                    val origin = Point.fromLngLat(location.longitude, location.latitude)
                    val rotaAtual = obterUltimaRota()
                    if (rotaAtual != null && rotaAtual.origemRota == origin) {
                        adicionarDestinoARotaExistente(rotaAtual, destination)
                    } else {
                        criarNovaRota(origin, destination, searchPlace.name)
                    }

                    // Solicita a rota e centraliza ao receber a resposta
                    mapaFragment.requestRoutes(origin, destination) { routeCoordinates ->
                        Toast.makeText(requireContext(), "Rota solicitada: $origin", Toast.LENGTH_SHORT).show()
                        fecharSearchPlaceView()
                        mapaFragment.removeLastMarker()
                        if (routeCoordinates.isNotEmpty()) {
                            mapaFragment.centralizeRoute(routeCoordinates)
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Localização do usuário não disponível", Toast.LENGTH_SHORT).show()
                }
            }
            // startActivity(createGeoIntent(searchPlace.coordinate))
        }


        searchPlaceView.addOnShareClickListener { searchPlace ->
            //startActivity(createShareIntent(searchPlace))
        }

        return root
    }

    private fun toggleActionBarForScreen(hide: Boolean) {
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        if (hide) {
            actionBar?.hide()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo) {
        val searchPlace = SearchPlace.createFromSearchResult(searchResult, responseInfo)
        searchPlaceView.open(searchPlace)

        // Adiciona o marcador no mapa
        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
        val coordinate = searchResult.coordinate
        if (mapaFragment != null && coordinate != null) {
            mapaFragment.addMarker(coordinate.latitude(), coordinate.longitude())
        }

        // Esconde o BottomNavigationView com animação
        val navView = requireActivity().findViewById<View>(R.id.nav_view)
        navView?.animate()
            ?.translationY(navView.height.toFloat())
            ?.alpha(0f)
            ?.setDuration(250)
            ?.withEndAction { navView.visibility = View.GONE }
            ?.start()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == Activity.RESULT_OK && data != null) {
            val searchResult = data.getParcelableExtra<SearchResult>("search_result")
            val responseInfo = data.getParcelableExtra<ResponseInfo>("response_info")
            if (searchResult != null && responseInfo != null) {
                onSearchResultSelected(searchResult, responseInfo)
            }
        }
    }

    private fun fecharSearchPlaceView() {
        searchPlaceView.hide()
        val navView = requireActivity().findViewById<View>(R.id.nav_view)
        navView?.apply {
            visibility = View.VISIBLE
            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .start()
        }
    }

    // Obtém a última rota existente, se houver
    private fun obterUltimaRota(): Rota? {
        val rotasAtuais = homeViewModel.rotas.value ?: emptyList()
        return rotasAtuais.lastOrNull()
    }

    // Cria uma nova rota com origem e primeiro destino
    private fun criarNovaRota(origin: Point, destination: Point, nomeRota: String?) {
        val novaRota = Rota(
            origemRota = origin,
            destinosRota = listOf(destination),
            criadorRota = null, // Preencha com o usuário logado se necessário
            distanciaRota = null,
            tempoTotalRota = null,
            dtModificacaoRota = Date(),
            nomeRota = nomeRota
        )
        homeViewModel.adicionarRota(novaRota)
    }

    // Retorna uma nova rota com o destino adicionado à lista de destinos
    private fun adicionarDestinoARotaExistente(rota: Rota, destination: Point){
        val novaRota = rota.copy(
            destinosRota = rota.destinosRota + destination,
            dtModificacaoRota = Date(),
        )
        homeViewModel.atualizarUltimaRota(novaRota)
    }
}