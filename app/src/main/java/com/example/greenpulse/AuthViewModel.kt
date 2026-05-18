package com.example.greenpulse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    val currentUser get() = auth.currentUser

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            _authState.value = AuthState.Authenticated
        } else {
            _authState.value = AuthState.Idle
        }
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.sendEmailVerification()
                        ?.addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                auth.signOut() // Force logout until verified
                                _authState.value = AuthState.Success("Verification email sent! Please verify before logging in.")
                            } else {
                                _authState.value = AuthState.Error(verificationTask.exception?.message ?: "Failed to send verification email.")
                            }
                        }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Sign up failed.")
                }
            }
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null && user.isEmailVerified) {
                        _authState.value = AuthState.Authenticated
                    } else {
                        auth.signOut()
                        _authState.value = AuthState.Error("Please verify your email first. Check your inbox.")
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed.")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
