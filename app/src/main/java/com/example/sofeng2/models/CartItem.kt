package com.example.sofeng2.models

data class CartItem(
    val name: String,
    val brand: String,
    var quantity: Int,
    val imageResId: Int,
    val storageName: String
) 