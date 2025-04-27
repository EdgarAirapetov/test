package com.numplates.nomera3.modules.notifications.ui.viewholder

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.string
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.itemdecorator.NotificationDividerItemDecorator

class SeparatorViewHolder(
    parent: ViewGroup
) : BaseViewHolder(parent, R.layout.item_notification_separator), NotificationDividerItemDecorator.NotDecoratable {

    private val viewClickAction = itemView.findViewById<View>(R.id.view_click_action)
    private val tvName = itemView.findViewById<TextView>(R.id.tv_name)
    private val tvAction = itemView.findViewById<TextView>(R.id.tv_action)

    fun bindTo(data: InfoSection, listener: (InfoSection) -> Unit) = with(itemView.context) {
        viewClickAction?.click { listener(data) }

        val actionName = when (data.action) {
            ACTION_TYPE_READ_ALL -> string(R.string.read_all_notification_section)
            ACTION_TYPE_DELETE_ALL -> string(R.string.delete_all_notification_section)
            else -> String.empty()
        }

        tvName?.text = data.name
        tvAction?.text = actionName
    }
}
