package com.example.mappi.domain.use_case.profile

import com.example.mappi.domain.model.Post
import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(post: Post) = repository.deletePost(post)
}