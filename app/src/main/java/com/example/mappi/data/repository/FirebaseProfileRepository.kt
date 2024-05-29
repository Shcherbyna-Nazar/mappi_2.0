package com.example.mappi.data.repository

import android.net.Uri
import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.domain.repository.ProfileRepository

class FirebaseProfileRepository(private val dataSource: FirebaseDataSource) : ProfileRepository {
    override suspend fun uploadPhoto(uri: Uri, isProfilePicture: Boolean): String {
        return dataSource.uploadPhoto(uri, isProfilePicture)
    }

    override suspend fun getPosts(): List<String> {
        return dataSource.getPosts()
    }
}