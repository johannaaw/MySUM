package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import com.example.sofeng2.utils.FirebaseDatabaseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var databaseHelper: FirebaseDatabaseHelper
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        databaseHelper = FirebaseDatabaseHelper()

        val emailInput = findViewById<TextInputEditText>(R.id.emailPhoneInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            loginButton.isEnabled = false
            loginButton.text = "Logging in..."

            // Attempt login
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Starting login process")
                    val result = databaseHelper.loginUser(email, password)
                    withContext(Dispatchers.Main) {
                        result.fold(
                            onSuccess = { user ->
                                Log.d(TAG, "Login successful, navigating to MainActivity")
                                // Login successful
                                val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                                    putExtra("user_name", user.name)
                                }
                                startActivity(intent)
                                finish()
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Login failed", exception)
                                // Login failed
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login failed: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Reset button state
                                loginButton.isEnabled = true
                                loginButton.text = "Login"
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error during login", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Reset button state
                        loginButton.isEnabled = true
                        loginButton.text = "Login"
                    }
                }
            }
        }

        // Handle register link click
        findViewById<TextView>(R.id.signUpLink).setOnClickListener {
            startActivity(Intent(this, RegisterForm1Activity::class.java))
            finish()
        }
    }
} 