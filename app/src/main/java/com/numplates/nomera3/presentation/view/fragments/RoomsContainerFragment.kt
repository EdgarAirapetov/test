package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenResumed
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.keyboard.KeyboardEventListener
import com.meera.core.views.SwipeDirection
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRoomsContainerBinding
import com.numplates.nomera3.modules.chatfriendlist.presentation.ChatFriendListFragment
import com.numplates.nomera3.modules.notifications.ui.fragment.NotificationFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_OPEN_EVENT
import com.numplates.nomera3.presentation.view.adapter.RoomsPagerAdapter
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showBelowView
import com.numplates.nomera3.presentation.viewmodel.RoomsContainerAction
import com.numplates.nomera3.presentation.viewmodel.RoomsContainerEvents
import com.numplates.nomera3.presentation.viewmodel.RoomsContainerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

private const val TOOLTIP_OFFSET = 5

class RoomsContainerFragment : BaseFragmentNew<FragmentRoomsContainerBinding>(),
    NotificationFragment.NotificationFragmentCallback {

    private val viewModel by viewModels<RoomsContainerViewModel> { App.component.getViewModelFactory() }
    private val tooltipScope: CoroutineScope = MainScope()

    private val openChatSearchTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_show_chat_search)
    }

    private var notificationsFragment: NotificationFragment? = null
    private var roomsFragment: RoomsFragmentV2? = null
    private var tvInComingCount: TextView? = null
    private var unreadMessageCountBar: TextView? = null
    private var fragmentAdapter: RoomsPagerAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRoomsContainerBinding
        get() = FragmentRoomsContainerBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeContainerEvents()
        addKeyboardListener()
        val holder = binding?.root
        val statusBar = holder?.findViewById<View>(R.id.rooms_statusbar)
        val params = statusBar?.layoutParams as? AppBarLayout.LayoutParams?
        params?.height = context.getStatusBarHeight()
        statusBar?.layoutParams = params

        fragmentAdapter = RoomsPagerAdapter(childFragmentManager)
        fragmentAdapter?.addTitleOfFragment(resources.getString(R.string.rooms_messages))
        fragmentAdapter?.addTitleOfFragment(resources.getString(R.string.rooms_notifications))

        initFragments()

        binding?.roomsViewPager?.offscreenPageLimit = 2
        binding?.roomsViewPager?.adapter = fragmentAdapter
        binding?.roomsViewPager?.let { binding?.roomsTabLayout?.setupWithViewPager(it) }

        binding?.nbBar?.chatMessagesCountListener = {
            handleMessageCount(it)
        }
        binding?.nbBar?.let { onActivityInteraction?.onGetNavigationBar(it) }

        binding?.roomsViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

            override fun onPageSelected(position: Int) {
                Timber.e("PAGE Pos:$position")
                if (position == 1) {
                    notificationsFragment?.onStartNotificationsFragment()
                    viewModel.handleViewAction(RoomsContainerAction.LogScreenForFragment(
                        screenName = notificationsFragment?.javaClass?.simpleName.orEmpty()
                    ))

                    binding?.roomsViewPager?.setAllowedSwipeDirection(SwipeDirection.LEFT)
                } else {
                    notificationsFragment?.onStopNotificationsFragment()
                    viewModel.handleViewAction(
                        RoomsContainerAction.LogScreenForFragment(
                            screenName = roomsFragment?.javaClass?.simpleName.orEmpty()
                        )
                    )
                    binding?.roomsViewPager?.setAllowedSwipeDirection(SwipeDirection.ALL)
                }
            }
        })

        binding?.ivStartNewChat?.setOnClickListener {
            add(ChatFriendListFragment(), Act.LIGHT_STATUSBAR)
            roomsFragment?.hideSearchKeyboard()
            roomsFragment?.resetUserSearch()
        }
        fragmentAdapter?.let {
            initCustomTabs(it)
        }

        val needToOpenEvent = arguments?.getBoolean(ARG_OPEN_EVENT, false)
        if (needToOpenEvent == true) {
            goToEvents()
        }

        if (savedInstanceState == null) {
            viewModel.handleViewAction(RoomsContainerAction.CheckSwipeDownToShowChatSearch)
        }
    }

    private fun initFragments() {
        roomsFragment = RoomsFragmentV2()
        notificationsFragment = NotificationFragment()
        notificationsFragment?.setNotificationFragmentCallback(this)
        fragmentAdapter?.addFragments(listOfNotNull(roomsFragment, notificationsFragment))
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        roomsFragment?.onReturnTransitionFragment()
        notificationsFragment?.startRequestUnreadNotificationsCount()
        notificationsFragment?.deleteNotificationsWhenUserBlocked()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        Timber.d("Bazaleev: onStartFragment")
        roomsFragment?.onStartRoomsFragment()
        if (notificationsFragment?.isAdded == true) {
            notificationsFragment?.onStartNotificationsFragment()
        }
        val screenName = when (binding?.roomsViewPager?.currentItem) {
            0 -> roomsFragment?.javaClass?.simpleName.orEmpty()
            1 -> notificationsFragment?.javaClass?.simpleName.orEmpty()

            else -> null
        }
        screenName?.let { viewModel.handleViewAction(RoomsContainerAction.LogScreenForFragment(it)) }
    }

    override fun onStopFragment() {
        super.onStopFragment()
        Timber.d("Bazaleev: onStopFragment")
        notificationsFragment?.onStopNotificationsFragment()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("Bazaleev: onStart")
        binding?.nbBar?.selectMessenger(true)
        roomsFragment?.onStartFragment()
    }

    override fun onStop() {
        super.onStop()
        Timber.d("Bazaleev: onStop")
        binding?.nbBar?.selectMessenger(false)
        roomsFragment?.onStopFragment()
    }

    override fun updateScreenOnTapNavBar() {
        super.updateScreenOnTapNavBar()
        roomsFragment?.scrollAndRefresh()
    }

    // TODO: 21.10.2020 Пробрасывался из старого фрагмента
    override fun onUnreadNotifications(count: Int) {
        val params = tvInComingCount?.layoutParams
        if (count > 0) {
            val cnt = if (count > MAX_ALLOWED_ITEMS) {
                params?.width = BIGGER_CONTAINER_WIDTH.dp
                MAX_ALLOWED_ITEMS_MESSAGE
            } else {
                params?.width = SMALLER_CONTAINER_WIDTH.dp
                count.toString()
            }
            tvInComingCount?.layoutParams = params
            tvInComingCount?.text = cnt
            tvInComingCount?.visible()
            act.updateUnreadNotificationBadge(true)

        } else {
            act.updateUnreadNotificationBadge(false)
            tvInComingCount?.gone()
        }
    }

    override fun onHideHints() {
        super.onHideHints()
        tooltipScope.coroutineContext.cancelChildren()
    }

    fun goToEvents() {
        fragmentAdapter?.let {
            binding?.roomsViewPager?.currentItem = it.count
        }
    }

    fun resetUserSearch() {
        roomsFragment?.hideSearchKeyboard()
        roomsFragment?.resetUserSearch()
    }

    private fun observeContainerEvents() {
        viewModel.roomsEvents
            .flowWithLifecycle(
                lifecycle = lifecycle,
                minActiveState = Lifecycle.State.CREATED
            )
            .onEach(::handleRoomsContainerEvent)
            .launchIn(lifecycleScope)
    }

    private fun handleRoomsContainerEvent(event: RoomsContainerEvents) {
        when (event) {
            is RoomsContainerEvents.ShowSwipeToShowChatSearchEvent -> showSwipeDownToOpenSearchTooltip()
        }
    }

    private fun showSwipeDownToOpenSearchTooltip() {
        showLegacyTooltip()
    }

    private fun showLegacyTooltip() {
        tooltipScope.launch {
            whenResumed {
                delay(TooltipDuration.COMMON_START_DELAY)
                viewModel.handleViewAction(RoomsContainerAction.ConfirmSwipeDownToShowChatTooltip)
                binding?.roomsTabLayout?.let {
                    openChatSearchTooltip?.showBelowView(
                        fragment = this@RoomsContainerFragment,
                        view = it,
                        offsetY = TOOLTIP_OFFSET.dp,
                    )
                }
                delay(TooltipDuration.COMMON_END_DELAY)
            }
        }.invokeOnCompletion {
            openChatSearchTooltip?.dismiss()
        }
    }

    private fun handleMessageCount(count: Int) {
        if (count > 0) {
            val params = unreadMessageCountBar?.layoutParams
            val cnt = if (count > MAX_ALLOWED_ITEMS) {
                params?.width = BIGGER_CONTAINER_WIDTH.dp
                MAX_ALLOWED_ITEMS_MESSAGE
            } else {
                params?.width = SMALLER_CONTAINER_WIDTH.dp
                count.toString()
            }
            unreadMessageCountBar?.layoutParams = params
            unreadMessageCountBar?.text = cnt
            unreadMessageCountBar?.visible()
        } else {
            unreadMessageCountBar?.gone()
        }
    }

    private fun initCustomTabs(pagerAdapter: RoomsPagerAdapter) {
        binding?.roomsTabLayout?.let { tabLayout ->
            for (i in 0 until tabLayout.tabCount) {
                val v = LayoutInflater.from(context).inflate(R.layout.tab_item_rooms_container, null, false)
                val title = v.findViewById<TextView>(R.id.tabTitle)
                title.text = pagerAdapter.getPageTitle(i)
                if (i == 1) {
                    tvInComingCount = v.findViewById(R.id.tabCount)
                }
                if (i == 0) {
                    unreadMessageCountBar = v.findViewById(R.id.tabCount)
                }
                tabLayout.getTabAt(i)?.customView = v
            }
        }
    }

    private fun addKeyboardListener() {
        KeyboardEventListener(requireActivity() as AppCompatActivity) { isOpen ->
            binding?.nbBar?.isGone = isOpen
        }
    }

    companion object {
        private const val MAX_ALLOWED_ITEMS = 99
        private const val BIGGER_CONTAINER_WIDTH = 30
        private const val SMALLER_CONTAINER_WIDTH = 20
        private const val MAX_ALLOWED_ITEMS_MESSAGE = "+99"

        fun newInstance(argOpenEvent: Boolean): RoomsContainerFragment =
            RoomsContainerFragment().apply {
                val args = Bundle()
                args.putBoolean(ARG_OPEN_EVENT, argOpenEvent)
                arguments = args
            }
    }
}
