package com.example.sofeng2.utils

import android.util.Log
import com.example.sofeng2.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseHelper {
    private val TAG = "FirebaseDatabaseHelper"
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun registerUser(user: User): Result<String> {
        return try {
            Log.d(TAG, "Registering user: ${user.email}")

            // Firebase Authentication
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID is null")

            // Store user data (except password)
            val userMap = mapOf(
                "name" to user.name,
                "organization" to user.organization,
                "city" to user.city,
                "email" to user.email
            )

            database.child("users").child(uid).setValue(userMap).await()

            Log.d(TAG, "Registration successful for: ${user.email}")
            Result.success(user.email)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed: ${user.email}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Logging in: $email")

            // Firebase Auth
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("UID is null")

            // Get data from Firebase DB
            val snapshot = database.child("users").child(uid).get().await()
            if (snapshot.exists()) {
                val user = User(
                    name = snapshot.child("name").value.toString(),
                    organization = snapshot.child("organization").value.toString(),
                    city = snapshot.child("city").value.toString(),
                    email = snapshot.child("email").value.toString(),
                    password = password // for local use only
                )
                Log.d(TAG, "Login successful for: $email")
                Result.success(user)
            } else {
                Log.d(TAG, "User data not found for: $email")
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for: $email", e)
            Result.failure(e)
        }
    }
}
