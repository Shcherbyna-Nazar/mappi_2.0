package com.example.mappi.presentation.ui.sign_in.composables

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mappi.R
import com.example.mappi.presentation.ui.sign_in.SignInState

@ExperimentalAnimationApi
@Composable
fun SignInScreen(
    state: SignInState,
    onSignInWithGoogleClick: () -> Unit,
    onEmailSignInClick: (email: String, password: String) -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val email by remember { mutableStateOf("") }
    val password by remember { mutableStateOf("") }
    var showAuth by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = state.signInError) {
        state.signInError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.background_main_screen),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                "Adventures at Your Fingertips",
                color = Color(0xFF3F6B88),
                fontFamily = FontFamily(Font(R.font.carratere)),
                fontSize = 35.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(50.dp))

            AnimatedVisibility(
                visible = showAuth,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SignInContent(email, password, onEmailSignInClick)
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Button(
                onClick = { showAuth = !showAuth },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9BB455)),
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(45)),
                border = null // Убираем рамку
            ) {
                Text("Sign In", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSignInWithGoogleClick,
                modifier = Modifier
                    .height(60.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(45)) // Adjusted for proper clipping
                    .then(
                        Modifier.border(
                            BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(Color(0xFFF5C398), Color(0xFF03DAC5)))
                            ),
                            shape = RoundedCornerShape(45) // Ensure this matches the clip shape
                        )
                    ),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google), // Ensure you have a drawable resource named `ic_google_logo`
                        contentDescription = "Google Sign In",
                        modifier = Modifier.size(24.dp) // Adjust the size as needed
                    )
                    Spacer(Modifier.width(8.dp)) // Adjust spacing between image and text as needed
                    Text("Sign In With Google", color = Color.White, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            TextButton(
                onClick = { navController.navigate("register") },
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50)),
                // Also removing the border by not specifying it and setting background to transparent
                colors = ButtonDefaults.textButtonColors(backgroundColor = Color.Transparent)
            ) {
                Text("Create an Account", color = Color.White, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

