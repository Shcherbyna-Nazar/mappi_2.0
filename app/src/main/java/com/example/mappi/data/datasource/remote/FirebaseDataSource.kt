package com.example.mappi.data.datasource.remote

import android.content.Context
import android.net.Uri
import com.example.mappi.data.datasource.remote.dto.UserDto
import com.example.mappi.util.Resource
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val storageReference: StorageReference,
    private val firebaseAuth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val context: Context
) {

    suspend fun uploadPhoto(uri: Uri): String {
        val photoRef =
            storageReference.child("posts/${firebaseAuth.currentUser?.uid}/${System.currentTimeMillis()}.jpg")
        return try {
            photoRef.putFile(uri).await()
            photoRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun getPosts(): List<String> {
        return try {
            val listResult =
                storageReference.child("posts/${firebaseAuth.currentUser?.uid}").listAll().await()
            listResult.items.map { it.downloadUrl.await().toString() }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Resource<UserDto> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Resource.Success(
                    UserDto(
                        userId = it.uid,
                        userName = it.displayName,
                        email = it.email,
                        profilePictureUrl = it.photoUrl.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    suspend fun signUpWithEmail(name: String, email: String, password: String): Resource<UserDto> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { user ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                user.updateProfile(profileUpdate).await()

                Resource.Success(
                    UserDto(
                        userId = user.uid,
                        userName = user.displayName,
                        email = user.email,
                        profilePictureUrl = user.photoUrl.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    fun getSignedInUser(): UserDto? {
        return firebaseAuth.currentUser?.let {
            UserDto(
                userId = it.uid,
                userName = it.displayName,
                email = it.email,
                profilePictureUrl = it.photoUrl.toString()
            )
        }
    }

}