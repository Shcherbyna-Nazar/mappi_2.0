package com.example.mappi.data.repository

import android.net.Uri
import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.mapper.PostMapper
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.repository.ProfileRepository

class FirebaseProfileRepository(private val dataSource: FirebaseDataSource) : ProfileRepository {
    override suspend fun uploadPhoto(
        uri: Uri,
        latitude: Double?,
        longitude: Double?,
        isProfilePicture: Boolean
    ): String {
        return dataSource.uploadPhoto(uri, latitude, longitude, isProfilePicture)
    }

    override suspend fun deletePost(post: Post) {
        dataSource.deletePost(post)
    }

    override suspend fun getPosts(): List<Post> {
        return dataSource.getPosts().map { PostMapper.mapToDomain(it) }
    }
}