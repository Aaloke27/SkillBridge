
package com.example.skillbridge.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val validProfessions = listOf(
        "Electrician",
        "Plumber",
        "Carpenter",
        "Cleaner",
        "Other"
    )

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        profession: String = ""
    ): Result<String> {
        return try {
            require(role == "Customer" || role == "Worker") {
                "Please select a valid role."
            }

            val selectedProfession = profession.trim()

            if (role == "Worker") {
                require(selectedProfession in validProfessions) {
                    "Please select a valid profession."
                }
            }

            val authResult = auth
                .createUserWithEmailAndPassword(email.trim(), password)
                .await()

            val user = authResult.user
                ?: return Result.failure(
                    Exception(
                        "Account created, but user details are unavailable."
                    )
                )

            val userId = user.uid

            val profile = hashMapOf(
                "uid" to userId,
                "name" to name.trim(),
                "email" to email.trim(),
                "role" to role,
                "createdAt" to Timestamp.now()
            )

            val batch = firestore.batch()

            // Save the private user profile.
            batch.set(
                firestore.collection("users").document(userId),
                profile
            )

            // Save the public worker profile, including profession.
            if (role == "Worker") {
                val publicProfile = hashMapOf(
                    "uid" to userId,
                    "name" to name.trim(),
                    "role" to role,
                    "profession" to selectedProfession
                )

                batch.set(
                    firestore.collection("workerProfiles")
                        .document(userId),
                    publicProfile
                )
            }

            // Save both documents together.
            batch.commit().await()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<String> {
        return try {
            val result = auth
                .signInWithEmailAndPassword(email.trim(), password)
                .await()

            val userId = result.user?.uid
                ?: return Result.failure(
                    Exception(
                        "Login succeeded, but user ID is unavailable."
                    )
                )

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserRole(uid: String): Result<String> {
        return try {
            val document = firestore.collection("users")
                .document(uid)
                .get()
                .await()

            val role = document.getString("role")
                ?: throw Exception("User role was not found.")

            if (role != "Customer" && role != "Worker") {
                throw Exception("Invalid user role.")
            }

            Result.success(role)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
