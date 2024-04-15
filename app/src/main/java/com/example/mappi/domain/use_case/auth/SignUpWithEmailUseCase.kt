package com.example.mappi.domain.use_case.auth

import com.example.mappi.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(name: String, email: String, password: String) =
        authRepository.signUpWithEmail(name, email, password)
}
