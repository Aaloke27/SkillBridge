
package com.example.skillbridge.ui.worker

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.skillbridge.R
import com.example.skillbridge.data.Booking
import com.example.skillbridge.location.LocationHelper
import com.example.skillbridge.ui.worker.components.JobRequestCard
import com.example.skillbridge.ui.worker.components.SummaryCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val WorkerNavy = Color(0xFF101D3A)
val WorkerTeal = Color(0xFF12A594)
private val WorkerBackground = Color(0xFFF4F7FB)
private val WorkerGray = Color(0xFF858D9A)

@Composable
fun WorkerDashboardScreen() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("") }
    var workerArea by remember { mutableStateOf("Detecting location...") }
    var workerName by remember { mutableStateOf("Worker") }
    var workerProfession by remember { mutableStateOf("Service Professional") }

    var isAvailable by remember { mutableStateOf(true) }
    var isAvailabilityLoaded by remember { mutableStateOf(false) }

    var requests by remember {
        mutableStateOf<List<Booking>>(emptyList())
    }
    var isLoadingRequests by remember { mutableStateOf(true) }
    var requestError by remember { mutableStateOf<String?>(null) }

    var acceptedJobs by remember { mutableStateOf(0) }
    var completedJobs by remember { mutableStateOf(0) }

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
                                LocationHelper.getCurrentLocation(context)

                            if (location != null) {
                                val area = LocationHelper.getLocality(
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
                                            SetOptions.merge()
                                        )
                                        .await()
                                }
                            }
                        } catch (e: Exception) {
                            message = e.localizedMessage
                                ?: "Couldn't detect location."
                        }
                    }
                }
            } else {
                workerArea = "Location permission needed"
            }
        }

    // Load worker profile and service location.
    LaunchedEffect(Unit) {
        val workerId = auth.currentUser?.uid

        if (workerId == null) {
            requestError = "Please sign in again."
            isAvailabilityLoaded = true
            isLoadingRequests = false
            return@LaunchedEffect
        }

        try {
            val profile = firestore.collection("workerProfiles")
                .document(workerId)
                .get()
                .await()

            isAvailable = profile.getBoolean("isAvailable") ?: true

            workerName = profile.getString("name")
                ?: auth.currentUser?.displayName?.substringBefore(" ")
                        ?: "Worker"

            workerProfession = profile.getString("profession")
                ?: profile.getString("skill")
                        ?: "Service Professional"

            acceptedJobs = profile.getLong("acceptedJobs")?.toInt() ?: 0
            completedJobs = profile.getLong("completedJobs")?.toInt() ?: 0

            val fineGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            val coarseGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (fineGranted || coarseGranted) {
                val location = LocationHelper.getCurrentLocation(context)

                if (location != null) {
                    val detectedArea = LocationHelper.getLocality(
                        context,
                        location.latitude,
                        location.longitude
                    )

                    if (!detectedArea.isNullOrBlank()) {
                        workerArea = detectedArea

                        firestore.collection("workerProfiles")
                            .document(workerId)
                            .set(
                                mapOf("serviceArea" to detectedArea),
                                SetOptions.merge()
                            )
                            .await()
                    }
                } else {
                    workerArea = profile.getString("serviceArea")
                        ?: "Location unavailable"
                }
            } else {
                workerArea = profile.getString("serviceArea")
                    ?: "Location permission needed"

                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        } catch (e: Exception) {
            requestError = e.localizedMessage
                ?: "Couldn't load worker profile."
        } finally {
            isAvailabilityLoaded = true
        }
    }

    // Listen for incoming pending requests in real time.
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
                        requestError = "Failed to load booking requests."
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WorkerBackground)
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "SkillBridge logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Hi, ",
                        fontSize = 22.sp,
                        color = WorkerNavy,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = workerName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WorkerNavy
                    )
                }

                Text(
                    text = workerProfession,
                    fontSize = 17.sp,
                    color = WorkerNavy
                )

                Text(
                    text = "Location: $workerArea",
                    fontSize = 17.sp,
                    color = WorkerGray
                )
            }
        }

        // Online availability card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = WorkerTeal
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAvailable) {
                            "You're online"
                        } else {
                            "You're offline"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = if (isAvailable) {
                            "Available for new booking"
                        } else {
                            "Go online to receive bookings"
                        },
                        fontSize = 17.sp,
                        color = Color.White
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
                                            SetOptions.merge()
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
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = WorkerNavy,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Summary cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                color = WorkerTeal,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Incoming requests",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = WorkerNavy
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Keep the request list scrollable above the bottom navigation.
        when {
            !isAvailable -> {
                Text(
                    text = "Turn on availability to receive new requests.",
                    color = WorkerGray,
                    fontSize = 13.sp
                )
            }

            isLoadingRequests -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = WorkerTeal)
                }
            }

            requestError != null -> {
                Text(
                    text = requestError ?: "Something went wrong.",
                    color = Color.Red,
                    fontSize = 13.sp
                )
            }

            requests.isEmpty() -> {
                Text(
                    text = "No pending requests right now.",
                    color = WorkerGray,
                    fontSize = 13.sp
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

                                        acceptedJobs++

                                        // Persist the updated count.
                                        val workerId = auth.currentUser?.uid
                                        if (workerId != null) {
                                            firestore.collection("workerProfiles")
                                                .document(workerId)
                                                .set(
                                                    mapOf(
                                                        "acceptedJobs" to acceptedJobs
                                                    ),
                                                    SetOptions.merge()
                                                )
                                                .await()
                                        }

                                        message =
                                            "Request accepted: ${request.profession}"
                                    } catch (e: Exception) {
                                        message = e.localizedMessage
                                            ?: "Failed to accept request."
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

                                        message =
                                            "Request declined: ${request.profession}"
                                    } catch (e: Exception) {
                                        message = e.localizedMessage
                                            ?: "Failed to decline request."
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        if (requests.isEmpty() || !isAvailable ||
            isLoadingRequests || requestError != null
        ) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
