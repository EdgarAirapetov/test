package com.numplates.nomera3.presentation.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.loadGlide
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.ProfileListCallback
import com.numplates.nomera3.presentation.view.view.ProfileListItem

class CityHolder(
    itemView: View, private val callback: ProfileListCallback
) : RecyclerView.ViewHolder(itemView), View.OnClickListener, ProfileListHolder {

    var content: View = itemView.findViewById(R.id.llContent)
    var tvName: TextView = itemView.findViewById(R.id.tvName)
    var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

    override fun bind(item: ProfileListItem) {
        tvName.text = item.caption
        ivPicture.loadGlide(item.imageUrl)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.content -> callback.onClick(this)
            else -> callback.onClick(this)
        }
    }

    init {
        content.setOnClickListener(this)
    }
}
