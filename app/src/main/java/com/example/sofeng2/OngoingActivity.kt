package com.example.sofeng2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.data.BorrowManager
import com.example.sofeng2.models.BorrowData
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.*

class OngoingActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OngoingAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

        // Initialize views
        recyclerView = findViewById(R.id.ongoingRecyclerView)
        adapter = OngoingAdapter()
        tabLayout = findViewById(R.id.tabLayout)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup TabLayout
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> adapter.showAllBorrows()
                    1 -> adapter.showActiveBorrows()
                    2 -> adapter.showCompletedBorrows()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Load data
        loadBorrowData()

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the history item as selected
        bottomNavigation.selectedItemId = R.id.navigation_history
    }

    private fun loadBorrowData() {
        val borrows = BorrowManager.getOngoingBorrows()
        adapter.submitList(borrows)
    }

    override fun onResume() {
        super.onResume()
        loadBorrowData()
    }
}

class OngoingAdapter : RecyclerView.Adapter<OngoingAdapter.ViewHolder>() {
    private var allBorrows = listOf<BorrowData>()
    private var currentFilter = Filter.ALL

    enum class Filter {
        ALL, ACTIVE, COMPLETED
    }

    fun submitList(borrows: List<BorrowData>) {
        allBorrows = borrows
        filterBorrows()
    }

    fun showAllBorrows() {
        currentFilter = Filter.ALL
        filterBorrows()
    }

    fun showActiveBorrows() {
        currentFilter = Filter.ACTIVE
        filterBorrows()
    }

    fun showCompletedBorrows() {
        currentFilter = Filter.COMPLETED
        filterBorrows()
    }

    private fun filterBorrows() {
        val currentTime = Date()
        val filteredList = when (currentFilter) {
            Filter.ALL -> allBorrows
            Filter.ACTIVE -> allBorrows.filter { it.returnDate.after(currentTime) }
            Filter.COMPLETED -> allBorrows.filter { it.returnDate.before(currentTime) }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val storageName: android.widget.TextView = view.findViewById(R.id.storageName)
        val timeLeft: android.widget.TextView = view.findViewById(R.id.timeLeft)
        val itemName: android.widget.TextView = view.findViewById(R.id.itemName)
        val itemQuantity: android.widget.TextView = view.findViewById(R.id.itemQuantity)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val borrow = allBorrows[position]
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        // Set storage name (using purpose as storage name for now)
        holder.storageName.text = borrow.purpose

        // Calculate time left
        val currentTime = Date()
        val timeLeft = borrow.returnDate.time - currentTime.time
        val daysLeft = timeLeft / (1000 * 60 * 60 * 24)
        val hoursLeft = (timeLeft % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        holder.timeLeft.text = "${daysLeft}d ${hoursLeft}h left"

        // Set first item details
        if (borrow.items.isNotEmpty()) {
            val firstItem = borrow.items[0]
            holder.itemName.text = firstItem.name
            holder.itemQuantity.text = firstItem.quantity.toString()
        }
    }

    override fun getItemCount() = allBorrows.size
} 