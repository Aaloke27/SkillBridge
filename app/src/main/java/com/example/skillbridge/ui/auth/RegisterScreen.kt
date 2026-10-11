
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

private val RegisterBackground = Color(0xFFF3F6FA)
private val RegisterNavy = Color(0xFF344D78)
private val RegisterTeal = Color(0xFF00A596)
private val RegisterBorder = Color(0xFFD9DCE5)
private val RegisterHint = Color(0xFF6C7890)

@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onRegisterClick: (
        name: String,
        email: String,
        password: String,
        role: String,
        profession: String
    ) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable {
        mutableStateOf(false)
    }
    var role by rememberSaveable { mutableStateOf("Customer") }
    var profession by rememberSaveable {
        mutableStateOf("Electrician")
    }
    var professionMenuExpanded by remember {
        mutableStateOf(false)
    }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val professions = listOf(
        "Electrician",
        "Plumber",
        "Carpenter",
        "Cleaner",
        "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RegisterBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 30.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .offset(x = (-18).dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Go back",
                tint = Color(0xFF252B35)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Create Account",
            color = RegisterNavy,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = "Join SkillBridge and connect with local opportunities.",
            color = RegisterNavy,
            fontSize = 13.sp
        )

        Spacer(Modifier.height(25.dp))

        // Full name
        RegisterField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            title = "Full Name",
            hint = "Enter your full name",
            keyboardType = KeyboardType.Text,
            leadingIcon = {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = RegisterNavy
                )
            }
        )

        Spacer(Modifier.height(13.dp))

        // Email
        RegisterField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            title = "Email Address",
            hint = "Enter your email",
            keyboardType = KeyboardType.Email,
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = RegisterNavy
                )
            }
        )

        Spacer(Modifier.height(13.dp))

        // Password
        RegisterField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            title = "Password",
            hint = "Create a password",
            keyboardType = KeyboardType.Password,
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = RegisterNavy
                )
            },
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = { showPassword = !showPassword }
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
                        }
                    )
                }
            }
        )

        Spacer(Modifier.height(13.dp))

        // Confirm password
        RegisterField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = ""
            },
            title = "Confirm Password",
            hint = "Re-enter your password",
            keyboardType = KeyboardType.Password,
            leadingIcon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = RegisterNavy
                )
            },
            visualTransformation = if (showConfirmPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        showConfirmPassword = !showConfirmPassword
                    }
                ) {
                    Icon(
                        imageVector = if (showConfirmPassword) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (showConfirmPassword) {
                            "Hide confirm password"
                        } else {
                            "Show confirm password"
                        }
                    )
                }
            }
        )

        Spacer(Modifier.height(19.dp))

        Text(
            text = "I want to join as:",
            color = RegisterNavy,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RoleOption(
                title = "As Customer",
                selected = role == "Customer",
                modifier = Modifier.weight(1f),
                onClick = {
                    role = "Customer"
                    errorMessage = ""
                }
            )

            RoleOption(
                title = "As Worker",
                selected = role == "Worker",
                modifier = Modifier.weight(1f),
                onClick = {
                    role = "Worker"
                    errorMessage = ""
                }
            )
        }

        if (role == "Worker") {
            Spacer(Modifier.height(13.dp))

            Text(
                text = "Select your profession",
                color = RegisterNavy,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(6.dp))

            Box {
                Surface(
                    onClick = {
                        professionMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        RegisterBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 14.dp,
                                vertical = 13.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Badge,
                            contentDescription = null,
                            tint = RegisterNavy
                        )

                        Text(
                            text = profession,
                            color = RegisterNavy,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            fontSize = 13.sp
                        )

                        Text(
                            text = "⌄",
                            color = RegisterNavy,
                            fontSize = 20.sp
                        )
                    }
                }

                DropdownMenu(
                    expanded = professionMenuExpanded,
                    onDismissRequest = {
                        professionMenuExpanded = false
                    }
                ) {
                    professions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                profession = item
                                professionMenuExpanded = false
                                errorMessage = ""
                            }
                        )
                    }
                }
            }
        }

        if (errorMessage.isNotBlank()) {
            Spacer(Modifier.height(10.dp))

            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(22.dp))

        Button(
            onClick = {
                when {
                    name.isBlank() -> {
                        errorMessage = "Please enter your name"
                    }

                    !android.util.Patterns.EMAIL_ADDRESS
                        .matcher(email.trim())
                        .matches() -> {
                        errorMessage = "Please enter a valid email"
                    }

                    password.length < 6 -> {
                        errorMessage =
                            "Password must be at least 6 characters"
                    }

                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                    }

                    role == "Worker" && profession.isBlank() -> {
                        errorMessage = "Please select your profession"
                    }

                    else -> {
                        errorMessage = ""
                        onRegisterClick(
                            name.trim(),
                            email.trim(),
                            password,
                            role,
                            if (role == "Worker") profession else ""
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = RegisterTeal,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Register",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account?",
                color = Color(0xFF252B35),
                fontSize = 15.sp
            )

            TextButton(onClick = onSignInClick) {
                Text(
                    text = "Sign In",
                    color = RegisterTeal,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    title: String,
    hint: String,
    keyboardType: KeyboardType,
    leadingIcon: @Composable () -> Unit,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp),
        placeholder = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    color = RegisterNavy,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = hint,
                    color = RegisterHint,
                    fontSize = 12.sp
                )
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(9.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = RegisterTeal,
            unfocusedBorderColor = RegisterBorder,
            focusedTextColor = RegisterNavy,
            unfocusedTextColor = RegisterNavy,
            cursorColor = RegisterTeal
        )
    )
}

@Composable
private fun RoleOption(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) {
            Color(0xFFE1F5F2)
        } else {
            Color.White
        },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) RegisterTeal else RegisterBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = title,
                color = RegisterNavy,
                fontSize = 11.sp,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        }
    }
}
