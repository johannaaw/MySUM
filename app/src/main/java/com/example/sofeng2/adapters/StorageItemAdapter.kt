package com.example.sofeng2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.models.StorageItem

class StorageItemAdapter(private val onItemClick: (StorageItem) -> Unit) : 
    ListAdapter<StorageItem, StorageItemAdapter.ViewHolder>(StorageItemDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val name: TextView = view.findViewById(R.id.itemName)
        val brand: TextView = view.findViewById(R.id.itemBrand)
        val quantity: TextView = view.findViewById(R.id.itemQuantity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_storage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.image.setImageResource(item.imageResId)
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.quantity.text = item.quantity.toString()

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }
}

class StorageItemDiffCallback : DiffUtil.ItemCallback<StorageItem>() {
    override fun areItemsTheSame(oldItem: StorageItem, newItem: StorageItem): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: StorageItem, newItem: StorageItem): Boolean {
        return oldItem == newItem
    }
} 