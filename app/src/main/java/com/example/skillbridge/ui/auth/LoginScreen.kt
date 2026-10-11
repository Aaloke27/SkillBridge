
package com.example.skillbridge.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LoginBackground = Color(0xFFF3F6FA)
private val LoginNavy = Color(0xFF344D78)
private val LoginTeal = Color(0xFF00A596)
private val LoginBorder = Color(0xFFD9DCE5)
private val LoginHint = Color(0xFF6C7890)

@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: (email: String, password: String) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LoginBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical =50.dp)
    ) {
        // Back arrow
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(44.dp)
                .offset(x = (-18).dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = Color(0xFF252B35)
            )
        }

        Spacer(Modifier.height(14.dp))

        // Heading
        Text(
            text = "Welcome Back",
            color = LoginNavy,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Sign in to your SkillBridge account",
            color = LoginNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(100.dp))
Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxSize()

) {
    // Email field
    OutlinedTextField(
        value = email,
        onValueChange = {
            email = it
            errorMessage = ""
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        placeholder = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Email Address",
                    color = LoginNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Enter your email",
                    color = LoginHint,
                    fontSize = 12.sp
                )
            }
        },
        leadingIcon = {
            Icon(
                Icons.Default.MailOutline,
                contentDescription = null,
                tint = Color(0xFF17447C)
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email
        ),
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = LoginTeal,
            unfocusedBorderColor = LoginBorder,
            focusedTextColor = LoginNavy,
            unfocusedTextColor = LoginNavy,
            cursorColor = LoginTeal
        )
    )

    Spacer(Modifier.height(15.dp))

    // Password field
    OutlinedTextField(
        value = password,
        onValueChange = {
            password = it
            errorMessage = ""
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        placeholder = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Password",
                    color = LoginNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Enter your password",
                    color = LoginHint,
                    fontSize = 12.sp
                )
            }
        },
        leadingIcon = {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = Color(0xFF17447C)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = {
                    showPassword = !showPassword
                }
            ) {
                Icon(
                    imageVector = if (showPassword) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = if (showPassword) {
                        "Hide password"
                    } else {
                        "Show password"
                    },
                    tint = Color(0xFF526273),
                    modifier = Modifier.size(19.dp)
                )
            }
        },
        singleLine = true,
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = LoginTeal,
            unfocusedBorderColor = LoginBorder,
            focusedTextColor = LoginNavy,
            unfocusedTextColor = LoginNavy,
            cursorColor = LoginTeal
        )
    )

    // Validation / authentication errors
    if (errorMessage.isNotBlank()) {
        Spacer(Modifier.height(12.dp))

        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 12.sp
        )
    }

    Spacer(Modifier.height(66.dp))

    // Sign In button
    Button(
        onClick = {
            when {
                !android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email.trim())
                    .matches() -> {
                    errorMessage = "Enter a valid email address"
                }

                password.isBlank() -> {
                    errorMessage = "Enter your password"
                }

                else -> {
                    errorMessage = ""
                    onLoginClick(email.trim(), password)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LoginTeal,
            contentColor = Color.White
        )
    ) {
        Text(
            text = "Sign in",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

        Spacer(Modifier.height(70.dp))

        // Create Account footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                color = Color(0xFF252B35),
                fontSize = 15.sp
            )

            Spacer(Modifier.width(3.dp))

            TextButton(
                onClick = onRegisterClick
            ) {
                Text(
                    text = "Create Account",
                    color = LoginTeal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
