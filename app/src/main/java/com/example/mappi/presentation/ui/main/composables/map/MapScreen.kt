package com.example.mappi.presentation.ui.main.composables.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.mappi.R
import com.example.mappi.domain.model.Post
import com.example.mappi.presentation.ui.decisions.compose.TravelMode
import com.example.mappi.presentation.ui.main.viewmodel.MapViewModel
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel
import com.example.mappi.util.RouteUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MapScreen(
    context: Context,
    mapViewModel: MapViewModel,
    profileViewModel: ProfileViewModel
) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val myPosts = profileState.posts

    val friendPosts by mapViewModel.friendPosts.collectAsState()
    val friendPostsLoading by mapViewModel.isLoading.collectAsState()

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState()

    val clickedMarker = remember { mutableStateOf<String?>(null) }
    val markerBitmaps = remember { mutableStateMapOf<String, Bitmap?>() }
    val loadingMarkers = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(profileState.isLoading, friendPostsLoading) {
        if (!profileState.isLoading && !friendPostsLoading) {
            (myPosts + friendPosts).forEach { post ->
                val postId = post.id.toString()
                loadingMarkers[postId] = true
                loadMarkerBitmap(context, post.url) { bitmap ->
                    markerBitmaps[postId] = bitmap
                    loadingMarkers[postId] = false
                }
            }
        }
    }

    val allMarkersLoaded by remember {
        derivedStateOf {
            (myPosts + friendPosts).all { post ->
                loadingMarkers[post.id.toString()] == false
            }
        }
    }

    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var routePolyline by remember { mutableStateOf<PolylineOptions?>(null) }
    var isRouteVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!profileState.isLoading && allMarkersLoaded) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                onMapLoaded = {
                    userLocation?.let {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                    }
                }
            ) {
                routePolyline?.takeIf { isRouteVisible }?.let {
                    Polyline(
                        points = it.points,
                        color = Color(0xFF0F3C3B),
                        width = 6f
                    )
                }

                (myPosts + friendPosts).forEach { post ->
                    val markerPosition = LatLng(post.latitude, post.longitude)
                    val postId = post.id.toString()
                    val isSelected = clickedMarker.value == postId
                    val bitmap = markerBitmaps[postId]

                    bitmap?.let {
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                            if (isSelected) {
                                addCircularBorderToBitmap(
                                    scaleBitmap(it, 100, 100),
                                    Color(0xFF0F3C3B)
                                )
                            } else {
                                addCircularBorderToBitmap(
                                    scaleBitmap(it, 75, 75),
                                    Color(0xFF0F3C3B)
                                )
                            }
                        )
                        Marker(
                            state = rememberMarkerState(position = markerPosition),
                            title = if (myPosts.contains(post)) "My Post" else "Friend's Post",
                            snippet = if (isSelected) "(${post.latitude}, ${post.longitude})" else null,
                            icon = bitmapDescriptor,
                            zIndex = if (isSelected) 1f else 0f,
                            onClick = {
                                clickedMarker.value = if (isSelected) null else postId
                                true
                            }
                        )
                    }
                }
            }
        } else {
            CircularProgressIndicator(
                color = Color(0xFF0F3C3B),
                modifier = Modifier.align(Alignment.Center)
            )
        }

        IconButton(
            onClick = {
                selectedPost =
                    (myPosts + friendPosts).find { it.id.toString() == clickedMarker.value }
            },
            modifier = Modifier
                .size(48.dp)
                .background(Color.Transparent, CircleShape)
                .align(Alignment.BottomStart)
                .offset(y = (-25).dp, x = 16.dp)
                .padding(16.dp)
        ) {
            val painter = rememberAsyncImagePainter(model = R.drawable.baseline_zoom_out_map_24)
            Image(
                painter = painter,
                contentDescription = "View Post Image",
                colorFilter = ColorFilter.tint(if (clickedMarker.value != null) Color(0xFF0F3C3B) else Color.White),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp)
            )
        }

        selectedPost?.let { post ->
            FullScreenImageDialog(
                imageUrl = post.url,
                onDismissRequest = { selectedPost = null }
            )
        }

        // Airplane button in the top-left corner
        if (clickedMarker.value != null) {
            IconButton(
                onClick = {
                    if (isRouteVisible) {
                        routePolyline = null
                        isRouteVisible = false
                    } else {
                        val selectedPost = (myPosts + friendPosts).find { it.id.toString() == clickedMarker.value }
                        userLocation?.let { origin ->
                            selectedPost?.let { post ->
                                val destination = LatLng(post.latitude, post.longitude)
                                CoroutineScope(Dispatchers.IO).launch {
                                    val route = RouteUtils.fetchRoute(origin, destination, TravelMode.WALKING)
                                    withContext(Dispatchers.Main) {
                                        routePolyline = route
                                        isRouteVisible = true
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF0F3C3B), CircleShape)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    painter = rememberAsyncImagePainter(model = R.drawable.ic_airplane),
                    contentDescription = "Generate Route",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}


suspend fun loadMarkerBitmap(
    context: Context,
    imageUrl: String,
    onBitmapLoaded: (Bitmap?) -> Unit
) {
    withContext(Dispatchers.IO) {
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

fun addCircularBorderToBitmap(bitmap: Bitmap, borderColor: Color): Bitmap {
    val borderSize = 5
    val diameter = bitmap.width + borderSize * 2
    val newBitmap = Bitmap.createBitmap(diameter, diameter, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(newBitmap)
    val paint = Paint().apply {
        color = borderColor.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = borderSize.toFloat()
        isAntiAlias = true
    }
    val radius = diameter / 2f
    canvas.drawCircle(radius, radius, radius - borderSize / 2f, paint)
    canvas.drawBitmap(bitmap, borderSize.toFloat(), borderSize.toFloat(), null)
    return newBitmap
}