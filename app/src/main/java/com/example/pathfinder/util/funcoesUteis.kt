package com.example.pathfinder.util

import android.net.Uri
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

object funcoesUteis {
    suspend fun uploadImage(
        uri: Uri,
        storage: FirebaseStorage,
    ): String = withContext(Dispatchers.IO) {
        val ref = storage.reference.child("images/${UUID.randomUUID()}.jpg")

        // Espera o upload terminar
        val uploadTask = ref.putFile(uri).await()

        // Só então acessa o downloadUrl
        val url = ref.downloadUrl.await().toString()
        Log.d("funcoesUteis", "Image uploaded to: $url")

        return@withContext url
    }

    fun parseDate(dateStr: String): Timestamp? {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate = dateFormat.parse(dateStr)
            if (parsedDate != null) Timestamp(parsedDate) else null
        } catch (e: Exception) {
            null
        }
    }
}