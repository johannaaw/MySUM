package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sofeng2.R
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the home tab as selected
        bottomNavigation.selectedItemId = R.id.navigation_home

        // Get user name from intent
        val userName = intent.getStringExtra("user_name") ?: "User"
        
        // Update the name in toolbar
        findViewById<TextView>(R.id.userNameText).text = userName

        // Handle View More button click
        findViewById<MaterialButton>(R.id.viewMoreButton).setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }
    }
}