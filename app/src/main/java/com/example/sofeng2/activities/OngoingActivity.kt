package com.example.sofeng2.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.adapters.OngoingAdapter
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView

class OngoingActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OngoingAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

        // Initialize views
        recyclerView = findViewById(R.id.ongoingRecyclerView)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupRecyclerView()
        setupBottomNavigation()
    }

    private fun setupRecyclerView() {
        adapter = OngoingAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@OngoingActivity)
            adapter = this@OngoingActivity.adapter
        }

        // Add sample data
        val items = listOf(
            OngoingItem("Storage 1", "2 days left", "Penghapus, Pensil"),
            OngoingItem("Storage 2", "5 days left", "Buku, Pulpen"),
            OngoingItem("Storage 3", "1 day left", "Stapler, Kertas")
        )
        adapter.submitList(items)
    }

    private fun setupBottomNavigation() {
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_ongoing
    }
}

data class OngoingItem(
    val storageName: String,
    val timeLeft: String,
    val items: String
) 