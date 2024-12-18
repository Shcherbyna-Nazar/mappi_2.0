package com.example.mappi.presentation.ui.main.composables.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
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
import com.example.mappi.presentation.ui.decisions.viewmodel.DecisionsViewModel
import com.example.mappi.presentation.ui.main.viewmodel.MapViewModel
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel
import com.example.mappi.util.RouteUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.sin

private const val RECOMMENDATION_MARKER_ID = "recommendation_place"

@Composable
fun MapScreen(
    context: Context,
    mapViewModel: MapViewModel,
    profileViewModel: ProfileViewModel,
    decisionsViewModel: DecisionsViewModel,
) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            hasLocationPermission = true
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val myPosts = profileState.posts

    val recommendationPlace by decisionsViewModel.placeRecommendation.collectAsState()

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

    // Load post markers
    LaunchedEffect(profileState.isLoading, friendPostsLoading) {
        if (!profileState.isLoading && !friendPostsLoading) {
            (myPosts + friendPosts).forEach { post ->
                val postId = post.id
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
                loadingMarkers[post.id] == false
            }
        }
    }

    // Load recommendation place bitmap
    val (recommendedBitmap, setRecommendedBitmap) = remember { mutableStateOf<Bitmap?>(null) }
    var recommendationLoading by remember { mutableStateOf(false) }

    LaunchedEffect(recommendationPlace) {
        recommendationPlace?.let { place ->
            recommendationLoading = true
            loadMarkerBitmap(context, place.photoUrl) { bitmap ->
                setRecommendedBitmap(bitmap)
                recommendationLoading = false
            }
        }
    }

    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var routePolyline by remember { mutableStateOf<PolylineOptions?>(null) }
    var isRouteVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!profileState.isLoading && allMarkersLoaded && !recommendationLoading) {
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

                // Draw user's and friends' posts
                (myPosts + friendPosts).forEach { post ->
                    val markerPosition = LatLng(post.latitude, post.longitude)
                    val postId = post.id
                    val isSelected = clickedMarker.value == postId
                    val bitmap = markerBitmaps[postId]

                    bitmap?.let {
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                            if (isSelected) {
                                addCircularBorderToBitmap(
                                    scaleBitmap(it, 100, 100),
                                    Color(0xFF0F3C3B).toArgb()
                                )
                            } else {
                                addCircularBorderToBitmap(
                                    scaleBitmap(it, 75, 75),
                                    Color(0xFF0F3C3B).toArgb()
                                )
                            }
                        )
                        val title: String = if (myPosts.contains(post)) {
                            "My Post"
                        } else {
                            "${post.userName}'s Post"
                        }
                        Marker(
                            state = rememberMarkerState(position = markerPosition),
                            title = title,
                            snippet = if (isSelected) "(${post.latitude}, ${post.longitude})" else null,
                            icon = bitmapDescriptor,
                            zIndex = if (isSelected) 1f else 0f,
                            onClick = {
                                routePolyline = null
                                isRouteVisible = false
                                clickedMarker.value = if (isSelected) null else postId
                                false
                            }
                        )
                    }
                }

                // Recommended place marker with a distinct star shape
                recommendationPlace?.let { place ->
                    recommendedBitmap?.let { bmp ->
                        val isSelected = (clickedMarker.value == RECOMMENDATION_MARKER_ID)
                        val markerBitmap = createStarShapedMarker(bmp, isSelected)
                        val location = place.location
                        val markerPosition = LatLng(location.latitude, location.longitude)
                        Marker(
                            state = rememberMarkerState(position = markerPosition),
                            title = "Recommended: ${place.name}",
                            snippet = if (isSelected) "(${location.latitude}, ${location.longitude})" else "Expert Recommendation",
                            icon = BitmapDescriptorFactory.fromBitmap(markerBitmap),
                            zIndex = if (isSelected) 2f else 0f,
                            onClick = {
                                routePolyline = null
                                isRouteVisible = false
                                clickedMarker.value =
                                    if (isSelected) null else RECOMMENDATION_MARKER_ID
                                false
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

        // Button to open post details
        IconButton(
            onClick = {
                selectedPost = (myPosts + friendPosts).find { it.id == clickedMarker.value }
            },
            modifier = Modifier
                .size(56.dp)
                .background(Color.Transparent, CircleShape)
                .align(Alignment.BottomStart)
                .offset(y = (-25).dp, x = 16.dp)
                .padding(12.dp)
        ) {
            val painter = rememberAsyncImagePainter(model = R.drawable.baseline_zoom_out_map_24)
            Image(
                painter = painter,
                contentDescription = "View Post Image",
                colorFilter = ColorFilter.tint(
                    if (clickedMarker.value != null) Color(0xFF0F3C3B) else Color.White
                ),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(42.dp)
            )
        }

        // Full screen image with comments only applies to posts
        selectedPost?.let { post ->
            FullScreenImageWithComments(
                myPosts,
                friendPosts,
                postId = post.id,
                onDismissRequest = { selectedPost = null },
                onAddComment = { comment ->
                    if (myPosts.find { it.id == post.id } != null) {
                        profileViewModel.addComment(post.id, comment)
                    } else {
                        mapViewModel.addComment(post, comment)
                    }
                }
            )
        }

        // Route button: works for both posts and recommended place
        if (clickedMarker.value != null) {
            IconButton(
                onClick = {
                    if (isRouteVisible) {
                        // Hide route
                        routePolyline = null
                        isRouteVisible = false
                    } else {
                        // Show route
                        userLocation?.let { origin ->
                            if (clickedMarker.value == RECOMMENDATION_MARKER_ID) {
                                // Route to recommended place
                                recommendationPlace?.let { place ->
                                    val destination = LatLng(place.location.latitude, place.location.longitude)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val route = RouteUtils.fetchRoute(origin, destination, TravelMode.WALKING)
                                        withContext(Dispatchers.Main) {
                                            routePolyline = route
                                            isRouteVisible = true
                                        }
                                    }
                                }
                            } else {
                                // Route to selected post
                                val selectedPost = (myPosts + friendPosts).find { it.id == clickedMarker.value }
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
                    }
                },
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Transparent, CircleShape)
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = R.drawable.ic_walking),
                    contentDescription = "Generate Walking Route",
                    colorFilter = ColorFilter.tint(
                        if (clickedMarker.value != null) Color(0xFF0F3C3B) else Color.White
                    ),
                    modifier = Modifier.size(42.dp)
                )
            }
        }
    }
}

