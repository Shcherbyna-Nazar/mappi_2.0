package com.example.mappi.presentation.ui.main.composables.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.example.mappi.R
import com.example.mappi.domain.model.Post
import com.example.mappi.presentation.ui.main.viewmodel.MapViewModel
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MapScreen(context: Context) {
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

    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileState by profileViewModel.profileState.collectAsStateWithLifecycle()
    val myPosts = profileState.posts

    val mapViewModel: MapViewModel = hiltViewModel()
    val friendPosts by mapViewModel.friendPosts.collectAsState()

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

    val initialPosition = userLocation ?: LatLng(0.0, 0.0)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition, 10f)
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 10f))
        }
    }

    val clickedMarker = remember { mutableStateOf<String?>(null) }
    val markerBitmaps = remember { mutableStateMapOf<String, Bitmap?>() }
    val loadingMarkers = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(myPosts, friendPosts) {
        val allPosts = myPosts + friendPosts
        allPosts.forEach { post ->
            val postId = post.id.toString()
            loadingMarkers[postId] = true
            loadMarkerBitmap(context, post.url) { bitmap ->
                markerBitmaps[postId] = bitmap
                loadingMarkers[postId] = false
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

    Box(modifier = Modifier.fillMaxSize()) {
        if (allMarkersLoaded) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission
                )
            ) {
                (myPosts + friendPosts).forEach { post ->
                    val markerPosition = LatLng(post.latitude, post.longitude)
                    val isSelected = post.id.toString() == clickedMarker.value
                    val bitmap = markerBitmaps[post.id.toString()]

                    bitmap?.let {
                        val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(
                            if (isSelected) {
                                if (myPosts.contains(post)) {
                                    addCircularBorderToBitmap(
                                        scaleBitmap(it, 100, 100),
                                        Color(0xFF0F3C3B)
                                    )
                                } else {
                                    scaleBitmap(it, 100, 100)
                                }
                            } else {
                                if (myPosts.contains(post)) {
                                    addCircularBorderToBitmap(
                                        scaleBitmap(it, 75, 75),
                                        Color(0xFF0F3C3B)
                                    )
                                } else {
                                    scaleBitmap(it, 75, 75)
                                }
                            }
                        )
                        Marker(
                            state = rememberMarkerState(position = markerPosition),
                            title = if (myPosts.contains(post)) "My Post" else "Friend's Post",
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
                .size(56.dp)
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
                modifier = Modifier.size(50.dp)
            )
        }
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            IconButton(
                onClick = {
                    clickedMarker.value?.let { markerId ->
                        val selectedPost =
                            (myPosts + friendPosts).find { it.id.toString() == markerId }
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
                    .size(56.dp)
                    .background(Color.Transparent, CircleShape)
            ) {
                val painter = rememberAsyncImagePainter(model = R.drawable.ic_airplane)
                Image(
                    painter = painter,
                    contentDescription = "Navigate to Selected Marker",
                    colorFilter = ColorFilter.tint(if (clickedMarker.value != null) Color(0xFF0F3C3B) else Color.White),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        selectedPost?.let { post ->
            FullScreenImageDialog(
                imageUrl = post.url,
                onDismissRequest = { selectedPost = null }
            )
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

@Composable
fun FullScreenImageDialog(
    imageUrl: String,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            ZoomableImage(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon",
                    tint = Color.Red,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val maxScale = 3f
    val minScale = 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                    val maxX = (size.width * (scale - 1)) / 2
                    val maxY = (size.height * (scale - 1)) / 2
                    offsetX = (offsetX + pan.x * scale).coerceIn(-maxX, maxX)
                    offsetY = (offsetY + pan.y * scale).coerceIn(-maxY, maxY)
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .background(Color.Transparent) // Ensure no background
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent), // Ensure loading background is also transparent
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.Green,
                        strokeWidth = 4.dp,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent), // Ensure error background is also transparent
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_foreground),
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        )
    }
}
