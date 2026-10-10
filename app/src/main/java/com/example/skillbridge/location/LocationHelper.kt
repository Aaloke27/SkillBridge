
package com.example.skillbridge.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

object LocationHelper {

    suspend fun getCurrentLocation(
        context: Context
    ): Location? {

        val finePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!finePermission && !coarsePermission) {
            return null
        }

        val client =
            LocationServices.getFusedLocationProviderClient(context)

        return try {
            client.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            ).await() ?: client.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    fun getLocality(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? {
        return try {
            val geocoder = android.location.Geocoder(context)
            val addresses = geocoder.getFromLocation(
                latitude,
                longitude,
                1
            )

            addresses
                ?.firstOrNull()
                ?.let { address ->
                    address.subLocality
                        ?: address.locality
                        ?: address.subAdminArea
                }
        } catch (e: Exception) {
            null
        }
    }

}


