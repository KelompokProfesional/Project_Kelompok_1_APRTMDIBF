package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileSetupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val editTextUserId = findViewById<EditText>(R.id.editTextUserId)
        val editTextDisplayName = findViewById<EditText>(R.id.editTextDisplayName)
        val buttonSaveProfile = findViewById<Button>(R.id.buttonSaveProfile)

        val uid = auth.currentUser?.uid ?: return

        // Check if profile exists
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Profile exists: Pre-fill and make userId read-only
                    val existingUserId = snapshot.child("userId").getValue(String::class.java) ?: ""
                    val existingDisplayName = snapshot.child("displayName").getValue(String::class.java) ?: ""
                    editTextUserId.setText(existingUserId)
                    editTextUserId.isEnabled = false  // User ID cannot be changed
                    editTextDisplayName.setText(existingDisplayName)
                    buttonSaveProfile.text = "Update Profile"
                } else {
                    // No profile: Allow full setup
                    buttonSaveProfile.text = "Save Profile"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileSetupActivity, "Error loading profile", Toast.LENGTH_SHORT).show()
            }
        })

        buttonSaveProfile.setOnClickListener {
            val userId = editTextUserId.text.toString().trim()
            val displayName = editTextDisplayName.text.toString().trim()

            if (userId.isEmpty() || displayName.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val profile = mapOf("userId" to userId, "displayName" to displayName)
            database.child("users").child(uid).setValue(profile).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}