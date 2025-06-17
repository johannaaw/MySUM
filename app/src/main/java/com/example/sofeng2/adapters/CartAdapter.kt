package com.example.sofeng2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.models.CartItem

class CartAdapter(private val onRemoveClick: (CartItem) -> Unit) : 
    RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private var items = listOf<CartItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.itemImage)
        val name: TextView = view.findViewById(R.id.itemName)
        val brand: TextView = view.findViewById(R.id.itemBrand)
        val quantity: TextView = view.findViewById(R.id.itemQuantity)
        val storage: TextView = view.findViewById(R.id.itemStorage)
        val removeButton: View = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.image.setImageResource(item.imageResId)
        holder.name.text = item.name
        holder.brand.text = item.brand
        holder.quantity.text = item.quantity.toString()
        holder.storage.text = item.storageName

        holder.removeButton.setOnClickListener {
            onRemoveClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<CartItem>) {
        items = newItems
        notifyDataSetChanged()
    }
} 