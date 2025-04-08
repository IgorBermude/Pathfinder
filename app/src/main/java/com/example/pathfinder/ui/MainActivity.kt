package com.example.pathfinder.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapboxMap(
                Modifier.fillMaxSize(),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        zoom(2.0)
                        center(Point.fromLngLat(-98.0, 39.5))
                        pitch(0.0)
                        bearing(0.0)
                    }
                },
            )
        }
    }
}

//package com.example.pathfinder.ui
//
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import android.view.View
//import android.widget.Toast
//import com.google.android.material.bottomnavigation.BottomNavigationView
//import androidx.appcompat.app.AppCompatActivity
//import androidx.navigation.findNavController
//import androidx.navigation.ui.AppBarConfiguration
//import androidx.navigation.ui.setupActionBarWithNavController
//import androidx.navigation.ui.setupWithNavController
//import com.example.pathfinder.R
//import com.example.pathfinder.databinding.ActivityMainBinding
//import com.google.firebase.auth.FirebaseAuth
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.ui.Modifier
//import com.mapbox.geojson.Point
//import com.mapbox.maps.extension.compose.MapboxMap
//import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityMainBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val navView: BottomNavigationView = binding.navView
//
//        // Verificar se o usuário está logado
//        // Se não tiver logado leva para a tela de login
//        if (!isUserLoggedIn()) {
//            navView.visibility = View.GONE // Esconder o BottomNavigationView
//            val navController = findNavController(R.id.nav_host_fragment_activity_main)
//            navController.navigate(R.id.action_navigation_home_to_loginFragment)
//            showMessage("Usuário não está logado")
//            return
//        } else { // Se o usuário estiver logado, vai para a tela home e mostra o BottomNavigationView
//            navView.visibility = View.VISIBLE // Mostrar o BottomNavigationView
//            val navController = findNavController(R.id.nav_host_fragment_activity_main)
//            navController.navigate(R.id.navigation_home)
//            showMessage("Usuário está logado")
//        }
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_rotas, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
//    }
//
//    private fun isUserLoggedIn(): Boolean {
//        return FirebaseAuth.getInstance().currentUser != null
//    }
//
//    private fun showMessage(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_home, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_profile -> {
//                // Ação quando o ícone de perfil for clicado
//                Toast.makeText(this, "Perfil clicado", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MapboxMap(
//                java.lang.reflect.Modifier.fillMaxSize(),
//                mapViewportState = rememberMapViewportState {
//                    setCameraOptions {
//                        zoom(2.0)
//                        center(Point.fromLngLat(-98.0, 39.5))
//                        pitch(0.0)
//                        bearing(0.0)
//                    }
//                },
//            )
//        }
//    }
//}