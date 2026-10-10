
package com.example.skillbridge.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.data.Booking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private val BookingsNavy = Color(0xFF101D3A)
private val BookingsTeal = Color(0xFF12A594)
private val BookingsBackground = Color(0xFFF5F7FA)

@Composable
fun CustomerBookingsScreen(
    onHomeClick: () -> Unit = {}
) {
    var bookings by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    LaunchedEffect(refreshKey) {
        val customerId = auth.currentUser?.uid

        if (customerId.isNullOrBlank()) {
            error = "Please sign in again to view your bookings."
            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        error = null

        try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("customerId", customerId)
                .get()
                .await()

            bookings = snapshot.documents.mapNotNull { document ->
                document.toObject(Booking::class.java)
                    ?.copy(id = document.id)
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            error = e.localizedMessage
                ?: "Couldn't load your bookings."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookingsBackground)
            .padding(20.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Bookings",
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                color = BookingsNavy
            )

            TextButton(onClick = onHomeClick) {
                Text("Home", color = BookingsTeal)
            }
        }


        Spacer(Modifier.height(6.dp))

        Text(
            text = "Track your service requests",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(20.dp))

        when {
            isLoading -> {
                CircularProgressIndicator(color = BookingsTeal)
            }

            error != null -> {
                Text(
                    text = error ?: "Something went wrong.",
                    color = Color.Red
                )

                TextButton(
                    onClick = { refreshKey++ }
                ) {
                    Text("Try again", color = BookingsTeal)
                }
            }

            bookings.isEmpty() -> {
                Text(
                    text = "You haven't booked any services yet.",
                    color = Color.Gray
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = bookings,
                        key = { it.id }
                    ) { booking ->
                        BookingCard(booking)
                    }
                }

                TextButton(
                    onClick = { refreshKey++ }
                ) {
                    Text("Refresh bookings", color = BookingsTeal)
                }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: Booking) {
    val statusColor = when (booking.status) {
        "Accepted" -> Color(0xFF16834A)
        "Rejected" -> Color(0xFFD32F2F)
        else -> Color(0xFFB7791F)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = booking.profession,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BookingsNavy
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Worker: ${booking.workerName}",
                color = Color.DarkGray
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "Your request: ${booking.description}",
                color = Color.Gray
            )

            Spacer(Modifier.height(12.dp))

            Row {
                Text(
                    text = "Status: ",
                    fontWeight = FontWeight.Medium,
                    color = BookingsNavy
                )

                Text(
                    text = booking.status,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}