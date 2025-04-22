package com.example.pathfinder.data.repositories

import com.example.pathfinder.data.models.Usuario
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthenticationFirebaseRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun loginWithEmailAndPassword(usuario: Usuario): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(usuario.emailUsuario ?: "", usuario.senhaUsuario ?: "")
    }

    fun createUserWithEmailAndPassword(usuario: Usuario): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(usuario.emailUsuario ?: "", usuario.senhaUsuario ?: "")
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid
                if (userId != null) {
                    firestore.collection("usuarios").document(userId).set(usuario)
                }
            }
    }
}
