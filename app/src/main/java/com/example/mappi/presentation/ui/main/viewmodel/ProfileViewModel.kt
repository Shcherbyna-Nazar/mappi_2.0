package com.example.mappi.presentation.ui.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.use_case.auth.SignOutUseCase
import com.example.mappi.domain.use_case.profile.GetPostsUseCase
import com.example.mappi.domain.use_case.profile.UploadPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    private val _posts = MutableStateFlow<List<String>>(emptyList())
    val posts: StateFlow<List<String>> get() = _posts

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _posts.value = getPostsUseCase()
            Log.d("ProfileViewModel", "Posts: ${_posts.value}")
        }
    }

    fun uploadPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val url = uploadPhotoUseCase(uri)
                if (url.isNotEmpty()) {
                    _posts.value += url
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }
}
