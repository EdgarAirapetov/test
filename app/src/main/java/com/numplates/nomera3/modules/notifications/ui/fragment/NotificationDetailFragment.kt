package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentNotificationsGroupDetailBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.notifications.ui.adapter.ClearNotificationGroupAdapter
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.DetailNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewEvent
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer

class NotificationDetailFragment : BaseNotificationsFragment<FragmentNotificationsGroupDetailBinding>() {

    private val notificationViewModel by viewModels<DetailNotificationViewModel> { App.component.getViewModelFactory() }
    private val headerAdapter = ClearNotificationGroupAdapter()
    private var countUnreadNotifications: List<NotificationUiModel> = mutableListOf()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNotificationsGroupDetailBinding
        get() = FragmentNotificationsGroupDetailBinding::inflate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel.setAttributes(
            notificationGroupId = arguments?.getString(NOTIFICATION_ID),
            notificationType = arguments?.getString(NOTIFICATION_TYPE),
            isNotificationRead = arguments?.getBoolean(NOTIFICATION_IS_READ)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (notificationViewModel.notificationGroupId == null) {
            goBack()
            return
        }

        initToolbar()
        binding?.rvGroupNotifications?.let { initRecycler(it) }
        binding?.srlDetailNotifications?.setOnRefreshListener {
            notificationViewModel.refresh()
            binding?.srlDetailNotifications?.isRefreshing = false
        }
        notificationViewModel.liveNotificationPaged?.observe(this.viewLifecycleOwner) { list ->
            countUnreadNotifications = list.filter { it.isRead.not() }
            onUpdatedNotificationList(list)
            binding?.srlDetailNotifications?.isRefreshing = false
        }
        notificationViewModel.liveEvent.observe(viewLifecycleOwner) {
            handleViewEvent(it)
        }
        if (savedInstanceState == null) {
            notificationViewModel.refresh()
        }
    }

    override fun onStop() {
        super.onStop()
        onStopNotificationsFragment()
    }

    override fun initRecycler(recyclerView: RecyclerView, refreshLayout: SwipeRefreshLayout?) {
        super.initRecycler(recyclerView, refreshLayout)
        recyclerView.adapter = ConcatAdapter(headerAdapter, pagingAdapter)
    }

    override fun getAbsoluteAdapterPosition(pagingAdapterPosition: Int): Int {
        return headerAdapter.itemCount + pagingAdapterPosition
    }

    override fun restoreNotificationIfNotNull(id: String) {
        notificationViewModel.restoreNotificationIfNotNull(id)
    }

    override fun deleteNotification(id: String, isGroup: Boolean) {
        notificationViewModel.deleteNotification(id, isGroup)
    }

    override fun markAsRead(id: String, isGroup: Boolean) =
        notificationViewModel.markAsRead(id, isGroup, countUnreadNotifications)

    override fun readAll() = Unit

    override fun deleteAll() = Unit

    override fun onItemDeleted() = Unit

    override fun onProfileClick(userId: Long) = Unit

    override fun logOpenCommunity() {
        notificationViewModel.analyticsInteractor.logCommunityScreenOpened(
            AmplitudePropertyWhereCommunityOpen.NOTIFICATIONS
        )
    }

    private fun changeLoadIndicatorVisibility(isVisible: Boolean) {
        binding?.srlDetailNotifications?.isRefreshing = !isVisible
    }

    private fun initToolbar() {
        val layoutParamsStatusBar =
            binding?.statusBarUserPost?.layoutParams as AppBarLayout.LayoutParams

        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarUserPost?.layoutParams = layoutParamsStatusBar
        binding?.ivBackToolbar?.click { act.onBackPressed() }
    }

    private fun onUpdatedNotificationList(notificationList: PagedList<NotificationUiModel>) {
        configureHeaderAdapter(notificationList)
        configureConcatAdapter(notificationList)
    }

    private fun configureConcatAdapter(notificationList: PagedList<NotificationUiModel>) {
        pagingAdapter.submitList(notificationList)
    }

    private fun configureHeaderAdapter(notificationList: List<NotificationUiModel>) {
        headerAdapter.setVisibility(isVisible = false)
        val notificationGroupId = notificationViewModel.notificationGroupId ?: return
        if (notificationList.all { it.isRead }) {
            headerAdapter.onNewAction(
                actionLabel = getString(R.string.delete_all_notification_section),
                onActionClicked = {
                    showConfirmDeleteDialog { deleteNotification(id = notificationGroupId, isGroup = true) }
                }
            )
        } else {
            headerAdapter.onNewAction(
                actionLabel = getString(R.string.read_all_notification_section),
                onActionClicked = { markAsRead(id = notificationGroupId, isGroup = true) }
            )
        }
        headerAdapter.setVisibility(isVisible = notificationList.isNotEmpty())
    }

    private fun goBack() = act.onBackPressed()

    private fun handleViewEvent(event: NotificationViewEvent?) {
        when (event) {
            is NotificationViewEvent.CloseFragment -> goBack()
            is NotificationViewEvent.OpenSupportAdminChat -> {
                act?.addFragment(
                    ChatFragmentNew(), Act.LIGHT_STATUSBAR,
                    Arg(
                        IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                            initType = ChatInitType.FROM_PROFILE,
                            userId = event.adminId
                        )
                    )
                )
            }

            is NotificationViewEvent.SetLoadIndicatorVisibility ->
                changeLoadIndicatorVisibility(isVisible = event.isVisible)

            else -> Unit
        }
    }

    companion object {
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
        const val NOTIFICATION_DETAIL_REQUEST_KEY = "NOTIFICATION_DETAIL_REQUEST_KEY"
        const val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
        const val NOTIFICATION_IS_READ = "NOTIFICATION_IS_READ"
    }
}
