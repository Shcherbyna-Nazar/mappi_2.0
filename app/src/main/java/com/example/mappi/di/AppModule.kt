package com.example.mappi.di

import android.content.Context
import com.example.mappi.data.datasource.remote.FirebaseDataSource
import com.example.mappi.data.repository.FirebaseAuthRepository
import com.example.mappi.data.repository.FirebaseProfileRepository
import com.example.mappi.data.repository.FirebaseUserRepository
import com.example.mappi.domain.repository.AuthRepository
import com.example.mappi.domain.repository.ProfileRepository
import com.example.mappi.domain.repository.UserRepository
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    fun provideSignInClient(@ApplicationContext context: Context): SignInClient =
        Identity.getSignInClient(context)

    @Provides
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context

    @Provides
    fun provideStorageReference(): StorageReference =
        FirebaseStorage.getInstance().reference

    @Provides
    fun provideFirebaseDataSource(
        storageReference: StorageReference,
        firebaseAuth: FirebaseAuth,
        firebaseDatabase: FirebaseDatabase,
    ): FirebaseDataSource = FirebaseDataSource(storageReference, firebaseAuth, firebaseDatabase)

    @Provides
    fun provideAuthRepository(dataSource: FirebaseDataSource): AuthRepository =
        FirebaseAuthRepository(dataSource)

    @Provides
    fun provideProfileRepository(dataSource: FirebaseDataSource): ProfileRepository =
        FirebaseProfileRepository(dataSource)

    @Provides
    fun provideUserRepository(dataSource: FirebaseDataSource): UserRepository =
        FirebaseUserRepository(dataSource)

}