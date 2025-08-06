package com.example.pathfinder.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

object FirebaseUtil {
    fun currentUserUid(): String {
        val auth = FirebaseAuth.getInstance()
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
    }

    // Não usar pois não temos o Storage no projeto
    fun getCurrentProfilePicStorageRef(): StorageReference {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("Usuário não autenticado")
        return FirebaseStorage.getInstance().reference.child("profile_pics")
            .child(FirebaseUtil.currentUserUid())
    }

    // Usar esse.
    // Converter Uri da imagem para Base64 (Limite de arquivo 1MB no Firestore)
    fun uriToBase64(context: Context, uri: Uri): String? {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        return if (bytes != null) android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT) else null
    }

    // Converter Base64 para Bitmap
    fun base64ToBitmap(base64Str: String): Bitmap? {
        val bytes = android.util.Base64.decode(base64Str, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}