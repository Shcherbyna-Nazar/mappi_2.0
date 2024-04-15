package com.example.mappi.presentation.ui.sign_up.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mappi.R

@Composable
fun RegisterScreen(
    onRegisterClick: (
        username: String,
        email: String,
        password: String,
        repeatPassword: String
    ) -> Unit,
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    val carratereFont = FontFamily(Font(R.font.carratere))

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_register),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Text(
                text = "Adventures at Your Fingertips",
                fontFamily = carratereFont,
                color = Color(0xFFC2D6D3),
                fontSize = 35.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(40.dp))
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF228BA0).copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.Black,
                            cursorColor = Color(0xFF27577B),
                            leadingIconColor = Color.White,
                            trailingIconColor = Color.White,
                            backgroundColor = Color(0xFFFEFBEA).copy(alpha = 0.4f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = Color.White) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.textFieldColors(
                            textColor = Color.Black,
                            cursorColor = Color(0xFF27577B),
                            leadingIconColor = Color.White,
                            trailingIconColor = Color.White,
                            backgroundColor = Color(0xFFFEFBEA).copy(alpha = 0.4f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        isVisible = passwordVisibility,
                        onVisibilityChange = { passwordVisibility = it })
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordField(
                        value = repeatPassword,
                        onValueChange = { repeatPassword = it },
                        label = "Repeat Password",
                        isVisible = passwordVisibility,
                        onVisibilityChange = { passwordVisibility = it })
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
            Button(
                onClick = { onRegisterClick(username, email, password, repeatPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(57.dp),
                shape = RoundedCornerShape(45.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFAEC013))
            ) {
                Text(
                    text = "Create an account",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Already have an account? Sign in!",
                color = Color.White,
                fontSize = 18.sp,
                modifier = Modifier.clickable {
                    navController.navigate("sign_in")
                }
            )
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    val visualTransformation =
        if (isVisible) VisualTransformation.None else PasswordVisualTransformation()
    val icon = if (isVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White) },
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = {
            IconButton(onClick = { onVisibilityChange(!isVisible) }) {
                Icon(imageVector = icon, contentDescription = "Toggle password visibility")
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.textFieldColors(
            textColor = Color.Black,
            cursorColor = Color(0xFF27577B),
            leadingIconColor = Color.White,
            trailingIconColor = Color.White,
            backgroundColor = Color(0xFFFEFBEA).copy(alpha = 0.4f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
