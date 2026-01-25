package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class JoinRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_room)

        val etRoomCode = findViewById<EditText>(R.id.etRoomCode)
        val btnJoin = findViewById<Button>(R.id.btnJoinRoom)

        btnJoin.setOnClickListener {
            val code = etRoomCode.text.toString().trim().uppercase()
            if (code.isNotEmpty()) {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("TARGET_USER_ID", "ROOM_$code")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Masukkan kode room!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}