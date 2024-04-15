package com.example.mappi.di

import android.content.Context
import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.repository.FirebaseAuthRepository
import com.example.mappi.domain.repository.AuthRepository
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient =
        Identity.getSignInClient(context)

    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    fun provideFirebaseDataSource(
        firebaseAuth: FirebaseAuth,
        signInClient: SignInClient,
        context: Context
    ): FirebaseDataSource = FirebaseDataSource(firebaseAuth, signInClient, context)

    @Provides
    fun provideAuthRepository(dataSource: FirebaseDataSource): AuthRepository =
        FirebaseAuthRepository(dataSource)

}