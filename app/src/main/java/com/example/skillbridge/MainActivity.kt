
package com.example.skillbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.ui.auth.RegisterScreen
import com.example.skillbridge.ui.theme.SkillBridgeTheme
import com.example.skillbridge.ui.auth.LoginScreen
import com.example.skillbridge.ui.customer.CustomerHomeScreen
import com.example.skillbridge.ui.worker.WorkerDashboardScreen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.runtime.rememberCoroutineScope
import com.example.skillbridge.data.FirebaseAuthManager
import kotlinx.coroutines.launch
import com.example.skillbridge.ui.customer.CustomerBookingsScreen
import com.example.skillbridge.ui.customer.CustomerProfileScreen
import com.example.skillbridge.ui.worker.WorkerMainScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth
private val Navy = Color(0xFF101D3A)
private val Teal = Color(0xFF12A594)
private val LightText = Color(0xFFD4DEEE)


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkillBridgeTheme {
                var currentScreen by remember {
                    mutableStateOf("checking_session")
                }

                val authManager = remember {
                    FirebaseAuthManager()
                }

                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    val user = FirebaseAuth.getInstance().currentUser

                    if (user == null) {
                        currentScreen = "welcome"
                    } else {
                        val roleResult = authManager.getUserRole(user.uid)

                        currentScreen = if (roleResult.isSuccess) {
                            when (roleResult.getOrThrow()) {
                                "Customer" -> "customer_home"
                                "Worker" -> "worker_dashboard"
                                else -> "welcome"
                            }
                        } else {
                            "welcome"
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (currentScreen == "welcome") {
                        Navy
                    } else {
                        Color.White
                    }
                ) {
                    when (currentScreen) {
                        "checking_session" -> Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        "welcome" -> WelcomeScreen(
                            onCreateAccountClick = {
                                currentScreen = "register"
                            },
                            onSignInClick = {
                                currentScreen = "login"
                            }
                        )

                        "register" -> RegisterScreen(
                            onBackClick = {
                                currentScreen = "welcome"
                            },
                            onSignInClick = {
                                currentScreen = "login"
                            },

                            onRegisterClick = { name, email, password, role, profession ->

                                scope.launch {
                                    val result = authManager.register(
                                        name = name,
                                        email = email,
                                        password = password,
                                        role = role,
                                        profession = profession
                                    )

                                    result.onSuccess {
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Account created successfully!",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        currentScreen = if (role == "Customer") {
                                            "customer_home"
                                        } else {
                                            "worker_dashboard"
                                        }
                                    }

                                    result.onFailure { error ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            error.localizedMessage ?: "Registration failed.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                        )

                        "login" -> LoginScreen(
                            onBackClick = {
                                currentScreen = "welcome"
                            },
                            onRegisterClick = {
                                currentScreen = "register"
                            },
                            onLoginClick = { email, password ->
                                scope.launch {
                                    val loginResult = authManager.login(
                                        email = email,
                                        password = password
                                    )

                                    if (loginResult.isSuccess) {
                                        val uid = loginResult.getOrThrow()
                                        val roleResult = authManager.getUserRole(uid)

                                        if (roleResult.isSuccess) {
                                            currentScreen = when (roleResult.getOrThrow()) {
                                                "Customer" -> "customer_home"
                                                "Worker" -> "worker_dashboard"
                                                else -> "welcome"
                                            }
                                        } else {
                                            authManager.signOut()

                                            Toast.makeText(
                                                this@MainActivity,
                                                roleResult.exceptionOrNull()?.localizedMessage
                                                    ?: "Could not load your profile.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@MainActivity,
                                            loginResult.exceptionOrNull()?.localizedMessage
                                                ?: "Login failed.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        )


                        "customer_home" -> CustomerHomeScreen(
                            onBookingsClick = {
                                currentScreen = "customer_bookings"
                            },
                            onProfileClick = {
                                currentScreen = "customer_profile"
                            }
                        )


                        "customer_profile" -> CustomerProfileScreen(
                            onBackClick = {
                                currentScreen = "customer_home"
                            },
                            onLogoutClick = {
                                currentScreen = "welcome"
                            },
                            onHomeClick = {
                                currentScreen = "customer_home"
                            },
                            onBookingsClick = {
                                currentScreen = "customer_bookings"
                            },
                            onProfileClick = {
                                currentScreen = "customer_profile"
                            }
                        )



                        "customer_bookings" -> CustomerBookingsScreen(
                            onHomeClick = {
                                currentScreen = "customer_home"
                            },
                            onProfileClick = {
                                currentScreen = "customer_profile"
                            }
                        )



                        "worker_dashboard" -> WorkerMainScreen(
                            onLogoutClick = {
                                currentScreen = "welcome"
                            }
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val backgroundColor = Color(0xFFF4F7FB)
    val tealColor = Color(0xFF12A594)
    val navyColor = Color(0xFF101D3A)
    val mutedColor = Color(0xFF667085)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top spacing
        Spacer(modifier = Modifier.weight(1f))

        // SkillBridge logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "SkillBridge logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(22.dp))

        // Tagline
        Text(
            text = "Connecting Talent to Opportunity",
            color = navyColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = "Find trusted local services.\nConnect with skilled professionals.",
            color = Color(0xFF303846),
            fontSize = 13.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )

        // Flexible space before the buttons
        Spacer(modifier = Modifier.weight(1f))

        // Register button
        Button(
            onClick = onCreateAccountClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Register",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sign-in button
        OutlinedButton(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = tealColor
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = tealColor
            )
        ) {
            Text(
                text = "Sign in",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

