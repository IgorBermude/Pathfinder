package com.example.pathfinder.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Postagem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class TimelineFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private var postsListener: ListenerRegistration? = null
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var rvPosts: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_timeline, container, false)

        rvPosts = view.findViewById(R.id.rv_posts)
        setupRecyclerView()

        // FAB para criar post
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_create_post)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_timelineFragment_to_createPostFragment)
        }

        return view
    }

    private fun setupRecyclerView() {
        postsAdapter = PostsAdapter(auth.currentUser?.uid ?: "") { post, action ->
            when (action) {
                is PostsAdapter.Action.Delete -> deletePost(post)
                else -> {}
            }
        }
        // safe context usage: requireContext() is ok here because onCreateView -> fragment is attached,
        // but keep as is. RecyclerView config
        rvPosts.layoutManager = LinearLayoutManager(requireContext())
        rvPosts.adapter = postsAdapter

        loadPosts()
    }

    private fun loadPosts() {
        // guarda o ListenerRegistration para poder remover quando o fragment for destruído
        postsListener = firestore.collection("postagens")
            .orderBy("horaPostagem", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // Se o fragment não estiver anexado, ignora o callback
                if (!isAdded) return@addSnapshotListener

                val ctx = context
                if (error != null) {
                    ctx?.let { Toast.makeText(it, "Erro ao carregar postagens.", Toast.LENGTH_SHORT).show() }
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull {
                    it.toObject(Postagem::class.java)
                } ?: emptyList()

                postsAdapter.submitList(posts)
            }
    }

    private fun deletePost(post: Postagem) {
        firestore.collection("postagens").document(post.idPostagem ?: return)
            .delete()
            .addOnSuccessListener {
                context?.let { Toast.makeText(it, "Post deletado", Toast.LENGTH_SHORT).show() }
            }
            .addOnFailureListener {
                context?.let { Toast.makeText(it, "Erro ao deletar post", Toast.LENGTH_SHORT).show() }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postsListener?.remove()
        postsListener = null
    }
}
