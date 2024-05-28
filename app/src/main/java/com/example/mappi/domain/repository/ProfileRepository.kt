package com.example.mappi.domain.repository

import android.net.Uri

interface ProfileRepository {
    suspend fun uploadPhoto(uri: Uri): String
    suspend fun getPosts(): List<String>
}