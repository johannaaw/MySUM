package com.example.sofeng2.data

import com.example.sofeng2.models.BorrowData

object BorrowManager {
    private val ongoingBorrows = mutableListOf<BorrowData>()

    fun addBorrow(borrowData: BorrowData) {
        ongoingBorrows.add(borrowData)
    }

    fun getOngoingBorrows(): List<BorrowData> = ongoingBorrows.toList()
} 