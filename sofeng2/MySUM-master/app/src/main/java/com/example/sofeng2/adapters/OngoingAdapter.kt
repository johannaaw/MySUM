package com.example.sofeng2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.activities.OngoingItem

class OngoingAdapter : RecyclerView.Adapter<OngoingAdapter.ViewHolder>() {
    private var items = listOf<OngoingItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val storageName: TextView = view.findViewById(R.id.ongoingStorage)
        val timeLeft: TextView = view.findViewById(R.id.ongoingTimeLeft)
        val items: TextView = view.findViewById(R.id.ongoingItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.storageName.text = item.storageName
        holder.timeLeft.text = item.timeLeft
        holder.items.text = item.items
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<OngoingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 