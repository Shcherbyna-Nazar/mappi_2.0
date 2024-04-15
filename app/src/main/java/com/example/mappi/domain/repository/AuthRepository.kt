package com.example.mappi.domain.repository

import com.example.mappi.domain.model.UserData
import com.example.mappi.util.Resource

interface AuthRepository{
    suspend fun signInWithEmail(email: String, password: String): Resource<UserData>
    suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserData>
    suspend fun signOut(): Resource<Unit>
    fun getSignedInUser(): UserData?
}