package com.example.sofeng2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterForm1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_form1)

        val nameInput = findViewById<TextInputEditText>(R.id.nameInput)
        val organizationInput = findViewById<TextInputEditText>(R.id.organizationInput)
        val cityInput = findViewById<TextInputEditText>(R.id.cityInput)
        val nextButton = findViewById<MaterialButton>(R.id.nextButton)

        nextButton.setOnClickListener {
            val name = nameInput.text.toString()
            val organization = organizationInput.text.toString()
            val city = cityInput.text.toString()

            if (name.isBlank() || organization.isBlank() || city.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Pass data to next form
            val intent = Intent(this, RegisterForm2Activity::class.java).apply {
                putExtra("name", name)
                putExtra("organization", organization)
                putExtra("city", city)
            }
            startActivity(intent)
        }
    }
} 