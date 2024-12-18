package com.example.mappi.presentation.ui.main.composables.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.example.mappi.R
import com.example.mappi.domain.model.Post
import com.example.mappi.presentation.ui.friends.viewmodel.FriendsViewModel
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onAddPostClick: () -> Unit,
    onProfilePictureClick: () -> Unit,
    onSignOut: () -> Unit,
    onSearchFriendsClick: () -> Unit,
    onDeletePostClick: (Post) -> Unit,
    onFriendsClick: () -> Unit
) {
    val friendsViewModel: FriendsViewModel = hiltViewModel()
    val friends = friendsViewModel.friends.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<Post?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (profileState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
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
        } else {
            val maxWidth = constraints.maxWidth.toFloat()
            val aspectRatio = 185f / 32f
            val height = (maxWidth / aspectRatio).dp
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.flowers),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                painter = painterResource(id = R.drawable.menu),
                                contentDescription = "Menu Icon",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                onSignOut()
                                showMenu = false
                            }) {
                                Text("Sign Out")
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        IconButton(onClick = { onSearchFriendsClick() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search_friends),
                                contentDescription = "Search Friends Icon",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .offset(y = (-75).dp)
                ) {
                    RoundedCornerImageView(
                        imageUrl = profileState.profilePictureUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .size(150.dp)
                            .padding(16.dp)
                            .clickable { onProfilePictureClick() }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .offset(y = (-60).dp)
                ) {
                    profileState.userData?.userName?.let { userName ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userName,
                                textAlign = TextAlign.Center,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF113030)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Edit icon placed right next to the username
                            IconButton(onClick = { showEditDialog = true }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_edit_24), // Make sure you have an edit icon drawable
                                    contentDescription = "Edit Profile",
                                    tint = Color(0xFF3E8B67) // Matches theme color
                                )
                            }
                        }
                    }

                    profileState.userData?.email?.let { email ->
                        Text(
                            text = email,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = Color(0xFF408C68)
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-40).dp),
                    elevation = 4.dp,
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem(
                            amount = "${friends.value.size}",
                            label = "Friends",
                            onClick = onFriendsClick
                        )
                        StatItem(amount = "5", label = "Markers")
                        StatItem(amount = "${profileState.posts.size}", label = "Posts")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .offset(y = (-40).dp)
                ) {
                    items(profileState.posts) { post ->
                        PostItem(post = post, onClick = { selectedPost = post })
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddPostClick,
                backgroundColor = Color(0xFF3E8B67),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = "Add Post"
                )
            }

            selectedPost?.let { post ->
                FullScreenDialog(
                    imageUrl = post.url,
                    onDismissRequest = { selectedPost = null },
                    onDeleteClick = {
                        onDeletePostClick(post)
                        selectedPost = null
                    }
                )
            }
        }

        if (showEditDialog) {
            EditProfileBottomSheet(
                currentUserName = profileState.userData?.userName.orEmpty(),
                currentEmail = profileState.userData?.email.orEmpty(),
                onDismiss = { showEditDialog = false },
                onSave = { updatedUserName, updatedEmail ->
                    profileViewModel.updateUserProfile(updatedUserName, updatedEmail)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val maxScale = 3f
    val minScale = 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(minScale, maxScale)
                    val maxX = (size.width * (scale - 1)) / 2
                    val maxY = (size.height * (scale - 1)) / 2
                    offsetX = (offsetX + pan.x * scale).coerceIn(-maxX, maxX)
                    offsetY = (offsetY + pan.y * scale).coerceIn(-maxY, maxY)
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offsetX,
                translationY = offsetY
            )
            .background(Color.Transparent) // Ensure no background
    ) {
        SubcomposeAsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent), // Ensure loading background is also transparent
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
                        .background(Color.Transparent), // Ensure error background is also transparent
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person_foreground),
                        contentDescription = null,
                        tint = Color.Red
                    )
                }
            }
        )
    }
}

@Composable
fun FullScreenDialog(
    imageUrl: String,
    onDismissRequest: () -> Unit,
    onDeleteClick: (() -> Unit)?
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            ZoomableImage(
                imageUrl = imageUrl,
                modifier = Modifier.fillMaxSize()
            )
            if (onDeleteClick != null) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_delete_24),
                        contentDescription = "Delete Icon",
                        tint = Color.Red,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}