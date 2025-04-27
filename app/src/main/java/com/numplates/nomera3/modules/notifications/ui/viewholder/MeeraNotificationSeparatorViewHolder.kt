package com.numplates.nomera3.modules.notifications.ui.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.itemdecorator.MeeraNotificationDividerItemDecorator

class MeeraNotificationSeparatorViewHolder(
    view: View
): RecyclerView.ViewHolder(view), MeeraNotificationDividerItemDecorator.NotDecoratable {

    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)

    fun bind(data: InfoSection) {
        tvName?.text = data.name
    }
}
