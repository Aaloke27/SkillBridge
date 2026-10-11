
package com.example.skillbridge.ui.customer

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.skillbridge.R
import com.example.skillbridge.data.Booking
import com.example.skillbridge.data.BookingRepository
import com.example.skillbridge.data.ServiceCategory
import com.example.skillbridge.data.Worker
import com.example.skillbridge.location.LocationHelper
import com.example.skillbridge.ui.customer.components.BottomTab
import com.example.skillbridge.ui.customer.components.CategoryCard
import com.example.skillbridge.ui.customer.components.WorkerCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val HomeNavy = Color(0xFF101D3A)
val HomeTeal = Color(0xFF12A594)
private val HomeBackground = Color(0xFFF3F6FB)
private val HomeMuted = Color(0xFF7B8492)

@Composable
fun CustomerHomeScreen(
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val bookingRepository = remember { BookingRepository() }
    val scope = rememberCoroutineScope()

    var customerName by remember { mutableStateOf("there") }
    var currentArea by remember { mutableStateOf("Detecting location...") }
    var customerLatitude by remember { mutableStateOf<Double?>(null) }
    var customerLongitude by remember { mutableStateOf<Double?>(null) }

    var locationRefreshKey by remember { mutableIntStateOf(0) }
    var workerRefreshKey by remember { mutableIntStateOf(0) }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf("Home") }

    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    var bookingError by remember { mutableStateOf<String?>(null) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                locationRefreshKey++
            } else {
                currentArea = "Location unavailable"
                customerLatitude = null
                customerLongitude = null

                Toast.makeText(
                    context,
                    "Location permission is needed to find workers nearby.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Load the signed-in customer's name from Firestore.
    LaunchedEffect(auth.currentUser?.uid) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect

        try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            customerName = document.getString("name")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: "there"
        } catch (_: Exception) {
            customerName = "there"
        }
    }

    // Read the current location, including after permission is granted.
    LaunchedEffect(locationRefreshKey) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            currentArea = "Location unavailable"
            customerLatitude = null
            customerLongitude = null
            return@LaunchedEffect
        }

        currentArea = "Detecting location..."

        try {
            val location = LocationHelper.getCurrentLocation(context)

            if (location != null) {
                customerLatitude = location.latitude
                customerLongitude = location.longitude

                val area = LocationHelper.getLocality(
                    context,
                    location.latitude,
                    location.longitude
                )

                currentArea = area
                    ?.takeIf { it.isNotBlank() }
                    ?: "Location unavailable"
            } else {
                currentArea = "Location unavailable"
            }
        } catch (_: Exception) {
            currentArea = "Location unavailable"
        }
    }

    // Listen to worker profiles in Firestore.
    DisposableEffect(workerRefreshKey) {
        isLoading = true
        loadError = null

        val registration = firestore.collection("workerProfiles")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    loadError = "Couldn't load workers. Please try again."
                    isLoading = false
                    return@addSnapshotListener
                }

                workers = snapshot?.documents
                    ?.mapNotNull { document ->
                        val name = document.getString("name")
                        val role = document.getString("role")
                        val available =
                            document.getBoolean("isAvailable") ?: true

                        if (
                            !name.isNullOrBlank() &&
                            role == "Worker" &&
                            available
                        ) {
                            Worker(
                                uid = document.id,
                                name = name,
                                profession = document.getString("profession")
                                    ?: "Other",
                                serviceArea = document.getString("serviceArea")
                                    ?: ""
                            )
                        } else {
                            null
                        }
                    } ?: emptyList()

                isLoading = false
            }

        onDispose {
            registration.remove()
        }
    }

    val filteredWorkers = workers.filter { worker ->
        val matchesArea =
            currentArea != "Detecting location..." &&
                    currentArea != "Location unavailable" &&
                    worker.serviceArea.isNotBlank() &&
                    worker.serviceArea.equals(currentArea, ignoreCase = true)

        val query = searchText.trim()

        val matchesSearch =
            query.isEmpty() ||
                    worker.name.contains(query, ignoreCase = true) ||
                    worker.profession.contains(query, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    worker.profession.equals(
                        selectedCategory,
                        ignoreCase = true
                    )

        matchesArea && matchesSearch && matchesCategory
    }

    val categories = listOf(
        ServiceCategory("Electrician", "⚡"),
        ServiceCategory("Plumber", "🔧"),
        ServiceCategory("Carpenter", "🪚"),
        ServiceCategory("Cleaner", "🧹")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
            .padding(top = 30.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 12.dp,
                bottom = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Brand and location chip.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SkillBridge",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = HomeNavy
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE0E5EC)),
                        modifier = Modifier.clickable {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 8.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = "Location",
                                tint = HomeNavy,
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = currentArea
                                    .takeIf {
                                        it != "Detecting location..." &&
                                                it != "Location unavailable"
                                    } ?: "Set location",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HomeTeal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Greeting.
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "SkillBridge logo",
                        modifier = Modifier
                            .width(150.dp)
                            .height(75.dp)
                            .align(Alignment.Start)
                            .offset(x = (-40).dp)
                    )


                    Text(
                        text = "Hello, $customerName!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeNavy
                    )

                    Text(
                        text = "How can we help you today?",
                        fontSize = 13.sp,
                        color = Color(0xFF606B7C)
                    )
                }
            }

            // Search.
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFDDE3EB))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⌕",
                            fontSize = 27.sp,
                            color = Color(0xFF42668A)
                        )

                        Spacer(Modifier.width(9.dp))

                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                fontSize = 12.sp,
                                color = HomeNavy
                            ),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (searchText.isEmpty()) {
                                        Text(
                                            text = "What service do you need?",
                                            fontSize = 12.sp,
                                            color = Color(0xFF858D9A)
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        if (searchText.isNotEmpty()) {
                            Text(
                                text = "×",
                                fontSize = 22.sp,
                                color = HomeMuted,
                                modifier = Modifier
                                    .clickable { searchText = "" }
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Category heading.
            item {
                Text(
                    text = "Service Categories",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
            }

            // Category grid. Tapping a selected category clears it.
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        CategoryCard(
                            category = categories[0],
                            selected = selectedCategory == "Electrician",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory =
                                if (selectedCategory == "Electrician") {
                                    "All"
                                } else {
                                    "Electrician"
                                }
                        }

                        CategoryCard(
                            category = categories[1],
                            selected = selectedCategory == "Plumber",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory =
                                if (selectedCategory == "Plumber") {
                                    "All"
                                } else {
                                    "Plumber"
                                }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        CategoryCard(
                            category = categories[3],
                            selected = selectedCategory == "Cleaner",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory =
                                if (selectedCategory == "Cleaner") {
                                    "All"
                                } else {
                                    "Cleaner"
                                }
                        }

                        CategoryCard(
                            category = categories[2],
                            selected = selectedCategory == "Carpenter",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory =
                                if (selectedCategory == "Carpenter") {
                                    "All"
                                } else {
                                    "Carpenter"
                                }
                        }
                    }
                }
            }

            // Available workers heading.
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == "All") {
                            "Available Workers"
                        } else {
                            "Available ${selectedCategory}s"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = HomeNavy
                    )

                    Text(
                        text = "${filteredWorkers.size} found",
                        fontSize = 11.sp,
                        color = HomeMuted
                    )
                }
            }

            when {
                isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = HomeTeal)
                        }
                    }
                }

                loadError != null -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = loadError.orEmpty(),
                                color = MaterialTheme.colorScheme.error
                            )

                            TextButton(
                                onClick = { workerRefreshKey++ }
                            ) {
                                Text("Retry", color = HomeTeal)
                            }
                        }
                    }
                }

                currentArea == "Detecting location..." -> {
                    item {
                        Text(
                            text = "Finding workers near you…",
                            fontSize = 12.sp,
                            color = HomeMuted
                        )
                    }
                }

                currentArea == "Location unavailable" -> {
                    item {
                        Column {
                            Text(
                                text = "Enable location access to find workers serving your area.",
                                fontSize = 12.sp,
                                color = HomeMuted
                            )

                            TextButton(
                                onClick = {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            ) {
                                Text("Enable location", color = HomeTeal)
                            }
                        }
                    }
                }

                filteredWorkers.isEmpty() -> {
                    item {
                        Text(
                            text = when {
                                workers.isEmpty() ->
                                    "No workers registered yet."

                                searchText.isNotBlank() ->
                                    "No matching workers. Try another search."

                                selectedCategory != "All" ->
                                    "No ${selectedCategory.lowercase()}s found nearby."

                                else ->
                                    "No available workers found in your area."
                            },
                            fontSize = 12.sp,
                            color = HomeMuted,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                else -> {
                    items(
                        items = filteredWorkers,
                        key = { it.uid }
                    ) { worker ->
                        WorkerCard(
                            worker = worker,
                            onBookClick = {
                                selectedWorker = worker
                                bookingError = null
                            }
                        )
                    }
                }
            }
        }

        // Bottom navigation.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6F0FA))
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab(
                selectedTab = selectedTab,
                onHomeClick = {
                    selectedTab = "Home"
                },
                onBookingsClick = onBookingsClick,
                onProfileClick = onProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }
    }

    // Booking dialog and existing Firestore booking workflow.
    if (selectedWorker != null) {
        var description by remember(selectedWorker?.uid) {
            mutableStateOf("")
        }

        AlertDialog(
            onDismissRequest = {
                if (!isBooking) {
                    selectedWorker = null
                    bookingError = null
                }
            },
            title = {
                Text("Book ${selectedWorker?.name ?: "Worker"}")
            },
            text = {
                Column {
                    Text(
                        text = "Service: ${selectedWorker?.profession.orEmpty()}"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = {
                            description = it
                            bookingError = null
                        },
                        label = {
                            Text("Describe the service you need")
                        },
                        placeholder = {
                            Text("e.g. Repair a leaking kitchen tap")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        enabled = !isBooking
                    )

                    bookingError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !isBooking && description.isNotBlank(),
                    onClick = {
                        val worker = selectedWorker ?: return@Button
                        val user = auth.currentUser

                        if (user == null) {
                            bookingError = "Please sign in again."
                            return@Button
                        }

                        val customerId = user.uid
                        val workerId = worker.uid

                        if (workerId.isBlank()) {
                            bookingError = "This worker's account ID is missing."
                            return@Button
                        }

                        isBooking = true
                        bookingError = null

                        scope.launch {
                            try {
                                val customerDocument = firestore
                                    .collection("users")
                                    .document(customerId)
                                    .get()
                                    .await()

                                val savedName = customerDocument
                                    .getString("name")
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "Customer"

                                val result = bookingRepository.createBooking(
                                    Booking(
                                        customerId = customerId,
                                        customerName = savedName,
                                        workerId = workerId,
                                        workerName = worker.name,
                                        profession = worker.profession,
                                        description = description.trim(),
                                        customerArea = currentArea,
                                        customerLatitude = customerLatitude,
                                        customerLongitude = customerLongitude
                                    )
                                )

                                result.onSuccess {
                                    Toast.makeText(
                                        context,
                                        "Booking request sent successfully!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    selectedWorker = null
                                    bookingError = null
                                }.onFailure { error ->
                                    bookingError = error.localizedMessage
                                        ?: "Failed to send booking. Please try again."
                                }
                            } catch (error: Exception) {
                                bookingError = error.localizedMessage
                                    ?: "Failed to load customer profile."
                            } finally {
                                isBooking = false
                            }
                        }
                    }
                ) {
                    Text(
                        if (isBooking) "Booking..." else "Confirm Booking"
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isBooking,
                    onClick = {
                        selectedWorker = null
                        bookingError = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
