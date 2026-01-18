package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnLogout: Button

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val TAG = "GOOGLE_AUTH"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase
        auth = FirebaseAuth.getInstance()

        // Views
        btnLogout = findViewById(R.id.btnLogout)
        val btnGoogle = findViewById<SignInButton>(R.id.btnGoogleSignIn)

        // Google Sign-In config
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Buat ngatur visibilitas tombol
        updateLogoutVisibility()

        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun signInWithGoogle() {
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in failed", e)
                Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Welcome ${auth.currentUser?.email}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Check if profile exists
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    FirebaseDatabase.getInstance().reference.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                // Profile exists, go to menu
                                startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                            } else {
                                // No profile, go to setup
                                startActivity(Intent(this@MainActivity, ProfileSetupActivity::class.java))
                            }
                            finish()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(this@MainActivity, "Error checking profile", Toast.LENGTH_SHORT).show()
                        }
                    })

                    updateLogoutVisibility()
                } else {
                    Toast.makeText(
                        this,
                        "Firebase authentication failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

        // After sign-in success, navigate to MenuActivity
//        startActivity(Intent(this, MenuActivity::class.java))
//        finish()
    private fun logout() {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Biar tombol gak keliatan
            updateLogoutVisibility()
        }
    }

    // Buat update status login
    private fun updateLogoutVisibility() {
        btnLogout.visibility =
            if (auth.currentUser != null) View.VISIBLE else View.GONE
    }
}