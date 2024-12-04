package com.example.mappi.presentation.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mappi.domain.model.Post
import com.example.mappi.domain.use_case.map.GetFriendPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getFriendPosts: GetFriendPostsUseCase
) : ViewModel() {
    private val _friendPosts = MutableStateFlow<List<Post>>(emptyList())
    val friendPosts: StateFlow<List<Post>> get() = _friendPosts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    init {
        loadFriendPosts()
    }


    fun loadFriendPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _friendPosts.value = getFriendPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finally {
                _isLoading.value = false
            }
        }
    }

}