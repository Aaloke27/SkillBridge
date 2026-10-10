
package com.example.skillbridge.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val ProfileNavy = Color(0xFF101D3A)
private val ProfileTeal = Color(0xFF12A594)
private val ProfileBackground = Color(0xFFF5F7FA)

@Composable
fun CustomerProfileScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
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

            name = document.getString("name") ?: ""
            email = document.getString("email")
                ?: auth.currentUser?.email.orEmpty()
        } catch (e: Exception) {
            message = e.localizedMessage ?: "Couldn't load your profile."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back", color = ProfileTeal)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "My Profile",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = ProfileNavy
        )

        Text(
            text = "Manage your SkillBridge account",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(Modifier.height(28.dp))

        if (isLoading) {
            CircularProgressIndicator(color = ProfileTeal)
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfileNavy
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            message = null
                        },
                        label = { Text("Full name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        label = { Text("Email address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true
                    )

                    message?.let {
                        Text(
                            text = it,
                            color = if (it.startsWith("Profile updated")) {
                                ProfileTeal
                            } else {
                                Color.Red
                            },
                            fontSize = 13.sp
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving && !isLoading
                    ) {
                        Text(
                            if (isSaving) "Saving..." else "Save Changes"
                        )
                    }
                    Button(
                        onClick = {
                            auth.signOut()
                            onLogoutClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}


