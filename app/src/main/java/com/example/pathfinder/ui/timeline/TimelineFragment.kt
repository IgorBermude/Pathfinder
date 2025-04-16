package com.example.pathfinder.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.databinding.FragmentTimelineBinding
import java.util.*

class TimelineFragment : Fragment() {

    private var _binding: FragmentTimelineBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var postsAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupFab()
        setupScrollBehavior()
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(emptyList()) { post, action ->
            when (action) {
                is PostsAdapter.Action.Like -> handleLike(post)
                is PostsAdapter.Action.Comment -> showComments(post)
                is PostsAdapter.Action.Share -> sharePost(post)
                is PostsAdapter.Action.Delete -> deletePost(post)
            }
        }

        binding.rvPosts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = postsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        viewModel.posts.observe(viewLifecycleOwner) { posts ->
            postsAdapter = PostsAdapter(posts) { post, action ->
                when (action) {
                    is PostsAdapter.Action.Like -> handleLike(post)
                    is PostsAdapter.Action.Comment -> showComments(post)
                    is PostsAdapter.Action.Share -> sharePost(post)
                    is PostsAdapter.Action.Delete -> deletePost(post)
                }
            }
            binding.rvPosts.adapter = postsAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddPost.setOnClickListener {
            val newPost = Post(
                id = UUID.randomUUID().toString(),
                userId = "currentUser",
                username = "Você",
                content = "Novo post de exemplo!",
                timestamp = Date()
            )
            viewModel.addPost(newPost)
        }
    }

    private fun setupScrollBehavior() {
        binding.rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // dy > 0 = rolagem para baixo (esconde o FAB)
                // dy < 0 = rolagem para cima (mostra o FAB)
                if (dy > 0 && binding.fabAddPost.isShown) {
                    binding.fabAddPost.hide()
                } else if (dy < 0 && !binding.fabAddPost.isShown) {
                    binding.fabAddPost.show()
                }
            }
        })
    }

    private fun handleLike(post: Post) {
        val updatedPost = post.copy(
            isLiked = !post.isLiked,
            likes = if (!post.isLiked) post.likes + 1 else post.likes - 1
        )
        viewModel.updatePost(updatedPost)
    }

    private fun showComments(post: Post) {
        // Implemente a navegação para os comentários
    }

    private fun sharePost(post: Post) {
        // Implemente o compartilhamento
    }

    private fun deletePost(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Deletar postagem")
            .setMessage("Tem certeza que deseja deletar esta postagem?")
            .setPositiveButton("Deletar") { _, _ ->
                viewModel.deletePost(post.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}