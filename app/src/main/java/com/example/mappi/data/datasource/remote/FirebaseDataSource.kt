package com.example.mappi.data.datasource.remote

import android.net.Uri
import android.util.Log
import com.example.mappi.data.datasource.remote.dto.CommentDto
import com.example.mappi.data.datasource.remote.dto.FriendRequestDto
import com.example.mappi.data.datasource.remote.dto.PostDto
import com.example.mappi.data.datasource.remote.dto.UserDecisionDto
import com.example.mappi.data.datasource.remote.dto.UserDto
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.model.RequestStatus
import com.example.mappi.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val storageReference: StorageReference,
    private val firebaseAuth: FirebaseAuth,
    firebaseDatabase: FirebaseDatabase
) {
    private val usersRef = firebaseDatabase.getReference("users")
    private val friendRequestsRef = firebaseDatabase.getReference("friend_requests")
    private val decisionsRef = firebaseDatabase.getReference("decisions")
    private val postsRef = firebaseDatabase.getReference("posts")

    suspend fun uploadPhoto(
        postId: String,
        uri: Uri,
        latitude: Double? = null,
        longitude: Double? = null,
        userName: String,
        rating: Int,
        comment: CommentDto,
        isProfilePicture: Boolean
    ): String {
        val currentUser = firebaseAuth.currentUser ?: return ""
        val photoRef = if (isProfilePicture) {
            storageReference.child("profilePictures/${currentUser.uid}.jpg")
        } else {
            storageReference.child("posts/${currentUser.uid}/$postId.jpg")
        }

        return try {
            // Upload photo to Firebase Storage
            photoRef.putFile(uri).await()

            // Retrieve download URL
            val url = photoRef.downloadUrl.await().toString()

            // Update profile picture or post metadata in Realtime Database
            if (isProfilePicture) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(url))
                    .build()
                currentUser.updateProfile(profileUpdate).await()
                usersRef.child(currentUser.uid).child("profilePictureUrl").setValue(url).await()
            } else {
                // Construct post metadata
                val post = mapOf(
                    "id" to postId,
                    "url" to url,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "userName" to userName,
                    "rating" to rating,
                    "comments" to listOf(comment)
                )
                postsRef.child(currentUser.uid).child(postId).setValue(post).await()
            }

            url
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    suspend fun getPosts(): List<PostDto> {
        val currentUser = firebaseAuth.currentUser ?: return emptyList()
        return try {
            val postsSnapshot = postsRef.child(currentUser.uid).get().await()
            if (!postsSnapshot.exists()) return emptyList()

            postsSnapshot.children.mapNotNull { snapshot ->
                try {
                    val id = snapshot.key.orEmpty()
                    val url = snapshot.child("url").value.toString()
                    val latitude =
                        snapshot.child("latitude").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val longitude =
                        snapshot.child("longitude").value?.toString()?.toDoubleOrNull() ?: 0.0
                    val userName = snapshot.child("userName").value.toString()
                    val rating = snapshot.child("rating").value?.toString()?.toIntOrNull() ?: 0
                    val comments =
                        snapshot.child("comments").children.mapNotNull { commentSnapshot ->
                            val text = commentSnapshot.child("text").value.toString()
                            val commentUserName = commentSnapshot.child("userName").value.toString()
                            val timestamp =
                                commentSnapshot.child("timestamp").value?.toString()?.toLongOrNull()
                                    ?: 0L
                            val profilePictureUrl =
                                commentSnapshot.child("profilePictureUrl").value.toString()

                            val ownerId = commentSnapshot.child("ownerId").value.toString()
                            CommentDto(
                                text,
                                commentUserName,
                                ownerId,
                                timestamp,
                                profilePictureUrl
                            )
                        }

                    PostDto(
                        id = id,
                        url = url,
                        latitude = latitude,
                        longitude = longitude,
                        userName = userName,
                        rating = rating,
                        comments = comments
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    suspend fun signInWithEmail(email: String, password: String): Resource<UserDto> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Resource.Success(
                    UserDto(
                        userId = it.uid,
                        userName = it.displayName,
                        email = it.email,
                        profilePictureUrl = it.photoUrl.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserDto> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdate).await()

                usersRef.child(user.uid)
                    .setValue(UserDto(user.uid, name, email, user.photoUrl.toString()))

                Resource.Success(
                    UserDto(
                        userId = user.uid,
                        userName = user.displayName,
                        email = user.email,
                        profilePictureUrl = user.photoUrl.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    fun getSignedInUser(): UserDto? {
        return firebaseAuth.currentUser?.let {
            UserDto(
                userId = it.uid,
                userName = it.displayName,
                email = it.email,
                profilePictureUrl = it.photoUrl.toString()
            )
        }
    }

    suspend fun deletePost(post: Post) {
        try {
            val storagePath = extractStoragePath(post.url) ?: return
            val photoRef = storageReference.child(storagePath)

            photoRef.delete().await()

            postsRef.child(firebaseAuth.currentUser?.uid ?: return).child(post.id).removeValue().await()

            Log.d("FirebaseDataSource", "Post and file deleted successfully")
        } catch (e: Exception) {
            when (e) {
                is StorageException -> {
                    if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        Log.e("FirebaseDataSource", "File does not exist: ${e.localizedMessage}")
                    } else {
                        Log.e("FirebaseDataSource", "Storage error: ${e.localizedMessage}")
                    }
                }
                else -> Log.e("FirebaseDataSource", "Error deleting post: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Helper function to extract the storage path from the download URL.
     */
    private fun extractStoragePath(downloadUrl: String): String? {
        return try {

            val urlParts = downloadUrl.split("?")[0]
            val encodedPath = urlParts.substringAfter("/o/")
            Uri.decode(encodedPath)
        } catch (e: Exception) {
            Log.e("FirebaseDataSource", "Error extracting storage path: ${e.localizedMessage}")
            null
        }
    }

    suspend fun getFriends(): List<UserDto> {
        val userId = firebaseAuth.currentUser?.uid ?: return emptyList()
        val friendsListSnapshot = usersRef.child(userId).child("friends").get().await()
        val friendsIds = friendsListSnapshot.children.map { it.key.orEmpty() }
        return friendsIds.mapNotNull { friendId ->
            usersRef.child(friendId).get().await().getValue(UserDto::class.java)
        }
    }

    suspend fun searchUsers(query: String): List<UserDto> {
        val currentUser = firebaseAuth.currentUser ?: return emptyList()
        return try {
            val friendsListSnapshot = usersRef.child(currentUser.uid).child("friends").get().await()
            val friendsIds = friendsListSnapshot.children.map { it.key.orEmpty() }

            val resultSnapshot =
                usersRef.orderByChild("userName").startAt(query).endAt(query + "\uf8ff").get()
                    .await()
            val userDtos = resultSnapshot.children.mapNotNull {
                it.getValue(UserDto::class.java)
            }.filter { it.userId != currentUser.uid }

            userDtos.map { user ->
                val sentRequestSnapshot =
                    friendRequestsRef.orderByChild("fromUserId").equalTo(currentUser.uid).get()
                        .await()
                val receivedRequestSnapshot =
                    friendRequestsRef.orderByChild("toUserId").equalTo(currentUser.uid).get()
                        .await()

                val requestStatus = when {
                    friendsIds.contains(user.userId) -> RequestStatus.ACCEPTED
                    sentRequestSnapshot.children.any {
                        it.child("toUserId").value == user.userId && it.child(
                            "status"
                        ).value == RequestStatus.SENT.name
                    } -> RequestStatus.SENT

                    receivedRequestSnapshot.children.any {
                        it.child("fromUserId").value == user.userId && it.child(
                            "status"
                        ).value == RequestStatus.SENT.name
                    } -> RequestStatus.RECEIVED

                    else -> RequestStatus.NONE
                }
                user.copy(requestStatus = requestStatus)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendFriendRequest(toUserId: String) {
        val fromUserId = firebaseAuth.currentUser?.uid ?: return
        val requestId = friendRequestsRef.push().key ?: return
        val request = FriendRequestDto(
            requestId = requestId,
            fromUserId = fromUserId,
            toUserId = toUserId,
            status = RequestStatus.SENT
        )
        friendRequestsRef.child(requestId).setValue(request).await()
    }

    suspend fun acceptFriendRequest(requestId: String) {
        val requestSnapshot = friendRequestsRef.child(requestId).get().await()
        val fromUserId = requestSnapshot.child("fromUserId").value as? String ?: return
        val toUserId = requestSnapshot.child("toUserId").value as? String ?: return

        usersRef.child(fromUserId).child("friends").child(toUserId).setValue(true).await()
        usersRef.child(toUserId).child("friends").child(fromUserId).setValue(true).await()
        friendRequestsRef.child(requestId).removeValue().await()
    }

    suspend fun rejectFriendRequest(requestId: String) {
        friendRequestsRef.child(requestId).removeValue().await()
    }

    suspend fun getFriendRequests(): List<FriendRequestDto> {
        val userId = firebaseAuth.currentUser?.uid ?: return emptyList()
        val receivedRequestsSnapshot =
            friendRequestsRef.orderByChild("toUserId").equalTo(userId).get().await()
        return receivedRequestsSnapshot.children.mapNotNull {
            it.getValue(FriendRequestDto::class.java)
        }
    }

    suspend fun getUserById(userId: String): UserDto? {
        return try {
            usersRef.child(userId).get().await().getValue(UserDto::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getLast10PostsFromFriends(): List<PostDto> {
        val currentUser = firebaseAuth.currentUser ?: return emptyList()
        val friendsIds = usersRef.child(currentUser.uid).child("friends").get()
            .await().children.map { it.key.orEmpty() }
        val posts = mutableListOf<PostDto>()

        friendsIds.forEach { friendId ->
            try {
                val friendPostsSnapshot =
                    postsRef.child(friendId).orderByKey().limitToLast(10).get().await()
                friendPostsSnapshot.children.mapNotNull { snapshot ->
                    try {
                        val id = snapshot.key.orEmpty()
                        val url = snapshot.child("url").value.toString()
                        val latitude =
                            snapshot.child("latitude").value?.toString()?.toDoubleOrNull() ?: 0.0
                        val longitude =
                            snapshot.child("longitude").value?.toString()?.toDoubleOrNull() ?: 0.0
                        val userName = snapshot.child("userName").value.toString()
                        val rating = snapshot.child("rating").value?.toString()?.toIntOrNull() ?: 0
                        val comments =
                            snapshot.child("comments").children.mapNotNull { commentSnapshot ->
                                val text = commentSnapshot.child("text").value.toString()
                                val commentUserName =
                                    commentSnapshot.child("userName").value.toString()
                                val timestamp = commentSnapshot.child("timestamp").value?.toString()
                                    ?.toLongOrNull() ?: 0L

                                val profilePictureUrl =
                                    commentSnapshot.child("profilePictureUrl").value.toString()
                                val ownerId = commentSnapshot.child("ownerId").value.toString()
                                CommentDto(
                                    text,
                                    commentUserName,
                                    ownerId,
                                    timestamp,
                                    profilePictureUrl
                                )
                            }

                        posts.add(
                            PostDto(
                                id = id,
                                url = url,
                                latitude = latitude,
                                longitude = longitude,
                                userName = userName,
                                rating = rating,
                                comments = comments
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return posts.sortedByDescending { it.id }.take(10)
    }

    suspend fun deleteFriend(friendId: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        usersRef.child(userId).child("friends").child(friendId).removeValue().await()
        usersRef.child(friendId).child("friends").child(userId).removeValue().await()
    }

    suspend fun getUserDecisions(place_Ids: List<String>): Map<String, UserDecisionDto> {
        val currentUser = firebaseAuth.currentUser ?: return emptyMap()
        return place_Ids.mapNotNull { placeId ->
            val statsSnapshot = decisionsRef.child(currentUser.uid).child(placeId).get().await()
            statsSnapshot.getValue(UserDecisionDto::class.java)?.let { placeId to it }
        }.toMap()
    }

    suspend fun makeDecision(placeId: String, decisionType: Boolean) {
        val currentUser = firebaseAuth.currentUser ?: return
        val decisionRef = decisionsRef.child(currentUser.uid).child(placeId)
        val stats =
            decisionRef.get().await().getValue(UserDecisionDto::class.java) ?: UserDecisionDto(
                placeId
            )

        if (decisionType) {
            stats.successCount++
        } else {
            stats.failureCount++
        }
        decisionRef.setValue(stats).await()
    }

    fun addComment(postId: String, mapToDto: CommentDto) {
        val comment = mapOf(
            "text" to mapToDto.text,
            "userName" to mapToDto.userName,
            "ownerId" to mapToDto.ownerId,
            "timestamp" to mapToDto.timestamp,
            "profilePictureUrl" to mapToDto.profilePictureUrl
        )
        postsRef.child(mapToDto.ownerId).child(postId).child("comments").push().setValue(comment)
    }
}