package com.example.mappi.domain.use_case.profile

import android.net.Uri
import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadPhotoUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(uri: Uri): String {
        return repository.uploadPhoto(uri)
    }
}