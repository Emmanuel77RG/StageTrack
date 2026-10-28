package com.example.stagetrack.core.ubicacion

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/**
 * Representa un par de coordenadas geográficas de latitud y longitud.
 */
data class CoordenadasGps(
    val latitud: Double,
    val longitud: Double
)

/**
 * Utilidad para la obtención rápida de coordenadas GPS (Latitud, Longitud) en traspasos de custodia y devoluciones.
 */
object UtilidadUbicacionGps {

    /**
     * Obtiene las coordenadas GPS actuales del dispositivo si los permisos están concedidos.
     */
    @android.annotation.SuppressLint("MissingPermission")
    fun obtenerCoordenadasGpsActuales(context: Context): CoordenadasGps? {
        val tieneFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val tieneCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!tieneFineLocation && !tieneCoarseLocation) {
            return null
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return null

            val locationGps: Location? = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (_: Exception) { null }

            val locationNetwork: Location? = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: Exception) { null }

            val mejorUbicacion = when {
                locationGps != null && locationNetwork != null -> {
                    if (locationGps.time > locationNetwork.time) locationGps else locationNetwork
                }
                locationGps != null -> locationGps
                locationNetwork != null -> locationNetwork
                else -> null
            }

            mejorUbicacion?.let {
                CoordenadasGps(it.latitude, it.longitude)
            }
        } catch (_: Exception) {
            null
        }
    }
}
