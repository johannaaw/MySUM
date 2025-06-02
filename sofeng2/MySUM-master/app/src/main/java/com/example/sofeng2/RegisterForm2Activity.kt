package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import com.example.sofeng2.models.User
import com.example.sofeng2.utils.FirebaseDatabaseHelper
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterForm2Activity : AppCompatActivity() {
    private lateinit var databaseHelper: FirebaseDatabaseHelper
    private val TAG = "RegisterForm2Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_form2)

        FirebaseApp.initializeApp(this)

        databaseHelper = FirebaseDatabaseHelper()

        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<TextInputEditText>(R.id.confirmPasswordInput)
        val registerButton = findViewById<MaterialButton>(R.id.registerButton)

        // Get data from previous form
        val name = intent.getStringExtra("name") ?: ""
        val organization = intent.getStringExtra("organization") ?: ""
        val city = intent.getStringExtra("city") ?: ""

        registerButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate email format
            if (!email.contains("@")) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate password length
            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading state
            registerButton.isEnabled = false
            registerButton.text = "Registering..."

            // Create user object
            val user = User(
                name = name,
                organization = organization,
                city = city,
                email = email,
                password = password
            )

            // Register user
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(TAG, "Starting registration process")
                    val result = databaseHelper.registerUser(user)
                    withContext(Dispatchers.Main) {
                        result.fold(
                            onSuccess = {
                                Log.d(TAG, "Registration successful, navigating to MainActivity")
                                // Registration successful
                                val intent = Intent(this@RegisterForm2Activity, MainActivity::class.java).apply {
                                    putExtra("user_name", name)
                                }
                                startActivity(intent)
                                finish()
                            },
                            onFailure = { exception ->
                                Log.e(TAG, "Registration failed", exception)
                                // Registration failed
                                Toast.makeText(
                                    this@RegisterForm2Activity,
                                    "Registration failed: ${exception.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Reset button state
                                registerButton.isEnabled = true
                                registerButton.text = "Register"
                            }
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Unexpected error during registration", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterForm2Activity,
                            "Error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        // Reset button state
                        registerButton.isEnabled = true
                        registerButton.text = "Register"
                    }
                }
            }
        }

        // Handle login link click
        findViewById<TextView>(R.id.loginLink).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
} 