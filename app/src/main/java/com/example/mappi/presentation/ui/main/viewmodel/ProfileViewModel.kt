package com.example.mappi.presentation.ui.main.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.use_case.auth.GetSignedInUserUseCase
import com.example.mappi.domain.use_case.auth.SignOutUseCase
import com.example.mappi.domain.use_case.profile.AddCommentUseCase
import com.example.mappi.domain.use_case.profile.DeletePostUseCase
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
    private val getPostsUseCase: GetPostsUseCase,
    private val getSignedInUser: GetSignedInUserUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val addCommentUseCase: AddCommentUseCase
) : ViewModel() {

    private val _profileState =
        MutableStateFlow(ProfileState(null, emptyList(), null, isLoading = false))
    val profileState: StateFlow<ProfileState> get() = _profileState

    init {
        loadProfile()
    }

    fun loadProfile() {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userData = getSignedInUser() ?: throw Exception("User not signed in")
                val posts = getPostsUseCase()
                _profileState.value = _profileState.value.copy(
                    userData = userData,
                    posts = posts,
                    profilePictureUrl = userData.profilePictureUrl
                )
                Log.d("ProfileViewModel", "Profile loaded successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile", e)
                updateErrorState(e.message)
            } finally {
                updateLoadingState(false)
            }
        }
    }

    fun uploadPhoto(
        uri: Uri,
        latitude: Double? = null,
        longitude: Double? = null,
        rating: Int,
        comment: String,
        isProfilePicture: Boolean = false
    ) {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                val userData = getSignedInUser() ?: throw Exception("User not signed in")
                val commentData = Comment(
                    comment,
                    userData.userName ?: "Unknown",
                    userData.userId,
                    System.currentTimeMillis(),
                    userData.profilePictureUrl ?: ""
                )
                val postId = System.currentTimeMillis().toString()
                val url = uploadPhotoUseCase(
                    id = postId,
                    uri = uri,
                    latitude = latitude,
                    longitude = longitude,
                    userName = userData.userName ?: "Unknown",
                    rating = rating,
                    comment = commentData,
                    isProfilePicture = isProfilePicture
                )

                if (isProfilePicture) {
                    updateProfilePicture(url)
                } else {
                    addNewPost(
                        postId,
                        url,
                        latitude,
                        longitude,
                        rating,
                        commentData,
                        userData.userName ?: "Unknown"
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error uploading photo", e)
                updateErrorState(e.message)
            } finally {
                updateLoadingState(false)
            }
        }
    }

    fun addComment(postId: String, comment: String) {
        viewModelScope.launch {
            try {
                val commentData = Comment(
                    comment,
                    _profileState.value.userData?.userName ?: "Unknown",
                    _profileState.value.userData?.userId ?: "Unknown",
                    System.currentTimeMillis(),
                    _profileState.value.userData?.profilePictureUrl ?: ""
                )
                addCommentUseCase(postId, commentData)
                val updatedPosts = _profileState.value.posts.map { post ->
                    if (post.id == postId) {
                        post.copy(comments = post.comments + commentData)
                    } else {
                        post
                    }
                }
                _profileState.value = _profileState.value.copy(posts = updatedPosts)
                Log.d("ProfileViewModel", "Comment added successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error adding comment", e)
                updateErrorState(e.message)
            }
        }
    }

    private fun updateProfilePicture(url: String) {
        val userData = getSignedInUser() ?: return
        _profileState.value = _profileState.value.copy(
            userData = userData.copy(profilePictureUrl = url),
            profilePictureUrl = url
        )
    }

    private fun addNewPost(
        id: String,
        url: String,
        latitude: Double?,
        longitude: Double?,
        rating: Int,
        comment: Comment,
        userName: String
    ) {
        val post = Post(
            id = id,
            url = url,
            latitude = latitude ?: 0.0,
            longitude = longitude ?: 0.0,
            rating = rating,
            userName = userName,
            comments = listOf(comment),
        )
        _profileState.value = _profileState.value.copy(posts = _profileState.value.posts + post)
    }

    fun deletePost(post: Post) {
        updateLoadingState(true)
        viewModelScope.launch {
            try {
                deletePostUseCase(post)
                _profileState.value = _profileState.value.copy(
                    posts = _profileState.value.posts.filterNot { it.id == post.id }
                )
                Log.d("ProfileViewModel", "Post deleted successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error deleting post", e)
                updateErrorState(e.message)
            } finally {
                updateLoadingState(false)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
            _profileState.value = ProfileState(null, emptyList(), null, isLoading = false)
            Log.d("ProfileViewModel", "User signed out")
        }
    }

    private fun updateLoadingState(isLoading: Boolean) {
        _profileState.value = _profileState.value.copy(isLoading = isLoading)
    }

    private fun updateErrorState(error: String?) {
        _profileState.value = _profileState.value.copy(error = error)
    }
}
