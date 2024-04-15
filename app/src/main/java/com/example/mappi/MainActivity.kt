package com.example.mappi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mappi.presentation.ui.profile.viewmodel.ProfileViewModel
import com.example.mappi.presentation.ui.sign_in.GoogleAuthUiClient
import com.example.mappi.presentation.ui.sign_in.ProfileScreen
import com.example.mappi.presentation.ui.sign_in.composables.SignInScreen
import com.example.mappi.presentation.ui.sign_in.viemodel.SignInViewModel
import com.example.mappi.presentation.ui.sign_up.composables.RegisterScreen
import com.example.mappi.presentation.ui.sign_up.viewmodel.SignUpViewModel
import com.example.mappi.presentation.ui.theme.MappiTheme
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MappiTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "sign_in") {
                        composable("sign_in") {
                            val viewModelSignIn: SignInViewModel by viewModels()
                            val singInState by viewModelSignIn.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null) {
                                    navController.navigate("profile")
                                }
                            }

                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModelSignIn.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = singInState.isSignInSuccessful) {
                                if (singInState.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate("profile")
                                    viewModelSignIn.resetState()
                                } else if (singInState.signInError != null) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in failed: ${singInState.signInError}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    viewModelSignIn.resetState()
                                }
                            }

                            SignInScreen(
                                state = singInState,
                                onSignInWithGoogleClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()
                                        launcher.launch(
                                            IntentSenderRequest.Builder(
                                                signInIntentSender ?: return@launch
                                            ).build()
                                        )
                                    }
                                },
                                onEmailSignInClick = { email, password ->
                                    lifecycleScope.launch {
                                        viewModelSignIn.signInWithEmail(email, password)
                                    }
                                },
                                navController = navController
                            )
                        }
                        composable("profile") {
                            val profileViewModel: ProfileViewModel by viewModels()

                            ProfileScreen(
                                userData = googleAuthUiClient.getSignedInUser(),
                                onSignOut = {
                                    lifecycleScope.launch {
                                        profileViewModel.signOut()
                                        Toast.makeText(
                                            applicationContext,
                                            "Signed out",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                        // Registration screen route
                        composable("register") {
                            val viewModelSignUp: SignUpViewModel by viewModels()
                            val signUpState by viewModelSignUp.state.collectAsStateWithLifecycle()

                            LaunchedEffect(
                                key1 = signUpState.isSignUpSuccessful,
                                key2 = signUpState.signUpError
                            ) {
                                if (signUpState.isSignUpSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Registration successful",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigate("profile")
                                    viewModelSignUp.resetState()
                                } else if (signUpState.signUpError != null) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Registration failed: ${signUpState.signUpError}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    viewModelSignUp.resetState()
                                }
                            }

                            RegisterScreen(
                                onRegisterClick = { username, email, password, repeatPassword ->
                                    lifecycleScope.launch {
                                        viewModelSignUp.signUpWithEmail(
                                            username,
                                            email,
                                            password,
                                            repeatPassword
                                        )
                                    }
                                },
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}
