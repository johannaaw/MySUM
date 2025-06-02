package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Make the entire screen clickable
        findViewById<View>(android.R.id.content).setOnClickListener {
            startActivity(Intent(this, RegisterForm1Activity::class.java))
            finish()
        }
    }
} 