package com.example.mappi.presentation.ui.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.example.mappi.R
import com.example.mappi.domain.model.UserData
import com.example.mappi.util.Resource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    private val database = Firebase.database
    private val usersRef = database.getReference("users")

    suspend fun signIn(): IntentSender? {
        return try {
            val result = oneTapClient.beginSignIn(buildSignInRequest()).await()
            result.pendingIntent.intentSender
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "Error during sign-in: ${e.localizedMessage}", e)
            if (e is CancellationException) throw e
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): Resource<UserData> {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleToken = credential.googleIdToken ?: return Resource.Error("Google token not found.")
        val googleCredentials = GoogleAuthProvider.getCredential(googleToken, null)
        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            user?.let {
                val userSnapshot = usersRef.child(it.uid).get().await()
                Log.d("GoogleAuthUiClient", "User snapshot: ${userSnapshot.value}")
                if (userSnapshot.exists()) {
                    return Resource.Success(
                        UserData(
                            userId = it.uid,
                            userName = it.displayName,
                            email = it.email,
                            profilePictureUrl = it.photoUrl?.toString()
                        )
                    )
                } else {
                    usersRef.child(it.uid).setValue(
                        mapOf(
                            "userId" to it.uid,
                            "userName" to it.displayName,
                            "email" to it.email,
                            "profilePictureUrl" to it.photoUrl?.toString()
                        )
                    )
                    return Resource.Success(
                        UserData(
                            userId = it.uid,
                            userName = it.displayName,
                            email = it.email,
                            profilePictureUrl = it.photoUrl?.toString()
                        )
                    )
                }
            } ?: Resource.Error("User not found")
        } catch (e: Exception) {
            Log.e("GoogleAuthUiClient", "Error during sign-in with intent: ${e.localizedMessage}", e)
            if (e is CancellationException) throw e
            Resource.Error(e.localizedMessage ?: "An error occurred during sign-in")
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
            BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                .setSupported(true)
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(context.getString(R.string.web_client_id))
                .build()
        )
        .setAutoSelectEnabled(true)
        .build()
}
