package com.example.sofeng2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.activities.BorrowSummaryItem

class BorrowSummaryAdapter : RecyclerView.Adapter<BorrowSummaryAdapter.ViewHolder>() {
    private var items = listOf<BorrowSummaryItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.itemName)
        val quantity: TextView = view.findViewById(R.id.itemQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrow_summary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.quantity.text = "Quantity: ${item.quantity}"
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<BorrowSummaryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 