package com.example.sofeng2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class OngoingActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OngoingAdapter
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        recyclerView = findViewById(R.id.ongoingRecyclerView)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OngoingAdapter(getSampleOngoingItems())
        recyclerView.adapter = adapter

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the ongoing tab as selected
        bottomNavigation.selectedItemId = R.id.navigation_ongoing
    }

    private fun getSampleOngoingItems(): List<OngoingItem> {
        return listOf(
            OngoingItem(
                "Peminjaman Alat Tulis",
                "Storage 1",
                "2 hari tersisa",
                listOf("Pensil x2", "Penghapus x1")
            ),
            OngoingItem(
                "Peminjaman Buku",
                "Storage 2",
                "5 hari tersisa",
                listOf("Buku Matematika x1", "Buku Fisika x1")
            )
        )
    }
}

data class OngoingItem(
    val title: String,
    val storageName: String,
    val timeLeft: String,
    val items: List<String>
)

class OngoingAdapter(private val items: List<OngoingItem>) : 
    RecyclerView.Adapter<OngoingAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val title: android.widget.TextView = view.findViewById(R.id.ongoingTitle)
        val storage: android.widget.TextView = view.findViewById(R.id.ongoingStorage)
        val timeLeft: android.widget.TextView = view.findViewById(R.id.ongoingTimeLeft)
        val itemsList: android.widget.TextView = view.findViewById(R.id.ongoingItems)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.storage.text = item.storageName
        holder.timeLeft.text = item.timeLeft
        holder.itemsList.text = item.items.joinToString("\n")
    }

    override fun getItemCount() = items.size
} 