package com.example.mappi.domain.use_case.auth

import com.example.mappi.domain.repository.AuthRepository

class GetSignedInUserUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getSignedInUser()
}