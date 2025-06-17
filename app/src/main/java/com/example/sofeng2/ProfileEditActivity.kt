package com.example.sofeng2

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var cityInput: TextInputEditText
    private lateinit var organizationInput: TextInputEditText
    private lateinit var saveButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // Initialize views
        nameInput = findViewById(R.id.nameInput)
        cityInput = findViewById(R.id.cityInput)
        organizationInput = findViewById(R.id.organizationInput)
        saveButton = findViewById(R.id.saveButton)

        // Load current user data
        loadUserData()

        // Set up save button click listener
        saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun loadUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(user.uid)
            userRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    nameInput.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                    cityInput.setText(snapshot.child("city").getValue(String::class.java) ?: "")
                    organizationInput.setText(snapshot.child("organization").getValue(String::class.java) ?: "")
                }
            }
        }
    }

    private fun saveUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            val name = nameInput.text.toString().trim()
            val city = cityInput.text.toString().trim()
            val organization = organizationInput.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            val updates = mapOf(
                "name" to name,
                "city" to city,
                "organization" to organization
            )

            FirebaseDatabase.getInstance().getReference("users")
                .child(user.uid)
                .updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
} 