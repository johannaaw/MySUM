package com.example.sofeng2.utils

import android.util.Log
import com.example.sofeng2.models.User
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

class FirebaseDatabaseHelper {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance().apply {
        // Enable offline persistence
        setPersistenceEnabled(true)
    }
    private val usersRef: DatabaseReference = database.getReference("users").apply {
        // Keep the users data synced
        keepSynced(true)
    }
    private val TAG = "FirebaseDatabaseHelper"

    suspend fun registerUser(user: User): Result<String> {
        return try {
            Log.d(TAG, "Starting registration for user: ${user.email}")
            
            // Create new user with email as key
            val userKey = user.email.replace(".", ",") // Firebase doesn't allow . in keys
            Log.d(TAG, "Creating new user with key: $userKey")
            
            // Try to write directly - Firebase will reject if key exists
            try {
                usersRef.child(userKey).setValue(user).await()
                Log.d(TAG, "User registration successful for: ${user.email}")
                Result.success(userKey)
            } catch (e: Exception) {
                // If write fails, check if it's because the key exists
                val snapshot = usersRef.child(userKey).get().await()
                if (snapshot.exists()) {
                    Log.d(TAG, "Email already registered: ${user.email}")
                    Result.failure(Exception("Email already registered"))
                } else {
                    throw e
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed for ${user.email}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            Log.d(TAG, "Attempting login for: $email")
            
            // Use direct path access instead of query
            val userKey = email.replace(".", ",")
            val snapshot = usersRef.child(userKey).get().await()
            
            if (!snapshot.exists()) {
                Log.d(TAG, "User not found: $email")
                return Result.failure(Exception("User not found"))
            }

            val user = snapshot.getValue(User::class.java)
            if (user?.password != password) {
                Log.d(TAG, "Invalid password for user: $email")
                return Result.failure(Exception("Invalid password"))
            }

            Log.d(TAG, "Login successful for: $email")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for $email", e)
            Result.failure(e)
        }
    }
} 