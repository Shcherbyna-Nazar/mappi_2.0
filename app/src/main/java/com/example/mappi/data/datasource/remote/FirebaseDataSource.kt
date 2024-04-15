package com.example.mappi.data.datasource.remote

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.mappi.R
import com.example.mappi.data.datasource.remote.dto.UserDto
import com.example.mappi.util.Resource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

class FirebaseDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val oneTapClient: SignInClient,
    private val context: Context
) {

    suspend fun prepareGoogleIntent(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithGoogle(intent: Intent): Resource<UserDto> {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleToken =
            credential.googleIdToken ?: return Resource.Error("Google token not found.")
        val googleCredentials = GoogleAuthProvider.getCredential(googleToken, null)
        return try {
            val user = firebaseAuth.signInWithCredential(googleCredentials).await().user
            user?.let {
                Resource.Success(
                    UserDto(
                        userId = it.uid,
                        userName = it.displayName,
                        profilePictureUrl = it.photoUrl.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    private fun buildSignInRequest(): BeginSignInRequest = BeginSignInRequest.Builder()
        .setGoogleIdTokenRequestOptions(
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

    suspend fun signInWithEmail(email: String, password: String): Resource<UserDto> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let {
                Resource.Success(
                    UserDto(
                        userId = it.uid,
                        userName = it.displayName,
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
                profilePictureUrl = it.photoUrl.toString()
            )
        }
    }

}