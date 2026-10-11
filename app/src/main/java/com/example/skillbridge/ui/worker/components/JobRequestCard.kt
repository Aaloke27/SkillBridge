
package com.example.skillbridge.ui.worker.components

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.data.Booking
import com.example.skillbridge.ui.worker.WorkerNavy
import com.example.skillbridge.ui.worker.WorkerTeal

private val WorkerReject = Color(0xFFFF666F)
private val WorkerMuted = Color(0xFF858D9A)

@Composable
fun JobRequestCard(
    request: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = Color(0xFFF0F3FA),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Customer",
                    tint = Color(0xFF45658F),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Customer and request details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = request.customerName.ifBlank {
                        "Customer"
                    },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = WorkerNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = request.profession,
                    fontSize = 12.sp,
                    color = WorkerNavy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Request: ${request.description.ifBlank { "Service requested" }}",
                    fontSize = 12.sp,
                    color = WorkerMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Compact Accept / Reject buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier
                        .width(60.dp)
                        .height(25.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 3.dp,
                        vertical = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB9F0E8),
                        contentColor = WorkerTeal
                    )
                ) {
                    Text(
                        text = "Accept",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = onReject,
                    modifier = Modifier
                        .width(60.dp)
                        .height(25.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 3.dp,
                        vertical = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFC9CC),
                        contentColor = Color(0xFFDE3845)
                    )
                ) {
                    Text(
                        text = "Reject",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(3.dp))

            // Navigate to customer's saved GPS coordinates
            IconButton(
                onClick = {
                    val latitude = request.customerLatitude
                    val longitude = request.customerLongitude

                    if (latitude == null || longitude == null) {
                        Toast.makeText(
                            context,
                            "Customer GPS location is unavailable",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        val navigationUri = Uri.parse(
                            "google.navigation:q=$latitude,$longitude"
                        )

                        val googleMapsIntent = Intent(
                            Intent.ACTION_VIEW,
                            navigationUri
                        ).setPackage("com.google.android.apps.maps")

                        try {
                            context.startActivity(googleMapsIntent)
                        } catch (e: ActivityNotFoundException) {
                            val fallbackUri = Uri.parse(
                                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                            )

                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, fallbackUri)
                            )
                        }
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Navigate to customer",
                    tint = WorkerMuted,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    }
}
