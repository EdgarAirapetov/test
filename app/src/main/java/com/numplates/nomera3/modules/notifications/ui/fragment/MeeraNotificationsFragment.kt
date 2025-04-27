package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideScaleDown
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showScaleUp
import com.meera.core.extensions.view.addOnScrollWithBottomSheetListener
import com.meera.core.extensions.visible
import com.meera.db.models.notifications.ACTION_TYPE_DELETE_ALL
import com.meera.db.models.notifications.ACTION_TYPE_READ_ALL
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.notifications.ui.entity.InfoSection
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationScreenState
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.viewmodel.MeeraNotificationViewModel
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewEvent
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.view.adapter.AnyChangeDataObserver
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val DELAY_AFTER_SCROLLING_STATE_CHANGE_MLS = 500L
private const val NOTIFICATIONS_FLOW_DEBOUNCE = 250L
private const val NOTIFICATIONS_REMOVED_DELAY = 200L

/**
 * https://www.figma.com/file/wyLhqHbHkvWWjLHznv6Wz8/Social-Chat-New?type=design&node-id=1-12&mode=design
 */
class MeeraNotificationsFragment : MeeraBaseNotificationsFragment() {

    private val notificationViewModel by viewModels<MeeraNotificationViewModel> {
        App.component.getViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationViewModel.observeMoments()
        notificationViewModel.refresh()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        initObservers()

        NavigationManager.getManager().getTopBehaviour()?.isDraggable = false

        NavigationManager.getManager().getBottomBehaviour()?.let {
            binding.rvNotifications
                .addOnScrollWithBottomSheetListener(it, DELAY_AFTER_SCROLLING_STATE_CHANGE_MLS)
        }
    }

    override fun onStart() {
        super.onStart()
        notificationViewModel.requestCounter()
    }

    override fun restoreNotificationIfNotNull(id: String) {
        notificationViewModel.restoreNotificationIfNotNull(id)
    }

    override fun deleteNotification(id: String, isGroup: Boolean) {
        notificationViewModel.deleteNotification(id, isGroup)
    }

    override fun markAsRead(id: String, isGroup: Boolean) {
        notificationViewModel.markAsRead(id, isGroup)
    }

    override fun readAll() {
        clearNotificationCounter()
        notificationViewModel.readAll()
    }

    override fun deleteAll() {
        notificationViewModel.deleteAll()
    }

    override fun onItemDeleted() {
        doDelayed(NOTIFICATIONS_REMOVED_DELAY) {
            binding.tvNotificationsZeroData.isGone = pagingAdapter.itemCount > 0
        }
    }

    override fun onUserSetCache(user: User?) {
        notificationViewModel.cacheUserProfileForChat(user)
    }

    override fun logOpenCommunity() {
        notificationViewModel.logCommunityScreenOpened()
    }

    fun deleteNotificationsWhenUserBlocked() {
        notificationViewModel.deleteNotificationsWhenUserBlocked()
    }

    private fun setupViews() {
        binding.apply {
            notificationsFragmentNaw.addScrollableView(rvNotifications)
            initRecycler(recyclerView = rvNotifications)
            setupSwipeDeleteHandler(recyclerView = rvNotifications)
            btnRefreshNotifications.setThrottledClickListener {
                refreshForce()
            }
            rvNotifications.adapter?.registerAdapterDataObserver(object : AnyChangeDataObserver() {
                override fun changesTriggered() = updateZeroDataState()
            })
        }
    }

    private fun refreshForce() {
        notificationViewModel.refresh()
        binding.btnRefreshNotifications.hideScaleDown()
    }

    private fun initObservers() {
        notificationViewModel.liveNotificationPaged
            .distinctUntilChanged()
            .observe(viewLifecycleOwner) { pagedList ->
                performActionWhenIdle {
                    pagingAdapter.submitList(pagedList)
                    val firstInfoSection = pagedList.firstOrNull()?.data?.infoSection
                    setupNotificationRemoveActions(firstInfoSection)
                }
            }

        notificationViewModel.liveViewEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach(::handleViewEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        @Suppress("OPT_IN_USAGE")
        notificationViewModel.screenStateFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .debounce(NOTIFICATIONS_FLOW_DEBOUNCE)
            .onEach(::handleScreenState)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupNotificationRemoveActions(infoSection: InfoSection?) {
        setupNotificationRemoveActionsName(infoSection)
        setupNotificationRemoveActionsClick(infoSection)
    }

    private fun performActionWhenIdle(action: () -> Unit) {
        val behavior = NavigationManager.getManager().getBottomBehaviour() ?: return
        val movingStates = arrayOf(BottomSheetBehavior.STATE_DRAGGING, BottomSheetBehavior.STATE_SETTLING)
        if (behavior.state !in movingStates) {
            action.invoke()
        } else {
            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState !in movingStates) {
                        action.invoke()
                        behavior.removeBottomSheetCallback(this)
                    }
                }
            })
        }
    }

    private fun setupNotificationRemoveActionsName(infoSection: InfoSection?) {
        infoSection?.let { info ->
            when (info.action) {
                ACTION_TYPE_READ_ALL -> setInfoActionText(R.string.read_all_notification_section)
                ACTION_TYPE_DELETE_ALL -> setInfoActionText(R.string.delete_all_notification_section)
                else -> binding.ivRemoveAllNotifications.gone()
            }
        }
    }

    private fun setupNotificationRemoveActionsClick(infoSection: InfoSection?) {
        binding.ivRemoveAllNotifications.setThrottledClickListener {
            infoSection?.let { info ->
                when (info.action) {
                    ACTION_TYPE_DELETE_ALL -> showConfirmDeleteDialog { deleteAll() }
                    ACTION_TYPE_READ_ALL -> readAll()
                }
            }
        }
    }

    private fun setInfoActionText(textRes: Int) {
        binding.ivRemoveAllNotifications.visible()
        binding.ivRemoveAllNotifications.text = getString(textRes)
    }

    private fun updateZeroDataState() {
        val isNotificationsEmpty = pagingAdapter.itemCount == 0
        binding.ivRemoveAllNotifications.isVisible = isNotificationsEmpty.not()
    }

    private fun handleScreenState(screenState: NotificationScreenState) {
        binding.tvNotificationsZeroData.isVisible = screenState.isEmptyNotifications
        binding.pbNotificationsLoader.isVisible = screenState.isInitialLoading
    }

    private fun handleViewEvent(event: NotificationViewEvent) {
        when (event) {
            is NotificationViewEvent.ShowRefreshBtn -> {
                binding.btnRefreshNotifications.showScaleUp()
            }

            is NotificationViewEvent.HideRefreshBtn -> {
                binding.btnRefreshNotifications.hideScaleDown()
            }

            is NotificationViewEvent.OpenSupportAdminChat -> {
                openChatFragment(event.adminId.toString())
            }

            is NotificationViewEvent.UpdateGlobalNotificationCounter -> {
                updateNotificationCounter(event.count)
            }

            else -> Unit
        }
    }
}
