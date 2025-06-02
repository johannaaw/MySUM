//package com.example.sofeng2
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.ArrayAdapter
//import android.widget.AutoCompleteTextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.button.MaterialButton
//import com.google.android.material.textfield.TextInputEditText
//
//class AddItemActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_add_item)
//
//        val itemNameInput = findViewById<TextInputEditText>(R.id.itemNameInput)
//        val itemDescInput = findViewById<TextInputEditText>(R.id.itemDescInput)
//        val storageDropdown = findViewById<AutoCompleteTextView>(R.id.storageDropdown)
//        val addButton = findViewById<MaterialButton>(R.id.addButton)
//
//        // Ambil daftar storage dari intent
//        val storageList = intent.getStringArrayListExtra("storageList") ?: arrayListOf()
//        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, storageList)
//        storageDropdown.setAdapter(adapter)
//
//        addButton.setOnClickListener {
//            val name = itemNameInput.text.toString().trim()
//            val desc = itemDescInput.text.toString().trim()
//            val storage = storageDropdown.text.toString().trim()
//            if (name.isEmpty() || desc.isEmpty() || storage.isEmpty()) {
//                Toast.makeText(this, "Lengkapi semua field!", Toast.LENGTH_SHORT).show()
//            } else {
//                // TODO: Simpan item ke sistem/database jika perlu
//                Toast.makeText(this, "Item berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//}