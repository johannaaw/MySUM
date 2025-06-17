package com.example.sofeng2

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.models.BorrowItem
import com.example.sofeng2.models.BorrowingItem
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : AppCompatActivity() {
    private lateinit var borrowSummaryRecyclerView: RecyclerView
    private lateinit var adapter: BorrowSummaryAdapter
    private val borrowItems = mutableListOf<BorrowItem>()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        
        try {
            auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: run {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            
            val borrowDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId).child("borrows")

            // Get cart data from intent
            val cartNames = intent.getStringArrayListExtra("cartNames")
            val cartQuantities = intent.getIntegerArrayListExtra("cartQuantities")

            if (cartNames == null || cartQuantities == null) {
                Log.e("CheckoutActivity", "Missing cart data in intent")
                Toast.makeText(this, "Error: Missing cart data", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            Log.d("CheckoutActivity", "Received cart data: names=$cartNames, quantities=$cartQuantities")

            // Setup UI
            val borrowPurposeInput = findViewById<TextInputEditText>(R.id.borrowPurposeInput)
            val waInput = findViewById<TextInputEditText>(R.id.waInput)
            val lineInput = findViewById<TextInputEditText>(R.id.lineInput)
            val borrowDateInput = findViewById<TextInputEditText>(R.id.borrowDateInput)
            val returnDateInput = findViewById<TextInputEditText>(R.id.returnDateInput)
            val borrowButton = findViewById<MaterialButton>(R.id.borrowButton)
            borrowSummaryRecyclerView = findViewById(R.id.borrowSummaryRecyclerView)

            // Setup RecyclerView
            for (i in cartNames.indices) {
                borrowItems.add(BorrowItem(cartNames[i], cartQuantities.getOrElse(i) { 1 }))
            }
            adapter = BorrowSummaryAdapter(borrowItems)
            borrowSummaryRecyclerView.layoutManager = LinearLayoutManager(this)
            borrowSummaryRecyclerView.adapter = adapter

            // Date pickers
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            borrowDateInput.setOnClickListener {
                showDatePicker { date -> borrowDateInput.setText(dateFormat.format(date)) }
            }
            returnDateInput.setOnClickListener {
                showDatePicker { date -> returnDateInput.setText(dateFormat.format(date)) }
            }

            borrowButton.setOnClickListener {
                val purpose = borrowPurposeInput.text.toString().trim()
                val wa = waInput.text.toString().trim()
                val line = lineInput.text.toString().trim()
                val borrowDate = borrowDateInput.text.toString().trim()
                val returnDate = returnDateInput.text.toString().trim()

                if (purpose.isEmpty() || wa.isEmpty() || borrowDate.isEmpty() || returnDate.isEmpty()) {
                    Toast.makeText(this, "Lengkapi semua field wajib!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create borrowing item
                val borrowingItem = BorrowingItem(
                    purpose = purpose,
                    wa = wa,
                    line = line,
                    borrowDate = borrowDate,
                    returnDate = returnDate,
                    items = borrowItems
                )

                // Save to Firebase
                val borrowId = borrowDatabase.push().key
                if (borrowId == null) {
                    Toast.makeText(this, "Gagal membuat ID peminjaman", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                borrowDatabase.child(borrowId).setValue(borrowingItem)
                    .addOnSuccessListener {
                        // 🔽 Update storage quantities
                        val storageRef = FirebaseDatabase.getInstance().getReference("storage")

                        storageRef.get().addOnSuccessListener { snapshot ->
                            for (borrowItem in borrowItems) {
                                val itemName = borrowItem.name
                                val quantityToSubtract = borrowItem.quantity

                                snapshot.children.forEach { slotSnapshot ->
                                    slotSnapshot.children.forEach { itemSnapshot ->
                                        val name = itemSnapshot.child("name").getValue(String::class.java)
                                        if (name == itemName) {
                                            val currentQty = itemSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                                            val newQty = (currentQty - quantityToSubtract).coerceAtLeast(0)
                                            itemSnapshot.ref.child("quantity").setValue(newQty)
                                        }
                                    }
                                }
                            }

                            Toast.makeText(this, "Peminjaman berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, OngoingActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal mengupdate stok: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Firebase", "Error saving data", e)
                    }
            }

        } catch (e: Exception) {
            Log.e("CheckoutActivity", "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        dialog.show()
    }
}

// Adapter for borrow summary RecyclerView
class BorrowSummaryAdapter(private val items: List<BorrowItem>) :
    RecyclerView.Adapter<BorrowSummaryAdapter.ViewHolder>() {
    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val name: android.widget.TextView = view.findViewById(android.R.id.text1)
        val quantity: android.widget.TextView = view.findViewById(android.R.id.text2)
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.widget.LinearLayout(parent.context).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            val nameView = android.widget.TextView(parent.context).apply {
                id = android.R.id.text1
                textSize = 16f
                setTextColor(0xFF2C2C2C.toInt())
                setPadding(0, 0, 16, 0)
                layoutParams = android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val qtyView = android.widget.TextView(parent.context).apply {
                id = android.R.id.text2
                textSize = 16f
                setTextColor(0xFF2C2C2C.toInt())
                setPadding(0, 0, 0, 0)
                layoutParams = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            addView(nameView)
            addView(qtyView)
        }
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.quantity.text = "x${item.quantity}"
    }
    override fun getItemCount() = items.size
} 