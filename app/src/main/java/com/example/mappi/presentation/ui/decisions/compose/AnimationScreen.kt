package com.example.mappi.presentation.ui.decisions.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mappi.util.RouteUtils.fetchRoute
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.util.Locale

enum class TravelMode(val mode: String, val color: Color, val icon: ImageVector) {
    DRIVING("driving", Color(0xFF6ABF69), Icons.Default.DirectionsCar),
    WALKING("walking", Color(0xFF89CFF0), Icons.Default.DirectionsWalk),
    BICYCLING("bicycling", Color(0xFFFFD580), Icons.Default.DirectionsBike)
}

@Composable
fun AnimationScreen(
    userLocation: LatLng,
    restaurantLatLng: LatLng,
    onBackToFindingPlaces: () -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 14f)
    }

    var selectedMode by remember { mutableStateOf(TravelMode.DRIVING) }
    var routePolyline by remember { mutableStateOf<PolylineOptions?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(selectedMode) {
        coroutineScope.launch {
            routePolyline = fetchRoute(userLocation, restaurantLatLng, selectedMode)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFA7FFEB))))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Avoid overlapping
        ) {
            // Header Section
            Text(
                text = "Route to Your Destination",
                style = MaterialTheme.typography.h5.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E8B67)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            // Map Section in a Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(400.dp), // Reduced height for better balance
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center

                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = userLocation),
                            title = "Your Location",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                        )
                        Marker(
                            state = MarkerState(position = restaurantLatLng),
                            title = "Restaurant",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                        routePolyline?.let {
                            Polyline(
                                points = it.points,
                                color = selectedMode.color.copy(alpha = 0.8f),
                                width = 8f
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Transport Mode Selector
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Select Travel Mode",
                    style = MaterialTheme.typography.subtitle1.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF408C68)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TravelModeButton(
                        mode = TravelMode.WALKING,
                        isSelected = selectedMode == TravelMode.WALKING,
                        onSelect = { selectedMode = TravelMode.WALKING }
                    )
                    TravelModeButton(
                        mode = TravelMode.DRIVING,
                        isSelected = selectedMode == TravelMode.DRIVING,
                        onSelect = { selectedMode = TravelMode.DRIVING }
                    )
                    TravelModeButton(
                        mode = TravelMode.BICYCLING,
                        isSelected = selectedMode == TravelMode.BICYCLING,
                        onSelect = { selectedMode = TravelMode.BICYCLING }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Floating Back Button
            FloatingActionButton(
                onClick = onBackToFindingPlaces,
                backgroundColor = Color(0xFF408C68),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.Start) // Align with start edge
                    .padding(bottom = 16.dp)
                    .size(56.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
        }
    }
}

@Composable
fun TravelModeButton(
    mode: TravelMode,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) mode.color else Color(0xFFE0F7FA)
    )
    val iconColor by animateColorAsState(targetValue = if (isSelected) Color.White else mode.color)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { onSelect() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally)
                .background(backgroundColor)
                .shadow(4.dp, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = mode.icon,
                contentDescription = mode.mode,
                tint = iconColor
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = mode.mode.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.body2,
            color = if (isSelected) mode.color else Color(0xFF757575),
            fontSize = 14.sp
        )
    }
}
