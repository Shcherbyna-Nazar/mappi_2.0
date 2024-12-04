package com.example.mappi.presentation.ui.decisions.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mappi.R
import com.example.mappi.domain.model.Place

@Composable
fun RestaurantRecommendationContent(place: Place) {
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
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = place.photoUrl),
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
                text = place.name,
                style = MaterialTheme.typography.h6.copy(color = Color.White),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Restaurant Details with Icons
        Text(
            text = "Place Type: ${place.placeType.displayName}",
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
            Text(text = place.address, color = Color(0xFF6E6E6E), fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_attach_money_24),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Price: ${place.priceRange}",
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
            Text(text = "Rating: ${place.rating}", color = Color(0xFF6E6E6E), fontSize = 14.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_dist),
                contentDescription = null,
                tint = Color(0xFF6E6E6E)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${place.distance} km away",
                color = Color(0xFF6E6E6E),
                fontSize = 14.sp
            )
        }
    }
}