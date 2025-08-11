package com.example.pathfinder.util

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import kotlin.div

object FuncoesUteis {
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

    fun trocarTextViewPorEditText(parent: ViewGroup, textView: TextView): EditText {
        val index = parent.indexOfChild(textView)
        val editText = EditText(textView.context).apply {
            id = textView.id
            layoutParams = textView.layoutParams
            setText(textView.text)
            hint = textView.hint
            textSize = textView.textSize / resources.displayMetrics.scaledDensity
            setTextColor(textView.currentTextColor)
            typeface = textView.typeface
        }
        parent.removeViewAt(index)
        parent.addView(editText, index)
        return editText
    }

    fun trocarEditTextPorTextView(parent: ViewGroup, editText: EditText, textView: TextView): TextView {
        textView.text = editText.text
        val index = parent.indexOfChild(editText)
        parent.removeViewAt(index)
        parent.addView(textView, index)
        return textView
    }
}