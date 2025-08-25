package com.example.pathfinder.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pathfinder.data.models.Postagem
import java.util.*

class TimelineViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Postagem>>(emptyList())
    val posts: LiveData<List<Postagem>> = _posts

    fun addPost(post: Postagem) {
        val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
        currentPosts.add(0, post)
        _posts.value = currentPosts
    }

    fun updatePost(updatedPost: Postagem) {
        _posts.value = _posts.value?.map { post ->
            if (post.idPostagem == updatedPost.idPostagem) updatedPost else post
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value?.filterNot { it.idPostagem == postId }
    }
}

//data class Post(
//    val id: String,
//    val userId: String,
//    val username: String,
//    val userPhoto: String? = null,  // <--- foto de perfil do usuário
//    val content: String,
//    val imageUrl: String? = null,   // <--- foto do post
//    val timestamp: Date,
//    var likes: Int = 0,
//    var comments: Int = 0,
//    var shares: Int = 0,
//    var isLiked: Boolean = false
//) {
//    fun formattedDate(): String {
//        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//        return sdf.format(timestamp)
//    }
//}

