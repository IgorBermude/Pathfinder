package com.example.pathfinder.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Destino
import com.example.pathfinder.data.models.Rota
import com.example.pathfinder.databinding.FragmentHomeBinding
import com.example.pathfinder.ui.MainActivity
import com.example.pathfinder.ui.components.DestinoAdapter
import com.example.pathfinder.ui.components.MapaBottomSheetFragment
import com.example.pathfinder.ui.components.MapaFragment
import com.example.pathfinder.ui.searchAc.SearchActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
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
    private val binding get() = _binding!!
    private lateinit var targetIcon: ImageView
    private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var destinoAdapter: DestinoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        targetIcon = binding.root.findViewById(R.id.ac_target)
        searchPlaceView = binding.root.findViewById(R.id.search_place_view)
        searchPlaceView.initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
        
        val bottomSheet = binding.root.findViewById<LinearLayout>(R.id.bottom_sheet_destinos)
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recycler_destinos)
        bottomSheetBehavior = bottomSheet?.let { BottomSheetBehavior.from(it) }!!
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val acTarget = requireView().findViewById<View>(R.id.ac_target)
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        mostrarBottomNavigationView()
                        acTarget?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = dpToPx(120)
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        esconderBottomNavigationView()
                        val displayMetrics = resources.displayMetrics
                        val targetHeight = (displayMetrics.heightPixels * 0.35).toInt()
                        acTarget?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = targetHeight + dpToPx(16)
                        }
                    }
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        // Lixeira do destino adapter
        destinoAdapter = DestinoAdapter(emptyList()) { destino ->
            homeViewModel.removerDestinoDaRota(destino)
            val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
            val rota = homeViewModel.obterUltimaRota()
            val origem = rota?.origemRota
            val destinos = rota?.destinosRota

            if (origem != null && !destinos.isNullOrEmpty()) {
                mapaFragment?.requestRoutes(origem, destinos){}
            } else {
                mapaFragment?.clearRoutes()
            }
        }
        recyclerView?.adapter = destinoAdapter
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        childFragmentManager.commit {
            replace(R.id.map_container, MapaFragment().getInstance())
        }

        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            toggleActionBarForScreen(destination.id == R.id.profileFragment)
        }

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
            searchPlaceView.hide()
            //showDestinos()
            mostrarBottomNavigationView()
        }

        searchPlaceView.addOnNavigateClickListener { searchPlace ->
            val destination = Destino(
                nome = searchPlace.name ?: "Destino",
                ponto = searchPlace.coordinate,
                distancia = null
            )
            val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment

            mapaFragment?.getUserLocation { location ->
                if (location != null) {
                    val origin = Point.fromLngLat(location.longitude, location.latitude)
                    val rotaAtual = homeViewModel.obterUltimaRota()

                    if (rotaAtual != null) {
                        homeViewModel.adicionarDestinoARotaExistente(destination)
                    } else {
                        homeViewModel.criarNovaRota(origin, destination, searchPlace.name)
                    }

                    val destinos = homeViewModel.obterUltimaRota()?.destinosRota ?: listOf(destination)
                    mapaFragment.requestRoutes(origin, destinos) {
                        Toast.makeText(requireContext(), "Rota solicitada: $origin", Toast.LENGTH_SHORT).show()
                        searchPlaceView.hide()
                        mapaFragment.removeLastMarker()
                    }
                } else {
                    Toast.makeText(requireContext(), "Localização do usuário não disponível", Toast.LENGTH_SHORT).show()
                }
            }
        }

        searchPlaceView.addOnShareClickListener { searchPlace ->
            //startActivity(createShareIntent(searchPlace))
        }

        homeViewModel.rotas.observe(viewLifecycleOwner) { rotas ->
            val destinos = rotas.lastOrNull()?.destinosRota ?: emptyList()
            showDestinos(destinos)
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

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
        val coordinate = searchResult.coordinate
        if (mapaFragment != null && coordinate != null) {
            mapaFragment.addMarker(coordinate.latitude(), coordinate.longitude())
        }

        esconderBottomNavigationView()
    }

    private fun esconderBottomNavigationView(){
        val navView = requireActivity().findViewById<View>(R.id.nav_view)
        navView?.animate()
            ?.translationY(navView.height.toFloat())
            ?.alpha(0f)
            ?.setDuration(250)
            ?.withEndAction { navView.visibility = View.GONE }
            ?.start()
    }

    private fun mostrarBottomNavigationView() {
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

    private fun showDestinos(destinos: List<Destino>) {
        destinoAdapter.update(destinos)
        val bottomSheet = requireView().findViewById<LinearLayout>(R.id.bottom_sheet_destinos)

        if (destinos.isEmpty()) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        } else {
            val displayMetrics = resources.displayMetrics
            val targetHeight = (displayMetrics.heightPixels * 0.35).toInt()
            bottomSheet.layoutParams.height = targetHeight
            bottomSheet.requestLayout()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }
}