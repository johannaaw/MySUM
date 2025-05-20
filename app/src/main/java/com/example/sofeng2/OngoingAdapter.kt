package com.example.sofeng2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OngoingAdapter(
    private val items: List<OngoingItem>,
    private val onViewMoreClick: (OngoingItem) -> Unit
) : RecyclerView.Adapter<OngoingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val eventTitle: TextView = view.findViewById(R.id.eventTitle)
        val viewMoreButton: Button = view.findViewById(R.id.viewMoreButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ongoing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.eventTitle.text = item.title
        holder.viewMoreButton.setOnClickListener {
            onViewMoreClick(item)
        }
    }

    override fun getItemCount() = items.size
}

data class OngoingItem(
    val id: String,
    val title: String,
    // Add more fields as needed
) 