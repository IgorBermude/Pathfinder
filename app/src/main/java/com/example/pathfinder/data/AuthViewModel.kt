package com.example.pathfinder.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pathfinder.LoginUiState
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.data.repositories.AuthenticationFirebaseRepository
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val db: AuthenticationFirebaseRepository): ViewModel() {
    private val _authUiState = MutableStateFlow(LoginUiState.LOADING)
    val authUiState: StateFlow<LoginUiState>
        get() = _authUiState.asStateFlow()

    fun login(usuario: Usuario) {
        viewModelScope.launch {
            db.loginWithEmailAndPassword(usuario).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authUiState.value = LoginUiState.SUCCESS
                } else {
                    _authUiState.value = LoginUiState.ERROR
                }
            }
        }
    }

    fun register(usuario: Usuario) {
        viewModelScope.launch {
            db.createUserWithEmailAndPassword(usuario).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authUiState.value = LoginUiState.SUCCESS
                } else {
                    _authUiState.value = LoginUiState.ERROR
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            val repo = AuthenticationFirebaseRepository(Firebase.auth, Firebase.firestore)
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(repo) as T
            }
        }
    }
}
