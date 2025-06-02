package com.example.sofeng2.utils

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.sofeng2.R
import com.example.sofeng2.MainActivity
import com.example.sofeng2.StorageActivity
import com.example.sofeng2.OngoingActivity
import com.example.sofeng2.CalendarActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHandler {
    fun setupBottomNavigation(activity: AppCompatActivity, bottomNavigation: BottomNavigationView) {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    if (activity !is MainActivity) {
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.navigation_items -> {
                    if (activity !is StorageActivity) {
                        activity.startActivity(Intent(activity, StorageActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.navigation_calendar -> {
                    if (activity !is CalendarActivity) {
                        activity.startActivity(Intent(activity, CalendarActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                R.id.navigation_ongoing -> {
                    if (activity !is OngoingActivity) {
                        activity.startActivity(Intent(activity, OngoingActivity::class.java))
                        activity.finish()
                    }
                    true
                }
                else -> false
            }
        }
    }
} 