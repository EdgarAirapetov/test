package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.ui.PostCallback

class CreatePostHolder(val postCallback: PostCallback?, view: View) : RecyclerView.ViewHolder(view) {

    private val tvAddPost: TextView? = view.findViewById(R.id.tv_field_add_post)

    fun bind() {
        tvAddPost?.setOnClickListener {
            postCallback?.onCreatePostClicked(false)
        }

        itemView.tag = this //need to handle in @FeedRecyclerView
    }
}
