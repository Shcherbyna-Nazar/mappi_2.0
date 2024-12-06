package com.example.mappi.presentation.ui.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.use_case.auth.GetSignedInUserUseCase
import com.example.mappi.domain.use_case.auth.SignOutUseCase
import com.example.mappi.domain.use_case.profile.DeletePostUseCase
import com.example.mappi.domain.use_case.profile.GetPostsUseCase
import com.example.mappi.domain.use_case.profile.UploadPhotoUseCase
import com.example.mappi.presentation.ui.main.composables.profile.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val uploadPhotoUseCase: UploadPhotoUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val getSignedInUser: GetSignedInUserUseCase,
    private val deletePostUseCase: DeletePostUseCase
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState(null, emptyList(), null, isLoading = false))
    val profileState: StateFlow<ProfileState> get() = _profileState

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isLoading = true)
            try {
                val userData = getSignedInUser() ?: return@launch
                Log.e("ProfileViewModel", "loadProfile start")
                val posts = getPostsUseCase()
                _profileState.value = _profileState.value.copy(
                    userData = userData,
                    posts = posts,
                    profilePictureUrl = userData.profilePictureUrl,
                    isLoading = false
                )
                Log.e("ProfileViewModel", "loadProfile done")
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(error = e.message)
            } finally {
                _profileState.value = _profileState.value.copy(isLoading = false)
            }
        }
    }

    fun uploadPhoto(
        uri: Uri,
        latitude: Double? = null,
        longitude: Double? = null,
        isProfilePicture: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _profileState.value = _profileState.value.copy(isLoading = true)
                val url = uploadPhotoUseCase(uri, latitude, longitude, isProfilePicture)
                val userData = getSignedInUser()
                if (isProfilePicture) {
                    _profileState.value = _profileState.value.copy(
                        userData = userData?.copy(profilePictureUrl = url),
                        profilePictureUrl = url
                    )
                } else {
                    _profileState.value = _profileState.value.copy(
                        userData = userData,
                        posts = _profileState.value.posts + Post(
                            url,
                            latitude!!,
                            longitude!!,
                        )
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(error = e.message)
            } finally {
                _profileState.value = _profileState.value.copy(isLoading = false)
            }
        }
    }

    fun deletePost(post: Post) {
        viewModelScope.launch {
            try {
                _profileState.value = _profileState.value.copy(isLoading = true)
                deletePostUseCase(post)
                _profileState.value = _profileState.value.copy(
                    posts = _profileState.value.posts.filter { it != post }
                )
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(error = e.message)
            } finally {
                _profileState.value = _profileState.value.copy(isLoading = false)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _profileState.value = ProfileState(null, emptyList(), null, isLoading = false)
        }
    }
}
