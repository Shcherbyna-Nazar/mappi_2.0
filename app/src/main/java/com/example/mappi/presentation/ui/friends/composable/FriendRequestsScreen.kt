package com.example.mappi.presentation.ui.friends.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mappi.domain.model.FriendRequest
import com.example.mappi.presentation.ui.friends.viewmodel.FriendsViewModel

@Composable
fun FriendRequestsScreen() {
    val friendsViewModel: FriendsViewModel = hiltViewModel()
    val friendRequests by friendsViewModel.friendRequests.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Friend Requests", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))

        if (friendRequests.isNotEmpty()) {
            friendRequests.forEach { request ->
                FriendRequestItem(
                    request = request,
                    onAccept = { friendsViewModel.acceptRequest(request.requestId) },
                    onReject = { friendsViewModel.rejectRequest(request.requestId) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            Text("No friend requests", style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun FriendRequestItem(request: FriendRequest, onAccept: () -> Unit, onReject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundedImage(
                    imageUrl = request.fromUser.profilePictureUrl,
                    contentDescription = "Profile picture"
                )
                Column(
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(request.fromUser.userName ?: "Unknown", style = MaterialTheme.typography.body1)
                    Text(request.fromUser.email ?: "", style = MaterialTheme.typography.body2, color = Color.Gray)
                }
            }

            Row {
                IconButton(onClick = onAccept) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accept",
                        tint = Color.Green
                    )
                }
                IconButton(onClick = onReject) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Reject",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}
