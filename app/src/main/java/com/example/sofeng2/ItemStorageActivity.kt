package com.example.sofeng2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ItemStorageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_storage)

        // Set up bottom navigation
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the items tab as selected
        bottomNavigation.selectedItemId = R.id.navigation_items
    }
} 