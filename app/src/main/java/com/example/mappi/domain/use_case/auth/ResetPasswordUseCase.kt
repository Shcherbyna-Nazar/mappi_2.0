package com.example.mappi.domain.use_case.auth

import com.example.mappi.domain.repository.AuthRepository
import javax.inject.Inject

class ResetPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String) {
        authRepository.resetPassword(email)
    }
}