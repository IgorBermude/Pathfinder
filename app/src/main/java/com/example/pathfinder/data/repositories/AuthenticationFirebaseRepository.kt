package com.example.pathfinder.data.repositories

import android.util.Log
import com.example.pathfinder.data.models.Usuario
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.text.set

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
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                // Cria um novo objeto com idUsuario preenchido
                val usuarioComId = usuario.copy(idUsuario = userId)

                // Salva no Firestore
                firestore.collection("usuarios").document(userId).set(usuarioComId)
            }
            .addOnFailureListener { e ->
                Log.e("Auth", "Erro ao registrar: ${e.message}")
            }
    }

    fun updateUser(usuario: Usuario): Task<Void> {
        if (usuario.idUsuario == null) {
            val tcs = TaskCompletionSource<Void>()
            tcs.setException(Exception("Usuário não autenticado"))
            return tcs.task
        }
        return firestore.collection("usuarios").document(usuario.idUsuario!!).set(usuario)
            .addOnSuccessListener {
                Log.d("Auth", "Usuário atualizado com sucesso")
            }
            .addOnFailureListener { e ->
                Log.e("Auth", "Erro ao atualizar usuário: ${e.message}")
            }
    }
}
