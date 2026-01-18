package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SelectUserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        val editTextUserId = findViewById<EditText>(R.id.editTextUserId)
        val buttonStartChat = findViewById<Button>(R.id.buttonStartChat)

        buttonStartChat.setOnClickListener {
            val targetUserId = editTextUserId.text.toString().trim()
            if (targetUserId.isNotEmpty()) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("TARGET_USER_ID", targetUserId)  // Pass the target user ID
                startActivity(intent)
            } else {
                Toast.makeText(this, "Enter a user ID", Toast.LENGTH_SHORT).show()
            }
        }
    }
}