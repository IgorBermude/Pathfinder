package com.example.pathfinder.ui.home

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.pathfinder.R
import com.example.pathfinder.databinding.FragmentHomeBinding
import com.example.pathfinder.ui.MainActivity
import com.example.pathfinder.ui.components.MapaFragment
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var searchIcon: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

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
            startActivity(intent)
            requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        binding.root.findViewById<View>(R.id.search_text).setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
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

}
