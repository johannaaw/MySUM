package com.example.sofeng2

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.adapters.BorrowingListAdapter
import com.example.sofeng2.models.BorrowingItem
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class OngoingActivity : AppCompatActivity() {
    private lateinit var pendingRecyclerView: RecyclerView
    private lateinit var ongoingRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var pendingAdapter: BorrowingListAdapter
    private lateinit var ongoingAdapter: BorrowingListAdapter
    private lateinit var historyAdapter: BorrowingListAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var emptyStateText: TextView
    private lateinit var bottomNavigation: BottomNavigationView

    private val pendingItems = mutableListOf<BorrowingItem>()
    private val ongoingItems = mutableListOf<BorrowingItem>()
    private val historyItems = mutableListOf<BorrowingItem>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ongoing)

        try {
            initializeViews()
            setupRecyclerViews()
            setupTabLayout()
            setupBottomNavigation()
            loadBorrowingData()
            loadUserName()
        } catch (e: Exception) {
            Toast.makeText(this, "Error initializing: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun initializeViews() {
        pendingRecyclerView = findViewById(R.id.pendingRecyclerView)
        ongoingRecyclerView = findViewById(R.id.ongoingRecyclerView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        emptyStateText = findViewById(R.id.emptyStateText)
        bottomNavigation = findViewById(R.id.bottomNavigation)
    }

    private fun setupRecyclerViews() {
        pendingAdapter = BorrowingListAdapter(pendingItems) { item ->
            // Handle return action for pending items
            updateBorrowingStatus(item.id, "ongoing")
        }
        ongoingAdapter = BorrowingListAdapter(ongoingItems) { item ->
            // Handle return action for ongoing items
            updateBorrowingStatus(item.id, "history")
        }
        historyAdapter = BorrowingListAdapter(historyItems) { item ->
            // No action for history items
        }

        pendingRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OngoingActivity)
            adapter = pendingAdapter
        }

        ongoingRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OngoingActivity)
            adapter = ongoingAdapter
        }

        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OngoingActivity)
            adapter = historyAdapter
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        pendingRecyclerView.visibility = View.VISIBLE
                        ongoingRecyclerView.visibility = View.GONE
                        historyRecyclerView.visibility = View.GONE
                        updateEmptyState(pendingItems.isEmpty())
                    }
                    1 -> {
                        pendingRecyclerView.visibility = View.GONE
                        ongoingRecyclerView.visibility = View.VISIBLE
                        historyRecyclerView.visibility = View.GONE
                        updateEmptyState(ongoingItems.isEmpty())
                    }
                    2 -> {
                        pendingRecyclerView.visibility = View.GONE
                        ongoingRecyclerView.visibility = View.GONE
                        historyRecyclerView.visibility = View.VISIBLE
                        updateEmptyState(historyItems.isEmpty())
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBottomNavigation() {
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_ongoing
    }

    private fun loadBorrowingData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val borrowRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("borrows")

        borrowRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    pendingItems.clear()
                    ongoingItems.clear()
                    historyItems.clear()

                    for (borrowSnapshot in snapshot.children) {
                        val borrowData = borrowSnapshot.getValue(BorrowingItem::class.java)
                        borrowData?.let {
                            it.id = borrowSnapshot.key ?: ""
                            when (it.status) {
                                "pending" -> pendingItems.add(it)
                                "ongoing" -> ongoingItems.add(it)
                                "history", "returned" -> historyItems.add(it)
                                else -> {
                                    // Fallback to old logic if status is missing
                                    when {
                                        isPending(it.borrowDate) -> pendingItems.add(it)
                                        isOngoing(it.returnDate) -> ongoingItems.add(it)
                                        else -> historyItems.add(it)
                                    }
                                }
                            }
                        }
                    }

                    pendingAdapter.notifyDataSetChanged()
                    ongoingAdapter.notifyDataSetChanged()
                    historyAdapter.notifyDataSetChanged()
                    updateEmptyState(when (tabLayout.selectedTabPosition) {
                        0 -> pendingItems.isEmpty()
                        1 -> ongoingItems.isEmpty()
                        else -> historyItems.isEmpty()
                    })
                } catch (e: Exception) {
                    Toast.makeText(this@OngoingActivity, "Error loading data: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OngoingActivity, "Database error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun isPending(borrowDateStr: String): Boolean {
        return try {
            val borrowDate = dateFormat.parse(borrowDateStr)
            val currentDate = Calendar.getInstance().time
            borrowDate?.after(currentDate) ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun isOngoing(returnDateStr: String): Boolean {
        return try {
            val returnDate = dateFormat.parse(returnDateStr)
            val currentDate = Calendar.getInstance().time
            returnDate?.after(currentDate) ?: false
        } catch (e: Exception) {
            false
        }
    }

    private fun updateBorrowingStatus(borrowId: String, newStatus: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val borrowRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId)
            .child("borrows")
            .child(borrowId)

        // First get the borrowing item to know what items to return
        borrowRef.get().addOnSuccessListener { snapshot ->
            val borrowingItem = snapshot.getValue(BorrowingItem::class.java)
            if (borrowingItem != null) {
                // If the new status is "history" or "returned", we need to return the items
                if (newStatus == "history" || newStatus == "returned") {
                    val storageRef = FirebaseDatabase.getInstance().getReference("storage")
                    
                    // For each borrowed item, update its quantity in storage
                    for (item in borrowingItem.items) {
                        storageRef.get().addOnSuccessListener { storageSnapshot ->
                            // Search through all storage locations
                            for (storageSlot in storageSnapshot.children) {
                                for (storageItem in storageSlot.children) {
                                    val name = storageItem.child("name").getValue(String::class.java)
                                    if (name == item.name) {
                                        val currentQty = storageItem.child("quantity").getValue(Int::class.java) ?: 0
                                        val newQty = currentQty + item.quantity
                                        storageItem.ref.child("quantity").setValue(newQty)
                                    }
                                }
                            }
                        }
                    }
                }

                // Update the status
                borrowRef.child("status").setValue(newStatus)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Items returned successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to get borrowing details: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun loadUserName() {
        val userNameTextView = findViewById<TextView>(R.id.userNameText)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.getValue(String::class.java)
                    userNameTextView.text = name ?: "User"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        } else {
            userNameTextView.text = "User"
        }
    }
} 