package com.example.mappi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mappi.presentation.ui.NavHostSetup
import com.example.mappi.presentation.ui.decisions.compose.AnimationScreen
import com.example.mappi.presentation.ui.decisions.compose.DecisionsScreen
import com.example.mappi.presentation.ui.decisions.viewmodel.DecisionsViewModel
import com.example.mappi.presentation.ui.friends.composable.FriendsListScreen
import com.example.mappi.presentation.ui.friends.composable.SearchFriendsScreenContent
import com.example.mappi.presentation.ui.main.composables.MainScreen
import com.example.mappi.presentation.ui.main.composables.map.MapScreen
import com.example.mappi.presentation.ui.main.composables.profile.ProfileScreen
import com.example.mappi.presentation.ui.main.viewmodel.MapViewModel
import com.example.mappi.presentation.ui.main.viewmodel.ProfileViewModel
import com.example.mappi.presentation.ui.sign_in.GoogleAuthUiClient
import com.example.mappi.presentation.ui.sign_in.SignInState
import com.example.mappi.presentation.ui.sign_in.composables.SignInScreen
import com.example.mappi.presentation.ui.sign_in.viemodel.SignInViewModel
import com.example.mappi.presentation.ui.sign_up.SignUpState
import com.example.mappi.presentation.ui.sign_up.composables.RegisterScreen
import com.example.mappi.presentation.ui.sign_up.viewmodel.SignUpViewModel
import com.example.mappi.presentation.ui.theme.MappiTheme
import com.example.mappi.util.LocationUtils
import com.example.mappi.util.PermissionUtils
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private lateinit var locationUtils: LocationUtils
    private lateinit var permissionUtils: PermissionUtils
    private val decisionViewModel: DecisionsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val mapViewModel: MapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationUtils = LocationUtils(this)
        permissionUtils = PermissionUtils(this)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, BuildConfig.API_KEY)
        }

        permissionUtils.checkLocationPermission()

        fetchNearbyPlaces()

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
                                mapScreen = {
                                    MapScreen(
                                        applicationContext,
                                        mapViewModel,
                                        profileViewModel,
                                        decisionViewModel,
                                    )
                                },
                                decisionScreen = { DecisionsScreenContent(navController) },
                                profileScreen = { ProfileScreenContent(navController) },
                            )
                        },
                        mapScreen = {
                            MapScreen(
                                applicationContext,
                                mapViewModel,
                                profileViewModel,
                                decisionViewModel,
                            )
                        },
                        recommendationScreen = { DecisionsScreenContent(navController) },
                        searchFriendsScreen = { SearchFriendsScreenContent() },
                        friendsListScreen = {
                            FriendsListScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        },
                        profileScreen = { ProfileScreenContent(navController) },
                        animationScreen = { userLocation, restaurantLocation ->
                            AnimationScreen(
                                userLocation = userLocation,
                                restaurantLatLng = restaurantLocation,
                                onBackToFindingPlaces = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    private fun fetchNearbyPlaces() {
        this.locationUtils.getCurrentLocation { location ->
            lifecycleScope.launch {
                decisionViewModel.fetchRecommendation(
                    location,
                    forceRefresh = true
                )
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

        val launcher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        val signInResult =
                            googleAuthUiClient.signInWithIntent(result.data ?: return@launch)
                        signInViewModel.onSignInResult(signInResult)
                    }
                }
            }

        LaunchedEffect(signInState.isSignInSuccessful, signInState.signInError) {
            handleSignInState(signInState, signInViewModel, profileViewModel, navController)
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
                lifecycleScope.launch { signInViewModel.signInWithEmail(email, password) }
            },
            onResetPasswordClick = { email ->
                lifecycleScope.launch { signInViewModel.resetPassword(email) }
            },
            navController = navController
        )
    }

    private fun handleSignInState(
        signInState: SignInState,
        signInViewModel: SignInViewModel,
        profileViewModel: ProfileViewModel,
        navController: NavController
    ) {
        when {
            signInState.isSignInSuccessful -> {
                showToast("Sign in successful")
                navController.navigate("main") {
                    popUpTo("sign_in") { inclusive = true }
                }
                profileViewModel.loadProfile()
                mapViewModel.loadFriendPosts()
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
                    signUpViewModel.signUpWithEmail(
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
                profileViewModel.loadProfile()
                mapViewModel.loadFriendPosts()
                signUpViewModel.resetState()
            }

            signUpState.signUpError != null -> {
                showToast("Registration failed: ${signUpState.signUpError}")
                signUpViewModel.resetState()
            }
        }
    }

    @Composable
    fun DecisionsScreenContent(navController: NavController) {
        val userLocation = remember { mutableStateOf<Location?>(null) }

        LaunchedEffect(Unit) {
            locationUtils.getCurrentLocation { location ->
                userLocation.value = location
            }
        }

        if (userLocation.value != null) {
            DecisionsScreen(
                navController,
                decisionViewModel,
                userLocation = userLocation.value!!,
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF3E8B67))
            }
        }
    }


    @Composable
    private fun ProfileScreenContent(navController: NavController) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var isProfileImage by remember { mutableStateOf(false) }
        var showRatingDialog by remember { mutableStateOf(false) }
        var selectedRating by remember { mutableStateOf(0) }
        var commentText by remember { mutableStateOf("") }

        val takePicture =
            rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
                if (isSuccess) {
                    imageUri?.let {
                        if (isProfileImage) {
                            lifecycleScope.launch {
                                val imageUrl = profileViewModel.uploadPhoto(
                                    it,
                                    0.0,
                                    0.0,
                                    0,
                                    "",
                                    isProfilePicture = true
                                )
                                showToast("Profile picture uploaded: $imageUrl")
                            }
                        } else {
                            showRatingDialog = true // Open the rating dialog after taking a photo
                        }
                    }
                }
            }

        val selectImageLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    lifecycleScope.launch {
                        val imageUrl = profileViewModel.uploadPhoto(
                            it,
                            0.0,
                            0.0,
                            0,
                            "",
                            isProfilePicture = true
                        )
                        showToast("Profile picture uploaded: $imageUrl")
                    }
                }
            }

        var showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Select Profile Photo") },
                text = { Text("Choose an option to set your profile photo") },
                buttons = {
                    Row(
                        modifier = Modifier.padding(all = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = {
                            showDialog = false
                            selectImageLauncher.launch("image/*")
                        }) { Text("Choose from Gallery") }
                        TextButton(onClick = {
                            showDialog = false
                            isProfileImage = true
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
                                    arrayOf(Manifest.permission.CAMERA),
                                    REQUEST_CAMERA_PERMISSION
                                )
                            }
                        }) { Text("Take a Photo") }
                    }
                }
            )
        }

        if (showRatingDialog) {
            AlertDialog(
                onDismissRequest = { showRatingDialog = false },
                title = {
                    Text(
                        text = "Rate and Comment",
                        style = MaterialTheme.typography.h6,
                        color = Color(0xFF408C68) // Using the custom color for title
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Please rate your post:",
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(onClick = { selectedRating = star }) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star Rating",
                                        tint = if (star <= selectedRating) Color(0xFFFFD700) else Color(0xFFCCCCCC), // Yellow for selected, light gray for unselected
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                        TextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, // Remove the default underline
                                unfocusedIndicatorColor = Color.Transparent, // Remove the default underline
                                cursorColor = Color(0xFF408C68),
                                textColor = MaterialTheme.colors.onSurface
                            ),
                            textStyle = MaterialTheme.typography.body1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFF0F0F0), // Light background
                                    shape = RoundedCornerShape(12.dp) // Rounded corners
                                )
                                .border(
                                    width = 2.dp,
                                    color = Color(0xFF408C68), // Green border
                                    shape = RoundedCornerShape(12.dp) // Match the background's shape
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp) // Add padding inside the frame
                        )

                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        showRatingDialog = false
                        locationUtils.getCurrentLocation { location ->
                            lifecycleScope.launch {
                                imageUri?.let { uri ->
                                    profileViewModel.uploadPhoto(
                                        uri,
                                        location.latitude,
                                        location.longitude,
                                        rating = selectedRating,
                                        comment = commentText
                                    )
                                    showToast("Post uploaded with rating and comment!")
                                }
                            }
                        }
                    }) {
                        Text(
                            text = "Submit",
                            style = MaterialTheme.typography.button,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFF408C68))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRatingDialog = false }) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.button,
                            color = MaterialTheme.colors.onSurface
                        )
                    }
                },
                backgroundColor = Color(0xFFE8F5E9), // Light green background for dialog
                shape = RoundedCornerShape(12.dp) // Rounded corners for modern feel
            )
        }


        ProfileScreen(
            profileViewModel = profileViewModel,
            onAddPostClick = {
                isProfileImage = false
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationUtils.checkLocationSettings {
                        val uri = FileProvider.getUriForFile(
                            applicationContext,
                            "${BuildConfig.APPLICATION_ID}.provider",
                            createImageFile()
                        )
                        imageUri = uri
                        takePicture.launch(uri)
                    }
                } else {
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
            },
            onProfilePictureClick = { showDialog = true },
            onSearchFriendsClick = { navController.navigate("search_friends") },
            onDeletePostClick = { post ->
                lifecycleScope.launch {
                    profileViewModel.deletePost(post)
                }
            },
            onFriendsClick = { navController.navigate("friends_list") },
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
        return File.createTempFile("JPEG_${UUID.randomUUID()}_", ".jpg", storageDir)
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 100
        internal const val REQUEST_CHECK_SETTINGS = 101
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
