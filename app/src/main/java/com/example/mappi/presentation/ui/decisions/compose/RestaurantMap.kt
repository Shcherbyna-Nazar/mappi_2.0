package com.example.mappi.presentation.ui.decisions.compose

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.location.Location
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun RestaurantMap(
    userLocation: Location,
    restaurantLocation: Location,
    restaurantTitle: String,
    restaurantPhotoUrl: String
) {
    val cameraPositionState = rememberCameraPositionState()

    // Ustawienie kamery tak, aby obejmowała obie lokalizacje
    LaunchedEffect(userLocation, restaurantLocation) {
        val boundsBuilder = LatLngBounds.Builder()
        boundsBuilder.include(LatLng(userLocation.latitude, userLocation.longitude))
        boundsBuilder.include(LatLng(restaurantLocation.latitude, restaurantLocation.longitude))
        val bounds = boundsBuilder.build()

        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 100))
    }

    val restaurantMarkerBitmap = remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Załaduj zdjęcie restauracji jako bitmapę
    LaunchedEffect(restaurantPhotoUrl) {
        val zoomLevel = cameraPositionState.position.zoom
        val markerSize = calculateMarkerSize(zoomLevel)
        restaurantMarkerBitmap.value = createRoundedMarkerBitmap(restaurantPhotoUrl, markerSize)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clip(RoundedCornerShape(16.dp)),
        cameraPositionState = cameraPositionState
    ) {
        // Marker dla lokalizacji użytkownika
        Marker(
            state = MarkerState(position = LatLng(userLocation.latitude, userLocation.longitude)),
            title = "You are here",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // Połyskujący krąg wokół lokalizacji użytkownika
        Circle(
            center = LatLng(userLocation.latitude, userLocation.longitude),
            radius = 50.0,
            fillColor = Color(0x5500FFFF),
            strokeColor = Color(0x5500FFFF),
            strokeWidth = 2f
        )

        // Marker dla restauracji
        if (restaurantMarkerBitmap.value != null) {
            Marker(
                state = MarkerState(
                    position = LatLng(restaurantLocation.latitude, restaurantLocation.longitude)
                ),
                title = restaurantTitle,
                icon = restaurantMarkerBitmap.value
            )
        }

        // Zakrzywiony łuk łączący lokalizacje
        DrawCurvedLine(
            startLatLng = LatLng(userLocation.latitude, userLocation.longitude),
            endLatLng = LatLng(restaurantLocation.latitude, restaurantLocation.longitude),
            color = Color.Black, // Czarny łuk
            width = 5f
        )
    }
}

@Composable
fun DrawCurvedLine(
    startLatLng: LatLng,
    endLatLng: LatLng,
    color: androidx.compose.ui.graphics.Color,
    width: Float
) {
    val controlLatLng = LatLng(
        (startLatLng.latitude + endLatLng.latitude) / 2,
        (startLatLng.longitude + endLatLng.longitude) / 2 + 0.005 // Przesunięcie dla efektu łuku
    )

    Polyline(
        points = listOf(startLatLng, controlLatLng, endLatLng),
        color = color,
        width = width
    )
}



suspend fun createRoundedMarkerBitmap(photoUrl: String, size: Int): BitmapDescriptor? {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(photoUrl)
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())

            // Create a rounded bitmap scaled to the provided size
            val outputBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(outputBitmap)
            val paint = Paint()
            paint.isAntiAlias = true

            val rect = Rect(0, 0, size, size)
            val rectF = RectF(rect)
            canvas.drawOval(rectF, paint)

            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, size, size, false)
            canvas.drawBitmap(scaledBitmap, rect, rect, paint)

            BitmapDescriptorFactory.fromBitmap(outputBitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun calculateMarkerSize(zoomLevel: Float): Int {
    return when {
        zoomLevel > 16f -> 150 // Larger marker for close zoom
        zoomLevel > 14f -> 100 // Medium marker for medium zoom
        else -> 75 // Smaller marker for far zoom
    }
}
