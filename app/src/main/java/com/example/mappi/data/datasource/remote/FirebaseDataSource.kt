package com.example.mappi.data.datasource.remote

import android.net.Uri
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
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storageMetadata
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

    suspend fun uploadPhoto(
        uri: Uri,
        latitude: Double? = null,
        longitude: Double? = null,
        isProfilePicture: Boolean
    ): String {
        val currentUser = firebaseAuth.currentUser ?: return ""
        val photoRef = if (isProfilePicture) {
            storageReference.child("profilePictures/${currentUser.uid}.jpg")
        } else {
            storageReference.child("posts/${currentUser.uid}/${System.currentTimeMillis()}.jpg")
        }

        return try {
            photoRef.putFile(uri).await()
            latitude?.let {
                photoRef.updateMetadata(storageMetadata {
                    setCustomMetadata("latitude", it.toString())
                    setCustomMetadata("longitude", longitude.toString())
                }).await()
            }
            val url = photoRef.downloadUrl.await().toString()
            if (isProfilePicture) {
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(url))
                    .build()
                currentUser.updateProfile(profileUpdate).await()
                usersRef.child(currentUser.uid).child("profilePictureUrl").setValue(url).await()
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
            val listResult = storageReference.child("posts/${currentUser.uid}").listAll().await()
            listResult.items.map {
                val url = it.downloadUrl.await().toString()
                val metadata = it.metadata.await()
                val latitude = metadata.getCustomMetadata("latitude")?.toDouble() ?: 0.0
                val longitude = metadata.getCustomMetadata("longitude")?.toDouble() ?: 0.0
                PostDto(url, latitude, longitude)
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
        val currentUser = firebaseAuth.currentUser ?: return
        val fileName = Uri.parse(post.url).lastPathSegment ?: return
        val photoRef = storageReference.child("posts/${currentUser.uid}/$fileName")
        try {
            photoRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
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
            val friendPosts = storageReference.child("posts/$friendId").listAll().await()
            friendPosts.items.take(5).map {
                val url = it.downloadUrl.await().toString()
                val metadata = it.metadata.await()
                val latitude = metadata.getCustomMetadata("latitude")?.toDouble() ?: 0.0
                val longitude = metadata.getCustomMetadata("longitude")?.toDouble() ?: 0.0
                posts.add(PostDto(url, latitude, longitude))
            }
        }

        return posts
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


}
