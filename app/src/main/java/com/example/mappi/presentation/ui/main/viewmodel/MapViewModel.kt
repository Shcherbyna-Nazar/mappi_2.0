package com.example.mappi.presentation.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Comment
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.use_case.auth.GetSignedInUserUseCase
import com.example.mappi.domain.use_case.map.GetFriendPostsUseCase
import com.example.mappi.domain.use_case.profile.AddCommentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFriendPosts: GetFriendPostsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getSignedInUserUseCase: GetSignedInUserUseCase
) : ViewModel() {
    private val _friendPosts = MutableStateFlow<List<Post>>(emptyList())
    val friendPosts: StateFlow<List<Post>> get() = _friendPosts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    init {
        loadFriendPosts()
    }

    fun addComment(post: Post, comment: String) {
        viewModelScope.launch {
            val user = getSignedInUserUseCase() ?: return@launch
            val firstComment = post.comments[0]
            val commentData = Comment(
                text = comment,
                userName = user.userName ?: "",
                ownerId = firstComment.ownerId,
                timeStamp = System.currentTimeMillis(),
                profilePictureUrl = user.profilePictureUrl ?: ""
            )
            addCommentUseCase(post.id, commentData)
            val updatedPosts = _friendPosts.value.map {
                if (it.id == post.id) {
                    it.copy(comments = it.comments + commentData)
                } else {
                    it
                }
            }
            _friendPosts.value = updatedPosts
        }
    }

    fun loadFriendPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _friendPosts.value = getFriendPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

}