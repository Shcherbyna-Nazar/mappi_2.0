package com.example.mappi.presentation.ui.decisions.compose

import android.location.Location
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mappi.domain.model.Place
import com.example.mappi.presentation.ui.decisions.viewmodel.DecisionsViewModel
import com.example.mappi.util.nearby.PlaceType

@Composable
fun DecisionsScreen(
    navController: NavController,
    viewModel: DecisionsViewModel,
    userLocation: Location
) {
    val restaurant by viewModel.placeRecommendation.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val placeTypes = PlaceType.values().toList()
    var selectedPlaceTypes by remember { mutableStateOf(setOf(PlaceType.RESTAURANT)) } // Immutable set
    var showDetailScreen by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFA7FFEB)))),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Compact Chip Menu at the Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            placeTypes.forEach { type ->
                Chip(
                    type = type,
                    isSelected = selectedPlaceTypes.contains(type),
                    onClick = {
                        selectedPlaceTypes = if (selectedPlaceTypes.contains(type)) {
                            selectedPlaceTypes - type
                        } else {
                            selectedPlaceTypes + type
                        }
                        viewModel.fetchRecommendation(
                            userLocation,
                            placeTypes = selectedPlaceTypes.toList(),
                            forceRefresh = true
                        )
                    }
                )
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color(0xFF0F3C3B))
                }

                restaurant != null -> {
                    SwipeableRestaurantCard(
                        place = restaurant!!,
                        onAccept = {
                            viewModel.makeDecision(userLocation, restaurant!!.id, true)
                            navController.navigate(
                                "animation/${userLocation.latitude},${userLocation.longitude}/" +
                                        "${restaurant!!.location.latitude},${restaurant!!.location.longitude}"
                            )
                        },
                        onReject = {
                            viewModel.makeDecision(userLocation, restaurant!!.id, false)
                        },
                        onRestaurantClick = {
                            selectedPlace = restaurant
                            showDetailScreen = true
                        }
                    )
                }

                error != null -> {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colors.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    Text(
                        text = "No recommendations available for selected types",
                        color = MaterialTheme.colors.onSurface,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Swipe Instructions at the Bottom
        if (!isLoading && restaurant != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SwipeInstructions()
            }
        }
    }

    // Animated Detail Screen
    if (showDetailScreen && selectedPlace != null) {
        RestaurantDetailScreen(
            userLocation = userLocation,
            place = selectedPlace!!,
            onAccept = {
                showDetailScreen = false
                viewModel.makeDecision(userLocation, selectedPlace!!.id, true)
                navController.navigate(
                    "animation/${userLocation.latitude},${userLocation.longitude}/" +
                            "${restaurant!!.location.latitude},${restaurant!!.location.longitude}"
                )
            },
            onReject = {
                showDetailScreen = false
                viewModel.makeDecision(userLocation, selectedPlace!!.id, false)
            },
            onDismiss = { showDetailScreen = false }
        )
    }
}

@Composable
fun Chip(
    type: PlaceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) Color(0xFF0F3C3B) else Color(0xFFE0F7FA),
        contentColor = if (isSelected) Color.White else Color(0xFF0F3C3B),
        border = if (isSelected) BorderStroke(2.dp, Color(0xFFFFA726)) else null,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = type.iconResId),
                contentDescription = type.name,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) Color.White else Color(0xFF0F3C3B)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = type.name,
                style = MaterialTheme.typography.body2.copy(
                    color = if (isSelected) Color.White else Color(0xFF0F3C3B),
                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                )
            )
        }
    }
}



