package com.example.mappi.presentation.ui.main.composables.profile

import com.example.mappi.domain.model.UserData

data class ProfileState(
    val signedInUser: UserData? = null,
    val signOutSuccess: Boolean = false,
)