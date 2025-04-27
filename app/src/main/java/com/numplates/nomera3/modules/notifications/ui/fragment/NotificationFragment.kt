package com.numplates.nomera3.modules.notifications.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideScaleDown
import com.meera.core.extensions.showScaleUp
import com.meera.core.extensions.visible
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentNotificationsBinding
import com.numplates.nomera3.modules.avatar.ContainerAvatarFragment
import com.numplates.nomera3.modules.chat.ChatNavigator
import com.numplates.nomera3.modules.notifications.data.mediator.PAGE_SIZE
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewEvent
import com.numplates.nomera3.modules.notifications.ui.viewmodel.NotificationViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE
import com.numplates.nomera3.presentation.view.adapter.AnyChangeDataObserver
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val DELAY_MEDIUM = 200L

class NotificationFragment : BaseNotificationsFragment<FragmentNotificationsBinding>() {

    private var chatNavigator: ChatNavigator? = null
    private var recyclerPagination: RecyclerViewPaginator? = null

    interface NotificationFragmentCallback {
        fun onUnreadNotifications(count: Int)
    }

    fun deleteNotificationsWhenUserBlocked() {
        notificationViewModel.deleteNotificationsWhenUserBlocked()
    }

    private val notificationViewModel by viewModels<NotificationViewModel> {
        App.component.getViewModelFactory()
    }
    private var notificationFragmentCallback: NotificationFragmentCallback? = null
    private var avatarAnimation: String? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentNotificationsBinding
        get() = FragmentNotificationsBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        initChatNavigator()
        initObservers()
        if (savedInstanceState == null) refresh()
    }

    override fun onStartNotificationsFragment() {
        super.onStartNotificationsFragment()
        refresh()
    }

    private fun initObservers() {
        notificationViewModel.liveUnreadNotificationCounter.observe(viewLifecycleOwner) { count ->
            notificationFragmentCallback?.onUnreadNotifications(count)
        }
        notificationViewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleViewEvent)
        lifecycleScope.launchWhenResumed {
            notificationViewModel.getOwnAvatarAnimationFlow().collect { animation ->
                avatarAnimation = animation
            }
        }
    }

    private fun setupViews() {
        binding?.apply {
            initRecycler(
                recyclerView = rvNotifications,
                refreshLayout = srlNotifications,
            )
            srlNotifications.setOnRefreshListener {
                refresh()
                scheduleScrollToTop()
            }
            btnRefreshNew.click {
                refresh()
                scheduleScrollToTop()
            }
            rvNotifications.adapter?.registerAdapterDataObserver(object : AnyChangeDataObserver() {
                override fun changesTriggered() = updateZeroDataState()
            })
        }

        recyclerPagination = RecyclerViewPaginator(
            recyclerView = binding?.rvNotifications ?: return,
            onLast = { notificationViewModel.isLast },
            isLoading = { notificationViewModel.isLoading },
            loadMore = { notificationViewModel.loadNotifications(it) }
        ).apply {
            threshold = PAGE_SIZE
        }

        notificationViewModel.notifications
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { items ->
                pagingAdapter.submitList(items) {
                    binding?.tvNotificationsZeroData?.isGone = items.isNotEmpty() || pagingAdapter.itemCount > 0
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        notificationViewModel.loadNotifications(page = 0)
    }

    private fun refresh() {
        if (isAdded) {
            notificationViewModel.refresh()
            binding?.srlNotifications?.isRefreshing = false
            binding?.btnRefreshNew?.hideScaleDown()
        }
    }

    private fun initChatNavigator() {
        chatNavigator = ChatNavigator(onActivityInteraction, this)
    }

    private fun scheduleScrollToTop() {
        binding?.rvNotifications?.adapter?.registerAdapterDataObserver(object : AnyChangeDataObserver() {
            override fun changesTriggered() {
                binding?.rvNotifications?.postDelayed({
                    binding?.rvNotifications?.smoothScrollToPosition(0)
                    binding?.rvNotifications?.post {
                        runCatching {
                            binding?.rvNotifications?.adapter?.unregisterAdapterDataObserver(this)
                        }
                    }
                }, DELAY_MEDIUM)
            }
        })
    }

    private fun handleViewEvent(event: NotificationViewEvent) {
        when (event) {
            is NotificationViewEvent.ShowRefreshBtn -> {
                binding?.btnRefreshNew?.showScaleUp()
            }

            is NotificationViewEvent.HideRefreshBtn -> {
                binding?.btnRefreshNew?.hideScaleDown()
            }

            is NotificationViewEvent.OpenSupportAdminChat -> {
                chatNavigator?.openChatFragment(event.adminId.toString())
            }

            else -> Unit
        }
    }

    override fun onDestroyView() {
        chatNavigator = null
        super.onDestroyView()
    }

    private fun exitScreen() {
        chatNavigator?.openPreviousViewPagerItem()
    }

    override fun goToCreateAvatar() {
        add(ContainerAvatarFragment(), Act.LIGHT_STATUSBAR, Arg(ARG_AVATAR_STATE, avatarAnimation))
        observeAvatarChange()
    }

    private fun observeAvatarChange() {
        requireActivity().supportFragmentManager
            .setFragmentResultListener(REQUEST_NMR_KEY_AVATAR, viewLifecycleOwner) { _, bundle ->
                val avatarState: String =
                    bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
                notificationViewModel.setUserAvatarState(avatarState)
                notificationViewModel.saveAvatarInFile(avatarState)
                exitScreen()
            }
    }

    private fun updateZeroDataState() {
        binding?.tvNotificationsZeroData?.isVisible = pagingAdapter.itemCount == 0
    }

    override fun onStart() {
        super.onStart()
        notificationViewModel.requestCounter()
    }

    fun startRequestUnreadNotificationsCount() {
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
        notificationViewModel.readAll()
    }

    override fun deleteAll() {
        notificationViewModel.deleteAll()
    }

    override fun onItemDeleted() {
        doDelayed(DELAY_MEDIUM) {
            if (pagingAdapter.itemCount > 0)
                binding?.tvNotificationsZeroData?.gone()
            else
                binding?.tvNotificationsZeroData?.visible()
        }
    }

    internal fun setNotificationFragmentCallback(callback: NotificationFragmentCallback) {
        this.notificationFragmentCallback = callback
    }

    override fun onProfileClick(userId: Long) = Unit

    override fun logOpenCommunity() {
        notificationViewModel.logCommunityScreenOpened()
    }
}
