package com.example.mappi.data.repository

import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.mapper.UserDataMapper
import com.example.mappi.domain.model.UserData
import com.example.mappi.domain.repository.AuthRepository
import com.example.mappi.util.Resource
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource
) : AuthRepository {

    override suspend fun signInWithEmail(email: String, password: String): Resource<UserData> {
        return when (val resource = firebaseDataSource.signInWithEmail(email, password)) {
            is Resource.Success -> {
                resource.data?.let { userDataDto ->
                    Resource.Success(UserDataMapper.mapToDomain(userDataDto))
                } ?: Resource.Error("No user data available")
            }
            is Resource.Error -> Resource.Error(resource.message ?: "An error occurred")
            else -> Resource.Error("Unsupported resource type")
        }
    }

    override suspend fun signUpWithEmail(
        name: String,
        email: String,
        password: String
    ): Resource<UserData> {
        return when (val resource = firebaseDataSource.signUpWithEmail(name, email, password)) {
            is Resource.Success -> {
                resource.data?.let { userDataDto ->
                    Resource.Success(UserDataMapper.mapToDomain(userDataDto))
                } ?: Resource.Error("No user data available")
            }
            is Resource.Error -> Resource.Error(resource.message ?: "An error occurred")
            else -> Resource.Error("Unsupported resource type")
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return firebaseDataSource.signOut()
    }

    override fun getSignedInUser(): UserData? {
        return firebaseDataSource.getSignedInUser()?.let { UserDataMapper.mapToDomain(it) }
    }
}