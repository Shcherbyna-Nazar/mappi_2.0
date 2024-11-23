package com.example.mappi.presentation.ui.decisions.compose

import android.location.Location
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.example.mappi.R
import com.example.mappi.domain.model.Restaurant
import com.example.mappi.presentation.ui.decisions.viewmodel.DecisionsViewModel
import kotlin.math.abs

@Composable
fun DecisionsScreen(
    viewModel: DecisionsViewModel = hiltViewModel(),
    userLocation: Location
) {
    val restaurant by viewModel.restaurantRecommendation.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(viewModel.nearbyRestaurants) {
        if (!isLoading) {
            viewModel.fetchRecommendation()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFA7FFEB))))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(color = Color(0xFF3E8B67))
            }

            restaurant != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SwipeableRestaurantCard(
                        restaurant = restaurant!!,
                        onAccept = { viewModel.makeDecision(restaurant!!.id, true) },
                        onReject = { viewModel.makeDecision(restaurant!!.id, false) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SwipeInstructions()
                }
            }

            error != null -> {
                Text(text = "Error: $error", color = MaterialTheme.colors.error)
            }

            else -> {
                Text(text = "No recommendations available")
            }
        }
    }
}

@Composable
fun SwipeableRestaurantCard(
    restaurant: Restaurant,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffsetX by animateDpAsState(targetValue = offsetX.dp, spring(stiffness = 300f))
    val rotation by animateFloatAsState(targetValue = offsetX / 30f, spring(stiffness = 300f))
    val opacity by animateFloatAsState(targetValue = if (abs(offsetX) > 100) 0.85f else 1f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .offset(x = animatedOffsetX)
            .rotate(rotation)
            .graphicsLayer {
                scaleX = if (offsetX < 0) 1.05f else 0.95f
                scaleY = if (offsetX < 0) 1.05f else 0.95f
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        offsetX = if (abs(offsetX) < 150) {
                            0f // Snap back if swipe is too small
                        } else {
                            if (offsetX > 0) onAccept() else onReject()
                            0f // Reset after swipe action
                        }
                    }
                ) { _, dragAmount ->
                    offsetX += dragAmount * 1.5f // Increased swipe sensitivity
                }
            }
            .shadow(12.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .alpha(opacity)
    ) {
        RestaurantRecommendationContent(restaurant = restaurant)
    }
}

@Composable
fun RestaurantRecommendationContent(restaurant: Restaurant) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Larger Restaurant Image with Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = rememberImagePainter(data = restaurant.photoUrl),
                contentDescription = "Restaurant Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xB3000000)),
                            startY = 150f
                        )
                    )
            )
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.h6.copy(color = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Restaurant Details with Icons
        Text(
            text = "Cuisine: ${restaurant.cuisineType}",
            color = Color(0xFF3E8B67),
            fontSize = 16.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_location),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = restaurant.address, color = Color(0xFF6E6E6E), fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_attach_money_24),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Price: ${restaurant.priceRange}",
                color = Color(0xFF6E6E6E),
                fontSize = 14.sp
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_rate),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Rating: ${restaurant.rating}", color = Color(0xFF6E6E6E), fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dist),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${restaurant.distance} km away",
                color = Color(0xFF6E6E6E),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun SwipeInstructions() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Swipe Right Icon for Accept
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(60.dp) // Icon container size
                .shadow(6.dp, shape = RoundedCornerShape(50)) // Soft shadow for depth
                .background(Color(0xFFE0F2F1), shape = RoundedCornerShape(50)) // Soft background
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_swipe_right_24), // Use a swipe right icon resource
                contentDescription = "Swipe Right to Accept",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp) // Icon size
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Accept",
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Swipe Left Icon for Reject
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(60.dp) // Icon container size
                .shadow(6.dp, shape = RoundedCornerShape(50)) // Soft shadow for depth
                .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(50)) // Soft background
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_swipe_left_24), // Use a swipe left icon resource
                contentDescription = "Swipe Left to Reject",
                tint = Color(0xFF3E8B67),
                modifier = Modifier.size(36.dp) // Icon size
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reject",
                color = Color(0xFF3E8B67),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


