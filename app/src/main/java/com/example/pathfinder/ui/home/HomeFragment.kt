package com.example.pathfinder.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.NavController
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Destino
import com.example.pathfinder.databinding.FragmentHomeBinding
import com.example.pathfinder.ui.MainActivity
import com.example.pathfinder.ui.components.DestinoAdapter
import com.example.pathfinder.ui.components.MapaBottomSheetFragment
import com.example.pathfinder.ui.components.MapaFragment
import com.example.pathfinder.ui.percorrerRota.RouteFragment
import com.example.pathfinder.ui.searchAc.SearchActivity
import com.example.pathfinder.util.NavigationViewUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.mapbox.geojson.Point
import com.mapbox.search.ResponseInfo
import com.mapbox.search.ReverseGeoOptions
import com.mapbox.search.SearchCallback
import com.mapbox.search.result.SearchResult
import com.mapbox.search.ui.view.place.SearchPlace
import androidx.core.view.isGone
import com.bumptech.glide.Glide
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.data.repositories.RotaRepository
import com.example.pathfinder.data.repositories.UsuarioRepository
import com.google.firebase.Timestamp
import com.example.pathfinder.ui.rotas.RotaSharedViewModel
import com.example.pathfinder.util.FirebaseUtil
import android.view.ViewGroup
import android.view.LayoutInflater

class HomeFragment : Fragment() {

    // Listener guard to avoid calling requireActivity() when fragment is detached
    private var navDestinationListener: NavController.OnDestinationChangedListener? = null

