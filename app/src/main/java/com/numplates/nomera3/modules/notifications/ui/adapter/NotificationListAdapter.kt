package com.numplates.nomera3.modules.notifications.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.notifications.ui.adapter.ViewType.Companion.fromInt
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions
import com.numplates.nomera3.modules.notifications.ui.viewholder.NotificationViewHolder
import com.numplates.nomera3.modules.notifications.ui.viewholder.SeparatorViewHolder

class NotificationListAdapter(
    private val separatorListener: (InfoSection) -> Unit,
    private val notificationListener: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit,
    private val isMomentsEnabled: Boolean,
    differCallback: DiffUtil.ItemCallback<NotificationUiModel> = COMPARATOR
) : ListAdapter<NotificationUiModel, RecyclerView.ViewHolder>(differCallback), IDeleting {

    private val deletingSet = hashSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType.fromInt()) {
            ViewType.NOTIFICATION_VIEW -> NotificationViewHolder(parent)
            else -> SeparatorViewHolder(parent)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.infoSection == null) {
            ViewType.NOTIFICATION_VIEW.type
        } else {
            ViewType.HEADER_VIEW.type
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificationViewHolder -> {
                val data = getItem(position) ?: return
                holder.bindTo(notificationListener, data, this, isMomentsEnabled)
            }

            is SeparatorViewHolder -> {
                val data = getItem(position)?.infoSection ?: return
                holder.bindTo(data, separatorListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.forEach { payload ->
                when (payload) {
                    is NotificationPayload.TimeAgo -> (holder as? NotificationViewHolder)?.bindTimeAgo(payload.timeAgo)
                }
            }
        }
    }

    fun getItemForPosition(position: Int): NotificationUiModel? = getItem(position)

    override fun setDeleting(id: String) {
        deletingSet.add(id)
    }

    override fun removeDeleting(id: String) {
        deletingSet.remove(id)
    }

    override fun isDeleting(id: String) = deletingSet.contains(id)
}

private val COMPARATOR =
    object : DiffUtil.ItemCallback<NotificationUiModel>() {

        override fun areItemsTheSame(
            oldItem: NotificationUiModel,
            newItem: NotificationUiModel
        ): Boolean {
            return oldItem.id == newItem.id
                && oldItem.infoSection == newItem.infoSection
        }

        override fun areContentsTheSame(
            oldItem: NotificationUiModel,
            newItem: NotificationUiModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun getChangePayload(oldItem: NotificationUiModel, newItem: NotificationUiModel): Any? {
            return if (oldItem.timeAgo != newItem.timeAgo) {
                NotificationPayload.TimeAgo(newItem.timeAgo)
            } else {
                super.getChangePayload(oldItem, newItem)
            }
        }
    }

private sealed interface NotificationPayload {
    data class TimeAgo(val timeAgo: String) : NotificationPayload
}

private enum class ViewType(val type: Int) {
    NOTIFICATION_VIEW(1),
    HEADER_VIEW(2);

    companion object {
        fun Int.fromInt(): ViewType = entries.first { it.type == this }
    }
}

interface IDeleting {
    fun setDeleting(id: String)
    fun removeDeleting(id: String)
    fun isDeleting(id: String): Boolean
}
