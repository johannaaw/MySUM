package com.example.sofeng2.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.R
import com.example.sofeng2.adapters.BorrowSummaryAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {
    private lateinit var borrowPurposeInput: TextInputEditText
    private lateinit var waInput: TextInputEditText
    private lateinit var lineInput: TextInputEditText
    private lateinit var borrowDateInput: TextInputEditText
    private lateinit var returnDateInput: TextInputEditText
    private lateinit var borrowButton: MaterialButton
    private lateinit var borrowSummaryRecyclerView: RecyclerView
    private lateinit var borrowSummaryAdapter: BorrowSummaryAdapter

    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        initializeViews()
        setupDatePickers()
        setupRecyclerView()
        setupBorrowButton()
    }

    private fun initializeViews() {
        borrowPurposeInput = findViewById(R.id.borrowPurposeInput)
        waInput = findViewById(R.id.waInput)
        lineInput = findViewById(R.id.lineInput)
        borrowDateInput = findViewById(R.id.borrowDateInput)
        returnDateInput = findViewById(R.id.returnDateInput)
        borrowButton = findViewById(R.id.borrowButton)
        borrowSummaryRecyclerView = findViewById(R.id.borrowSummaryRecyclerView)
    }

    private fun setupDatePickers() {
        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
        }

        borrowDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            borrowDateInput.setText(dateFormatter.format(calendar.time))
        }

        returnDateInput.setOnClickListener {
            DatePickerDialog(
                this,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
            returnDateInput.setText(dateFormatter.format(calendar.time))
        }
    }

    private fun setupRecyclerView() {
        borrowSummaryAdapter = BorrowSummaryAdapter()
        borrowSummaryRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = borrowSummaryAdapter
        }

        // Get cart items from intent
        val cartNames = intent.getStringArrayListExtra("cartNames") ?: arrayListOf()
        val cartQuantities = intent.getIntegerArrayListExtra("cartQuantities") ?: arrayListOf()
        
        // Create summary items
        val summaryItems = cartNames.zip(cartQuantities).map { (name, quantity) ->
            BorrowSummaryItem(name, quantity)
        }
        borrowSummaryAdapter.submitList(summaryItems)
    }

    private fun setupBorrowButton() {
        borrowButton.setOnClickListener {
            // TODO: Implement borrowing logic
            finish()
        }
    }
}

data class BorrowSummaryItem(
    val name: String,
    val quantity: Int
) 