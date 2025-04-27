package com.numplates.nomera3.presentation.view.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.callback.ProfileVehicleListCallback
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import com.numplates.nomera3.presentation.view.widgets.VipView

class FriendProfileHolder(itemView: View,
                          private val callback: ProfileVehicleListCallback) :
        RecyclerView.ViewHolder(itemView), ProfileListHolder {

    var content: View = itemView.findViewById(R.id.llContent)
    var vipView: VipView = itemView.findViewById(R.id.vipView123)

    override fun bind(item: ProfileListItem) {
        /** STUB */
    }

    init {
        content.setOnClickListener { v: View? -> callback.onClick(this) }
    }
}