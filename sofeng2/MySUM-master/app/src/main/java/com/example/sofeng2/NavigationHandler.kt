package com.example.sofeng2

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView

object NavigationHandler {
    fun setupBottomNavigation(activity: Activity, bottomNavigation: BottomNavigationView) {
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