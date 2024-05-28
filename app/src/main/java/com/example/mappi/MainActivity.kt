package com.example.mappi

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mappi.presentation.ui.NavHostSetup
import com.example.mappi.presentation.ui.main.composables.MainScreen
import com.example.mappi.presentation.ui.main.composables.ProfileScreen
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel
import com.example.mappi.presentation.ui.sign_in.GoogleAuthUiClient
import com.example.mappi.presentation.ui.sign_in.SignInState
import com.example.mappi.presentation.ui.sign_in.composables.SignInScreen
import com.example.mappi.presentation.ui.sign_in.viemodel.SignInViewModel
import com.example.mappi.presentation.ui.sign_up.SignUpState
import com.example.mappi.presentation.ui.sign_up.composables.RegisterScreen
import com.example.mappi.presentation.ui.sign_up.viewmodel.SignUpViewModel
import com.example.mappi.presentation.ui.theme.MappiTheme
import com.google.android.gms.auth.api.identity.Identity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import android.Manifest
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MappiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    NavHostSetup(
                        navController,
                        signInScreenContent = { SignInScreenContent(navController) },
                        registerScreenContent = { RegisterScreenContent(navController) },
                        mainScreen = {
                            MainScreen(
                                navController,
                                mapScreen = { MapScreen() },
                                chatScreen = { ChatScreen() },
                                profileScreen = { ProfileScreenContent(navController) },
                            )
                        },
                        mapScreen = { MapScreen() },
                        chatScreen = { ChatScreen() },
                        profileScreen = { ProfileScreenContent(navController) },
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun SignInScreenContent(navController: NavController) {
        val signInViewModel: SignInViewModel by viewModels()
        val signInState by signInViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            googleAuthUiClient.getSignedInUser()?.let {
                navController.navigate("main") {
                    popUpTo("sign_in") { inclusive = true }
                }
            }
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    val signInResult =
                        googleAuthUiClient.signInWithIntent(result.data ?: return@launch)
                    signInViewModel.onSignInResult(signInResult)
                }
            }
        }

        LaunchedEffect(signInState.isSignInSuccessful, signInState.signInError) {
            handleSignInState(signInState, signInViewModel, navController)
        }

        SignInScreen(
            state = signInState,
            onSignInWithGoogleClick = {
                lifecycleScope.launch {
                    val signInIntentSender = googleAuthUiClient.signIn()
                    launcher.launch(
                        IntentSenderRequest.Builder(signInIntentSender ?: return@launch).build()
                    )
                }
            },
            onEmailSignInClick = { email, password ->
                lifecycleScope.launch {
                    signInViewModel.signInWithEmail(email, password)
                }
            },
            navController = navController
        )
    }

    private fun handleSignInState(
        signInState: SignInState,
        signInViewModel: SignInViewModel,
        navController: NavController
    ) {
        when {
            signInState.isSignInSuccessful -> {
                showToast("Sign in successful")
                navController.navigate("main") {
                    popUpTo("sign_in") { inclusive = true }
                }
                signInViewModel.resetState()
            }

            signInState.signInError != null -> {
                showToast("Sign in failed: ${signInState.signInError}")
                signInViewModel.resetState()
            }
        }
    }

    @Composable
    private fun RegisterScreenContent(navController: NavController) {
        val signUpViewModel: SignUpViewModel by viewModels()
        val signUpState by signUpViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(signUpState.isSignUpSuccessful, signUpState.signUpError) {
            handleSignUpState(signUpState, signUpViewModel, navController)
        }

        RegisterScreen(
            onRegisterClick = { username, email, password, repeatPassword ->
                lifecycleScope.launch {
                    signUpViewModel.signUpWithEmail(username, email, password, repeatPassword)
                }
            },
            navController = navController
        )
    }

    private fun handleSignUpState(
        signUpState: SignUpState,
        signUpViewModel: SignUpViewModel,
        navController: NavController
    ) {
        when {
            signUpState.isSignUpSuccessful -> {
                showToast("Registration successful")
                navController.navigate("main") {
                    popUpTo("register") { inclusive = true }
                }
                signUpViewModel.resetState()
            }

            signUpState.signUpError != null -> {
                showToast("Registration failed: ${signUpState.signUpError}")
                signUpViewModel.resetState()
            }
        }
    }

    @Composable
    fun MapScreen() {
        // Your MapScreen content goes here
    }

    @Composable
    fun ChatScreen() {
        // Your ChatScreen content goes here
    }

    @Composable
    private fun ProfileScreenContent(navController: NavController) {
        val profileViewModel: ProfileViewModel by viewModels()
        val posts by profileViewModel.posts.collectAsStateWithLifecycle()

        var imageUri by remember { mutableStateOf<Uri?>(null) }
        val takePicture = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { isSuccess ->
            if (isSuccess) {
                // Upload the image to Firebase Storage
                imageUri?.let {
                    lifecycleScope.launch {
                        val imageUrl = profileViewModel.uploadPhoto(it)
                        showToast("Image uploaded: $imageUrl")
                    }
                }
            }
        }

        ProfileScreen(
            userData = googleAuthUiClient.getSignedInUser(),
            posts = posts,
            onAddPostClick = {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val uri = FileProvider.getUriForFile(
                        applicationContext,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        createImageFile()
                    )
                    imageUri = uri
                    takePicture.launch(uri)
                } else {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.CAMERA,
                        ),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
            },
            onSignOut = {
                lifecycleScope.launch {
                    profileViewModel.signOut()
                    showToast("Signed out")
                    navController.navigate("sign_in") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            }
        )
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${UUID.randomUUID()}_",
            ".jpg",
            storageDir
        )
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSIOTN = 100
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
