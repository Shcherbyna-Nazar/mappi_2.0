package com.example.mappi.presentation.ui.sign_in.viemodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.UserData
import com.example.mappi.domain.use_case.auth.SignInWithEmailUseCase
import com.example.mappi.presentation.ui.sign_in.SignInState
import com.example.mappi.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val SignInWithEmailUseCase: SignInWithEmailUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            val result = SignInWithEmailUseCase(email, password)
            onSignInResult(result)
        }
    }

    fun onSignInResult(result: Resource<UserData>) {
        _state.update {
            it.copy(
                isSignInSuccessful = result.data != null,
                signInError = result.message
            )
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}