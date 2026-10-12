
package com.example.skillbridge.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createBooking(booking: Booking): Result<String> {
        val bookingRef = db.collection("bookings").document()
        val locationRef = db.collection("bookingLocations")
            .document(bookingRef.id)

        return try {
            val createdAt = System.currentTimeMillis()

            // Save booking details WITHOUT GPS coordinates.
            val bookingData = hashMapOf<String, Any>(
                "id" to bookingRef.id,
                "customerId" to booking.customerId,
                "customerName" to booking.customerName,
                "workerId" to booking.workerId,
                "workerName" to booking.workerName,
                "profession" to booking.profession,
                "description" to booking.description,
                "status" to "Pending",
                "createdAt" to createdAt,
                "customerArea" to booking.customerArea
            )

            // Store GPS coordinates in a separate document.
            val locationData = hashMapOf<String, Any>(
                "bookingId" to bookingRef.id,
                "customerId" to booking.customerId,
                "workerId" to booking.workerId
            )

            val latitude = booking.customerLatitude
            val longitude = booking.customerLongitude

            // Save coordinates only when BOTH are available.
            if (latitude != null && longitude != null) {
                locationData["customerLatitude"] = latitude
                locationData["customerLongitude"] = longitude
            }

            // Both documents are created atomically.
            val batch = db.batch()
            batch.set(bookingRef, bookingData)
            batch.set(locationRef, locationData)

            batch.commit().await()

            Result.success(bookingRef.id)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
