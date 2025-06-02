package com.example.sofeng2.models

data class BorrowingItem(
    var id: String = "",
    var purpose: String = "",
    var wa: String = "",
    var line: String = "",
    var borrowDate: String = "",
    var returnDate: String = "",
    var items: List<BorrowItem> = listOf(),
    var status: String = ""
) 