// Utility functions

fun addCircularBorderToBitmap(bitmap: Bitmap, borderColor: Int, borderWidth: Float = 6f): Bitmap {
    val size = maxOf(bitmap.width, bitmap.height)
    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    val radius = size / 2f

    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    paint.shader = shader
    canvas.drawCircle(radius, radius, radius - borderWidth, paint)

    // Border
    paint.shader = null
    paint.style = Paint.Style.STROKE
    paint.color = borderColor
    paint.strokeWidth = borderWidth
    canvas.drawCircle(radius, radius, radius - borderWidth / 2, paint)

    return output
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

/**
 * Creates a star-shaped marker for the recommended place.
 * If `isSelected` is true, the star is larger and the border more pronounced.
 * Otherwise, it is smaller.
 */
fun createStarShapedMarker(bitmap: Bitmap, isSelected: Boolean): Bitmap {
    val size = if (isSelected) 175 else 125
    val scaledBitmap = scaleBitmap(bitmap, size, size)

    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val path = createStarPath(cx = size / 2f, cy = size / 2f, radius = size / 2f - 10f, spikes = 5)

    // Clip the canvas to the star shape
    val saveCount = canvas.save()
    canvas.clipPath(path)

    // Draw the scaled image inside the star
    val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    val shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    imagePaint.shader = shader
    canvas.drawPath(path, imagePaint)

    canvas.restoreToCount(saveCount)

    // Draw a gradient border outside the star path
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    borderPaint.style = Paint.Style.STROKE
    borderPaint.strokeWidth = if (isSelected) 10f else 6f

    val startColor = Color(0xFF0F3C3B).toArgb()
    val endColor = Color(0xFF33AACC).toArgb()
    val gradientShader = SweepGradient(
        size / 2f, size / 2f,
        intArrayOf(startColor, endColor, startColor),
        floatArrayOf(0f, 0.5f, 1f)
    )
    borderPaint.shader = gradientShader

    canvas.drawPath(path, borderPaint)

    return output
}

/**
 * Creates a star-shaped Path.
 * @param cx Center X
 * @param cy Center Y
 * @param radius Outer radius of the star
 * @param spikes Number of spikes in the star
 */
fun createStarPath(cx: Float, cy: Float, radius: Float, spikes: Int): Path {
    val path = Path()
    val innerRadius = radius / 2.5
    val angle = Math.PI / spikes

    path.moveTo(cx, cy - radius)
    for (i in 0 until spikes * 2) {
        val r = if (i % 2 == 0) radius else innerRadius.toFloat()
        val x = cx + (r * sin(i * angle)).toFloat()
        val y = cy - (r * cos(i * angle)).toFloat()
        path.lineTo(x, y)
    }
    path.close()
    return path
}
