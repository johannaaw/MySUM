package com.example.sofeng2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sofeng2.R
import com.example.sofeng2.models.CartItem
import com.example.sofeng2.models.StorageItem

class StorageViewModel : ViewModel() {
    private val _items = MutableLiveData<List<StorageItem>>()
    val items: LiveData<List<StorageItem>> = _items

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private val _totalItems = MutableLiveData<Int>()
    val totalItems: LiveData<Int> = _totalItems

    init {
        _items.value = emptyList()
        _cartItems.value = emptyList()
        updateTotalItems()
    }

    fun updateItems(newItems: List<StorageItem>) {
        _items.value = newItems
    }

    fun loadStorage1Items() {
        _items.value = emptyList()
    }

    fun loadStorage2Items() {
        _items.value = emptyList()
    }

    fun loadStorage3Items() {
        _items.value = emptyList()
    }

    fun addToCart(item: StorageItem, quantity: Int, storageName: String) {
        val currentCartItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCartItems.find { it.name == item.name && it.storageName == storageName }
        
        // Calculate total quantity (existing + new)
        val totalQuantity = (existingItem?.quantity ?: 0) + quantity
        
        // Check if total quantity exceeds available quantity
        if (totalQuantity > item.quantity) {
            // Don't add to cart if it would exceed available quantity
            return
        }
        
        // Additional validation to ensure we're not exceeding available quantity
        if (quantity > item.quantity) {
            return
        }
        
        if (existingItem != null) {
            // Check if adding the new quantity would exceed available quantity
            if (existingItem.quantity + quantity > item.quantity) {
                return
            }
            existingItem.quantity += quantity
        } else {
            currentCartItems.add(CartItem(item.name, item.brand, quantity, item.imageResId, storageName))
        }
        
        _cartItems.value = currentCartItems
        updateTotalItems()
    }

    fun removeFromCart(item: CartItem) {
        val currentCartItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        currentCartItems.remove(item)
        _cartItems.value = currentCartItems
        updateTotalItems()
    }

    private fun updateTotalItems() {
        _totalItems.value = _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun getStorageItems(storageId: Int): List<StorageItem> {
        return emptyList()
    }
} 