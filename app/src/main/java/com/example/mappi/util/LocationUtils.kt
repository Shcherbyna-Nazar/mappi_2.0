package com.example.mappi.util

import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Location
import android.widget.Toast
import com.example.mappi.MainActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

class LocationUtils(private val activity: MainActivity) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    private val settingsClient = LocationServices.getSettingsClient(activity)
    private val locationRequest: LocationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
    private val locationSettingsRequest: LocationSettingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationReceived: (Location) -> Unit) {
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it)
            } ?: showToast("Unable to get current location")
        }
    }

    fun checkLocationSettings(onSuccess: () -> Unit) {
        settingsClient.checkLocationSettings(locationSettingsRequest).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(activity, MainActivity.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    showToast("Error checking location settings: ${sendEx.message}")
                }
            } else {
                showToast("Location settings are not satisfied.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity.applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
