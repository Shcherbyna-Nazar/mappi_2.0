package com.example.mappi.presentation.ui.main.composables.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.mappi.R
import com.example.mappi.domain.model.Post
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MapScreen(context: Context, posts: List<Post>) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                hasLocationPermission = true
            }

            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    val initialPosition = if (posts.isNotEmpty()) {
        LatLng(posts[0].latitude, posts[0].longitude)
    } else {
        userLocation ?: LatLng(0.0, 0.0)
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }

    val clickedMarker = remember { mutableStateOf<String?>(null) }

    val markerBitmaps = remember { mutableStateMapOf<String, Bitmap?>() }

    LaunchedEffect(posts) {
        posts.forEach { post ->
            loadMarkerBitmap(context, post.url) { bitmap ->
                markerBitmaps[post.id.toString()] = bitmap
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            posts.forEach { post ->
                val markerPosition = LatLng(post.latitude, post.longitude)
                val isSelected = post.id.toString() == clickedMarker.value
                val bitmap = markerBitmaps[post.id.toString()]

                bitmap?.let {
                    val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                        if (isSelected) scaleBitmap(it, 100, 100) else scaleBitmap(it, 75, 75)
                    )
                    Marker(
                        state = rememberMarkerState(position = markerPosition),
                        title = if (isSelected) "Post by ${post.id}" else null,
                        snippet = if (isSelected) "(${post.latitude}, ${post.longitude})" else null,
                        icon = bitmapDescriptor,
                        zIndex = if (isSelected) 1f else 0f,
                        onClick = {
                            clickedMarker.value =
                                if (clickedMarker.value == post.id.toString()) null else post.id.toString()
                            true
                        }
                    )
                }
            }
        }
        IconButton(
            onClick = {
                clickedMarker.value?.let { markerId ->
                    val selectedPost = posts.find { it.id.toString() == markerId }
                    val gmmIntentUri =
                        Uri.parse("geo:0,0?q=${selectedPost?.latitude},${selectedPost?.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                        setPackage("com.google.android.apps.maps")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(mapIntent)
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .size(56.dp)
                .background(Color.Transparent, CircleShape)
                .align(Alignment.TopStart)
        ) {
            val painter = rememberAsyncImagePainter(model = R.drawable.ic_airplane)
            Image(
                painter = painter,
                contentDescription = "Navigate to Selected Marker",
                colorFilter = ColorFilter.tint(if (clickedMarker.value != null) Color.Blue else Color.White),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

fun loadMarkerBitmap(context: Context, imageUrl: String, onBitmapLoaded: (Bitmap?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .transformations(CircleCropTransformation())
            .build()
        val result = request.context.imageLoader.execute(request).drawable?.toBitmap()
        withContext(Dispatchers.Main) {
            onBitmapLoaded(result)
        }
    }
}

fun scaleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
    return Bitmap.createScaledBitmap(bitmap, width, height, false)
}
