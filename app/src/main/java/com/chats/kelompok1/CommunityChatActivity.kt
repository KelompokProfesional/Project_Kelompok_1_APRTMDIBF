package com.chats.kelompok1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var auth: FirebaseAuth
    private var valueEventListener: ValueEventListener? = null
    private var currentUserId = "Anonymous"  // Added: Declare as class variable
    private var currentDisplayName = "Anonymous"  // Added: Declare as class variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Fetch user profile (added)
        val uid = auth.currentUser?.uid ?: return
        database.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserId = snapshot.child("userId").getValue(String::class.java) ?: "Anonymous"
                currentDisplayName = snapshot.child("displayName").getValue(String::class.java) ?: "Anonymous"
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommunityChatActivity, "Error loading profile: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        recyclerView = findViewById(R.id.recyclerViewMessages)
        messageEditText = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.buttonSend)

        messageAdapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(
                    senderId = currentUserId,        // Use named parameters
                    displayName = currentDisplayName,
                    text = messageText,
                    timestamp = System.currentTimeMillis()
                )
                database.child("chats").push().setValue(message)
                messageEditText.text.clear()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                messages.sortBy { it.timestamp }
                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@CommunityChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        database.child("chats").addValueEventListener(valueEventListener!!)
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let { database.child("chats").removeEventListener(it) }
    }
}