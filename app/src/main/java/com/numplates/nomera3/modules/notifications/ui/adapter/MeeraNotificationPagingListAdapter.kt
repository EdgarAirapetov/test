package com.numplates.nomera3.modules.notifications.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.meera.uikit.widgets.UiKitNotificationCellView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.adapter.MeeraViewType.Companion.fromInt
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions
import com.numplates.nomera3.modules.notifications.ui.viewholder.MeeraNotificationCellHolder
import com.numplates.nomera3.modules.notifications.ui.viewholder.MeeraNotificationSeparatorViewHolder

class MeeraNotificationPagingListAdapter(
    private val notificationListener: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit,
    differCallback: DiffUtil.ItemCallback<NotificationCellUiModel> = COMPARATOR
) : PagedListAdapter<NotificationCellUiModel, RecyclerView.ViewHolder>(differCallback) {

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.data?.infoSection == null) {
            MeeraViewType.NOTIFICATION_VIEW.type
        } else {
            MeeraViewType.HEADER_VIEW.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType.fromInt()) {
            MeeraViewType.NOTIFICATION_VIEW ->
                MeeraNotificationCellHolder(UiKitNotificationCellView(parent.context, null))

            MeeraViewType.HEADER_VIEW ->
                MeeraNotificationSeparatorViewHolder(parent.inflate(R.layout.meera_item_notification_separator))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MeeraNotificationCellHolder -> {
                val item = getItem(position) ?: return
                holder.bind(item, notificationListener)
            }

            is MeeraNotificationSeparatorViewHolder -> {
                holder.bind(getItem(position)?.data?.infoSection ?: return)
            }
        }
    }

    fun getItemForPosition(position: Int): NotificationCellUiModel? = getItem(position)

}

private val COMPARATOR =
    object : DiffUtil.ItemCallback<NotificationCellUiModel>() {

        override fun areItemsTheSame(
            oldItem: NotificationCellUiModel,
            newItem: NotificationCellUiModel
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: NotificationCellUiModel,
            newItem: NotificationCellUiModel
        ): Boolean = oldItem.data == newItem.data
    }

private enum class MeeraViewType(val type: Int) {
    NOTIFICATION_VIEW(1),
    HEADER_VIEW(2);

    companion object {
        fun Int.fromInt(): MeeraViewType = entries.first { it.type == this }
    }
}
