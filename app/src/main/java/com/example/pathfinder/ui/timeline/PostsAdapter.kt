package com.example.pathfinder.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pathfinder.R
import com.example.pathfinder.data.models.Postagem
import com.example.pathfinder.data.models.Usuario
import com.example.pathfinder.util.FirebaseUtil
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PostsAdapter(
    private val currentUserId: String,
    private val onPostAction: (Postagem, Action) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    private var posts = emptyList<Postagem>()

    sealed class Action {
        data class Like(val post: Postagem) : Action()
        data class Comment(val post: Postagem) : Action()
        data class Share(val post: Postagem) : Action()
        data class Delete(val post: Postagem) : Action()
    }

    fun submitList(newPosts: List<Postagem>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        private val tvPostTime: TextView = itemView.findViewById(R.id.tv_post_time)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_post_content)
        private val ivPostImage: ImageView = itemView.findViewById(R.id.iv_post_image)
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val ibDelete: ImageButton = itemView.findViewById(R.id.ib_delete)

        fun bind(post: Postagem) {
            tvContent.text = post.descricaoPostagem
            tvPostTime.text = post.horaPostagem?.toLongOrNull()?.let { formatTimestamp(it) } ?: ""

            // Buscar nome do usuário do Firebase se não estiver no post
            post.usuarioPostagemId?.let { userId ->
                FirebaseFirestore.getInstance().collection("usuarios")
                    .document(userId)
                    .get()
                    .addOnSuccessListener { doc ->
                        val usuario = doc.toObject(Usuario::class.java)
                        tvUsername.text = usuario?.nomeUsuario ?: "Usuário"
                        // Avatar
                        if(!usuario?.fotoUsuario.isNullOrEmpty()) {
                            val bitmap = FirebaseUtil.base64ToBitmap(usuario?.fotoUsuario!!)
                            Glide.with(itemView.context)
                                .load(bitmap)
                                .circleCrop()
                                .into(ivUserAvatar)
                        } else {
                            ivUserAvatar.setImageResource(R.drawable.ic_profile)
                        }
                    }
            }

            // Imagem do post
            if(!post.fotoPostagem.isNullOrEmpty()) {
                ivPostImage.visibility = View.VISIBLE
                val bitmap = FirebaseUtil.base64ToBitmap(post.fotoPostagem!!)
                Glide.with(itemView.context)
                    .load(bitmap)
                    .centerCrop()
                    .into(ivPostImage)
            } else {
                ivPostImage.visibility = View.GONE
            }

            // Botão deletar apenas para o autor
            ibDelete.visibility = if(post.usuarioPostagemId == currentUserId) View.VISIBLE else View.GONE
            ibDelete.setOnClickListener { onPostAction(post, Action.Delete(post)) }
        }

        private fun formatTimestamp(timestamp: Long): String {
            val sdf = java.text.SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
            return sdf.format(timestamp)
        }
    }
}

