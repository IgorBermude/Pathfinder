package com.example.pathfinder.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pathfinder.data.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import at.favre.lib.crypto.bcrypt.BCrypt

class UsuarioRepository {

    private val _usuarioLogado = MutableLiveData<Usuario?>()
    val usuarioLogado: LiveData<Usuario?> = _usuarioLogado

    fun carregarUsuarioLogado() {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        _usuarioLogado.value = document.toObject(Usuario::class.java)
                    } else {
                        _usuarioLogado.value = null
                    }
                }
                .addOnFailureListener {
                    _usuarioLogado.value = null
                }
        } else {
            _usuarioLogado.value = null
        }
    }

    fun adicionarFotoPerfilAoUsuario(usuario: Usuario, fotoUrl: String, onResult: (Boolean) -> Unit) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("usuarios").document(userId)
            .update("fotoUsuario", fotoUrl)
            .addOnSuccessListener {
                usuario.fotoUsuario = fotoUrl
                _usuarioLogado.value = usuario
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun criptografarSenha(senha: String): String {
        val senhaHash = BCrypt.withDefaults().hashToString(12, senha.toCharArray())
        return senhaHash
    }

    fun verificarSenha(senha: String, senhaHash: String): Boolean {
        val resultado = BCrypt.verifyer().verify(senha.toCharArray(), senhaHash)
        return resultado.verified
    }

    fun buscarPorEmail(email: String): LiveData<Usuario?> {
        val usuarioLiveData = MutableLiveData<Usuario?>()
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("usuarios")
            .whereEqualTo("emailUsuario", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val usuario = document.toObject(Usuario::class.java)
                    usuarioLiveData.value = usuario
                } else {
                    usuarioLiveData.value = null
                }
            }
            .addOnFailureListener {
                usuarioLiveData.value = null
            }
        return usuarioLiveData
    }
}