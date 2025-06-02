package com.example.sofeng2

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.adapters.CartAdapter
import com.example.sofeng2.adapters.StorageItemAdapter
import com.example.sofeng2.models.CartItem
import com.example.sofeng2.models.StorageItem
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*

class StorageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StorageItemAdapter
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartPopup: View
    private lateinit var cartCheckOutButton: View
    private lateinit var totalItemsText: TextView
    private val cartItems = mutableListOf<CartItem>()
    private var currentStorageIndex: Int = 1
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        database = FirebaseDatabase.getInstance().getReference("storage")

        recyclerView = findViewById(R.id.itemsRecyclerView)
        cartPopup = findViewById(R.id.cartPopup)
        cartRecyclerView = cartPopup.findViewById(R.id.cartRecyclerView)
        cartCheckOutButton = cartPopup.findViewById(R.id.checkoutButton)
        totalItemsText = cartPopup.findViewById(R.id.totalItemsText)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupRecyclerViews()
        setupBottomNavigation()
        setupCartButton()
        setupTabLayout()

        loadItemsFromFirebase(currentStorageIndex)
    }

    private fun setupRecyclerViews() {
        adapter = StorageItemAdapter { item ->
            showQuantityDialog(item)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        cartAdapter = CartAdapter { item ->
            cartItems.remove(item)
            cartAdapter.submitList(cartItems.toList()) // submit a new list to trigger UI update
            updateCartUI()
        }
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter

        cartCheckOutButton.setOnClickListener {
            if (cartItems.isNotEmpty()) {
                val cartNames = ArrayList(cartItems.map { it.name })
                val cartQuantities = ArrayList(cartItems.map { it.quantity })
                val intent = Intent(this, CheckoutActivity::class.java).apply {
                    putStringArrayListExtra("cartNames", cartNames)
                    putIntegerArrayListExtra("cartQuantities", cartQuantities)
                }
                startActivity(intent)
                cartPopup.visibility = View.GONE
            }
        }
    }

    private fun setupBottomNavigation() {
        NavigationHandler.setupBottomNavigation(this, bottomNavigation)
        bottomNavigation.selectedItemId = R.id.navigation_items
    }

    private fun setupCartButton() {
        findViewById<MaterialButton>(R.id.cartButton).setOnClickListener {
            cartPopup.visibility = View.VISIBLE
        }
    }

    private fun setupTabLayout() {
        val tabLayout = findViewById<TabLayout>(R.id.storageTabs)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStorageIndex = when (tab?.position) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    else -> 1
                }
                loadItemsFromFirebase(currentStorageIndex)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadItemsFromFirebase(storageIndex: Int) {
        database.child(storageIndex.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<StorageItem>()
                for (itemSnapshot in snapshot.children) {
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                    val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    itemList.add(StorageItem(name, "", quantity, R.drawable.ic_items))//TODO:Ganti
                }
                adapter.submitList(itemList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StorageActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showQuantityDialog(item: StorageItem) {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        dialogView.findViewById<TextView>(R.id.itemNameText).text = item.name
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.quantityInput)
        dialog.setContentView(dialogView)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<MaterialButton>(R.id.addButton).setOnClickListener {
            val quantity = quantityInput.text.toString().toIntOrNull() ?: 0
            if (quantity > 0) {
                addToCart(item, quantity, "Storage $currentStorageIndex")
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun addToCart(item: StorageItem, quantity: Int, storageName: String) {
        val existingItem = cartItems.find { it.name == item.name && it.storageName == storageName }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(item.name, item.brand, quantity, item.imageResId, storageName))
        }
        updateCartUI()
    }

    private fun updateCartUI() {
        cartAdapter.submitList(cartItems.toList()) // Always submit a new list copy
        totalItemsText.text = cartItems.sumOf { it.quantity }.toString()
        cartPopup.visibility = if (cartItems.isNotEmpty()) View.VISIBLE else View.GONE
    }
}