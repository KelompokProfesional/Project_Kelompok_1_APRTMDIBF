package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SelectUserActivity : AppCompatActivity() {

    // Deklarasi variabel di tingkat kelas
    private lateinit var recyclerViewSelectUser: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<Map<String, String>>()
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi View (Pastikan ID ini ada di XML)
        recyclerViewSelectUser = findViewById(R.id.recyclerViewSelectUser)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        val searchView = findViewById<SearchView>(R.id.searchViewSelect)

        // Setup Adapter (Hanya 1 parameter lambda)
        userAdapter = UserAdapter { userMap ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("TARGET_USER_ID", userMap["uid"])
            startActivity(intent)
            finish()
        }

        recyclerViewSelectUser.layoutManager = LinearLayoutManager(this)
        recyclerViewSelectUser.adapter = userAdapter

        fetchAllUsers()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                userAdapter.filter(newText ?: "")
                updateVisibility()
                return true
            }
        })
    }

    private fun fetchAllUsers() {
        val myUid = auth.currentUser?.uid ?: return
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnap in snapshot.children) {
                    val uid = userSnap.key ?: ""
                    if (uid != myUid) {
                        userList.add(mapOf(
                            "uid" to uid,
                            "userId" to (userSnap.child("userId").getValue(String::class.java) ?: ""),
                            "displayName" to (userSnap.child("displayName").getValue(String::class.java) ?: "")
                        ))
                    }
                }
                userAdapter.setAllUsers(userList)
                userAdapter.filter("") // Tampilkan semua di awal
                updateVisibility()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateVisibility() {
        if (userAdapter.itemCount == 0) {
            recyclerViewSelectUser.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            recyclerViewSelectUser.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }
}