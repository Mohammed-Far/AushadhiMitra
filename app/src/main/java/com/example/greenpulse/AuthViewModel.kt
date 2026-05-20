package com.example.greenpulse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
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
    
    private val db: FirebaseFirestore by lazy { Firebase.firestore }

    private val _googleSignInClient = mutableStateOf<com.google.android.gms.auth.api.signin.GoogleSignInClient?>(null)

    fun initGoogleSignIn(context: android.content.Context) {
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            // This must match the Web Client ID in your Firebase Console
            .requestIdToken("241203154470-4nbs5880rhu4mm85if58hbvdn5qkekhh.apps.googleusercontent.com")
            .requestEmail()
            .build()
        _googleSignInClient.value = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleSignInIntent() = _googleSignInClient.value?.signInIntent

    fun firebaseAuthWithGoogle(idToken: String) {
        if (idToken.isEmpty()) {
            _authState.value = AuthState.Error("Invalid Google ID Token")
            return
        }
        
        _authState.value = AuthState.Loading
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserToFirestore(
                            uid = it.uid,
                            email = it.email ?: "",
                            name = it.displayName ?: ""
                        )
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    val errorMsg = task.exception?.message ?: "Google sign in failed."
                    _authState.value = AuthState.Error(errorMsg)
                }
            }
    }

    private fun saveUserToFirestore(uid: String, email: String, name: String = "") {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val userMap = hashMapOf(
                        "uid" to uid,
                        "email" to email,
                        "name" to name,
                        "role" to "user",
                        "createdAt" to com.google.firebase.Timestamp.now(),
                        "isSetupComplete" to false
                    )
                    db.collection("users").document(uid).set(userMap)
                        .addOnFailureListener { e ->
                            android.util.Log.e("AuthViewModel", "Failed to create user doc: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AuthViewModel", "Firestore connection error: ${e.message}")
            }
    }

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    val currentUser get() = auth.currentUser

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        if (auth.currentUser != null) {
            _authState.value = AuthState.Authenticated
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, psswrd: String) {
        if (email.isBlank() || psswrd.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }
        
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, psswrd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                } else {
                    val message = task.exception?.message ?: "Login failed"
                    _authState.value = AuthState.Error(message)
                }
            }
    }

    fun signUp(email: String, psswrd: String) {
        if (email.isBlank() || psswrd.isBlank()) {
            _authState.value = AuthState.Error("Please enter email and password")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, psswrd)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserToFirestore(
                            uid = it.uid,
                            email = it.email ?: "",
                            name = ""
                        )
                    }
                    _authState.value = AuthState.Authenticated
                } else {
                    val message = task.exception?.message ?: "Sign up failed"
                    _authState.value = AuthState.Error(message)
                }
            }
    }
}
