
package com.example.skillbridge.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.R
import com.example.skillbridge.ui.customer.components.BottomTab
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val ProfileNavy = Color(0xFF101D3A)
private val ProfileTeal = Color(0xFF12A594)
private val ProfileBackground = Color(0xFFF2F6FB)
private val ProfileCoral = Color(0xFFFF7B80)
private val ProfileGray = Color(0xFF858D9A)

@Composable
fun CustomerProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit = onBackClick,
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            message = "Please sign in again."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            name = document.getString("name").orEmpty()
            email = document.getString("email")
                ?: auth.currentUser?.email.orEmpty()
        } catch (e: Exception) {
            message = e.localizedMessage ?: "Couldn't load your profile."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = ProfileBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            BottomTab(
                selectedTab = "Profile",
                onHomeClick = onHomeClick,
                onBookingsClick = onBookingsClick,
                onProfileClick = onProfileClick,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical =40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start

            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "SkillBridge logo",
                    modifier = Modifier
                        .size(width = 165.dp, height = 85.dp)
                        .offset(x = (-60).dp)
                )
                Text(
                    text = "SkillBridge",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfileNavy,
                    modifier = Modifier
                    .offset(x = (-100).dp)
                )
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "My Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Manage your SkillBridge Account",
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ProfileTeal)
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ProfileNavy
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                message = null
                            },
                            label = { Text("Full name") },
                            placeholder = { Text("Enter your name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {},
                            label = { Text("Email address") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            readOnly = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        message?.let {
                            Text(
                                text = it,
                                color = if (it.startsWith("Profile updated")) {
                                    ProfileTeal
                                } else {
                                    Color.Red
                                },
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                val userId = auth.currentUser?.uid

                                if (userId == null) {
                                    message = "Please sign in again."
                                    return@Button
                                }

                                if (name.isBlank()) {
                                    message = "Name cannot be empty."
                                    return@Button
                                }

                                isSaving = true
                                message = null

                                scope.launch {
                                    try {
                                        firestore.collection("users")
                                            .document(userId)
                                            .update("name", name.trim())
                                            .await()

                                        message = "Profile updated successfully!"
                                    } catch (e: Exception) {
                                        message = e.localizedMessage
                                            ?: "Couldn't update your profile."
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            enabled = !isSaving,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileTeal
                            )
                        ) {
                            Text(
                                text = if (isSaving) "Saving..." else "Save Changes",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                auth.signOut()
                                onLogoutClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfileCoral
                            )
                        ) {
                            Text(
                                text = "Logout",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
