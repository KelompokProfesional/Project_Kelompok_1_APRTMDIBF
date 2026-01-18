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

class ChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var auth: FirebaseAuth
    private var valueEventListener: ValueEventListener? = null  // Store the listener to remove it later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("chats")

        recyclerView = findViewById(R.id.recyclerViewMessages)
        messageEditText = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.buttonSend)

        messageAdapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val userId = auth.currentUser?.uid ?: "Anonymous"
                val message = Message(userId, messageText, System.currentTimeMillis())
                database.push().setValue(message)
                messageEditText.text.clear()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        valueEventListener = object : ValueEventListener {  // Assign the listener to the variable
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()
                for (child in snapshot.children) {
                    val message = child.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                messages.sortBy { it.timestamp }
                messageAdapter.notifyDataSetChanged()
                recyclerView.scrollToPosition(messages.size - 1)  // Auto-scroll to latest
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }
        database.addValueEventListener(valueEventListener!!)  // Add the stored listener
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let { database.removeEventListener(it) }  // Remove the stored listener
    }
}
