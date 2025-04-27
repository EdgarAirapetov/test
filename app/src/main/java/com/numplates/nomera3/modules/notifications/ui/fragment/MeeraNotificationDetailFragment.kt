package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.viewmodel.MeeraDetailNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewEvent

class MeeraNotificationDetailFragment : MeeraBaseNotificationsFragment() {

    private val notificationViewModel by viewModels<MeeraDetailNotificationViewModel> {
        App.component.getViewModelFactory()
    }
    private var countUnreadNotifications: List<NotificationCellUiModel> = mutableListOf()

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
        setupBackButton()
        binding?.apply {
            notificationsFragmentNaw.addScrollableView(rvNotifications)
            initRecycler(rvNotifications)
        }
        notificationViewModel.liveNotificationPaged?.observe(this.viewLifecycleOwner) { list ->
            countUnreadNotifications = list.filter { it.data.isRead.not() }
            onUpdatedNotificationList(list)
        }
        notificationViewModel.liveEvent.observe(viewLifecycleOwner) {
            handleViewEvent(it)
        }
    }

    override fun onStart() {
        super.onStart()
        notificationViewModel.requestCounter()
    }

    override fun onStop() {
        super.onStop()
        onStopNotificationsFragment()
    }

    override fun restoreNotificationIfNotNull(id: String) {
        notificationViewModel.restoreNotificationIfNotNull(id)
    }

    override fun deleteNotification(id: String, isGroup: Boolean) {
        notificationViewModel.deleteNotification(id, isGroup)
    }

    override fun markAsRead(id: String, isGroup: Boolean) =
        notificationViewModel.markAsRead(id, isGroup, countUnreadNotifications)

    override fun readAll() {
        val notificationGroupId = notificationViewModel.notificationGroupId ?: return
        markAsRead(id = notificationGroupId, isGroup = true)
    }

    override fun deleteAll() {
        val notificationGroupId = notificationViewModel.notificationGroupId ?: return
        deleteNotification(id = notificationGroupId, isGroup = true)
    }

    override fun onItemDeleted() = Unit

    override fun logOpenCommunity() {
        notificationViewModel.analyticsInteractor.logCommunityScreenOpened(
            AmplitudePropertyWhereCommunityOpen.NOTIFICATIONS
        )
    }

    private fun setupBackButton() {
        binding.notificationsFragmentNaw.showBackArrow = true
        binding.notificationsFragmentNaw.backButtonClickListener = {
            findNavController().popBackStack()
        }
    }

    private fun onUpdatedNotificationList(notificationList: PagedList<NotificationCellUiModel>) {
        pagingAdapter.submitList(notificationList)
        setupNotificationRemoveActions(notificationList)
    }

    private fun setupNotificationRemoveActions(notificationList: PagedList<NotificationCellUiModel>) {
        val infoSection = if (notificationList.all { it.data.isRead }) {
            InfoSection(id = String.empty(), action = ACTION_TYPE_DELETE_ALL)
        } else {
            InfoSection(id = String.empty(), action = ACTION_TYPE_READ_ALL)
        }
        setupNotificationRemoveActionsName(infoSection)
        setupNotificationRemoveActionsClick(infoSection)
    }

    private fun setupNotificationRemoveActionsName(infoSection: InfoSection?) {
        infoSection?.let { info ->
            when (info.action) {
                ACTION_TYPE_READ_ALL -> setInfoActionText(R.string.read_all_notification_section)
                ACTION_TYPE_DELETE_ALL -> setInfoActionText(R.string.delete_all_notification_section)
                else -> binding?.ivRemoveAllNotifications?.gone()
            }
        }
    }

    private fun setupNotificationRemoveActionsClick(infoSection: InfoSection?) {
        binding?.ivRemoveAllNotifications?.click {
            infoSection?.let { info ->
                when (info.action) {
                    ACTION_TYPE_DELETE_ALL -> showConfirmDeleteDialog { deleteAll() }
                    ACTION_TYPE_READ_ALL -> readAll()
                }
            }
        }
    }

    private fun setInfoActionText(textRes: Int) {
        binding?.ivRemoveAllNotifications?.visible()
        binding?.ivRemoveAllNotifications?.text = getString(textRes)
    }

    private fun goBack() {
        findNavController().popBackStack()
    }

    override fun onUserSetCache(user: User?) {
        notificationViewModel.cacheUserProfileForChat(user)
    }

    private fun handleViewEvent(event: NotificationViewEvent?) {
        when (event) {
            is NotificationViewEvent.CloseFragment -> goBack()
            is NotificationViewEvent.OpenSupportAdminChat -> openChatFragment(event.adminId.toString())
            is NotificationViewEvent.SetLoadIndicatorVisibility -> Unit
            is NotificationViewEvent.UpdateGlobalNotificationCounter ->
                updateNotificationCounter(event.count)

            else -> Unit
        }
    }

    companion object {
        const val NOTIFICATION_ID = "NOTIFICATION_ID"
        const val NOTIFICATION_TYPE = "NOTIFICATION_TYPE"
        const val NOTIFICATION_IS_READ = "NOTIFICATION_IS_READ"
    }

}
