package com.example.mappi.presentation.ui.main.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.mappi.R
import com.example.mappi.domain.model.UserData

@Composable
fun ProfileScreen(
    userData: UserData?,
    posts: List<String>,
    onAddPostClick: () -> Unit,
    onSignOut: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.flowers),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
                Image(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu Icon",
                    modifier = Modifier
                        .size(65.dp)
                        .padding(16.dp)
                        .clickable { /* Handle menu icon click */ }
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (userData?.profilePictureUrl != null) {
                RoundedCornerImageView(
                    imageUrl = userData.profilePictureUrl,
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(16.dp)
                )
            } else {
                RoundedCornerImageView(
                    painter = painterResource(id = R.drawable.ic_person_foreground),
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (userData?.userName != null) {
                Text(
                    text = userData.userName,
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF113030)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (userData?.email != null) {
                Text(
                    text = userData.email,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color(0xFF408C68)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = 4.dp,
                backgroundColor = Color.White,
                shape = RoundedCornerShape(15.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatItem(amount = "10", label = "Friends")
                    StatItem(amount = "5", label = "Markers")
                    StatItem(amount = "3", label = "Posts")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(posts) { post ->
                    PostItem(post = post)
                }
            }
        }

        FloatingActionButton(
            onClick = onAddPostClick,
            backgroundColor = Color(0xFF408C68),
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(painter = painterResource(id = R.drawable.baseline_add_24), contentDescription = "Add Post")
        }
    }
}

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

@Composable
fun PostItem(post: String) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE8DFD0)),
        elevation = 4.dp
    ) {
        SubcomposeAsyncImage(
            model = post,
            contentDescription = "Post Image",
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Loading...")
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun RoundedCornerImageView(
    painter: Painter? = null,
    imageUrl: String? = null,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFFE8DFD0))
    ) {
        if (imageUrl != null) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                loading = {
                    Text(
                        text = "Loading...",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop
            )
        } else if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color.Transparent),
                contentScale = ContentScale.Crop
            )
        }
    }
}
