package com.example.pathfinder.ui.components.car

import android.content.Intent
import android.content.res.Configuration
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.Utils
import com.mapbox.maps.extension.androidauto.MapboxCarMap
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.ui.androidauto.MapboxCarContext
import com.mapbox.navigation.ui.androidauto.map.MapboxCarMapLoader
import com.mapbox.navigation.ui.androidauto.notification.MapboxCarNotificationOptions
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreen
import com.mapbox.navigation.ui.androidauto.screenmanager.MapboxScreenManager
import com.mapbox.navigation.ui.androidauto.screenmanager.prepareScreens
import com.mapbox.navigation.ui.androidauto.deeplink.GeoDeeplinkNavigateAction

class MainCarSession : Session() {

    // Create the MapboxCarContext and MapboxCarMap. You can use them to build
    // your own customizations.
    private val carMapLoader = MapboxCarMapLoader()
    private val mapboxCarMap = MapboxCarMap().registerObserver(carMapLoader)
    private val mapboxCarContext = MapboxCarContext(lifecycle, mapboxCarMap)

    init {
        // Attach the car lifecycle to MapboxNavigationApp.
        // You do not need to detach because it will interally detach when the lifecycle is detroyed.
        // But you will need to unregister any observer that was registered within the car lifecycle.
        MapboxNavigationApp.attach(lifecycleOwner = this)

        // Prepare a screen graph for the session. If you want to customize
        // any screens, use the MapboxScreenManager.
        mapboxCarContext.prepareScreens()

        // At any point you can customize the MapboxCarOptions available.
        mapboxCarContext.customize {
            // You need to tell the car notification which app to open when a
            // user taps it.
            notificationOptions = MapboxCarNotificationOptions.Builder()
                .startAppService(MainCarAppService::class.java)
                .build()
        }

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                // Ensure MapboxNavigationApp has an access token and a application context.
                // This can also be done in Application.onCreate. Use the isSetup condition in case the
                // options have been set by a separate lifecycle event, like an Activity onCreate.
                /*if (!MapboxNavigationApp.isSetup()) {
                    MapboxNavigationApp.setup(
                        NavigationOptions.Builder(carContext)
                            .accessToken(Utils.getMapboxAccessToken(carContext))
                            .build()
                    )
                }*/

                // Once a CarContext is available, pass it to the MapboxCarMap.
                //mapboxCarMap.setup(carContext, MapboxInitOptions(context = carContext))
            }

            override fun onDestroy(owner: LifecycleOwner) {
                // The car session is destroyed you so should remove any observers. This
                // will ensure every MapboxCarMapObserver.onDetached is called.
                mapboxCarMap.clearObservers()
            }
        })
    }

    override fun onCreateScreen(intent: Intent): Screen {
        // You can control the MapboxScreenManager from a mobile device, in which case you will
        // want to get the first screen from there. Most of the MapboxScreens require location
        // permission and the default NEEDS_LOCATION_PERMISSION will assume location permissions
        // are requested from a mobile app.
        val firstScreenKey = if (PermissionsManager.areLocationPermissionsGranted(carContext)) {
            MapboxScreenManager.current()?.key ?: MapboxScreen.FREE_DRIVE
        } else {
            MapboxScreen.NEEDS_LOCATION_PERMISSION
        }

        // Use the MapboxScreenManager to keep track of the screen stack.
        return mapboxCarContext.mapboxScreenManager.createScreen(firstScreenKey)
    }

    override fun onCarConfigurationChanged(newConfiguration: Configuration) {
        // Notify a map loader of the dark mode style change
        //carMapLoader.updateMapStyle(carContext.isDarkMode)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Use the GeoDeeplinkNavigateAction or GeoDeeplinkParser to parse
        // incomming intents and change the navigation screen
        //GeoDeeplinkNavigateAction(mapboxCarContext).onNewIntent(intent)
    }
}
