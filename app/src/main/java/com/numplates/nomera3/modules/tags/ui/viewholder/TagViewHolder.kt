package com.numplates.nomera3.modules.tags.ui.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity

class TagViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
    private val userName: TextView = v.findViewById(R.id.tv_user_name_tag)
    private val uniqueName: TextView = v.findViewById(R.id.tv_tag_unique_name)
    private val userAvatar: ImageView = v.findViewById(R.id.iv_user_avatar_tag)
    private val tagItemContainer: ConstraintLayout = v.findViewById(R.id.cl_tag_item_container)

    fun bind(data: UITagEntity, darkBackground: Boolean, onTagClick: (UITagEntity) -> Unit) {
        userName.text = data.userName ?: ""

        uniqueName.text = "@" + data.uniqueName

        data.image?.let {
            val options = RequestOptions()
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.fill_8_round)
                    .error(R.drawable.fill_8_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)

            Glide.with(userAvatar.context)
                    .load(it)
                    .apply(options)
                    .into(userAvatar)
        }

        tagItemContainer.setOnClickListener {
            onTagClick(data)
        }

        if (darkBackground) {
            userName.setTextColor(ContextCompat.getColor(itemView.context, R.color.white_1000))
            uniqueName.setTextColor(ContextCompat.getColor(itemView.context, R.color.transparent_white))
        }
    }
}
