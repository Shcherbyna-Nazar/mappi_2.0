package com.example.mappi.domain.use_case.profile

import com.example.mappi.domain.model.Post
import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(): List<Post> {
        return repository.getPosts()
    }
}