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
        loadStorage1Items()
        _cartItems.value = emptyList()
        updateTotalItems()
    }

    fun loadStorage1Items() {
        val storage1Items = listOf(
            StorageItem("Penghapus", "Faber Castell", 3, R.drawable.ic_eraser),
            StorageItem("Pensil", "Faber Castell", 4, R.drawable.ic_pencil),
            StorageItem("Penggaris", "Beautify 20cm", 1, R.drawable.ic_ruler),
            StorageItem("Gunting", "Joker", 6, R.drawable.ic_scissors)
        )
        _items.value = storage1Items
    }

    fun loadStorage2Items() {
        val storage2Items = listOf(
            StorageItem("Buku", "Sinar Dunia", 5, R.drawable.ic_book),
            StorageItem("Pulpen", "Pilot", 8, R.drawable.ic_pen)
        )
        _items.value = storage2Items
    }

    fun loadStorage3Items() {
        val storage3Items = listOf(
            StorageItem("Stapler", "Joyko", 2, R.drawable.ic_stapler),
            StorageItem("Kertas", "HVS A4", 100, R.drawable.ic_paper)
        )
        _items.value = storage3Items
    }

    fun addToCart(item: StorageItem, quantity: Int, storageName: String) {
        val currentCartItems = _cartItems.value?.toMutableList() ?: mutableListOf()
        val existingItem = currentCartItems.find { it.name == item.name && it.storageName == storageName }
        
        if (existingItem != null) {
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
    }

    private fun updateTotalItems() {
        _totalItems.value = _cartItems.value?.sumOf { it.quantity } ?: 0
    }

    fun getStorageItems(storageId: Int): List<StorageItem> {
        return when (storageId) {
            0 -> getStorage1Items()
            1 -> getStorage2Items()
            2 -> getStorage3Items()
            else -> getStorage1Items()
        }
    }

    private fun getStorage1Items(): List<StorageItem> {
        return listOf(
            StorageItem("Penghapus", "Faber Castell", 3, R.drawable.ic_eraser),
            StorageItem("Pensil", "Faber Castell", 4, R.drawable.ic_pencil),
            StorageItem("Penggaris", "Beautify 20cm", 1, R.drawable.ic_ruler),
            StorageItem("Gunting", "Joker", 6, R.drawable.ic_scissors)
        )
    }

    private fun getStorage2Items(): List<StorageItem> {
        return listOf(
            StorageItem("Buku", "Sinar Dunia", 5, R.drawable.ic_book),
            StorageItem("Pulpen", "Pilot", 8, R.drawable.ic_pen)
        )
    }

    private fun getStorage3Items(): List<StorageItem> {
        return listOf(
            StorageItem("Stapler", "Joyko", 2, R.drawable.ic_stapler),
            StorageItem("Kertas", "HVS A4", 100, R.drawable.ic_paper)
        )
    }
} 