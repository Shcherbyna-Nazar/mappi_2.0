package com.example.mappi.domain.use_case.profile

import android.net.Uri
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(
        id: String,
        uri: Uri,
        latitude: Double?,
        longitude: Double?,
        userName: String,
        rating: Int,
        comment: Comment,
        isProfilePicture: Boolean
    ): String {
        return repository.uploadPhoto(
            id,
            uri,
            latitude,
            longitude,
            userName,
            rating,
            comment,
            isProfilePicture
        )
    }
}