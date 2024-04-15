package com.example.mappi.presentation.ui.sign_up.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.UserData
import com.example.mappi.domain.use_case.auth.SignUpWithEmailUseCase
import com.example.mappi.presentation.ui.sign_up.SignUpState
import com.example.mappi.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val SignUpWithEmailUseCase: SignUpWithEmailUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpState())
    val state = _state.asStateFlow()

    fun signUpWithEmail(
        name: String,
        email: String,
        password: String,
        repeatPassword: String
    ) {
        if (password != repeatPassword) {
            _state.update {
                it.copy(
                    isSignUpSuccessful = false,
                    signUpError = "Passwords do not match"
                )
            }
            return
        }
        viewModelScope.launch {
            val result = SignUpWithEmailUseCase(name, email.trim(), password)
            onSignUpResult(result)
        }
    }

    private fun onSignUpResult(result: Resource<UserData>) {
        _state.update {
            it.copy(
                isSignUpSuccessful = result.data != null,
                signUpError = result.message
            )
        }
    }

    fun resetState() {
        _state.update { SignUpState() }
    }
}