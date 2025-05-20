package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailPhoneInput = findViewById<TextInputEditText>(R.id.emailPhoneInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)

        loginButton.setOnClickListener {
            val emailPhone = emailPhoneInput.text.toString()
            val password = passwordInput.text.toString()

            if (emailPhone.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Implement actual login logic here
            // For now, just navigate to main activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Handle sign up link click
        findViewById<TextView>(R.id.signUpLink).setOnClickListener {
            startActivity(Intent(this, RegisterForm1Activity::class.java))
            finish()
        }
    }
} 