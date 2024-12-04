package com.example.mappi.presentation.ui.decisions.compose

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.mappi.domain.model.Place
import kotlin.math.abs

@Composable
fun SwipeableRestaurantCard(
    place: Place,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onRestaurantClick: () -> Unit
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
                        if (abs(offsetX) > 150) {
                            if (offsetX > 0) onAccept() else onReject()
                        }
                        offsetX = 0f
                    }
                ) { _, dragAmount -> offsetX += dragAmount * 0.75f }
            }
            .clickable { onRestaurantClick() }
            .shadow(12.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .alpha(opacity)
    ) {
        RestaurantRecommendationContent(place = place)
    }
}