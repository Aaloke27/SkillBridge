
package com.example.skillbridge.ui.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun WorkerProfileScreen() {
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val workerId = auth.currentUser?.uid

    var name by remember { mutableStateOf("") }
    var profession by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email.orEmpty()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(workerId) {
        if (workerId != null) {
            try {
                val profile = firestore.collection("workerProfiles")
                    .document(workerId).get().await()

                name = profile.getString("name").orEmpty()
                profession = profile.getString("profession").orEmpty()
            } catch (e: Exception) {
                message = "Couldn't load your profile."
            }
        } else {
            message = "Please sign in again."
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "My Profile",
            style = MaterialTheme.typography.headlineMedium
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = email,
                onValueChange = {},
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                singleLine = true
            )

            OutlinedTextField(
                value = profession,
                onValueChange = { profession = it },
                label = { Text("Profession") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val uid = workerId
                    if (uid == null) {
                        message = "Please sign in again."
                    } else if (name.isBlank() || profession.isBlank()) {
                        message = "Name and profession cannot be empty."
                    } else {
                        scope.launch {
                            isSaving = true
                            message = ""

                            try {
                                firestore.runBatch { batch ->
                                    batch.update(
                                        firestore.collection("users").document(uid),
                                        "name", name.trim()
                                    )
                                    batch.update(
                                        firestore.collection("workerProfiles").document(uid),
                                        mapOf(
                                            "name" to name.trim(),
                                            "profession" to profession.trim()
                                        )
                                    )
                                }.await()

                                message = "Profile updated successfully!"
                            } catch (e: Exception) {
                                message = e.localizedMessage
                                    ?: "Couldn't save your profile."
                            } finally {
                                isSaving = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                Text(if (isSaving) "Saving..." else "Save Changes")
            }

            if (message.isNotBlank()) {
                Text(text = message)
            }
        }
    }
}


