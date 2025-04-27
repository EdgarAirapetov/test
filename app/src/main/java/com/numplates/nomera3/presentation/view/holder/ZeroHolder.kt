package com.numplates.nomera3.presentation.view.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.ProfileListZeroDataCallback

class ZeroHolder(itemView: View,
                 private val callback: ProfileListZeroDataCallback) :
        RecyclerView.ViewHolder(itemView) {

    var content: View = itemView.findViewById(R.id.content)
    var tvEmptyGarage: TextView = itemView.findViewById(R.id.tvEmptyGarage)
    var ivPicture: ImageView = itemView.findViewById(R.id.ivPicture)

    fun bind() {
        ivPicture.setImageResource(callback.zeroDataImageId)
        tvEmptyGarage.text = callback.zeroDataText
    }
}