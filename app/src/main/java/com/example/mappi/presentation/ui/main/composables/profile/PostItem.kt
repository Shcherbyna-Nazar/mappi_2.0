package com.example.mappi.presentation.ui.main.composables.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.mappi.R
import com.example.mappi.domain.model.Post

@Composable
fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE8DFD0))
            .clickable { onClick() },
        elevation = 8.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image Content
            SubcomposeAsyncImage(
                model = post.url,
                contentDescription = "Post Image",
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF0F3C3B),
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
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_person_foreground),
                            contentDescription = "Error Image",
                            tint = Color.Gray,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop
            )

            // Rating Overlay - Small and Sleek in Top-Right Corner
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(
                        color = Color(0x80000000),
                        shape = RoundedCornerShape(50) // Rounded pill shape
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp), // Padding inside the pill
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(post.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating Star",
                        tint = Color(0xFFFFD700), // Gold color for stars
                        modifier = Modifier.size(8.dp) // Smaller star size
                    )
                }
                repeat(5 - post.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Empty Star",
                        tint = Color(0xFFCCCCCC), // Gray for unselected stars
                        modifier = Modifier.size(8.dp)
                    )
                }
            }
        }
    }
}

