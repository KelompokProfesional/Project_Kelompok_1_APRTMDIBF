package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val etName = findViewById<EditText>(R.id.etProfileName)
        val etId = findViewById<EditText>(R.id.etProfileUserId)
        val tvInitial = findViewById<TextView>(R.id.tvAvatarInitial)

        val uid = auth.currentUser?.uid ?: return

        // Auto-fill nama dari akun Google untuk memudahkan user baru
        val googleName = auth.currentUser?.displayName
        etName.setText(googleName)

        // Cek data lama (jika user hanya ingin edit profil)
        database.child("users").child(uid).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("displayName").getValue(String::class.java) ?: ""
                val userId = snapshot.child("userId").getValue(String::class.java) ?: ""
                etName.setText(name)
                etId.setText(userId)
                tvInitial.text = if (name.isNotEmpty()) name.take(1).uppercase() else "U"

                // Kunci ID jika sudah ada
                if (userId.isNotEmpty()) {
                    etId.isEnabled = false
                    etId.alpha = 0.6f
                }
            }
        }

        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            val name = etName.text.toString().trim()
            val userId = etId.text.toString().trim()

            if (name.isNotEmpty() && userId.isNotEmpty()) {
                val userMap = mutableMapOf<String, Any>("displayName" to name)
                if (etId.isEnabled) userMap["userId"] = userId

                database.child("users").child(uid).updateChildren(userMap).addOnSuccessListener {
                    Toast.makeText(this, "Profil Disimpan!", Toast.LENGTH_SHORT).show()

                    // ALUR OTOMATIS: Setelah daftar, langsung ke Menu Utama
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
            }
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            auth.signOut()
            googleSignInClient.signOut().addOnCompleteListener {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}