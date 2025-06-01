package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up bottom navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the home item as selected
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