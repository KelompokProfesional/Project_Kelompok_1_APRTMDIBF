package com.chats.kelompok1

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CommunityChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var auth: FirebaseAuth

    private var currentUserId = "Anonymous"
    private var currentDisplayName = "Anonymous"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_chat)

        // 1. Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val uid = auth.currentUser?.uid ?: ""

        // 2. Inisialisasi View (ID harus sama dengan XML)
        val recyclerView = findViewById<RecyclerView>(R.id.rvCommunityMessages)
        val etMessage = findViewById<EditText>(R.id.etCommunityMessage)
        val btnSend = findViewById<ImageButton>(R.id.btnCommunitySend)

        // 3. Setup Adapter
        messageAdapter = MessageAdapter(messages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        // 4. Ambil Profil User (untuk identitas pengirim)
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserId = snapshot.child("userId").getValue(String::class.java) ?: "Anonymous"
                currentDisplayName = snapshot.child("displayName").getValue(String::class.java) ?: "Anonymous"
                messageAdapter.updateUserId(currentUserId)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 5. Logika Kirim Pesan ke Node "community"
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val msg = Message(
                    senderId = currentUserId,
                    displayName = currentDisplayName,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                database.child("community").push().setValue(msg)
                etMessage.text.clear()
            }
        }

        // 6. Dengarkan Pesan Masuk secara Real-time
        database.child("community").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (child in snapshot.children) {
                    val m = child.getValue(Message::class.java)
                    m?.let { messages.add(it) }
                }
                messages.sortBy { it.timestamp }
                messageAdapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommunityChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}