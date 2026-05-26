package com.example.greenpulse

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val error: String) : AuthState()
    object Authenticated : AuthState()
}

class AuthViewModel : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Authenticated)
    val authState: State<AuthState> = _authState

    val currentUser get() = null

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        _authState.value = AuthState.Authenticated
    }

    fun signUp(email: String, password: String) {
        _authState.value = AuthState.Authenticated
    }

    fun login(email: String, password: String) {
        _authState.value = AuthState.Authenticated
    }

    fun signOut() {
        _authState.value = AuthState.Idle
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
