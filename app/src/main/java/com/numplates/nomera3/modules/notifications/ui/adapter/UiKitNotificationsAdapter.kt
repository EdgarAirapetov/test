package com.numplates.nomera3.modules.notifications.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.meera.uikit.widgets.UiKitNotificationCellView
import com.numplates.nomera3.modules.notifications.ui.adapter.ViewTypeEnum.Companion.fromInt
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.viewholder.SeparatorViewHolder

@Deprecated("Используется только для тестов UiKit notifications")
class UiKitNotificationsAdapter(
    differCallback: DiffUtil.ItemCallback<NotificationCellUiModel> = COMPARATOR
): PagedListAdapter<NotificationCellUiModel, RecyclerView.ViewHolder>(differCallback) {

    override fun getItemViewType(position: Int): Int =
        if (getItem(position)?.data?.infoSection == null)
            ViewTypeEnum.NOTIFICATION_VIEW.type
        else
            ViewTypeEnum.HEADER_VIEW.type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType.fromInt()) {
            ViewTypeEnum.NOTIFICATION_VIEW -> NotificationCellHolder(UiKitNotificationCellView(parent.context, null))
            else -> SeparatorViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificationCellHolder -> {
                val item = getItem(position) ?: return
                holder.bind(item)
            }

            is SeparatorViewHolder ->
                holder.bindTo(getItem(position)?.data?.infoSection ?: return, listener = {})
        }
    }

    inner class NotificationCellHolder(val view: View): RecyclerView.ViewHolder(view) {
        private val cellView = view as UiKitNotificationCellView
        fun bind(item: NotificationCellUiModel) {
            cellView.setConfig(item.config)
        }
    }




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
        ): Boolean = oldItem == newItem
    }

private enum class ViewTypeEnum(val type: Int) {
    NOTIFICATION_VIEW(1),
    HEADER_VIEW(2);

    companion object {
        fun Int.fromInt(): ViewTypeEnum = values().first { it.type == this }
    }
}

// TODO: EXAMPLES

// Fragment
//internal val uiKitAdapter by lazy {
//    UiKitNotificationsAdapter()
//}
// uiKitAdapter.submitList(page)


// ViewModel
// private val uiKitNotificationCellMapper: UiKitNotificationCellMapper
//fun setupPagingNotification() {
//    val roadPostsDb = ds
//        .notificationDao()
//        .getAllNotificationPaged()
//        .map { domainNotificationMapper.apply(it) }
//        .map { uiNotificationMapper.apply(it) }
//        .map { uiKitNotificationCellMapper.mapToNotificationUiModel(it)  }
//
//    liveNotificationPaged = LivePagedListBuilder(roadPostsDb, preparePagingConfig())
//        .setBoundaryCallback(boundaryCallback)
//        .setFetchExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))
//        .build()
//
//    boundaryCallback.networkState.observeForever(fetchObserver)
//}
