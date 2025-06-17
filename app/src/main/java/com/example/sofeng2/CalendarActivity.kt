package com.example.sofeng2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sofeng2.models.BorrowingItem
import com.example.sofeng2.utils.NavigationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class CalendarActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var monthYearText: TextView
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dateTitle: TextView
    private lateinit var eventMessage: TextView
    private lateinit var eventDetailsCard: View
    private var currentDate = LocalDate.now()
    private val events = mutableMapOf<String, String>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        try {
            // Initialize views
            recyclerView = findViewById(R.id.calendarGrid)
            monthYearText = findViewById(R.id.monthYearText)
            bottomNavigation = findViewById(R.id.bottomNavigation)
            dateTitle = findViewById(R.id.dateTitle)
            eventMessage = findViewById(R.id.eventMessage)
            eventDetailsCard = findViewById(R.id.eventDetailsCard)

            // Setup RecyclerView
            recyclerView.layoutManager = GridLayoutManager(this, 7)
            adapter = CalendarAdapter { day ->
                showEventDetails(day)
            }
            recyclerView.adapter = adapter

            // Setup Bottom Navigation
            NavigationHandler.setupBottomNavigation(this, bottomNavigation)

            // Set the calendar item as selected
            bottomNavigation.selectedItemId = R.id.navigation_calendar

            // Load user name from Firebase
            val userNameTextView = findViewById<TextView>(R.id.userNameText)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                userRef.child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.getValue(String::class.java)
                        userNameTextView.text = name ?: "User"
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            } else {
                userNameTextView.text = "User"
            }

            // Load borrows from Firebase
            loadBorrowsFromFirebase()

            // Update calendar
            updateCalendar()

            // Setup navigation buttons
            findViewById<View>(R.id.prevMonthButton)?.setOnClickListener {
                currentDate = currentDate.minusMonths(1)
                updateCalendar()
            }

            findViewById<View>(R.id.nextMonthButton)?.setOnClickListener {
                currentDate = currentDate.plusMonths(1)
                updateCalendar()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBorrowsFromFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val borrowRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("borrows")

        borrowRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                events.clear()
                for (borrowSnapshot in snapshot.children) {
                    val borrowData = borrowSnapshot.getValue(BorrowingItem::class.java)
                    borrowData?.let {
                        // Convert dates to Firebase format (yyyy-MM-dd)
                        val borrowDate = convertToFirebaseDate(it.borrowDate)
                        val returnDate = convertToFirebaseDate(it.returnDate)
                        
                        // Create simplified event message
                        val eventDetails = buildString {
                            append(it.purpose)
                            append("\nReturn: ${it.returnDate}")
                        }
                        
                        // Add event for borrow date
                        events[borrowDate] = "Borrow Event:\n$eventDetails"
                        
                        // Add event for return date
                        events[returnDate] = "Return Event:\n$eventDetails"
                    }
                }
                updateCalendar()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun convertToFirebaseDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: return ""
            val calendar = Calendar.getInstance()
            calendar.time = date
            String.format("%04d-%02d-%02d", 
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH))
        } catch (e: Exception) {
            ""
        }
    }

    private fun updateCalendar() {
        try {
            // Clear existing items
            adapter.clearItems()

            // Update month/year text
            monthYearText.text = currentDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            // Generate new calendar
            generateCalendar()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun generateCalendar() {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(currentDate.year, currentDate.monthValue - 1, 1)

            // Get the first day of the month (0 = Sunday, 1 = Monday, etc.)
            val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1

            // Get the number of days in the month
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            // Add empty cells for days before the first day of the month
            for (i in 0 until firstDayOfMonth) {
                adapter.addEmptyCell()
            }

            // Add cells for each day of the month
            for (day in 1..daysInMonth) {
                val dateStr = String.format("%04d-%02d-%02d", currentDate.year, currentDate.monthValue, day)
                val hasEvent = events.containsKey(dateStr)
                adapter.addDayCell(day, hasEvent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showEventDetails(day: Int) {
        try {
            val selectedDate = currentDate.withDayOfMonth(day)
            dateTitle.text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
            
            val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            eventMessage.text = events[dateStr] ?: "No event"
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class CalendarAdapter(private val onDayClick: (Int) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {

    private val items = mutableListOf<CalendarItem>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayText: TextView = view.findViewById(R.id.dayText)
        val eventIndicator: View = view.findViewById(R.id.eventIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val item = items[position]
            holder.dayText.text = if (item.day > 0) item.day.toString() else ""

            // Show event indicator if there's an event
            if (item.day > 0 && item.hasEvent) {
                holder.eventIndicator.visibility = View.VISIBLE
            } else {
                holder.eventIndicator.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (item.day > 0) {
                    onDayClick(item.day)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount() = items.size

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun addEmptyCell() {
        items.add(CalendarItem(0, false))
        notifyItemInserted(items.size - 1)
    }

    fun addDayCell(day: Int, hasEvent: Boolean) {
        items.add(CalendarItem(day, hasEvent))
        notifyItemInserted(items.size - 1)
    }
}

data class CalendarItem(val day: Int, val hasEvent: Boolean)