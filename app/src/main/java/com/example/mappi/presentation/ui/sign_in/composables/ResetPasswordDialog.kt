package com.example.mappi.presentation.ui.sign_in.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mappi.R

@Composable
fun ResetPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onResetClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEmailValid = remember(email) { email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() }

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = Color.White,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = TextStyle(
                    color = Color(0xFF3F6B88),
                    fontSize = 20.sp,
                    fontFamily = FontFamily(Font(R.font.carratere))
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Enter your email", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    textColor = Color.Black,
                    cursorColor = Color(0xFF27577B),
                    leadingIconColor = Color.Gray,
                    trailingIconColor = Color.Gray,
                    backgroundColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color(0xFF9BB455),
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (isEmailValid) onResetClick(email) },
                enabled = isEmailValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9BB455))
            ) {
                Text("Send Reset Link", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF3F6B88))
            }
        }
    }
}