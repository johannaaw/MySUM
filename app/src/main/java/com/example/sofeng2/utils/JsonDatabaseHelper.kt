package com.example.sofeng2.utils

import android.content.Context
import android.util.Log
import com.example.sofeng2.models.User
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JsonDatabaseHelper(private val context: Context) {
    private val TAG = "JsonDatabaseHelper"
    private val fileName = "users.json"
    private val file: File
        get() = File(context.filesDir, fileName)

    init {
        // Create file if it doesn't exist
        if (!file.exists()) {
            file.writeText("[]")
        }
    }

    suspend fun registerUser(user: User): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting registration for user: ${user.email}")
            
            // Read existing users
            val jsonArray = JSONArray(file.readText())
            
            // Check if email already exists
            for (i in 0 until jsonArray.length()) {
                val existingUser = jsonArray.getJSONObject(i)
                if (existingUser.getString("email") == user.email) {
                    Log.d(TAG, "Email already registered: ${user.email}")
                    return@withContext Result.failure(Exception("Email already registered"))
                }
            }
            
            // Create new user object
            val userJson = JSONObject().apply {
                put("name", user.name)
                put("organization", user.organization)
                put("city", user.city)
                put("email", user.email)
                put("password", user.password)
            }
            
            // Add new user
            jsonArray.put(userJson)
            
            // Save to file
            file.writeText(jsonArray.toString())
            
            Log.d(TAG, "User registration successful for: ${user.email}")
            Result.success(user.email)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed for ${user.email}", e)
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting login for: $email")
            
            // Read users from file
            val jsonArray = JSONArray(file.readText())
            
            // Find user with matching email
            for (i in 0 until jsonArray.length()) {
                val userJson = jsonArray.getJSONObject(i)
                if (userJson.getString("email") == email) {
                    // Check password
                    if (userJson.getString("password") == password) {
                        val user = User(
                            name = userJson.getString("name"),
                            organization = userJson.getString("organization"),
                            city = userJson.getString("city"),
                            email = userJson.getString("email"),
                            password = userJson.getString("password")
                        )
                        Log.d(TAG, "Login successful for: $email")
                        return@withContext Result.success(user)
                    } else {
                        Log.d(TAG, "Invalid password for user: $email")
                        return@withContext Result.failure(Exception("Invalid password"))
                    }
                }
            }
            
            Log.d(TAG, "User not found: $email")
            Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Log.e(TAG, "Login failed for $email", e)
            Result.failure(e)
        }
    }
} 