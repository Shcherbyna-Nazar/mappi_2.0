package com.example.mappi.domain.use_case.profile

import com.example.mappi.domain.repository.UserRepository
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
){
    suspend operator fun invoke(
        userName: String,
        email: String,
    ) = userRepository.updateUserProfile(userName, email)
}