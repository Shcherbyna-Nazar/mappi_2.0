package com.example.mappi.data.repository

import android.net.Uri
import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.mapper.CommentMapper
import com.example.mappi.data.mapper.PostMapper
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.repository.ProfileRepository

class FirebaseProfileRepository(private val dataSource: FirebaseDataSource) : ProfileRepository {
    override suspend fun uploadPhoto(
        id: String,
        uri: Uri,
        latitude: Double?,
        longitude: Double?,
        userName: String,
        rating: Int,
        comment: Comment,
        isProfilePicture: Boolean
    ): String {
        return dataSource.uploadPhoto(
            id,
            uri,
            latitude,
            longitude,
            userName,
            rating,
            CommentMapper.mapToDto(comment),
            isProfilePicture
        )
    }

    override suspend fun addComment(postId: String, comment: Comment) {
        dataSource.addComment(postId, CommentMapper.mapToDto(comment))
    }

    override suspend fun deletePost(post: Post) {
        dataSource.deletePost(post)
    }

    override suspend fun getPosts(): List<Post> {
        return dataSource.getPosts().map { PostMapper.mapToDomain(it) }
    }

    override suspend fun getFriendPosts(): List<Post> {
        return dataSource.getLast10PostsFromFriends().map { PostMapper.mapToDomain(it) }
    }
}