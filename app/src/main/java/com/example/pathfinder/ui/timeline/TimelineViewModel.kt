package com.example.pathfinder.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*
import kotlin.random.Random

class TimelineViewModel : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>().apply {
        value = generateMockPosts()
    }
    val posts: LiveData<List<Post>> = _posts

    fun addPost(post: Post) {
        val currentPosts = _posts.value?.toMutableList() ?: mutableListOf()
        currentPosts.add(0, post)
        _posts.value = currentPosts
    }

    fun updatePost(updatedPost: Post) {
        _posts.value = _posts.value?.map { post ->
            if (post.id == updatedPost.id) updatedPost else post
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value?.filterNot { it.id == postId }
    }

    private fun generateMockPosts(): List<Post> {
        return listOf(
            Post(
                id = "1",
                userId = "user1",
                username = "João Silva",
                content = "Acabei de completar uma trilha incrível no Parque Nacional!",
                imageUrl = "https://picsum.photos/seed/${Random.nextInt()}/300/200",
                timestamp = Date(System.currentTimeMillis() - 3600000)
            ),
            Post(
                id = "2",
                userId = "user2",
                username = "Maria Oliveira",
                content = "Dicas para iniciantes em trekking!",
                imageUrl = "https://picsum.photos/seed/${Random.nextInt()}/300/200",
                timestamp = Date(System.currentTimeMillis() - 7200000)
            )
        )
    }
}

data class Post(
    val id: String,
    val userId: String,
    val username: String,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Date,
    var likes: Int = 0,
    var comments: Int = 0,
    var shares: Int = 0,
    var isLiked: Boolean = false
) {
    fun formattedDate(): String {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp)
    }
}