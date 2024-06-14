package com.example.mappi.domain.repository

import android.net.Uri
import com.example.mappi.domain.model.Post

interface ProfileRepository {
    suspend fun uploadPhoto(
        uri: Uri,
        latitude: Double?,
        longitude: Double?,
        isProfilePicture: Boolean
    ): String

    suspend fun getPosts(): List<Post>
    suspend fun deletePost(post: Post)
    suspend fun getFriendPosts(): List<Post>

}