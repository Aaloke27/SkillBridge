
package com.example.skillbridge.ui.customer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val HomeTeal = Color(0xFF12A594)
private val InactiveGray = Color(0xFF858D9A)

@Composable
fun BottomTab(
    selectedTab: String,
    onHomeClick: () -> Unit,
    onBookingsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color(0xFFF6F0FA)
    ) {
        NavigationBarItem(
            selected = selectedTab == "Home",
            onClick = onHomeClick,
            icon = {
                Icon(Icons.Filled.Home, contentDescription = "Home")
            },
            label = { Text("Home") },
            colors = navColors()
        )

        NavigationBarItem(
            selected = selectedTab == "Bookings",
            onClick = onBookingsClick,
            icon = {
                Icon(Icons.Filled.ReceiptLong, contentDescription = "Bookings")
            },
            label = { Text("Bookings") },
            colors = navColors()
        )

        NavigationBarItem(
            selected = selectedTab == "Profile",
            onClick = onProfileClick,
            icon = {
                Icon(Icons.Filled.Person, contentDescription = "Profile")
            },
            label = { Text("Profile") },
            colors = navColors()
        )
    }
}

@Composable
private fun navColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = HomeTeal,
    selectedTextColor = HomeTeal,
    indicatorColor = Color(0xFFD7F3EE),
    unselectedIconColor = InactiveGray,
    unselectedTextColor = InactiveGray
)
