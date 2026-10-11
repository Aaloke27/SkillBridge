
package com.example.skillbridge.ui.customer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.data.Booking
import com.example.skillbridge.ui.customer.components.BottomTab
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val BookingsNavy = Color(0xFF101D3A)
private val BookingsTeal = Color(0xFF12A594)
private val BookingsBackground = Color(0xFFF5F7FA)
private val BookingsMuted = Color(0xFF788397)

@Composable
fun CustomerBookingsScreen(
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var bookings by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    var selectedIds by remember {
        mutableStateOf<Set<String>>(emptySet())
    }
    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }
    var isDeleting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    // Load the current customer's bookings.
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

            selectedIds = emptySet()
        } catch (e: Exception) {
            error = e.localizedMessage
                ?: "Couldn't load your bookings."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BookingsBackground,
        bottomBar = {
            BottomTab(
                selectedTab = "Bookings",
                onHomeClick = onHomeClick,
                onBookingsClick = {},
                onProfileClick = onProfileClick,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BookingsBackground)
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                )
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(0xFFDDF5F0),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = BookingsTeal,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "My Bookings",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = BookingsNavy
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = "Track your service requests",
                        fontSize = 13.sp,
                        color = BookingsMuted
                    )
                }

                TextButton(
                    onClick = { refreshKey++ },
                    enabled = !isLoading && !isDeleting
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh bookings",
                        tint = BookingsTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(22.dp))

            // Booking count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Your service requests",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BookingsNavy
                )

                Surface(
                    shape = CircleShape,
                    color = Color(0xFFDDF5F0)
                ) {
                    Text(
                        text = "${bookings.size}",
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        ),
                        color = BookingsTeal,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = BookingsTeal
                            )

                            Spacer(Modifier.height(12.dp))

                            Text(
                                text = "Loading your bookings...",
                                color = BookingsMuted,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                error != null -> {
                    EmptyState(
                        title = "Couldn't load bookings",
                        message = error ?: "Please try again.",
                        actionText = "Try again",
                        onAction = { refreshKey++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                bookings.isEmpty() -> {
                    EmptyState(
                        title = "No bookings yet",
                        message = "When you request a service, your bookings will appear here.",
                        actionText = "Refresh",
                        onAction = { refreshKey++ },
                        modifier = Modifier.weight(1f)
                    )
                }

                else -> {
                    if (selectedIds.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE1F5F1)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical = 8.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${selectedIds.size} selected",
                                    color = BookingsNavy,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            selectedIds = emptySet()
                                        }
                                    ) {
                                        Text(
                                            text = "Cancel",
                                            color = BookingsNavy
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            showDeleteConfirmation = true
                                        },
                                        enabled = !isDeleting,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFD93636)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Delete")
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = bookings,
                            key = { it.id }
                        ) { booking ->
                            BookingCard(
                                booking = booking,
                                isSelected = booking.id in selectedIds,
                                onClick = {
                                    if (
                                        booking.status in listOf(
                                            "Completed",
                                            "Rejected"
                                        )
                                    ) {
                                        selectedIds =
                                            if (booking.id in selectedIds) {
                                                selectedIds - booking.id
                                            } else {
                                                selectedIds + booking.id
                                            }
                                    }
                                },
                                onLongClick = {
                                    if (
                                        booking.status in listOf(
                                            "Completed",
                                            "Rejected"
                                        )
                                    ) {
                                        selectedIds =
                                            selectedIds + booking.id
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Keep the dialog inside CustomerBookingsScreen,
    // but outside the Scaffold content lambda.
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteConfirmation = false
                }
            },
            title = {
                Text("Delete selected bookings?")
            },
            text = {
                Text(
                    "Permanently delete ${selectedIds.size} completed " +
                            "or rejected booking(s)? This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        scope.launch {
                            isDeleting = true
                            error = null

                            try {
                                val batch = firestore.batch()

                                selectedIds.forEach { id ->
                                    batch.delete(
                                        firestore.collection("bookings")
                                            .document(id)
                                    )
                                }

                                batch.commit().await()

                                bookings = bookings.filterNot {
                                    it.id in selectedIds
                                }

                                selectedIds = emptySet()
                                showDeleteConfirmation = false
                            } catch (e: Exception) {
                                error = e.localizedMessage
                                    ?: "Couldn't delete bookings."

                                showDeleteConfirmation = false
                            } finally {
                                isDeleting = false
                            }
                        }
                    }
                ) {
                    Text(
                        text = if (isDeleting) {
                            "Deleting..."
                        } else {
                            "Delete"
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(
                        color = Color(0xFFDDF5F0),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = BookingsTeal,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = title,
                color = BookingsNavy,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = message,
                color = BookingsMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BookingsTeal
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(actionText)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookingCard(
    booking: Booking,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val statusColor = when (booking.status) {
        "Accepted" -> Color(0xFF16834A)
        "Rejected" -> Color(0xFFD32F2F)
        "Completed" -> Color(0xFF16834A)
        else -> Color(0xFFB7791F)
    }

    val statusBackground = when (booking.status) {
        "Accepted" -> Color(0xFFE2F5E9)
        "Rejected" -> Color(0xFFFFE7E7)
        "Completed" -> Color(0xFFE2F5E9)
        else -> Color(0xFFFFF1D6)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = BookingsTeal,
                        shape = RoundedCornerShape(18.dp)
                    )
                } else {
                    Modifier
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFFE1F5F1)
            } else {
                Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            color = Color(0xFFDDF5F0),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = BookingsTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 8.dp)
                ) {
                    Text(
                        text = booking.profession,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = BookingsNavy
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "Worker: ${booking.workerName}",
                        fontSize = 13.sp,
                        color = BookingsMuted
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBackground
                ) {
                    Text(
                        text = booking.status.ifBlank { "Pending" },
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 6.dp
                        )
                    )
                }
            }

            Spacer(Modifier.height(15.dp))

            Text(
                text = "Service details",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BookingsMuted
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = booking.description.ifBlank {
                    "No additional details provided."
                },
                fontSize = 14.sp,
                color = BookingsNavy,
                lineHeight = 20.sp
            )

            if (booking.customerArea.isNotBlank()) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Location: ${booking.customerArea}",
                    fontSize = 12.sp,
                    color = BookingsMuted
                )
            }

            if (isSelected) {
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Selected for deletion",
                    color = BookingsTeal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (
                booking.status in listOf("Completed", "Rejected")
            ) {
                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Long-press to select for deletion",
                    color = BookingsMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
