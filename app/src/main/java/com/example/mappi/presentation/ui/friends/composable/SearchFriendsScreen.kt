package com.example.mappi.presentation.ui.friends.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mappi.domain.model.RequestStatus
import com.example.mappi.domain.model.UserData
import com.example.mappi.presentation.ui.friends.viewmodel.FriendsViewModel

@Composable
fun SearchFriendsScreen() {
    val friendsViewModel: FriendsViewModel = hiltViewModel()
    val searchResult by friendsViewModel.searchResults.collectAsState()

    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = {
                query = it
                friendsViewModel.searchFriends(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            singleLine = true,
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .background(
                            MaterialTheme.colors.surface,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(16.dp)
                ) {
                    if (query.isEmpty()) {
                        Text("Search friends...", style = MaterialTheme.typography.body1)
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchResult.isNotEmpty()) {
            searchResult.forEach { friend ->
                FriendItem(
                    user = friend,
                    onSendRequest = { friendsViewModel.sendRequest(friend.userId) }
                )
            }
        } else {
            Text("No friends found", style = MaterialTheme.typography.body1)
        }
    }
}

@Composable
fun FriendItem(user: UserData, onSendRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundedImage(
                imageUrl = user.profilePictureUrl,
                contentDescription = "Profile picture"
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(user.userName ?: "Unknown", style = MaterialTheme.typography.body1)
                Text(user.email ?: "", style = MaterialTheme.typography.body2, color = Color.Gray)
            }
        }

        when (user.requestStatus) {
            RequestStatus.NONE -> {
                IconButton(onClick = onSendRequest) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Add Friend",
                        tint = Color.Blue
                    )
                }
            }
            RequestStatus.SENT -> {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Request Sent",
                    tint = Color.Blue,
                    modifier = Modifier.size(24.dp)
                )
            }
            RequestStatus.RECEIVED -> {
                Text("Request Received", color = Color.Blue)
            }
            RequestStatus.ACCEPTED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accepted",
                    tint = Color.Green,
                    modifier = Modifier.size(24.dp)
                )
            }
            RequestStatus.REJECTED -> {
                Text("Rejected", color = Color.Red)
            }
        }
    }
}
