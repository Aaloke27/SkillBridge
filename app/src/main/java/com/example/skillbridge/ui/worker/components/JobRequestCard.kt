package com.example.skillbridge.ui.worker.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.skillbridge.data.Booking
import com.example.skillbridge.ui.worker.WorkerNavy
import com.example.skillbridge.ui.worker.WorkerTeal

@Composable
fun JobRequestCard(
    request: Booking,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current
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
                text = "📍 Service location: ${
                    request.customerArea.ifBlank { "Location unavailable" }
                }",
                color = WorkerTeal,
                fontWeight = FontWeight.SemiBold
            )


            Button(
                onClick = {
                    val latitude = request.customerLatitude
                    val longitude = request.customerLongitude

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
                        } catch (e: android.content.ActivityNotFoundException) {
                            val fallbackUri = Uri.parse(
                                "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
                            )

                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, fallbackUri)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = WorkerTeal
                )
            ) {
                Text("Navigate to Customer")
            }

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