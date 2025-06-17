package com.example.sofeng2

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
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
import com.google.firebase.auth.FirebaseAuth
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

        // Load user name from Firebase
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
        database.child(storageIndex.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<StorageItem>()
                for (itemSnapshot in snapshot.children) {
                    // Get the item key (item1, item2, etc.)
                    val itemKey = itemSnapshot.key ?: ""
                    
                    // Get the name and quantity from the item's data
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: itemKey
                    val quantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    val brand = itemSnapshot.child("brand").getValue(String::class.java) ?: ""
                    
                    // Create StorageItem with the correct name and quantity
                    itemList.add(StorageItem(name, brand, quantity, R.drawable.ic_items))
                }
                adapter.submitList(itemList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@StorageActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showQuantityDialog(item: StorageItem) {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        
        dialogView.findViewById<TextView>(R.id.itemNameText).text = item.name
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.quantityInput)
        val availableQuantityText = dialogView.findViewById<TextView>(R.id.availableQuantityText)
        val addButton = dialogView.findViewById<MaterialButton>(R.id.addButton)
        
        // Initially disable the add button
        addButton.isEnabled = false
        
        // Get the current storage reference
        val storageRef = database.child(currentStorageIndex.toString())
        
        // Set the available quantity text
        availableQuantityText.text = "Available: ${item.quantity} items"
        
        // Set input type and max length
        quantityInput.inputType = InputType.TYPE_CLASS_NUMBER
        
        // Add input filter to prevent entering numbers larger than available quantity
        quantityInput.filters = arrayOf(
            InputFilter.LengthFilter(item.quantity.toString().length),
            InputFilter { source, start, end, dest, dstart, dend ->
                val input = dest.subSequence(0, dstart).toString() + source.subSequence(start, end) + dest.subSequence(dend, dest.length)
                if (input.isEmpty()) return@InputFilter null
                try {
                    val value = input.toInt()
                    if (value <= item.quantity) null else ""
                } catch (e: NumberFormatException) {
                    ""
                }
            }
        )
        
        // Add text change listener to validate input
        quantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val inputText = s.toString()
                if (inputText.isNotEmpty()) {
                    val quantity = inputText.toIntOrNull() ?: 0
                    if (quantity > item.quantity) {
                        quantityInput.error = "The items are not enough. Please select the appropriate amount"
                        addButton.isEnabled = false
                    } else if (quantity <= 0) {
                        quantityInput.error = "Please enter a positive number"
                        addButton.isEnabled = false
                    } else {
                        quantityInput.error = null
                        addButton.isEnabled = true
                    }
                } else {
                    quantityInput.error = null
                    addButton.isEnabled = false
                }
            }
        })
        
        // Set up the add button click listener
        addButton.setOnClickListener {
            val inputText = quantityInput.text.toString()
            
            // Check if input is empty
            if (inputText.isEmpty()) {
                Toast.makeText(this@StorageActivity, "Please enter the amount you want to borrow", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Get the quantity
            val quantity = inputText.toInt()
            
            // Find the item key by name
            storageRef.orderByChild("name").equalTo(item.name).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val itemSnapshot = snapshot.children.first()
                    val latestQuantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    
                    // Double check if quantity is still valid
                    if (quantity > latestQuantity) {
                        Toast.makeText(this@StorageActivity, "The items are not enough. Please select the appropriate amount", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    
                    val storageName = "Storage $currentStorageIndex"
                    
                    // Just add to cart without updating Firebase
                    addToCart(item, quantity, storageName)
                    Toast.makeText(this@StorageActivity, "Successfully added to cart", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    
                    // Force reload items to show updated quantity
                    loadItemsFromFirebase(currentStorageIndex)
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this@StorageActivity, "Failed to check current quantity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.setContentView(dialogView)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        dialog.show()
    }

    private fun addToCart(item: StorageItem, quantity: Int, storageName: String) {
        val existingItem = cartItems.find { it.name == item.name && it.storageName == storageName }
        if (existingItem != null) {
            // Check if adding the new quantity would exceed available quantity
            if (existingItem.quantity + quantity > item.quantity) {
                Toast.makeText(this, "The items are not enough. Please select the appropriate amount", Toast.LENGTH_SHORT).show()
                return
            }
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

    private fun navigateToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a list to track validation results
        val validationResults = mutableListOf<Boolean>()
        var validationCount = 0

        // Validate all items in cart before proceeding
        for (cartItem in cartItems) {
            val storageRef = database.child(
                when (cartItem.storageName) {
                    "Storage 1" -> "1"
                    "Storage 2" -> "2"
                    "Storage 3" -> "3"
                    else -> "1"
                }
            )
            
            // Check current quantity in Firebase
            storageRef.orderByChild("name").equalTo(cartItem.name).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val itemSnapshot = snapshot.children.first()
                    val currentQuantity = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    
                    // If quantity in cart is more than available, show error
                    if (cartItem.quantity > currentQuantity) {
                        Toast.makeText(
                            this@StorageActivity,
                            "Cannot proceed: Only $currentQuantity ${cartItem.name} available in ${cartItem.storageName}",
                            Toast.LENGTH_LONG
                        ).show()
                        validationResults.add(false)
                    } else {
                        // Calculate new quantity
                        val newQuantity = currentQuantity - cartItem.quantity
                        
                        // Create a map of updates
                        val updates = HashMap<String, Any>()
                        updates["quantity"] = newQuantity
                        
                        // Update the quantity in Firebase
                        storageRef.child(itemSnapshot.key!!).updateChildren(updates)
                            .addOnSuccessListener {
                                validationResults.add(true)
                                validationCount++
                                
                                // When all validations are complete, check results
                                if (validationCount == cartItems.size) {
                                    if (validationResults.all { it }) {
                                        // All validations passed, proceed with checkout
                                        proceedToCheckout()
                                    }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@StorageActivity,
                                    "Failed to update quantity: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                validationResults.add(false)
                                validationCount++
                            }
                    }
                } else {
                    Toast.makeText(
                        this@StorageActivity,
                        "Item ${cartItem.name} not found in ${cartItem.storageName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    validationResults.add(false)
                    validationCount++
                }
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@StorageActivity,
                    "Failed to validate ${cartItem.name}: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                validationResults.add(false)
                validationCount++
            }
        }
    }

    private fun proceedToCheckout() {
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