    companion object {
        private const val REQUEST_CODE_SEARCH = 1001
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var targetIcon: ImageView
    private lateinit var saveIcon: ImageView
    // remoção: private lateinit var searchPlaceView: SearchPlaceBottomSheetView
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val rotaSharedViewModel: RotaSharedViewModel by activityViewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var destinoAdapter: DestinoAdapter
    private lateinit var txSelecionarDestino: TextView
    private var usuario: Usuario? = null

    // Variável para armazenar o listener
    private var mapClickListener: ((Point) -> Boolean)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val usuarioRepository = UsuarioRepository()

        usuarioRepository.carregarUsuarioLogado()
        usuarioRepository.usuarioLogado.observe(viewLifecycleOwner) { usuario ->
            this.usuario = usuario
            setarImgemUsuario(usuario, root)
        }

        targetIcon = binding.root.findViewById(R.id.ac_target)
        saveIcon = binding.root.findViewById(R.id.btn_salvar)
        // Removida inicialização do searchPlaceView e suas configurações/listeners
        // ...existing code...

        val bottomSheet = binding.root.findViewById<LinearLayout>(R.id.bottom_sheet_destinos)
        val recyclerView = binding.root.findViewById<RecyclerView>(R.id.recycler_destinos)
        bottomSheetBehavior = bottomSheet?.let { BottomSheetBehavior.from(it) }!!

        // Definindo os três estados: escondido, colapsado (peek), expandido
        val displayMetrics = resources.displayMetrics
        val navViewHeight = dpToPx(56) // Altura padrão do BottomNavigationView
        val peekHeight = navViewHeight + dpToPx(90) // "Pontinha" acima do navView
        val midHeight = (displayMetrics.heightPixels * 0.35).toInt()
        val expandedHeight = (displayMetrics.heightPixels * 0.85).toInt() // Quase tela cheia

        val uiContainer = binding.root.findViewById<View>(R.id.ui_container)

        bottomSheetBehavior.peekHeight = peekHeight
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = false

        txSelecionarDestino = binding.root.findViewById(R.id.tx_selecione_destino)

        // Removido: searchPlaceView.addOnBottomSheetStateChangedListener { ... }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val acTarget = requireView().findViewById<View>(R.id.ac_target)
                val btnIniciarRota = requireView().findViewById<View>(R.id.btn_iniciar_rota)
                val btnSalvarRota = requireView().findViewById<View>(R.id.btn_salvar)
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        //Toast.makeText(requireContext(), "STATE_HIDDEN", Toast.LENGTH_SHORT).show()
                        NavigationViewUtils.mostrarBottomNavigationView(requireActivity())
                        acTarget?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = dpToPx(120)
                        }
                        btnSalvarRota?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = dpToPx(120)
                        }
                        btnIniciarRota?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = dpToPx(119)
                        }
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //Toast.makeText(requireContext(), "STATE_COLLAPSED", Toast.LENGTH_SHORT).show()
                        if(uiContainer.isGone){
                            NavigationViewUtils.mostrarBottomNavigationView(requireActivity())
                        }
                        // Estado colapsado: só a "pontinha" acima do navView
                        bottomSheet.requestLayout()
                        acTarget?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = peekHeight + dpToPx(12)
                        }
                        btnSalvarRota?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = peekHeight + dpToPx(12)
                        }
                        btnIniciarRota?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = peekHeight + dpToPx(11)
                        }
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //Toast.makeText(requireContext(), "STATE_EXPANDED", Toast.LENGTH_SHORT).show()
                        NavigationViewUtils.esconderBottomNavigationView(requireActivity())
                        bottomSheet.layoutParams.height = midHeight
                        bottomSheet.requestLayout()
                        acTarget?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = midHeight + dpToPx(16)
                        }
                        btnSalvarRota.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = midHeight + dpToPx(16)
                        }
                        btnIniciarRota?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = midHeight + dpToPx(16)
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
                mapaFragment?.updateCamera(origem, destino.localDestino)
            } else {
                mapaFragment?.clearRoutes()
            }
        }
        recyclerView?.adapter = destinoAdapter
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // Mover o listener do navController para cá
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        // Register listener safely: check isAdded() before calling requireActivity()
        navDestinationListener = NavController.OnDestinationChangedListener { _, _, _ ->
            if (isAdded) {
                NavigationViewUtils.toggleActionBarForScreen(requireActivity(), true)
            }
        }
        navController.addOnDestinationChangedListener(navDestinationListener!!)

        binding.root.findViewById<View>(R.id.btn_iniciar_rota).setOnClickListener{
            esconderComponentes()
            uiContainer.visibility = View.VISIBLE
            childFragmentManager.commit {
                replace(R.id.ui_container, RouteFragment())
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheetBehavior.isHideable = false
            removerOnMapClickListener()
        }

        binding.root.findViewById<View>(R.id.search_container).setOnClickListener {
            txSelecionarDestino.visibility = View.GONE
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_SEARCH)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.root.findViewById<View>(R.id.search_text).setOnClickListener {
            txSelecionarDestino.visibility = View.GONE
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

        // Implementar para mostrar uma mensagem se deseja salvar rota e pedir o nome da rota e apos confirmar, salvar a rota com o rotaRepository
        binding.root.findViewById<View>(R.id.btn_salvar).setOnClickListener {
            NavigationViewUtils.mostrarDialogNomeRota({ nomeRota ->
                val rotaAtual = homeViewModel.obterUltimaRota()
                if (rotaAtual != null) {
                    // Atualiza o nome da rota
                    rotaAtual.nomeRota = nomeRota
                    rotaAtual.dtModificacaoRota = com.google.firebase.Timestamp.now()
                    rotaAtual.distanciaRota = rotaAtual.destinosRota.sumOf { it.distancia ?: 0.0 }

                    // Salva a rota no Firestore
                    val rotaRepository = com.example.pathfinder.data.repositories.RotaRepository()
                    rotaRepository.salvarRota(rotaAtual, {
                        Toast.makeText(requireContext(), "Rota salva com sucesso", Toast.LENGTH_SHORT).show()
                        // Ajuste visual imediato (mantido) e sincroniza estado no ViewModel
                        saveIcon.setImageResource(R.drawable.content_save_all)
                        saveIcon.setColorFilter(resources.getColor(R.color.blue, null))
                        homeViewModel.marcarRotaComoSalva()
                    } , { exception->
                        Toast.makeText(requireContext(), "Erro ao salvar rota: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("HomeFragment", "Erro ao salvar rota", exception)
                    })
                    homeViewModel.atualizarUltimaRota(rotaAtual)

                } else {
                    Toast.makeText(requireContext(), "Nenhuma rota para salvar", Toast.LENGTH_SHORT).show()
                }
            }, requireActivity())
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

        // Removido listeners de searchPlaceView (close, navigate, share) e lógica foi movida para showInfoLocal()

        homeViewModel.rotas.observe(viewLifecycleOwner) { rotas ->
            val destinos = rotas.lastOrNull()?.destinosRota ?: emptyList()
            showDestinos(destinos)
        }

        // Atualiza o ícone reagindo apenas ao estado local
        homeViewModel.rotaSalva.observe(viewLifecycleOwner) { salva ->
            if (salva == true) {
                saveIcon.setImageResource(R.drawable.content_save_all)
                saveIcon.setColorFilter(resources.getColor(R.color.blue, null))
            } else {
                // Opcional: volte para o ícone padrão, se existir, e remova a cor
                // saveIcon.setImageResource(R.drawable.content_save)
                saveIcon.clearColorFilter()
                saveIcon.setImageResource(R.drawable.content_save_all_outline)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Inicializo o MapaFragment
        childFragmentManager.commit {
            replace(R.id.map_container, MapaFragment().getInstance())
        }

        // Observa rota selecionada do RotaSharedViewModel
        rotaSharedViewModel.rotaSelecionada.observe(viewLifecycleOwner) { rota ->
            rota?.let {
                homeViewModel.substituirRotaAtual(it)
                // Vinda do banco => considera salva (reforça o estado)
                homeViewModel.marcarRotaComoSalva()
                val destinos = it.destinosRota.orEmpty()
                showDestinos(destinos)

                val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? com.example.pathfinder.ui.components.MapaFragment
                if (mapaFragment != null && destinos.isNotEmpty()) {
                    mapaFragment.getUserLocation { location ->
                        location?.let { loc ->
                            val origin = com.mapbox.geojson.Point.fromLngLat(loc.longitude, loc.latitude)
                            mapaFragment.requestRoutes(origin, destinos) {}
                            mapaFragment.updateCamera(origin, destinos.map { d -> d.localDestino })
                        } ?: Log.w("MapaFragment", "Localização do usuário indisponível")
                    }
                }
            }
        }

        // Aguarde o commit e o carregamento do mapa
        view.post {
            adicionarOnMapClickListenerParaPesquisa()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove navController listener to prevent callbacks after fragment is detached
        val navHostFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment
        val navController = navHostFragment?.navController
        navDestinationListener?.let { listener ->
            navController?.removeOnDestinationChangedListener(listener)
            navDestinationListener = null
        }
        // Ensure map click listener removed
        removerOnMapClickListener()
        _binding = null
    }

    private fun onSearchResultSelected(searchResult: SearchResult, responseInfo: ResponseInfo) {
        val searchPlace = SearchPlace.createFromSearchResult(searchResult, responseInfo)

        // Ao invés de abrir searchPlaceView, inflamos item_info_local no container
        showInfoLocal(searchPlace, searchResult.coordinate)
        bottomSheetBehavior.isHideable = false
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
        val coordinate = searchResult.coordinate
        mapaFragment?.addMarker(coordinate.latitude(), coordinate.longitude())

        NavigationViewUtils.esconderBottomNavigationView(requireActivity())
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SEARCH && resultCode == Activity.RESULT_OK && data != null) {
            if (data.getBooleanExtra("request_pesquisarPorClique", false)) {
                txSelecionarDestino.visibility = View.VISIBLE
                return
            }
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
        val btnSalvarRota = requireView().findViewById<View>(R.id.btn_salvar)
        val btnIniciarRota = requireView().findViewById<View>(R.id.btn_iniciar_rota)
        val txNomeRota = binding.root.findViewById<TextView>(R.id.tx_nome_rota)
        val txQtdDestinos = binding.root.findViewById<TextView>(R.id.tx_qtd_destinos)
        val rotaAtual = homeViewModel.obterUltimaRota()

        if (destinos.isEmpty()) {
            bottomSheetBehavior.isHideable = true
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            btnSalvarRota.visibility = View.GONE
            btnIniciarRota.visibility = View.GONE
        } else {
            // Ao mostrar, começa no estado colapsado (meio)
            /*val displayMetrics = resources.displayMetrics
            val targetHeight = (displayMetrics.heightPixels * 0.35).toInt()
            bottomSheet.layoutParams.height = targetHeight*/
            bottomSheet.requestLayout()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBehavior.isHideable = false
            btnSalvarRota.visibility = View.VISIBLE
            btnIniciarRota.visibility = View.VISIBLE
            txNomeRota.text = rotaAtual?.nomeRota
            txQtdDestinos.text = rotaAtual?.destinosRota?.size.toString()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    private fun esconderComponentes() {
        val searchBar = requireView().findViewById<View>(R.id.search_bar)
        val actionProfile = requireView().findViewById<View>(R.id.action_profile)
        val btnIniciarRota = requireView().findViewById<View>(R.id.btn_iniciar_rota)
        val acTarget = requireView().findViewById<View>(R.id.ac_target)
        val btnSalvar = requireView().findViewById<View>(R.id.btn_salvar)
        val btnMudarMapa = requireView().findViewById<View>(R.id.map_type_button)

        txSelecionarDestino.visibility = View.GONE

        // Animação de fade out e esconder
        listOf(searchBar, actionProfile, btnIniciarRota, btnSalvar, acTarget, btnMudarMapa).forEach { view ->
            view?.animate()
                ?.alpha(0f)
                ?.setDuration(300)
                ?.withEndAction { view.visibility = View.GONE }
                ?.start()
        }
    }

    fun mostrarComponentes() {
        val searchBar = requireView().findViewById<View>(R.id.search_bar)
        val actionProfile = requireView().findViewById<View>(R.id.action_profile)
        val btnIniciarRota = requireView().findViewById<View>(R.id.btn_iniciar_rota)
        val acTarget = requireView().findViewById<View>(R.id.ac_target)
        val btnSalvar = requireView().findViewById<View>(R.id.btn_salvar)
        val btnMudarMapa = requireView().findViewById<View>(R.id.map_type_button)

        val uiContainer = requireView().findViewById<View>(R.id.ui_container)
        uiContainer.visibility = View.GONE

        listOf(searchBar, actionProfile, btnIniciarRota, acTarget, btnSalvar, btnMudarMapa).forEach { view ->
            view?.visibility = View.VISIBLE
            view?.animate()?.alpha(1f)?.setDuration(300)?.start()
        }
    }

     fun adicionarOnMapClickListenerParaPesquisa() {
        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
        mapClickListener = { point ->
            realizarPesquisaPorPonto(mapaFragment, point)
            true
        }
        mapaFragment?.setOnMapClickListener(mapClickListener!!)
    }

    private fun removerOnMapClickListener(){
        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment
        mapClickListener?.let { listener ->
            mapaFragment?.removeOnMapClickListener(listener)
            mapClickListener = null
        }
    }

    private fun realizarPesquisaPorPonto(mapaFragment: MapaFragment?, point: Point) {
        txSelecionarDestino.visibility = View.GONE
        mapaFragment?.getUserLocation { location ->
            if (location != null) {
                mapaFragment.reverseGeocode(ReverseGeoOptions(point), object : SearchCallback {
                    override fun onResults(results: List<SearchResult>, responseInfo: ResponseInfo) {
                        val searchPlace = SearchPlace.createFromSearchResult(results.first(), responseInfo)
                        // Ao invés de abrir searchPlaceView, inflamos item_info_local no container
                        showInfoLocal(searchPlace, point)
                        bottomSheetBehavior.isHideable = false
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        mapaFragment.addMarker(point.latitude(), point.longitude())
                    }

                    override fun onError(error: Exception) {
                        Toast.makeText(requireContext(), "Não foi possível conectar aos serviços da Mapbox. Verifique sua conexão.", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(requireContext(), "Localização do usuário não disponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getDestinoAdapter(): DestinoAdapter {
        return destinoAdapter
    }

    // Função central: infla item_info_local no container, popula campos e conecta os botões
    private fun showInfoLocal(searchPlace: SearchPlace, coordinate: Point?) {
        val container = requireView().findViewById<ViewGroup>(R.id.info_local_container)
        val inflater = LayoutInflater.from(requireContext())
        container.removeAllViews()
        val infoLocalView = inflater.inflate(R.layout.item_info_local, container, false)
        container.addView(infoLocalView)
        container.visibility = View.VISIBLE

        val destinosContainer = requireView().findViewById<View>(R.id.destinos_container)
        destinosContainer.visibility = View.GONE

        val titulo = infoLocalView.findViewById<TextView>(R.id.titulo_destinos)
        val infoComp = infoLocalView.findViewById<TextView>(R.id.info_complementar)
        val distanciaTv = infoLocalView.findViewById<TextView>(R.id.distancia)
        val btnClose = infoLocalView.findViewById<ImageButton>(R.id.close_button)
        val btnNavigate = infoLocalView.findViewById<Button>(R.id.btn_navigate)

        titulo.text = searchPlace.name
        val enderecoParts = listOfNotNull(
            searchPlace.address?.place,
            searchPlace.address?.region,
            searchPlace.address?.street,
            searchPlace.address?.houseNumber
        ).filter { it.isNotBlank() }
        infoComp.text = if (enderecoParts.isNotEmpty()) enderecoParts.joinToString(", ") else ""
        distanciaTv.text = searchPlace.distanceMeters?.let { "${it.toInt()} m" } ?: ""

        val mapaFragment = childFragmentManager.findFragmentById(R.id.map_container) as? MapaFragment

        btnClose.setOnClickListener {
            container.removeAllViews()
            container.visibility = View.GONE
            mapaFragment?.removeLastMarker()

            // Se não houver nenhum destino na rota atual, fechar o bottom sheet
            val destinos = homeViewModel.obterUltimaRota()?.destinosRota
            if (destinos.isNullOrEmpty()) {
                bottomSheetBehavior.isHideable = true
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            destinosContainer.visibility = View.VISIBLE
        }

        btnNavigate.setOnClickListener {
            val destination = Destino(
                nomeDestino = searchPlace.name,
                localDestino = coordinate ?: searchPlace.coordinate,
                distancia = searchPlace.distanceMeters,
                endereco = enderecoParts.joinToString(", ")
            )
            mapaFragment?.getUserLocation { location ->
                if (location != null) {
                    val origin = Point.fromLngLat(location.longitude, location.latitude)
                    val rotaAtual = homeViewModel.obterUltimaRota()
                    if (rotaAtual != null) {
                        homeViewModel.adicionarDestinoARotaExistente(destination)
                    } else {
                        homeViewModel.criarNovaRota(origin, destination, searchPlace.name, usuario?.idUsuario, searchPlace.distanceMeters)
                    }
                    val destinos = homeViewModel.obterUltimaRota()?.destinosRota ?: listOf(destination)
                    mapaFragment.requestRoutes(origin, destinos) {
                        mapaFragment.removeLastMarker()
                    }
                    mapaFragment.updateCamera(origin, destination.localDestino)


                    container.removeAllViews()
                    container.visibility = View.GONE
                    bottomSheetBehavior.isHideable = false
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    destinosContainer.visibility = View.VISIBLE
                 } else {
                     Toast.makeText(requireContext(), "Localização do usuário não disponível", Toast.LENGTH_SHORT).show()
                 }
             }
         }

        // Ajustes de UI quando mostrar info local
        // O problema está relacionado com essas 3 linhas a baixo
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        NavigationViewUtils.esconderBottomNavigationView(requireActivity())
    }

    //Concertar
    fun setarImgemUsuario(usuario: Usuario?, view: View){
        val actionProfile = view.findViewById<ImageView>(R.id.action_profile)
        if (!usuario?.fotoUsuario.isNullOrEmpty()) {
            val bitmap = FirebaseUtil.base64ToBitmap(usuario.fotoUsuario!!)
            if (bitmap != null) {
                Glide.with(requireContext())
                    .load(bitmap)
                    .circleCrop()
                    .override(512, 512)
                    .into(actionProfile)
            } else {
                actionProfile.setImageResource(R.drawable.ic_profile)
            }
        } else {
            actionProfile.setImageResource(R.drawable.ic_profile)
        }
    }

    override fun onResume() {
        super.onResume()
        //bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }
}