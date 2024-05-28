package com.example.mappi.presentation.ui.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.mappi.R
import com.example.mappi.domain.model.UserData
import com.example.mappi.util.Resource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            null
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): Resource<UserData> {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleToken =
            credential.googleIdToken ?: return Resource.Error("Google token not found.")
        val googleCredentials = GoogleAuthProvider.getCredential(googleToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            user?.let {
                Resource.Success(
                    UserData(
                        userId = it.uid,
                        userName = it.displayName,
                        email = it.email,
                        profilePictureUrl = it.photoUrl?.toString()
                    )
                )
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "An error occurred")
        }
    }

    suspend fun signOut() {
        try {
            val user = auth.currentUser
            if (user != null) {
                val isGoogleSignIn = user.providerData.any { userInfo ->
                    userInfo.providerId == GoogleAuthProvider.PROVIDER_ID
                }

                if (isGoogleSignIn) {
                    oneTapClient.signOut().await()
                }
                auth.signOut()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            userName = displayName,
            email = email,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest = BeginSignInRequest.Builder()
        .setGoogleIdTokenRequestOptions(
            GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()

}
