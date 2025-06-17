package com.example.sofeng2.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.models.BorrowingItem
import java.text.SimpleDateFormat
import java.util.*

class BorrowingListAdapter(
    private val items: List<BorrowingItem>,
    private val onReturnClick: ((BorrowingItem) -> Unit)? = null
) : RecyclerView.Adapter<BorrowingListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val purpose: TextView = view.findViewById(R.id.purposeText)
        val contact: TextView = view.findViewById(R.id.contactText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val daysLeft: TextView = view.findViewById(R.id.daysLeftText)
        val itemsList: TextView = view.findViewById(R.id.itemsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_borrowing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.purpose.text = item.purpose
        
        // Format contact information
        val contactInfo = buildString {
            append("Contact: ${item.wa}")
            if (item.line.isNotEmpty()) {
                append(" • Line: ${item.line}")
            }
        }
        holder.contact.text = contactInfo
        
        holder.dateText.text = "Borrow: ${item.borrowDate} • Return: ${item.returnDate}"
        
        // Calculate days remaining
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val returnDate = dateFormat.parse(item.returnDate)
        val currentDate = Calendar.getInstance().time
        val daysRemaining = if (returnDate != null) {
            val diff = returnDate.time - currentDate.time
            val days = diff / (24 * 60 * 60 * 1000)
            if (days > 0) "$days days remaining" else "Overdue"
        } else {
            "Invalid date"
        }
        holder.daysLeft.text = daysRemaining

        // Format borrowed items list
        val itemsText = item.items.joinToString("\n") { "• ${it.name} (${it.quantity})" }
        holder.itemsList.text = itemsText

        // Set click listener to show popup
        holder.itemView.setOnClickListener {
            showBorrowingDetailsDialog(context, item)
        }
    }

    override fun getItemCount() = items.size

    private fun showBorrowingDetailsDialog(context: Context, item: BorrowingItem) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_borrowing_details)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set dialog content
        dialog.findViewById<TextView>(R.id.dialogPurpose).text = "Purpose: ${item.purpose}"
        dialog.findViewById<TextView>(R.id.dialogBorrowDate).text = "Borrow Date: ${item.borrowDate}"
        dialog.findViewById<TextView>(R.id.dialogReturnDate).text = "Return Date: ${item.returnDate}"
        dialog.findViewById<TextView>(R.id.dialogContact).text = "Contact: ${item.wa}"
        if (item.line.isNotEmpty()) {
            dialog.findViewById<TextView>(R.id.dialogLine).text = "Line: ${item.line}"
        } else {
            dialog.findViewById<TextView>(R.id.dialogLine).visibility = View.GONE
        }

        // Format and display borrowed items
        val itemsText = item.items.joinToString("\n") { "• ${it.name} (${it.quantity})" }
        dialog.findViewById<TextView>(R.id.dialogItems).text = itemsText

        // Show return button only for pending and ongoing items
        val returnButton = dialog.findViewById<Button>(R.id.dialogReturnButton)
        if (onReturnClick != null) {
            returnButton.visibility = View.VISIBLE
            returnButton.setOnClickListener {
                onReturnClick(item)
                dialog.dismiss()
            }
        } else {
            returnButton.visibility = View.GONE
        }

        // Close button
        dialog.findViewById<Button>(R.id.dialogCloseButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
} 