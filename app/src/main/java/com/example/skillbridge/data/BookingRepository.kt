
package com.example.skillbridge.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class BookingRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun createBooking(booking: Booking): Result<String> {
        return try {
            val document = db.collection("bookings").document()

            val bookingWithId = booking.copy(
                id = document.id,
                createdAt = System.currentTimeMillis()
            )

            document.set(bookingWithId).await()

            Result.success(document.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
