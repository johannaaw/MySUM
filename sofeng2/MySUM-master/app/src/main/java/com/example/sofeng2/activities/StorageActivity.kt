package com.example.sofeng2.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.adapters.CartAdapter
import com.example.sofeng2.adapters.StorageItemAdapter
import com.example.sofeng2.models.StorageItem
import com.example.sofeng2.utils.NavigationHandler
import com.example.sofeng2.viewmodels.StorageViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class StorageActivity : AppCompatActivity() {
    private lateinit var itemsRecyclerView: RecyclerView
    private lateinit var cartPopup: View
    private lateinit var cartButton: MaterialButton
    private lateinit var cartCheckOutButton: MaterialButton
    private lateinit var storageTabs: TabLayout
    private lateinit var bottomNavigation: View
    private lateinit var storageAdapter: StorageItemAdapter
    private lateinit var cartAdapter: CartAdapter
    private val viewModel = StorageViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        // Initialize views
        itemsRecyclerView = findViewById(R.id.itemsRecyclerView)
        cartPopup = findViewById(R.id.cartPopup)
        cartButton = findViewById(R.id.cartButton)
        cartCheckOutButton = findViewById(R.id.cartCheckOutButton)
        storageTabs = findViewById(R.id.storageTabs)
        bottomNavigation = findViewById(R.id.bottomNavigation)

        setupRecyclerViews()
        setupTabLayout()
        setupBottomNavigation()
        setupCartButton()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        // Setup Storage Items RecyclerView
        storageAdapter = StorageItemAdapter { item ->
            showQuantityDialog(item)
        }
        itemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StorageActivity)
            adapter = storageAdapter
        }

        // Setup Cart RecyclerView
        cartAdapter = CartAdapter { item ->
            viewModel.removeFromCart(item)
        }
        findViewById<RecyclerView>(R.id.cartRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@StorageActivity)
            adapter = cartAdapter
        }

        // Setup Checkout Button
        cartCheckOutButton.setOnClickListener {
            navigateToCheckout()
        }
    }

    private fun setupTabLayout() {
        storageTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.loadStorage1Items()
                    1 -> viewModel.loadStorage2Items()
                    2 -> viewModel.loadStorage3Items()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupBottomNavigation() {
        NavigationHandler.setupBottomNavigation(this, bottomNavigation as com.google.android.material.bottomnavigation.BottomNavigationView)
        bottomNavigation.findViewById<View>(R.id.navigation_items).isSelected = true
    }

    private fun setupCartButton() {
        cartButton.setOnClickListener {
            cartPopup.visibility = View.VISIBLE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.observe(this@StorageActivity) { items ->
                    storageAdapter.submitList(items)
                }
            }
        }
    }

    private fun showQuantityDialog(item: StorageItem) {
        val dialog = Dialog(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quantity, null)
        
        dialogView.findViewById<TextView>(R.id.itemNameText).text = item.name
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.quantityInput)
        
        dialog.setContentView(dialogView)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        dialog.findViewById<MaterialButton>(R.id.addButton).setOnClickListener {
            val quantity = quantityInput.text.toString().toIntOrNull() ?: 0
            if (quantity > 0) {
                val storageName = when (storageTabs.selectedTabPosition) {
                    0 -> "Storage 1"
                    1 -> "Storage 2"
                    2 -> "Storage 3"
                    else -> "Storage 1"
                }
                viewModel.addToCart(item, quantity, storageName)
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }

    private fun navigateToCheckout() {
        val cartItems = viewModel.cartItems.value ?: emptyList()
        val cartNames = ArrayList(cartItems.map { it.name })
        val cartQuantities = ArrayList(cartItems.map { it.quantity })
        
        val intent = Intent(this, CheckoutActivity::class.java).apply {
            putStringArrayListExtra("cartNames", cartNames)
            putIntegerArrayListExtra("cartQuantities", cartQuantities)
        }
        startActivity(intent)
        finish()
        cartPopup.visibility = View.GONE
    }
} 