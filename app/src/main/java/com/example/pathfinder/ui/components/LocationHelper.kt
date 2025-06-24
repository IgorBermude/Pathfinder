package com.example.pathfinder.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

object LocationHelper {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Interface de retorno (callback)
    interface LocationCallback {
        fun onLocationResult(location: Location?)
    }

    fun initialize(context: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    }

    fun getCurrentLocation(context: Context, activity: Activity, callback: LocationCallback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    callback.onLocationResult(location)
                } else {
                    Toast.makeText(context, "Não foi possível obter a localização", Toast.LENGTH_SHORT).show()
                    callback.onLocationResult(null)
                }
            }
    }
}