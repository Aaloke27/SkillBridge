
package com.example.skillbridge.ui.worker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.platform.LocalContext

import com.example.skillbridge.R
import com.example.skillbridge.data.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val BookingBackground = Color(0xFFF4F7FB)
private val BookingGray = Color(0xFF858D9A)
private val BookingBorder = Color(0xFFE5E9F0)

@Composable
fun WorkerBookingsScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var bookings by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf("") }
    var completingBookingId by remember { mutableStateOf<String?>(null) }

    // Real-time bookings for the signed-in worker.
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
            .background(BookingBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "SkillBridge logo",
                modifier = Modifier.size(100.dp)
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
                    text = "Bookings",
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkerTeal
                )
            }
        }

        Text(
            text = "View all your service requests",
            fontSize = 17.sp,
            color = BookingGray,
            modifier = Modifier.padding(
                start = 50.dp,
                bottom = 16.dp
            ).offset(x = (-40).dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WorkerTeal)
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "Couldn't load bookings.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            bookings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You don't have any bookings yet.",
                        color = BookingGray,
                        fontSize = 17.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = bookings,
                        key = { it.id }
                    ) { booking ->

                        WorkerBookingCard(
                            booking = booking,
                            isCompleting = completingBookingId == booking.id,
                            onNavigate = {
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
                                    } catch (e: ActivityNotFoundException) {
                                        val fallbackUri = Uri.parse(
                                            "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                                        )

                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                fallbackUri
                                            )
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
                            onComplete = {
                                if (completingBookingId == null) {
                                    scope.launch {
                                        completingBookingId = booking.id

                                        try {
                                            firestore.collection("bookings")
                                                .document(booking.id)
                                                .update(
                                                    "status",
                                                    "Completed"
                                                )
                                                .await()

                                            message = "Job marked as completed."
                                        } catch (e: Exception) {
                                            message = e.localizedMessage
                                                ?: "Couldn't complete this job."
                                        } finally {
                                            completingBookingId = null
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (message.isNotBlank()) {
            Text(
                text = message,
                color = WorkerTeal,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun WorkerBookingCard(
    booking: Booking,
    isCompleting: Boolean,
    onNavigate: () -> Unit,
    onComplete: () -> Unit
) {
    val status = booking.status.ifBlank { "Pending" }

    val statusColor = when (status.lowercase()) {
        "pending" -> Color(0xFFFFE95C)
        "accepted" -> Color(0xFF83DDD0)
        "completed" -> Color(0xFF12A594)
        "rejected" -> Color(0xFFFFB7B7)
        else -> Color(0xFFE4E8EF)
    }

    val statusTextColor = when (status.lowercase()) {
        "pending" -> Color(0xFF776400)
        "accepted" -> Color(0xFF087F72)
        "completed" -> Color.White
        "rejected" -> Color(0xFFB42332)
        else -> WorkerNavy
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = BookingBorder
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer avatar
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = Color(0xFFF0F3F9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Customer",
                        tint = Color(0xFF42618F),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Customer and request details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = booking.customerName.ifBlank {
                        "Customer"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkerNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = booking.profession,
                    fontSize = 15.sp,
                    color = Color(0xFF4B6385),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Request: ${booking.description}",
                    fontSize = 15.sp,
                    color = BookingGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Status and optional completion action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor
                ) {
                    Text(
                        text = status,
                        color = statusTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                    )
                }

                if (status.equals("Accepted", ignoreCase = true)) {
                    Button(
                        onClick = onComplete,
                        enabled = !isCompleting,
                        contentPadding = PaddingValues(
                            horizontal = 7.dp,
                            vertical = 0.dp
                        ),
                        modifier = Modifier.height(25.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WorkerTeal
                        )
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Mark as\ncompleted",
                                fontSize = 9.sp,
                                lineHeight = 8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Navigation shortcut
            androidx.compose.material3.IconButton(
                onClick = onNavigate,
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Navigate to customer",
                    tint = BookingGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
