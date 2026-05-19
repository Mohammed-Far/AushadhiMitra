package com.example.greenpulse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val error: String) : AuthState()
    object Authenticated : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore  // ← ADD THIS

    private val _googleSignInClient = mutableStateOf<com.google.android.gms.auth.api.signin.GoogleSignInClient?>(null)

    fun initGoogleSignIn(context: android.content.Context) {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken("241203154470-4nbs5880rhu4mm85if58hbvdn5qkekhh.apps.googleusercontent.com")
            .requestEmail()
            .build()
        _googleSignInClient.value = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent() = _googleSignInClient.value?.signInIntent

    fun firebaseAuthWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // ← WRITE TO FIRESTORE FOR GOOGLE SIGN IN
                    user?.let {
                        saveUserToFirestore(
                            uid = it.uid,
                            email = it.email ?: "",
                            name = it.displayName ?: ""
                        )
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Google sign in failed.")
                }
            }
    }

    // ← NEW HELPER FUNCTION
    private fun saveUserToFirestore(uid: String, email: String, name: String = "") {
        val userMap = hashMapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "role" to "user",
            "createdAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("users")
            .document(uid)
            .set(userMap)
            .addOnSuccessListener {
                android.util.Log.d("Firestore", "User saved successfully!")
            }
            .addOnFailureListener { e ->
                android.util.Log.e("Firestore", "Failed to save user: ${e.message}")
            }
    }

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    val currentUser get() = auth.currentUser

    init {
        checkUserStatus()
    }

    private fun checkUse