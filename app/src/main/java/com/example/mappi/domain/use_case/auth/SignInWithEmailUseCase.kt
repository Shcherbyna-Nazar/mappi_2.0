package com.example.mappi.domain.use_case.auth

import com.example.mappi.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        authRepository.signInWithEmail(email, password)
}