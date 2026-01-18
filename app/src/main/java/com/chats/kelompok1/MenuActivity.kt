package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val buttonCommunityChat = findViewById<Button>(R.id.buttonCommunityChat)
        val buttonPrivateChat = findViewById<Button>(R.id.buttonPrivateChat)
        val buttonEditProfile = findViewById<Button>(R.id.buttonEditProfile)  // New button

        buttonCommunityChat.setOnClickListener {
            startActivity(Intent(this, CommunityChatActivity::class.java))
        }

        buttonPrivateChat.setOnClickListener {
            startActivity(Intent(this, SelectUserActivity::class.java))
        }

        buttonEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))  // Navigate to edit profile
        }
    }
}