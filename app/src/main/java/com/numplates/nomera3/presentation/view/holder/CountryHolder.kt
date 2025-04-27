package com.numplates.nomera3.presentation.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.ProfileListCallback
import com.numplates.nomera3.presentation.view.view.ProfileListItem


class CountryHolder(itemView: View,
                    private val callback: ProfileListCallback) :
        RecyclerView.ViewHolder(itemView), ProfileListHolder {

    var content: View = itemView.findViewById(R.id.llContent)
    var tvName: TextView = itemView.findViewById(R.id.tvName)
    var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

    override fun bind(item: ProfileListItem) {
        Glide.with(itemView)
                .load(item.imageUrl)
                .apply(RequestOptions
                        .circleCropTransform()
                        .placeholder(R.drawable.gray_circle_transparent_shape)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(ivPicture)
        tvName.text = item.caption
    }

    init {
        content.setOnClickListener { v: View? -> callback.onClick(this) }
    }
}