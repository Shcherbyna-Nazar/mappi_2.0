package com.example.mappi.presentation.ui.main.composables.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.mappi.R

@Composable
fun RoundedCornerImageView(
    imageUrl: String? = null,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(45.dp))
            .clip(RoundedCornerShape(45.dp))
            .background(Color(0xFFE8DFD0))
    ) {
        if (imageUrl != null && imageUrl.isNotEmpty()) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.Green,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(50.dp).clip(CircleShape)
                        )
                    }
                },
                error = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_foreground),
                        contentDescription = contentDescription,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(45.dp))
                            .background(Color.Transparent),
                        tint = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(45.dp))
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.ic_person_foreground),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(45.dp))
                    .background(Color.Transparent),
                tint = Color.Gray
            )
        }
    }
}
