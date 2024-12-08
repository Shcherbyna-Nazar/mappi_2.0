package com.example.mappi.presentation.ui.main.composables.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.model.Post

@Composable
fun FullScreenImageWithComments(
    myPosts: List<Post>,
    friendsPosts: List<Post>,
    postId: String,
    onDismissRequest: () -> Unit,
    onAddComment: (String) -> Unit
) {

    val post = (myPosts + friendsPosts).find { it.id == postId } ?: return
    var newComment by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)) // Fullscreen overlay
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(vertical = 16.dp, horizontal = 16.dp)
        ) {
            // Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Dialog",
                        tint = Color.Gray
                    )
                }
            }

            // Image Section with Rounded Corners
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp)) // Rounded corners for the image
                    .background(Color.LightGray) // Placeholder background
            ) {
                SubcomposeAsyncImage(
                    model = post.url,
                    contentDescription = null,
                    contentScale = ContentScale.Crop, // Cropped for a better fit
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rating Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rating:",
                    style = MaterialTheme.typography.subtitle1,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                repeat(post.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Filled Star",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                }
                repeat(5 - post.rating) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Empty Star",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comments Section
            Text(
                text = "Comments",
                style = MaterialTheme.typography.h6,
                color = Color.Black
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Allow scrolling
                    .padding(top = 8.dp)
            ) {
                items(post.comments) { comment ->
                    SmallCommentCard(comment)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Comment Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = newComment,
                    onValueChange = { newComment = it },
                    placeholder = { Text("Add a comment...") },
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color(0xFF408C68),
                        cursorColor = Color(0xFF408C68),
                        textColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (newComment.isNotBlank()) {
                            onAddComment(newComment)
                            newComment = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF408C68))
                ) {
                    Text("Submit", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun SmallCommentCard(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9F9F9))
            .padding(8.dp)
    ) {
        // User Profile Photo
        SubcomposeAsyncImage(
            model = comment.profilePictureUrl,
            contentDescription = "Profile Photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(50)) // Circular shape
                .background(Color.Gray.copy(alpha = 0.5f)) // Placeholder background
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Comment Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = comment.userName,
                style = MaterialTheme.typography.subtitle2,
                color = Color(0xFF408C68)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.body2,
                color = Color.Black,
                maxLines = 2, // Limit the number of visible lines
                overflow = TextOverflow.Ellipsis // Add ellipsis for long text
            )
        }
    }
}


