package com.example.mappi.presentation.ui.decisions.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mappi.R

@Composable
fun SwipeInstructions() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Swipe Left Icon for Accept
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(60.dp) // Icon container size
                .shadow(6.dp, shape = RoundedCornerShape(50)) // Soft shadow for depth
                .background(Color(0xFFFFEBEE), shape = RoundedCornerShape(50)) // Soft background
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_swipe_left_24), // Use a swipe right icon resource
                contentDescription = "Swipe Left to Accept",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(36.dp) // Icon size
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reject",
                color = Color(0xFFD32F2F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Swipe Right Icon for Reject
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(60.dp) // Icon container size
                .shadow(6.dp, shape = RoundedCornerShape(50)) // Soft shadow for depth
                .background(Color(0xFFE0F2F1), shape = RoundedCornerShape(50)) // Soft background
                .padding(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.baseline_swipe_right_24), // Use a swipe left icon resource
                contentDescription = "Swipe Right to Reject",
                tint = Color(0xFF3E8B67),
                modifier = Modifier.size(36.dp) // Icon size
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Accept",
                color = Color(0xFF3E8B67),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}