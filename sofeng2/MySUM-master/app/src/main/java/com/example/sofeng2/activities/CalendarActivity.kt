package com.example.sofeng2.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sofeng2.R
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView

class CalendarActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the calendar tab as selected
        bottomNavigation.selectedItemId = R.id.navigation_calendar
    }
} 