package com.numplates.nomera3.modules.redesign.fragments.main

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.distinctUntilChanged
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.safeNavigate
import com.meera.core.utils.text.limitCounterText
import com.meera.uikit.tooltip.TooltipShowHandler
import com.meera.uikit.tooltip.createTooltip
import com.meera.uikit.widgets.navigation.UiKitNavigationBarView
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewSizeState
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.meera.uikit.widgets.tablayout.UiKitRowTabLayout
import com.meera.uikit.widgets.tooltip.TooltipMessage
import com.meera.uikit.widgets.tooltip.UiKitTooltipBubbleMode
import com.meera.uikit.widgets.tooltip.UiKitTooltipViewState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMainChatFragmentBinding
import com.numplates.nomera3.modules.chat.requests.ui.fragment.ChatRequestBlockData
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT
import com.numplates.nomera3.modules.chat.requests.ui.fragment.KEY_CHAT_REQUEST_BLOCK_USER_RESULT
import com.numplates.nomera3.modules.chat.requests.ui.fragment.MeeraChatRequestFragment
import com.numplates.nomera3.modules.chatrooms.ui.MeeraRoomsFragment
import com.numplates.nomera3.modules.chatrooms.ui.RoomsBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.utils.viewModels
import com.numplates.nomera3.presentation.view.adapter.RoomsPagerAdapter
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration

const val KEY_CHAT_TRANSIT_FROM_RESULT = "KEY_CHAT_TRANSIT_FROM_RESULT"
const val KEY_BUNDLE_TRANSIT_FROM_MAIN = "TRANSIT_FROM_MAIN"
private const val ROOMS_TAB_INDEX = 0
private const val CHAT_REQUESTS_TAB_INDEX = 1
private const val PAGE_LIMIT = 2
private const val TOOLTIP_OFFSET = 5
private const val TAB_LAYOUT_ELEVATION = 20f
private const val DELAY_BEFORE_USER_BLOCK = 500L

class MainChatFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_main_chat_fragment,
        behaviourConfigState = ScreenBehaviourState.ScrollableFull(false)
    ),
    RoomsBaseFragment.MeeraRoomsFragmentInteraction {

    private val binding by viewBinding(MeeraMainChatFragmentBinding::bind)
    private val viewModel by viewModels<MeeraMainChatViewModel>()
    private var fragmentAdapter: RoomsPagerAdapter? = null
    private var roomsFragment: MeeraRoomsFragment? = null
    private var chatRequestFragment: MeeraChatRequestFragment? = null
    private var isTransitFromMain = true

    private val meeraOpenChatSearchTooltip: TooltipShowHandler by lazy {
        createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.CENTER_TOP,
                tooltipMessage = TooltipMessage.TooltipMessageString(
                    context?.getString(R.string.tooltip_swipe_down_the_screen_to_search).orEmpty()
                ),
                showCloseButton = false
            )
        )
    }

    override val isBottomNavBarVisibility: UiKitNavigationBarViewVisibilityState
        get() = UiKitNavigationBarViewVisibilityState.VISIBLE

    override val containerId: Int
        get() = R.id.fragment_first_container_view


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(KEY_CHAT_TRANSIT_FROM_RESULT) { _, bundle ->
            this.isTransitFromMain = bundle.getBoolean(KEY_BUNDLE_TRANSIT_FROM_MAIN, true)
        }
        setFragmentResultListener(KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT) { _, bundle ->
            lockRequestGetRooms()
            handleBlockReportUserResult(bundle)
        }
        setFragmentResultListener(KEY_CHAT_REQUEST_BLOCK_USER_RESULT) { _, bundle ->
            lockRequestGetRooms()
            handleBlockUserResult(bundle)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initObservables()
    }

    override fun onStart() {
        super.onStart()
        setupToolbar()
        // TODO: fix for transition from map screens when logo hidden
        view?.postDelayed({
            NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().showLogo = true
        }, 500)
    }

    override fun onSetTabLayoutElevation(isElevated: Boolean, isTop: Boolean) {
        setNavBarDimensionWhenScroll(isTop)
        setTabLayoutElevation(isElevated)
    }

    override fun onScrollState(isTop: Boolean) {
        setTabLayoutElevation(isElevated = isTop.not())
        setNavBarDimensionWhenScroll(isTop)
    }

    override fun isNeedLockReloadRooms(isLock: Boolean) {
        val roomsFragment = (fragmentAdapter?.instantiateItem(binding.vpRooms, ROOMS_TAB_INDEX) as MeeraRoomsFragment)
        roomsFragment.isNeedReloadRoomsIfResume = isLock.not()
    }

    override fun onShowSwipeDownToSearchTooltip() {
        binding.tabLayoutRooms.post {
            meeraOpenChatSearchTooltip.showBelowView(
                view = binding.tabLayoutRooms,
                duration = TooltipDuration.COMMON_END_DELAY,
                offsetY = TOOLTIP_OFFSET.dp
            )
        }
    }

    private fun initViews() {
        fragmentAdapter = RoomsPagerAdapter(childFragmentManager)
        fragmentAdapter?.addTitleOfFragment(resources.getString(R.string.meera_general_main))
        fragmentAdapter?.addTitleOfFragment(resources.getString(R.string.meera_general_requests))
        initMeeraFragments()
        binding.apply {
            vpRooms.offscreenPageLimit = PAGE_LIMIT
            vpRooms.adapter = fragmentAdapter
            tabLayoutRooms.setupWithViewPager(vpRooms)
            vpRooms.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrollStateChanged(state: Int) = Unit

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) = Unit

                override fun onPageSelected(position: Int) {
                    if (position == 0) {
                        val roomsFragment =
                            (fragmentAdapter?.instantiateItem(vpRooms, vpRooms.currentItem) as MeeraRoomsFragment)
                        roomsFragment.setScrollState(null)
                    } else {
                        val chatRequestFragment =
                            (fragmentAdapter?.instantiateItem(vpRooms, vpRooms.currentItem) as MeeraChatRequestFragment)
                        chatRequestFragment.setScrollState(null)
                    }
                }
            })
        }
    }

    private fun setupToolbar() {
        val toolbar = NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar()
        toolbar.hasSecondButton = false
        toolbar.setButtonsListeners(
            addButtonListener = {
                findNavController().safeNavigate(R.id.action_mainChatFragment2_to_chatFriendListFragment)
            },
            notificationsButtonListener = {
                NavigationManager.getManager().initGraph(R.navigation.bottom_notifications_graph)
            }
        )
    }

    private fun initMeeraFragments() {
        roomsFragment = MeeraRoomsFragment()
        roomsFragment?.setFragmentInteraction(this)
        chatRequestFragment = MeeraChatRequestFragment()
        chatRequestFragment?.setFragmentInteraction(this)
        fragmentAdapter?.addFragments(listOfNotNull(roomsFragment))
    }

    private fun initObservables() {
        viewModel.getTabLayoutUiState().distinctUntilChanged().observe(viewLifecycleOwner) { tabLayoutState ->
            tabLayoutState?.let { state ->
                if (state.isTabRequestVisible) showChatRequestTab() else hideChatRequestTab()
                onUnreadMessages(state.unreadMessageCount)
                onUnreadMessagesChatRequest(state.requestCounter)
            }
        }
    }

    private fun showChatRequestTab() {
        fragmentAdapter?.addFragmentByIndex(CHAT_REQUESTS_TAB_INDEX, chatRequestFragment)
        gotoChatRequestsIfNeeded()
    }

    private fun gotoChatRequestsIfNeeded() {
        if (isTransitFromMain.not()) binding.vpRooms.setCurrentItem(CHAT_REQUESTS_TAB_INDEX)
    }

    private fun gotoChatRoomsIfNeeded() {
        if (isTransitFromMain.not()) binding.vpRooms.setCurrentItem(ROOMS_TAB_INDEX)
    }

    private fun hideChatRequestTab() {
        fragmentAdapter?.removeFragmentByIndex(CHAT_REQUESTS_TAB_INDEX)
        gotoChatRoomsIfNeeded()
    }

    private fun handleBlockReportUserResult(bundle: Bundle) {
        val chatRequestBlockData =
            getParcelableChatRequestBlockData(bundle, KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA)
        executeChatRequestBlockUserAction { fragment -> fragment.blockWithReportUser(chatRequestBlockData) }
    }

    private fun handleBlockUserResult(bundle: Bundle) {
        val chatRequestBlockData = getParcelableChatRequestBlockData(bundle, KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA)
        executeChatRequestBlockUserAction { fragment -> fragment.blockUser(chatRequestBlockData) }
    }

    private fun lockRequestGetRooms() {
        val roomsFragment = (fragmentAdapter?.instantiateItem(binding.vpRooms, ROOMS_TAB_INDEX) as MeeraRoomsFragment)
        roomsFragment.isNeedReloadRoomsIfResume = false
    }

    private fun getParcelableChatRequestBlockData(bundle: Bundle, key: String): ChatRequestBlockData? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getParcelable(key, ChatRequestBlockData::class.java)
        } else {
            bundle.getParcelable(key)
        }
    }

    private fun executeChatRequestBlockUserAction(block: (fragment: MeeraChatRequestFragment) -> Unit) {
        doDelayed(DELAY_BEFORE_USER_BLOCK) {
            if (fragmentAdapter?.count == PAGE_LIMIT) {
                val chatRequestFragment =
                    (fragmentAdapter?.instantiateItem(
                        binding.vpRooms,
                        CHAT_REQUESTS_TAB_INDEX
                    ) as MeeraChatRequestFragment)
                block.invoke(chatRequestFragment)
            }
        }
    }

    private fun setNavBarDimensionWhenScroll(isTop: Boolean) {
        val navBar = NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView()
        if (isTop) {
            setNavBarSizeState(navBar, UiKitNavigationBarViewSizeState.MAX)
        } else {
            setNavBarSizeState(navBar, UiKitNavigationBarViewSizeState.MIN)
        }
    }

    private fun setNavBarSizeState(navBar: UiKitNavigationBarView, state: UiKitNavigationBarViewSizeState) {
        if (navBar.stateSize != state) {
            navBar.stateSize = state
        }
    }

    private fun setTabLayoutElevation(isElevated: Boolean) {
        if (isElevated) {
            ViewCompat.setElevation(binding.tabLayoutRooms, TAB_LAYOUT_ELEVATION)
        } else {
            ViewCompat.setElevation(binding.tabLayoutRooms, 0f)
        }
    }

    // Непрочитанные сообщения в обычных чатах
    private fun onUnreadMessages(count: Int) {
        setTabCountText(
            tab = binding.tabLayoutRooms,
            tabIndex = ROOMS_TAB_INDEX,
            count = count
        )
    }

    // Непрочитанные сообщения в запросах на переписку
    private fun onUnreadMessagesChatRequest(count: Int) {
        setTabCountText(
            tab = binding.tabLayoutRooms,
            tabIndex = CHAT_REQUESTS_TAB_INDEX,
            count = count
        )
    }

    private fun setTabCountText(
        tab: UiKitRowTabLayout,
        tabIndex: Int,
        count: Int
    ) {
        if (count > 0) {
            val countText = limitCounterText(count)
            tab.setBadgeText(tabIndex, countText)
        } else {
            tab.setBadgeText(tabIndex, String.empty())
        }
    }

}

data class TabLayoutUiState(
    val unreadMessageCount: Int = 0,
    val isTabRequestVisible: Boolean = false,
    val requestCounter: Int = 0
)
