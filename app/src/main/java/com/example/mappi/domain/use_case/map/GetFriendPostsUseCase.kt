package com.example.mappi.domain.use_case.map

import com.example.mappi.domain.repository.ProfileRepository
import javax.inject.Inject

class GetFriendPostsUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke() = repository.getFriendPosts()
}