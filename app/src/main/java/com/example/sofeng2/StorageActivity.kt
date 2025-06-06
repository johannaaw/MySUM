package com.example.sofeng2

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.tabs.TabLayout

class StorageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StorageItemAdapter
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartPopup: View
    private lateinit var cartCheckOutButton: View
    private lateinit var totalItemsText: TextView
    private val cartItems = mutableListOf<CartItem>()
    private var currentStorageName: String = "Storage 1"
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_storage)

        // Initialize views
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
    }

    private fun setupRecyclerViews() {
        // Setup Storage Items RecyclerView
        adapter = StorageItemAdapter(getSampleItems()) { item ->
            showQuantityDialog(item)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@StorageActivity)
            adapter = this@StorageActivity.adapter
        }

        // Setup Cart RecyclerView
        cartAdapter = CartAdapter(cartItems)
        cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@StorageActivity)
            adapter = this@StorageActivity.cartAdapter
        }

        // Setup Checkout Button
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
                currentStorageName = when (tab?.position) {
                    0 -> "Storage 1"
                    1 -> "Storage 2"
                    2 -> "Storage 3"
                    else -> "Storage 1"
                }
                when (tab?.position) {
                    0 -> adapter.updateItems(getSampleItems())
                    1 -> adapter.updateItems(getStorage2Items())
                    2 -> adapter.updateItems(getStorage3Items())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
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
                addToCart(item, quantity, currentStorageName)
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
        cartAdapter.notifyDataSetChanged()
        totalItemsText.text = cartItems.sumOf { it.quantity }.toString()
        
        // Show/hide cart popup based on items
        if (cartItems.isNotEmpty()) {
            cartPopup.visibility = View.VISIBLE
        } else {
            cartPopup.visibility = View.GONE
        }
    }

    private fun getSampleItems(): List<StorageItem> {
        return emptyList()
    }

    private fun getStorage2Items(): List<StorageItem> {
        return emptyList()
    }

    private fun getStorage3Items(): List<StorageItem> {
        return emptyList()
    }
}

data class StorageItem(
    val name: String,
    val brand: String,
    val quantity: Int,
    val imageResId: Int
)

data class CartItem(
    val name: String,
    val brand: String,
    var quantity: Int,
    val imageResId: Int,
    val storageName: String
)

class StorageItemAdapter(private var items: List<StorageItem>, private val onItemClick: (StorageItem) -> Unit) : 
    RecyclerView.Adapter<StorageItemAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val image: android.widget.ImageView = view.findViewById(R.id.itemImage)
        val name: android.widget.TextView = view.findViewById(R.id.itemName)
        val brand: android.widget.TextView = view.findViewById(R.id.itemBrand)
        val quantity: android.widget.TextView = view.findViewById(R.id.itemQuantity)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_storage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageResId)
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.quantity.text = item.quantity.toString()

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<StorageItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class CartAdapter(private var items: List<CartItem>) : 
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val image: android.widget.ImageView = view.findViewById(R.id.itemImage)
        val name: android.widget.TextView = view.findViewById(R.id.itemName)
        val brand: android.widget.TextView = view.findViewById(R.id.itemBrand)
        val quantity: android.widget.TextView = view.findViewById(R.id.itemQuantity)
        val storage: android.widget.TextView = view.findViewById(R.id.itemStorage)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageResId)
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.quantity.text = item.quantity.toString()
        holder.storage.text = item.storageName
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 