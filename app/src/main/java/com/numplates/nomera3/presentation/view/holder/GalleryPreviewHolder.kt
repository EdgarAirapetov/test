package com.numplates.nomera3.presentation.view.holder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.GalleryPreviewCallback


class GalleryPreviewHolder(itemView: View,
                           private val callback: GalleryPreviewCallback?) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

    var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)
    var header: View = itemView.findViewById(R.id.llContent)

    init {
        header.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (callback != null) {
            when (v.id) {
                R.id.llContent -> callback.onClick(this)
                else -> callback.onClick(this)
            }
        }
    }


}