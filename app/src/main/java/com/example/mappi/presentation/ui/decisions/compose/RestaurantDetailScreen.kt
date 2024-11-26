package com.example.mappi.presentation.ui.decisions.compose

import android.location.Location
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mappi.domain.model.Restaurant

@Composable
fun RestaurantDetailScreen(
    userLocation: Location,
    restaurant: Restaurant,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )

        // Content Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            // Detail Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(16.dp, shape = RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Restaurant Name
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E8B67)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Map with Custom Marker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(4.dp)
                ) {
                    RestaurantMap(
                        userLocation = userLocation,
                        restaurantLocation = restaurant.location,
                        restaurantTitle = restaurant.name,
                        restaurantPhotoUrl = restaurant.photoUrl
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restaurant Info Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Cuisine: ${restaurant.cuisineType}",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Rating: ${restaurant.rating} ⭐",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Address: ${restaurant.address}",
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Distance: ${restaurant.distance} km",
                        style = MaterialTheme.typography.body1
                    )
                }
            }

            // Action Buttons Section (Outside Card)
            Spacer(modifier = Modifier.height(16.dp))
            ActionButtons(
                onAccept = onAccept,
                onReject = onReject
            )
        }
    }
}

@Composable
fun ActionButtons(onAccept: () -> Unit, onReject: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Reject Button
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD32F2F)) // Reject button color
                .clickable { onReject() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reject",
                style = MaterialTheme.typography.button,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        // Accept Button
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF3E8B67)) // Accept button color
                .clickable { onAccept() }
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Accept",
                style = MaterialTheme.typography.button,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

