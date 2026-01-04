//    package com.chats.kelompok1
//
//    import android.app.Activity
//    import android.content.Intent
//    import android.os.Bundle
//    import android.util.Log
//    import android.widget.Toast
//    import androidx.activity.enableEdgeToEdge
//    import androidx.appcompat.app.AppCompatActivity
//    import androidx.core.view.ViewCompat
//    import androidx.core.view.WindowInsetsCompat
//    import com.google.android.gms.auth.api.signin.GoogleSignIn
//    import com.google.android.gms.auth.api.signin.GoogleSignInClient
//    import com.google.android.gms.auth.api.signin.GoogleSignInOptions
//    import com.google.android.gms.common.api.ApiException
//    import com.google.android.gms.common.SignInButton
//    import com.google.firebase.auth.FirebaseAuth
//    import com.google.firebase.auth.GoogleAuthProvider
//
//    class MainActivity : AppCompatActivity() {
//
//        private lateinit var auth: FirebaseAuth
//        private lateinit var googleSignInClient: GoogleSignInClient
//
//        companion object {
//            private const val RC_SIGN_IN = 9001
//            private const val TAG = "GOOGLE_AUTH"
//        }
//
//        override fun onCreate(savedInstanceState: Bundle?) {
//            super.onCreate(savedInstanceState)
//            enableEdgeToEdge()
//            setContentView(R.layout.activity_main)
//
//            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//                insets
//            }
//
//            // 1️⃣ Firebase Auth
//            auth = FirebaseAuth.getInstance()
//
//            // 2️⃣ Google Sign-In configuration
//            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build()
//
//            googleSignInClient = GoogleSignIn.getClient(this, gso)
//
//            // 3️⃣ Google Sign-In button
//            val btnGoogle = findViewById<SignInButton>(R.id.btnGoogleSignIn)
//            btnGoogle.setOnClickListener {
//                signInWithGoogle()
//            }
//        }
//
//        private fun signInWithGoogle() {
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
//        }
//
//        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//            super.onActivityResult(requestCode, resultCode, data)
//
//            if (requestCode == RC_SIGN_IN) {
//                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//                try {
//                    val account = task.getResult(ApiException::class.java)
//                    firebaseAuthWithGoogle(account.idToken!!)
//                } catch (e: ApiException) {
//                    Log.e(TAG, "Google sign-in failed", e)
//                    Toast.makeText(this, "Google sign-in failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        private fun firebaseAuthWithGoogle(idToken: String) {
//            val credential = GoogleAuthProvider.getCredential(idToken, null)
//
//            auth.signInWithCredential(credential)
//                .addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        val user = auth.currentUser
//                        Log.d(TAG, "signInWithCredential success: ${user?.email}")
//                        Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
//
//                        // 👉 Next step: go to ChatActivity / HomeActivity
//                        // startActivity(Intent(this, ChatActivity::class.java))
//                        // finish()
//
//                    } else {
//                        Log.e(TAG, "Firebase auth failed", task.exception)
//                        Toast.makeText(this, "Firebase authentication failed", Toast.LENGTH_SHORT).show()
//                    }
//                }
//        }
//    }
//
//
//
//

package com.chats.kelompok1

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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

        // 1️⃣ Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 2️⃣ Google Sign-In configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 🔁 Auto-skip login if already signed in
        if (auth.currentUser != null) {
            goToHome()
        }

        // 3️⃣ Google Sign-In button
        val btnGoogle = findViewById<SignInButton>(R.id.btnGoogleSignIn)
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // 🔴 Logout button (optional, for testing)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
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
                    val user = auth.currentUser
                    Log.d(TAG, "signIn success: ${user?.email}")
                    Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()

                    goToHome()
                } else {
                    Log.e(TAG, "Firebase auth failed", task.exception)
                    Toast.makeText(this, "Firebase authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // ✅ Proper Logout (Firebase + Google)
    private fun logout() {
        auth.signOut()

        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun goToHome() {
        // Replace with ChatActivity / HomeActivity
        // startActivity(Intent(this, ChatActivity::class.java))
        // finish()
    }
}

