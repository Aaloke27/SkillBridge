package com.example.skillbridge.ui.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.skillbridge.data.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.Button
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.platform.LocalContext
@Composable
fun WorkerBookingsScreen() {
    val context = LocalContext.current
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val workerId = auth.currentUser?.uid

        if (workerId.isNullOrBlank()) {
            errorMessage = "Please sign in again."
            isLoading = false
            onDispose { }
        } else {
            val registration = firestore.collection("bookings")
                .whereEqualTo("workerId", workerId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        errorMessage = "Couldn't load your bookings."
                        isLoading = false
                    } else if (snapshot != null) {
                        bookings = snapshot.documents.mapNotNull { document ->
                            document.toObject(Booking::class.java)
                                ?.copy(id = document.id)
                        }.sortedByDescending { it.createdAt }

                        errorMessage = null
                        isLoading = false
                    }
                }

            onDispose {
                registration.remove()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "My Bookings",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "View all your service requests",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )
            }

            bookings.isEmpty() -> {
                Text("You don't have any bookings yet.")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bookings, key = { it.id }) { booking ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = booking.profession,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Customer: ${booking.customerName}",
                                    modifier = Modifier.padding(top = 6.dp)
                                )

                                Text(
                                    text = "Request: ${booking.description}",
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Button(
                                    onClick = {
                                        val latitude = booking.customerLatitude
                                        val longitude = booking.customerLongitude

                                        if (latitude != null && longitude != null) {
                                            val navigationUri = Uri.parse(
                                                "google.navigation:q=$latitude,$longitude"
                                            )

                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                navigationUri
                                            ).apply {
                                                setPackage("com.google.android.apps.maps")
                                            }

                                            try {
                                                context.startActivity(intent)
                                            } catch (e: android.content.ActivityNotFoundException) {
                                                val fallbackUri = Uri.parse(
                                                    "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                                                )

                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, fallbackUri)
                                                )
                                            }
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Customer GPS location is unavailable",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.padding(top = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF12A594)
                                    )
                                ) {
                                    Text("📍 Navigate to Customer")
                                }


                                val statusColor = when (booking.status) {
                                    "Accepted" -> Color(0xFF16834A)
                                    "Rejected" -> MaterialTheme.colorScheme.error
                                    else -> Color(0xFFB7791F)
                                }

                                Text(
                                    text = "Status: ${booking.status}",
                                    color = statusColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 10.dp)
                                )

                                if (booking.status == "Accepted") {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    firestore.collection("bookings")
                                                        .document(booking.id)
                                                        .update("status", "Completed")
                                                        .await()
                                                } catch (e: Exception) {
                                                    errorMessage = e.localizedMessage
                                                        ?: "Couldn't complete this job."
                                                }
                                            }
                                        },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Mark as Completed")
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }
}
