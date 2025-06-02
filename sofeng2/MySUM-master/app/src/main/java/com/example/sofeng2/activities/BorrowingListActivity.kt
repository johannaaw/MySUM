package com.example.sofeng2.activities

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.adapters.BorrowingListAdapter
import com.example.sofeng2.models.BorrowingItem
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class BorrowingListActivity : AppCompatActivity() {
    private lateinit var ongoingRecyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var ongoingAdapter: BorrowingListAdapter
    private lateinit var historyAdapter: BorrowingListAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var emptyStateText: TextView

    private val ongoingItems = mutableListOf<BorrowingItem>()
    private val historyItems = mutableListOf<BorrowingItem>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borrowing_list)

        initializeViews()
        setupRecyclerViews()
        setupTabLayout()
        loadBorrowingData()
    }

    private fun initializeViews() {
        ongoingRecyclerView = findViewById(R.id.ongoingRecyclerView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        tabLayout = findViewById(R.id.tabLayout)
        emptyStateText = findViewById(R.id.emptyStateText)
    }

    private fun setupRecyclerViews() {
        ongoingAdapter = BorrowingListAdapter(ongoingItems)
        historyAdapter = BorrowingListAdapter(historyItems)

        ongoingRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BorrowingListActivity)
            adapter = ongoingAdapter
        }

        historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BorrowingListActivity)
            adapter = historyAdapter
        }
    }

    private fun setupTabLayout() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        ongoingRecyclerView.visibility = View.VISIBLE
                        historyRecyclerView.visibility = View.GONE
                        updateEmptyState(ongoingItems.isEmpty())
                    }
                    1 -> {
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

    private fun loadBorrowingData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val borrowRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("borrows")

        borrowRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ongoingItems.clear()
                historyItems.clear()

                for (borrowSnapshot in snapshot.children) {
                    val borrowData = borrowSnapshot.getValue(BorrowingItem::class.java)
                    borrowData?.let {
                        it.id = borrowSnapshot.key ?: ""
                        if (isOngoing(it.returnDate)) {
                            ongoingItems.add(it)
                        } else {
                            historyItems.add(it)
                        }
                    }
                }

                ongoingAdapter.notifyDataSetChanged()
                historyAdapter.notifyDataSetChanged()
                updateEmptyState(if (tabLayout.selectedTabPosition == 0) ongoingItems.isEmpty() else historyItems.isEmpty())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
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

    private fun updateEmptyState(isEmpty: Boolean) {
        emptyStateText.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
} 