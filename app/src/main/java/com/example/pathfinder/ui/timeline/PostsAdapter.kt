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

class PostsAdapter(
    private val posts: List<Post>,
    private val onPostAction: (Post, Action) -> Unit
) : RecyclerView.Adapter<PostsAdapter.PostViewHolder>() {

    sealed class Action {
        data class Like(val post: Post) : Action()
        data class Comment(val post: Post) : Action()
        data class Share(val post: Post) : Action()
        data class Delete(val post: Post) : Action()
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
        private val ibLike: ImageButton = itemView.findViewById(R.id.ib_like)
        private val ibComment: ImageButton = itemView.findViewById(R.id.ib_comment)
        private val ibShare: ImageButton = itemView.findViewById(R.id.ib_share)
        private val ibDelete: ImageButton = itemView.findViewById(R.id.ib_delete)

        fun bind(post: Post) {
            tvUsername.text = post.username
            tvContent.text = post.content
            tvPostTime.text = post.formattedDate()

            post.imageUrl?.let { url ->
                ivPostImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivPostImage)
            } ?: run { ivPostImage.visibility = View.GONE }

            Glide.with(itemView.context)
                .load(R.drawable.ic_profile)
                .circleCrop()
                .into(ivUserAvatar)

            ibDelete.visibility =View.VISIBLE

            ibLike.setImageResource(
                if (post.isLiked) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )

            ibLike.setOnClickListener { onPostAction(post, Action.Like(post)) }
            ibComment.setOnClickListener { onPostAction(post, Action.Comment(post)) }
            ibShare.setOnClickListener { onPostAction(post, Action.Share(post)) }
            ibDelete.setOnClickListener { onPostAction(post, Action.Delete(post)) }
        }
    }
}