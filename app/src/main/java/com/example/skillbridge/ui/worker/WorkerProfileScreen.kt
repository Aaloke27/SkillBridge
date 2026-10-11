
package com.example.skillbridge.ui.worker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.ExperimentalMaterial3Api


private val ProfileNavy = Color(0xFF101D3A)
private val ProfileTeal = Color(0xFF00A896)
private val ProfileBackground = Color(0xFFF4F7FB)
private val ProfileCoral = Color(0xFFFF7D82)
private val ProfileGray = Color(0xFF858D9A)

private val workerProfessions = listOf(
    "Electrician",
    "Plumber",
    "Carpenter",
    "Painter",
    "Cleaner",
    "AC Technician",
    "Appliance Repair",
    "Mechanic",
    "Other"
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerProfileScreen(
    onLogoutClick: () -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val workerId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var email by remember {
        mutableStateOf(auth.currentUser?.email.orEmpty())
    }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var professionMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(workerId) {
        if (workerId == null) {
            message = "Please sign in again."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            val profile = firestore.collection("workerProfiles")
                .document(workerId)
                .get()
                .await()

            name = profile.getString("name").orEmpty()
            profession = profile.getString("profession")
                ?: profile.getString("skill")
                        ?: ""

            email = auth.currentUser?.email.orEmpty()
        } catch (e: Exception) {
            message = "Couldn't load your profile."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileBackground)
            .padding(horizontal = 20.dp)
    ) {
        // Top bar and SkillBridge logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "SkillBridge logo",
                modifier = Modifier.size(70.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "My ",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkerNavy
                )
                Text(
                    text = "Profile",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkerTeal
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "My Profile",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Manage your SkillBridge Account",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 17.sp,
            color = Color.DarkGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = ProfileTeal)
            }
        } else {
            // Full name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                placeholder = { Text("Enter your full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = ProfileTeal,
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email: read-only
            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = ProfileTeal,
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profession dropdown
            androidx.compose.material3.ExposedDropdownMenuBox(
                expanded = professionMenuExpanded,
                onExpandedChange = {
                    professionMenuExpanded = !professionMenuExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = profession,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Profession") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Choose profession"
                        )
                    },
                    modifier = Modifier
                        .menuAnchor(
                            androidx.compose.material3.MenuAnchorType.PrimaryNotEditable
                        )
                        .fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = ProfileTeal,
                        unfocusedIndicatorColor = Color.Gray
                    )
                )

                DropdownMenu(
                    expanded = professionMenuExpanded,
                    onDismissRequest = {
                        professionMenuExpanded = false
                    },
                    modifier = Modifier.background(Color.White)
                ) {
                    workerProfessions.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                profession = item
                                professionMenuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Save changes
            Button(
                onClick = {
                    val uid = workerId

                    when {
                        uid == null -> {
                            message = "Please sign in again."
                        }

                        name.isBlank() || profession.isBlank() -> {
                            message = "Name and profession cannot be empty."
                        }

                        else -> {
                            scope.launch {
                                isSaving = true
                                message = ""

                                try {
                                    val updatedName = name.trim()
                                    val updatedProfession = profession.trim()

                                    firestore.runBatch { batch ->
                                        batch.set(
                                            firestore.collection("users")
                                                .document(uid),
                                            mapOf("name" to updatedName),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )

                                        batch.set(
                                            firestore.collection("workerProfiles")
                                                .document(uid),
                                            mapOf(
                                                "name" to updatedName,
                                                "profession" to updatedProfession
                                            ),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )
                                    }.await()

                                    name = updatedName
                                    profession = updatedProfession
                                    message = "Profile updated successfully!"
                                } catch (e: Exception) {
                                    message = e.localizedMessage
                                        ?: "Couldn't save your profile."
                                } finally {
                                    isSaving = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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

            Spacer(modifier = Modifier.height(14.dp))

            // Logout
            Button(
                onClick = {
                    auth.signOut()
                    onLogoutClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isSaving,
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

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = if (
                        message.contains("successfully", ignoreCase = true)
                    ) {
                        ProfileTeal
                    } else {
                        ProfileGray
                    },
                    fontSize = 12.sp
                )
            }
        }
    }
}
