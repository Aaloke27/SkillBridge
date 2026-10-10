
package com.example.skillbridge.data

data class Booking(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val profession: String = "",
    val description: String = "",
    val status: String = "Pending",
    val createdAt: Long = 0L
)
