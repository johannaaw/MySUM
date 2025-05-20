package com.example.sofeng2.models

import java.util.Date

data class BorrowData(
    val items: List<BorrowItem>,
    val purpose: String,
    val wa: String,
    val line: String,
    val borrowDate: Date,
    val returnDate: Date
)

data class BorrowItem(
    val name: String,
    val quantity: Int
) 