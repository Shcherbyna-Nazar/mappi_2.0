package com.example.mappi.domain.repository

import android.net.Uri
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.model.Post

interface ProfileRepository {
    suspend fun uploadPhoto(
        id: String,
        uri: Uri,
        latitude: Double?,
        longitude: Double?,
        userName: String,
        rating: Int,
        comment: Comment,
        isProfilePicture: Boolean
    ): String

    suspend fun getPosts(): List<Post>
    suspend fun deletePost(post: Post)
    suspend fun getFriendPosts(): List<Post>
    suspend fun addComment(postId: String, comment: Comment): Any

}