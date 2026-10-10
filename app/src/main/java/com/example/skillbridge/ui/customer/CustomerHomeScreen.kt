
package com.example.skillbridge.ui.customer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.data.Booking
import com.example.skillbridge.data.BookingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext


private val HomeNavy = Color(0xFF101D3A)
private val HomeTeal = Color(0xFF12A594)
private val HomeBackground = Color(0xFFF5F7FA)

private data class ServiceCategory(
    val name: String,
    val emoji: String
)


private data class Worker(
    val uid: String,
    val name: String,
    val profession: String,
    val serviceArea: String
)


@Composable
fun CustomerHomeScreen(
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {

    val context = LocalContext.current

    var currentArea by remember {
        mutableStateOf("Detecting location...")
    }

    var customerLatitude by remember {
        mutableStateOf<Double?>(null)
    }

    var customerLongitude by remember {
        mutableStateOf<Double?>(null)
    }


    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) {


        }


    LaunchedEffect(Unit) {
        val fineGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            val location = com.example.skillbridge.location.LocationHelper
                .getCurrentLocation(context)


            if (location != null) {
                // Save the GPS coordinates
                customerLatitude = location.latitude
                customerLongitude = location.longitude

                // Keep existing locality detection
                val area = com.example.skillbridge.location.LocationHelper
                    .getLocality(
                        context,
                        location.latitude,
                        location.longitude
                    )

                currentArea = area ?: "Location unavailable"
            } else {
                currentArea = "Location unavailable"
            }

        }
    }


    var selectedWorker by remember { mutableStateOf<Worker?>(null) }
    var isBooking by remember { mutableStateOf(false) }
    var bookingError by remember { mutableStateOf<String?>(null) }

    val bookingRepository = remember { BookingRepository() }
    val auth = remember { FirebaseAuth.getInstance() }
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf("Home") }

    val categories = listOf(
        ServiceCategory("Electrician", "⚡"),
        ServiceCategory("Plumber", "🔧"),
        ServiceCategory("Carpenter", "🪚"),
        ServiceCategory("Cleaner", "🧹")
    )

    var workers by remember { mutableStateOf<List<Worker>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }


    DisposableEffect(Unit) {
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
                        val isAvailable =
                            document.getBoolean("isAvailable") ?: true

                        if (name != null &&
                            role == "Worker" &&
                            isAvailable
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


    // Search by name or profession and location, then apply the selected category.

    val filteredWorkers = workers.filter { worker ->
        val matchesArea =
            currentArea != "Detecting location..." &&
                    currentArea != "Location unavailable" &&
                    worker.serviceArea.isNotBlank() &&
                    worker.serviceArea.equals(currentArea, ignoreCase = true)

        val matchesSearch =
            worker.name.contains(searchText.trim(), ignoreCase = true) ||
                    worker.profession.contains(searchText.trim(), ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    worker.profession.equals(selectedCategory, ignoreCase = true)

        matchesArea && matchesSearch && matchesCategory
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HomeBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 28.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hello there 👋",
                            fontSize = 15.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Find your skilled worker",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomeNavy
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White
                    ) {
                        Text(
                            text = "SB",
                            modifier = Modifier.padding(
                                horizontal = 14.dp,
                                vertical = 12.dp
                            ),
                            fontWeight = FontWeight.Bold,
                            color = HomeTeal
                        )
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "Your location",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = currentArea,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeNavy
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 14.dp,
                            vertical = 16.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍")
                        Spacer(Modifier.width(10.dp))

                        BasicTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchText.isEmpty()) {
                                    Text(
                                        text = "Search workers or services",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Service categories",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
            }

            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            category = categories[0],
                            selected = selectedCategory == "Electrician",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = "Electrician"
                        }

                        CategoryCard(
                            category = categories[1],
                            selected = selectedCategory == "Plumber",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = "Plumber"
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            category = categories[2],
                            selected = selectedCategory == "Carpenter",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = "Carpenter"
                        }

                        CategoryCard(
                            category = categories[3],
                            selected = selectedCategory == "Cleaner",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedCategory = "Cleaner"
                        }
                    }

                    TextButton(
                        onClick = { selectedCategory = "All" }
                    ) {
                        Text("Show all services", color = HomeTeal)
                    }
                }
            }

            item {
                Text(
                    text = if (selectedCategory == "All") {
                        "Available workers"
                    } else {
                        "Available ${selectedCategory.lowercase()}s"
                    },
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
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
                        Column {
                            Text(
                                text = loadError!!,
                                color = MaterialTheme.colorScheme.error
                            )
                            TextButton(onClick = {
                                loadError = null
                            }) {
                                Text("Retry", color = HomeTeal)
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

                                selectedCategory != "All" ->
                                    "No ${selectedCategory.lowercase()}s found. Try another category."

                                searchText.isNotBlank() ->
                                    "No workers match your search."

                                else ->
                                    "No matching workers found."
                            },
                            color = Color.Gray
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomTab(
                emoji = "⌂",
                label = "Home",
                selected = selectedTab == "Home"
            ) {
                selectedTab = "Home"
                selectedCategory = "All"
                searchText = ""
            }

            BottomTab(
                emoji = "▣",
                label = "Bookings",
                selected = selectedTab == "Bookings"
            ) {
                selectedTab = "Bookings"
                onBookingsClick()
            }

            BottomTab(
                emoji = "●",
                label = "Profile",
                selected = selectedTab == "Profile"
            ) {
                selectedTab = "Profile"
                onProfileClick()
            }
        }
    }

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
                        label = { Text("Describe the service you need") },
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

                        isBooking = true
                        bookingError = null

                        val customerId = user.uid
                        val workerId = worker.uid

                        if (workerId.isBlank()) {
                            bookingError = "This worker's account ID is missing."
                            isBooking = false
                            return@Button
                        }


                        scope.launch {
                            try {
                                // Read the customer's saved profile
                                val customerDocument = FirebaseFirestore
                                    .getInstance()
                                    .collection("users")
                                    .document(customerId)
                                    .get()
                                    .await()

                                val customerName = customerDocument
                                    .getString("name")
                                    ?.takeIf { it.isNotBlank() }
                                    ?: "Customer"


                                val result = bookingRepository.createBooking(
                                    Booking(
                                        customerId = customerId,
                                        customerName = customerName,
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
                    Text(if (isBooking) "Booking..." else "Confirm Booking")
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

@Composable
private fun CategoryCard(
    category: ServiceCategory,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) Color(0xFFD9F7F0) else Color.White,
        border = if (selected) {
            BorderStroke(1.dp, HomeTeal)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(category.emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))

            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = HomeNavy
            )
        }
    }
}


@Composable
private fun WorkerCard(
    worker: Worker,
    onBookClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFD9F7F0)
            ) {
                Text(
                    text = worker.name.firstOrNull()?.uppercase() ?: "?",
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeTeal
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = worker.name,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )

                Text(
                    text = worker.profession,
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onBookClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeTeal
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 14.dp,
                        vertical = 6.dp
                    )
                ) {
                    Text("Book Now")
                }
            }
        }
    }
}


@Composable
private fun BottomTab(
    emoji: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            color = if (selected) HomeTeal else Color.Gray,
            fontSize = 22.sp
        )

        Text(
            text = label,
            color = if (selected) HomeTeal else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
