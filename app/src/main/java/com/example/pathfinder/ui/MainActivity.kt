package com.example.pathfinder.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.pathfinder.R
import com.example.pathfinder.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.example.pathfinder.ui.rotas.RotaBottomSheetFragment
import androidx.navigation.ui.NavigationUI
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialize o MapboxNavigationApp se ainda não estiver inicializado
        if (!MapboxNavigationApp.isSetup()) {
            MapboxNavigationApp.setup {
                NavigationOptions.Builder(this)
                    .build()
            }
        }

        val navView: BottomNavigationView = binding.navView

        // Verificar se o usuário está logado
        // Se não tiver logado leva para a tela de login
        if (!isUserLoggedIn()) {
            navView.visibility = View.GONE // Esconder o BottomNavigationView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.action_navigation_home_to_loginFragment)
            showMessage("Usuário não está logado")
            return
        } else { // Se o usuário estiver logado, vai para a tela home e mostra o BottomNavigationView
            navView.visibility = View.VISIBLE // Mostrar o BottomNavigationView
            val navController = findNavController(R.id.nav_host_fragment_activity_main)
            navController.navigate(R.id.navigation_home)
            //showMessage("Usuário está logado")
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            toggleActionBarForScreen(destination.id, destination.id == R.id.profileFragment)
            toggleActionBarForScreen(destination.id, destination.id == R.id.navigation_home)
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_rotas, R.id.navigation_timeline
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_rotas -> {
                    RotaBottomSheetFragment().show(supportFragmentManager, "RotaBottomSheet")
                    true
                }
                else -> {
                    NavigationUI.onNavDestinationSelected(item, navController)
                }
            }
        }
    }

    private fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun toggleActionBarForScreen(destinationId: Int, hide: Boolean) {
        if (hide) {
            supportActionBar?.hide() // Esconde apenas o supportActionBar
        } else {
            supportActionBar?.show() // Mostra o supportActionBar
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(isUserLoggedIn()){
            menuInflater.inflate(R.menu.menu_home, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_profile -> {
                supportActionBar?.hide()
                binding.navView.visibility = View.GONE // Esconde o BottomNavigationView
                val navController = findNavController(R.id.nav_host_fragment_activity_main)
                navController.navigate(R.id.profileFragment)
                true
            }
            R.id.menu_fechar -> {
                finish() // Fecha o aplicativo
                true
            }
            R.id.menu_sair -> {
                FirebaseAuth.getInstance().signOut() // Faz logout do usuário
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent) // Redireciona para MainActivity
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
