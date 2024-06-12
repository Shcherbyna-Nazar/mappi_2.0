package com.example.mappi.domain.use_case.friends

import com.example.mappi.data.repository.FirebaseUserRepository
import com.example.mappi.domain.repository.UserRepository
import javax.inject.Inject

class GetFriendRequestsUseCase @Inject constructor(private val userRepository: UserRepository) {
    suspend operator fun invoke() = userRepository.getFriendRequests()
}