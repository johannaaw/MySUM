package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sofeng2.R
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.adapters.BorrowingListAdapter
import com.example.sofeng2.models.BorrowingItem
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.imageview.ShapeableImageView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigation: BottomNavigationView
    private val ongoingItems = mutableListOf<BorrowingItem>()
    private lateinit var ongoingAdapter: BorrowingListAdapter
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var borrowListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        val mainOngoingRecyclerView = findViewById<RecyclerView>(R.id.mainOngoingRecyclerView)
        ongoingAdapter = BorrowingListAdapter(ongoingItems) { /* No return action in main */ }
        mainOngoingRecyclerView.layoutManager = LinearLayoutManager(this)
        mainOngoingRecyclerView.adapter = ongoingAdapter

        // Setup Bottom Navigation
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        
        // Set the home tab as selected
        bottomNavigation.selectedItemId = R.id.navigation_home

        // Always fetch user name from database
        val userNameTextView = findViewById<TextView>(R.id.userNameText)
        val profileImageView = findViewById<ShapeableImageView>(R.id.profileImage)
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
            // Fetch ongoing borrowings in real time
            val borrowRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("borrows")
            borrowListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    ongoingItems.clear()
                    for (borrowSnapshot in snapshot.children) {
                        val borrowData = borrowSnapshot.getValue(BorrowingItem::class.java)
                        borrowData?.let {
                            it.id = borrowSnapshot.key ?: ""
                            // Match ongoing tab logic: status == "ongoing" or (fallback) returnDate in future
                            if (it.status == "ongoing" || (it.status.isEmpty() && isOngoing(it.returnDate))) {
                                ongoingItems.add(it)
                            }
                        }
                    }
                    ongoingAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(error: DatabaseError) {}
            }
            borrowRef.addValueEventListener(borrowListener!!)
        } else {
            userNameTextView.text = "User"
        }

        // Set up profile click listener
        profileImageView.setOnClickListener {
            showProfilePopup()
        }

        // Handle View More button click
        findViewById<MaterialButton>(R.id.viewMoreButton).setOnClickListener {
            startActivity(Intent(this, StorageActivity::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firebase listener to prevent memory leaks
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null && borrowListener != null) {
            FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("borrows")
                .removeEventListener(borrowListener!!)
        }
    }

    private fun showProfilePopup() {
        val dialog = ProfilePopupDialog(this)
        dialog.show()
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
}