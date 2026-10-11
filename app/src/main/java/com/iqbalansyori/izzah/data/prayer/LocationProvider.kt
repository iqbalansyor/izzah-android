package com.iqbalansyori.izzah.data.prayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.iqbalansyori.izzah.domain.model.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class LocationProvider(private val context: Context) {

    suspend fun current(): GeoPoint? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null
        val location = fetch() ?: return null
        return GeoPoint(location.latitude, location.longitude, label(location))
    }

    @Suppress("MissingPermission")
    private suspend fun fetch(): Location? {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER).firstOrNull { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }
            ?: return runCatching { manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()
        return try {
            suspendCancellableCoroutine { cont ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    manager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { cont.resume(it) }
                } else {
                    val listener = LocationListener { cont.resume(it) }
                    @Suppress("DEPRECATION")
                    manager.requestSingleUpdate(provider, listener, null)
                    cont.invokeOnCancellation { manager.removeUpdates(listener) }
                }
            }
        } catch (e: SecurityException) {
            null
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun label(location: Location): String = withContext(Dispatchers.IO) {
        runCatching {
            Geocoder(context).getFromLocation(location.latitude, location.longitude, 1)?.firstOrNull()
                ?.let { it.locality ?: it.subAdminArea ?: it.adminArea }.orEmpty()
        }.getOrDefault("")
    }
}
