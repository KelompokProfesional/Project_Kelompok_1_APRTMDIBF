package com.chats.kelompok1

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatActivity : AppCompatActivity() {
    private lateinit var rootDatabase: DatabaseReference
    private lateinit var messagesRef: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var auth: FirebaseAuth
    private var valueEventListener: ValueEventListener? = null

    private lateinit var myUid: String
    private var currentUserId = "Anonymous"
    private var currentDisplayName = "Anonymous"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        rootDatabase = FirebaseDatabase.getInstance().reference
        myUid = auth.currentUser?.uid ?: ""

        val targetId = intent.getStringExtra("TARGET_USER_ID") ?: ""

        // Menghubungkan variabel dengan ID tvChatTitle di XML
        val tvTitle = findViewById<TextView>(R.id.tvChatTitle)

        // Logika Folder: Handle Room vs Private Chat
        val chatId = if (targetId.startsWith("ROOM_")) {
            tvTitle.text = targetId.replace("ROOM_", "Room: ")
            targetId
        } else {
            tvTitle.text = "Private Chat"
            if (myUid < targetId) "${myUid}_$targetId" else "${targetId}_$myUid"
        }

        messagesRef = rootDatabase.child("chats").child(chatId)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMessages)
        val messageEditText = findViewById<EditText>(R.id.editTextMessage)
        val sendButton = findViewById<ImageButton>(R.id.buttonSend)

        messageAdapter = MessageAdapter(messages, currentUserId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = messageAdapter

        fetchMyProfile()

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                val message = Message(
                    senderId = currentUserId,
                    displayName = currentDisplayName,
                    text = messageText,
                    timestamp = System.currentTimeMillis()
                )
                messagesRef.push().setValue(message)
                messageEditText.text.clear()
            }
        }
    }

    private fun fetchMyProfile() {
        rootDatabase.child("users").child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserId = snapshot.child("userId").getValue(String::class.java) ?: "Anonymous"
                currentDisplayName = snapshot.child("displayName").getValue(String::class.java) ?: "Anonymous"
                messageAdapter.updateUserId(currentUserId)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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
                if (messages.isNotEmpty()) {
                    findViewById<RecyclerView>(R.id.recyclerViewMessages).scrollToPosition(messages.size - 1)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        messagesRef.addValueEventListener(valueEventListener!!)
    }

    override fun onStop() {
        super.onStop()
        valueEventListener?.let { messagesRef.removeEventListener(it) }
    }
}