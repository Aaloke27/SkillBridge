package com.example.skillbridge.ui.worker

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.DisposableEffect
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


private val WorkerNavy = Color(0xFF101D3A)
private val WorkerTeal = Color(0xFF12A594)
private val WorkerBackground = Color(0xFFF4F7FB)



@Composable
fun WorkerDashboardScreen() {

    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    var workerArea by remember {
        mutableStateOf("Detecting location...")
    }

    var isAvailable by remember { mutableStateOf(true) }
    var isAvailabilityLoaded by remember { mutableStateOf(false) }

    var requests by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }

    var isLoadingRequests by remember {
        mutableStateOf(true)
    }

    var requestError by remember {
        mutableStateOf<String?>(null)
    }

    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val granted = permissions.values.any { it }

            if (granted) {
                val workerId = auth.currentUser?.uid

                if (workerId != null) {
                    scope.launch {
                        try {
                            val location =
                                com.example.skillbridge.location.LocationHelper
                                    .getCurrentLocation(context)

                            if (location != null) {
                                val area =
                                    com.example.skillbridge.location.LocationHelper
                                        .getLocality(
                                            context,
                                            location.latitude,
                                            location.longitude
                                        )

                                if (!area.isNullOrBlank()) {
                                    workerArea = area


                                    firestore.collection("workerProfiles")
                                        .document(workerId)
                                        .set(
                                            mapOf("serviceArea" to area),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )
                                        .await()

                                    message = "Service area saved: $area"

                                }
                            }
                        } catch (e: Exception) {
                            requestError = "Save failed: ${e.localizedMessage}"
                        }
                    }
                }
            }
        }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        val workerId = FirebaseAuth.getInstance().currentUser?.uid
        if (workerId != null) {
            try {
                val profile = firestore.collection("workerProfiles")
                    .document(workerId)
                    .get()
                    .await()

                isAvailable = profile.getBoolean("isAvailable") ?: true

                val fineGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                val coarseGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (fineGranted || coarseGranted) {
                    val location = com.example.skillbridge.location.LocationHelper
                        .getCurrentLocation(context)

                    if (location != null) {
                        val detectedArea =
                            com.example.skillbridge.location.LocationHelper.getLocality(
                                context,
                                location.latitude,
                                location.longitude
                            )

                        if (!detectedArea.isNullOrBlank()) {
                            workerArea = detectedArea

                            firestore.collection("workerProfiles")
                                .document(workerId)
                                .update("serviceArea", detectedArea)
                                .await()
                        }
                    }
                }
            } catch (e: Exception) {
                requestError = "Error: ${e.localizedMessage}"
            }
        }

        isAvailabilityLoaded = true
    }

    DisposableEffect(Unit) {
        val workerId = auth.currentUser?.uid

        if (workerId.isNullOrBlank()) {
            requestError = "Please sign in again."
            isLoadingRequests = false

            onDispose { }
        } else {
            val registration = firestore.collection("bookings")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "Pending")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        requestError = "Failed to listen for booking requests."
                        isLoadingRequests = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        requests = snapshot.documents.mapNotNull { document ->
                            document.toObject(Booking::class.java)
                                ?.copy(id = document.id)
                        }

                        requestError = null
                        isLoadingRequests = false
                    }
                }

            onDispose {
                registration.remove()
            }
        }
    }


    var acceptedJobs by remember { mutableStateOf(0) }
    var completedJobs by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkerBackground)
            .padding(20.dp)
    ) {
        Text(
            text = "SkillBridge",
            fontSize = 16.sp,
            color = WorkerTeal,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Worker Dashboard",
            fontSize = 27.sp,
            fontWeight = FontWeight.Bold,
            color = WorkerNavy
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "📍 Your service area: $workerArea",
            color = Color.Gray,
            fontSize = 14.sp
        )


        Text(
            text = "Manage your work and service requests",
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = WorkerNavy
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your availability",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = if (isAvailable) {
                            "You're Online"
                        } else {
                            "You're currently offline, \n go online to get customers"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isAvailable,
                    enabled = isAvailabilityLoaded,
                    onCheckedChange = { newValue ->
                        val workerId = auth.currentUser?.uid

                        if (workerId != null) {
                            scope.launch {
                                try {
                                    firestore.collection("workerProfiles")
                                        .document(workerId)
                                        .set(
                                            mapOf("isAvailable" to newValue),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )
                                        .await()

                                    isAvailable = newValue
                                    message = if (newValue) {
                                        "You are now available."
                                    } else {
                                        "You are now unavailable."
                                    }
                                } catch (e: Exception) {
                                    message = e.localizedMessage
                                        ?: "Couldn't update availability."
                                }
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "New Requests",
                value = requests.size.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Accepted",
                value = acceptedJobs.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryCard(
                title = "Completed",
                value = completedJobs.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        if (message.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = message,
                color = WorkerTeal,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Incoming Requests",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = WorkerNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (!isAvailable) {
            Text(
                text = "Turn on availability to receive new requests.",
                color = Color.Gray
            )
        } else if (isLoadingRequests) {
            androidx.compose.material3.CircularProgressIndicator()
        } else if (requestError != null) {
            Text(
                text = requestError ?: "Something went wrong.",
                color = Color.Red
            )
        } else if (requests.isEmpty()) {
            Text(
                text = "No pending requests right now.",
                color = Color.Gray
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = requests,
                    key = { it.id }
                ) { request ->
                    JobRequestCard(
                        request = request,
                        onAccept = {
                            scope.launch {
                                try {
                                    firestore.collection("bookings")
                                        .document(request.id)
                                        .update("status", "Accepted")
                                        .await()

                                    requests = requests.filterNot {
                                        it.id == request.id
                                    }

                                    acceptedJobs++
                                    message = "Request accepted: ${request.profession}"
                                } catch (e: Exception) {
                                    message = e.localizedMessage
                                        ?: "Failed to accept request"
                                }
                            }
                        },
                        onReject = {
                            scope.launch {
                                try {
                                    firestore.collection("bookings")
                                        .document(request.id)
                                        .update("status", "Rejected")
                                        .await()

                                    requests = requests.filterNot {
                                        it.id == request.id
                                    }

                                    message = "Request declined: ${request.profession}"
                                } catch (e: Exception) {
                                    message = e.localizedMessage
                                        ?: "Failed to decline request"
                                }
                            }
                        }
                    )
                }
            }
        }
        }
    }
@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = value,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = WorkerTeal
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                color = WorkerNavy
            )
        }
    }
}

@Composable
private fun JobRequestCard(
    request: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
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
                text = request.profession,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WorkerNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customer: ${request.customerName}",
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Request: ${request.description}",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Status: ${request.status}",
                color = WorkerTeal,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkerTeal
                    )
                ) {
                    Text("Accept")
                }

                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WorkerNavy
                    )
                ) {
                    Text("Decline")
                }
            }
        }
    }
}

