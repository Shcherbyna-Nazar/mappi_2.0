package com.example.mappi.presentation.ui.main.composables.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun StatItem(amount: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = amount, fontSize = 18.sp, color = Color(0xFF408C68))
        Text(text = label, fontSize = 12.sp, color = Color(0xFF113030))
    }
}