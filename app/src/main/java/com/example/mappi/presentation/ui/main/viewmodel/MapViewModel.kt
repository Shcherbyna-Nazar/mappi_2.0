package com.example.mappi.presentation.ui.main.viewmodel

import androidx.compose.runtime.mutableStateOf
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

    init {
        loadFriendPosts()
    }


    private fun loadFriendPosts() {
        viewModelScope.launch {
            try {
                _friendPosts.value = getFriendPosts()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}