package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MenuActivity : AppCompatActivity() {

    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var userAdapter: UserAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Inisialisasi View dari activity_menu.xml
        recyclerViewMenu = findViewById(R.id.recyclerViewMenu)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        val searchView = findViewById<SearchView>(R.id.searchViewUsers)
        val cvProfileBtn = findViewById<CardView>(R.id.cvProfileBtn)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Setup Adapter dengan listener klik untuk masuk ke ChatActivity
        userAdapter = UserAdapter { userMap ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("TARGET_USER_ID", userMap["uid"])
            startActivity(intent)
        }

        recyclerViewMenu.layoutManager = LinearLayoutManager(this)
        recyclerViewMenu.adapter = userAdapter

        fetchAllData() // Ambil data awal (Users untuk Search & Chats untuk Home)
        setupBottomNavigation(bottomNav)

        // Logika Hybrid Search: Chat Aktif vs Semua User
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                isSearching = !newText.isNullOrEmpty()
                userAdapter.filter(newText ?: "")
                updateVisibility()
                return true
            }
        })

        // Navigasi ke halaman profil yang sudah ada
        cvProfileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileSetupActivity::class.java))
        }
    }

    private fun setupBottomNavigation(nav: BottomNavigationView) {
        nav.selectedItemId = R.id.nav_chats
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chats -> true // Tetap di halaman ini
                R.id.nav_users -> {
                    // Membuka fitur Private Room (sebelumnya Search)
                    startActivity(Intent(this, JoinRoomActivity::class.java))
                    false
                }
                R.id.nav_community -> {
                    // Membuka Community Chat
                    startActivity(Intent(this, CommunityChatActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun fetchAllData() {
        val myUid = auth.currentUser?.uid ?: return

        // 1. Ambil database SEMUA user untuk kebutuhan pencarian
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val allList = mutableListOf<Map<String, String>>()
                for (userSnap in snapshot.children) {
                    val uid = userSnap.key ?: ""
                    if (uid != myUid) {
                        allList.add(mapOf(
                            "uid" to uid,
                            "userId" to (userSnap.child("userId").getValue(String::class.java) ?: ""),
                            "displayName" to (userSnap.child("displayName").getValue(String::class.java) ?: "")
                        ))
                    }
                }
                userAdapter.setAllUsers(allList)
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 2. Pantau folder 'chats' untuk menampilkan chat aktif secara otomatis
        database.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeUids = mutableListOf<String>()
                for (chatSnap in snapshot.children) {
                    val chatId = chatSnap.key ?: ""
                    // Filter chat yang melibatkan saya dan bukan folder ROOM
                    if (chatId.contains(myUid) && chatSnap.hasChildren() && !chatId.startsWith("ROOM_")) {
                        val otherUid = chatId.replace(myUid, "").replace("_", "")
                        activeUids.add(otherUid)
                    }
                }
                fetchActiveDetails(activeUids)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchActiveDetails(uids: List<String>) {
        database.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeList = mutableListOf<Map<String, String>>()
                for (uid in uids) {
                    val userSnap = snapshot.child(uid)
                    if (userSnap.exists()) {
                        activeList.add(mapOf(
                            "uid" to uid,
                            "userId" to (userSnap.child("userId").getValue(String::class.java) ?: ""),
                            "displayName" to (userSnap.child("displayName").getValue(String::class.java) ?: "")
                        ))
                    }
                }
                // Tampilkan di halaman utama jika tidak sedang mencari
                userAdapter.setActiveChats(activeList, isSearching)
                updateVisibility()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateVisibility() {
        if (userAdapter.itemCount == 0) {
            recyclerViewMenu.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            recyclerViewMenu.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }
}