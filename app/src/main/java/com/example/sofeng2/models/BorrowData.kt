package com.example.sofeng2.models

import java.util.Date

data class BorrowData(
    val id: String = "",
    val purpose: String = "",
    val wa: String = "",
    val line: String = "",
    val borrowDate: String = "",
    val returnDate: String = "",
    val items: List<BorrowItem> = listOf()
) 