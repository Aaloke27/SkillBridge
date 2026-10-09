package com.example.skillbridge.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HomeNavy = Color(0xFF101D3A)
private val HomeTeal = Color(0xFF12A594)
private val HomeBackground = Color(0xFFF5F7FA)

private data class ServiceCategory(
    val name: String,
    val emoji: String
)

private data class Worker(
    val name: String,
    val profession: String,
    val rating: String,
    val price: String
)

@Composable
fun CustomerHomeScreen(
    onBookingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf("Home") }

    val categories = listOf(
        ServiceCategory("Electrician", "⚡"),
        ServiceCategory("Plumber", "🔧"),
        ServiceCategory("Carpenter", "🪚"),
        ServiceCategory("Cleaner", "🧹")
    )

    val workers = listOf(
        Worker("Rahul Das", "Electrician", "4.8", "₹300"),
        Worker("Amit Roy", "Plumber", "4.7", "₹250"),
        Worker("Suman Pal", "Carpenter", "4.9", "₹400"),
        Worker("Ravi Singh", "Cleaner", "4.6", "₹200")
    )

    val filteredWorkers = workers.filter { worker ->
        val matchesSearch =
            worker.name.contains(searchText, ignoreCase = true) ||
                    worker.profession.contains(searchText, ignoreCase = true)

        val matchesCategory =
            selectedCategory == "All" ||
                    worker.profession == selectedCategory

        matchesSearch && matchesCategory
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 20.sp)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Your location",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Khardah, West Bengal",
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
                                        "Search workers or services",
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            category = categories[0],
                            selected = selectedCategory == "Electrician",
                            modifier = Modifier.weight(1f)
                        ) { selectedCategory = "Electrician" }

                        CategoryCard(
                            category = categories[1],
                            selected = selectedCategory == "Plumber",
                            modifier = Modifier.weight(1f)
                        ) { selectedCategory = "Plumber" }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryCard(
                            category = categories[2],
                            selected = selectedCategory == "Carpenter",
                            modifier = Modifier.weight(1f)
                        ) { selectedCategory = "Carpenter" }

                        CategoryCard(
                            category = categories[3],
                            selected = selectedCategory == "Cleaner",
                            modifier = Modifier.weight(1f)
                        ) { selectedCategory = "Cleaner" }
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
                    text = "Available workers",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
            }

            if (filteredWorkers.isEmpty()) {
                item {
                    Text(
                        text = "No matching workers found.",
                        color = Color.Gray
                    )
                }
            } else {
                items(filteredWorkers) { worker ->
                    WorkerCard(worker = worker)
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
            androidx.compose.foundation.BorderStroke(1.dp, HomeTeal)
        } else null
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
private fun WorkerCard(worker: Worker) {
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
                    text = worker.name.first().toString(),
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 14.dp
                    ),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = HomeTeal
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    worker.name,
                    fontWeight = FontWeight.Bold,
                    color = HomeNavy
                )
                Text(
                    worker.profession,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    "⭐ ${worker.rating}  •  From ${worker.price}",
                    color = HomeNavy,
                    fontSize = 12.sp
                )
            }

            Text(
                text = "View",
                color = HomeTeal,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
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
