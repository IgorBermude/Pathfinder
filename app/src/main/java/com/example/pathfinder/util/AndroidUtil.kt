package com.example.pathfinder.util

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


object AndroidUtil {
    fun setProfilePic(context: Context, imageUri: Uri?, imageView: ImageView) {
        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView)
    }
}