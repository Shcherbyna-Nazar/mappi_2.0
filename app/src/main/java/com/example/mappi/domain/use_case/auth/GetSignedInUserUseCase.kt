package com.example.mappi.domain.use_case.auth

import com.example.mappi.domain.repository.AuthRepository
import javax.inject.Inject

class GetSignedInUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke() = authRepository.getSignedInUser()
}