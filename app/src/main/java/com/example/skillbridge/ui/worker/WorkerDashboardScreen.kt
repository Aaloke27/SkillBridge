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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val WorkerNavy = Color(0xFF101D3A)
private val WorkerTeal = Color(0xFF12A594)
private val WorkerBackground = Color(0xFFF4F7FB)

private data class JobRequest(
    val id: Int,
    val customer: String,
    val service: String,
    val location: String,
    val price: String
)

@Composable
fun WorkerDashboardScreen() {
    var isAvailable by remember { mutableStateOf(true) }

    val requests = remember {
        mutableStateListOf(
            JobRequest(
                1, "Rahul Sharma", "Electrician",
                "Khardah", "₹500"
            ),
            JobRequest(
                2, "Priya Das", "Fan Repair",
                "Barrackpore", "₹300"
            ),
            JobRequest(
                3, "Amit Roy", "Wiring Repair",
                "Titagarh", "₹700"
            )
        )
    }

    var acceptedJobs by remember { mutableStateOf(0) }
    var completedJobs by remember { mutableStateOf(5) }
    var message by remember { mutableStateOf("") }

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
                            "You're available for work"
                        } else {
                            "You're currently unavailable"
                        },
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Switch(
                    checked = isAvailable,
                    onCheckedChange = {
                        isAvailable = it
                        message = if (it) {
                            "You are now available."
                        } else {
                            "You are now unavailable."
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
                            requests.remove(request)
                            acceptedJobs++
                            message = "Request accepted: ${request.service}"
                        },
                        onReject = {
                            requests.remove(request)
                            message = "Request declined."
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
    request: JobRequest,
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
                text = request.service,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WorkerNavy
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Customer: ${request.customer}",
                color = Color.DarkGray
            )

            Text(
                text = "Location: ${request.location}",
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = "Estimated payment: ${request.price}",
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
