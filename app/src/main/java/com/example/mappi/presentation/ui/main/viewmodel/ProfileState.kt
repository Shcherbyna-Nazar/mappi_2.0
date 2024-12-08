package com.example.mappi.presentation.ui.main.viewmodel

import com.example.mappi.domain.model.Post
import com.example.mappi.domain.model.UserData

data class ProfileState(
    val userData: UserData?,
    val posts: List<Post>,
    val profilePictureUrl: String?,
    val isLoading: Boolean = false,
    val error: String? = null
)
