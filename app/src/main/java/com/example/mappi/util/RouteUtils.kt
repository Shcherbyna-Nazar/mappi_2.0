package com.example.mappi.util

import androidx.compose.ui.graphics.toArgb
import com.example.mappi.BuildConfig
import com.example.mappi.presentation.ui.decisions.compose.TravelMode
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object RouteUtils {
    suspend fun fetchRoute(
        origin: LatLng,
        destination: LatLng,
        mode: TravelMode
    ): PolylineOptions? {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = BuildConfig.API_KEY
                val urlString =
                    "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                            "&destination=${destination.latitude},${destination.longitude}" +
                            "&mode=${mode.mode}&key=$apiKey"

                val connection = URL(urlString).openConnection() as HttpURLConnection
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val points = jsonResponse.getJSONArray("routes")
                        .getJSONObject(0)
                        .getJSONObject("overview_polyline")
                        .getString("points")

                    val decodedPath = decodePolyline(points)

                    PolylineOptions().addAll(decodedPath)
                        .color(mode.color.copy(alpha = 0.7f).toArgb())
                        .width(8f)
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun decodePolyline(encodedPath: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = encodedPath.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encodedPath[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }

        return poly
    }
}