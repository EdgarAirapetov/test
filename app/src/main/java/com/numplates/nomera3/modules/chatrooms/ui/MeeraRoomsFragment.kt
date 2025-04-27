package com.numplates.nomera3.modules.chatrooms.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRoomsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.MeeraChatFragment
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chat.requests.ui.addDividerDecoratorLeftPadding
import com.numplates.nomera3.modules.chatrooms.ui.gestures.resetSwipedItems
import com.numplates.nomera3.modules.chatrooms.ui.gestures.setItemTouchHelper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_TRANSIT_FROM
import com.numplates.nomera3.presentation.view.adapter.AnyChangeDataObserver
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatRoomsPagedAdapterV2
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatSettingsAdapter
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatRoomsViewEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val LIST_ANIMATION_TIME = 100L
private const val DELAY_KEYBOARD = 400L
private const val BUTTONS_COUNT = 2
private const val DIVIDER_PADDING = 92

class MeeraRoomsFragment : RoomsBaseFragment(R.layout.meera_fragment_rooms) {

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    var isNeedReloadRoomsIfResume = true
    private val binding by viewBinding(MeeraFragmentRoomsBinding::bind)

    private var meeraRoomsAdapter: MeeraRoomsAdapter? = null
    private var settingAdapter: ChatSettingsAdapter? = null

    private var scrollToTop = false
    private var currentScrollPos: Int? = null
    private var isBackFromMessagesScreen = false

    private val recyclerAnimator by lazy {
        DefaultItemAnimator().apply {
            changeDuration = LIST_ANIMATION_TIME
            addDuration = LIST_ANIMATION_TIME
            removeDuration = LIST_ANIMATION_TIME
            moveDuration = LIST_ANIMATION_TIME
        }
    }

    private val roomsPagedObserver = Observer<PagedList<UiKitRoomCellConfig>> { roomsPaged ->
        binding.loadingProgress.gone()
        meeraRoomsAdapter?.submitList(roomsPaged) {
            if (scrollToTop) {
                scrollToTop = false
                binding.rvRooms.scrollToPosition(0)
            }
            currentScrollPos?.let { binding.rvRooms.scrollToPosition(it) }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        if (savedInstanceState == null) {
            binding.loadingProgress.visible()
            viewModel.checkSwipeDownToShowChatSearchTooltip()
        }
        viewModel.initPaging(isClearSearch = isBackFromMessagesScreen.not())
        initObservables()
        initSearchInput()
    }

    override fun onResume() {
        super.onResume()
        if (binding.ukisRoomsSearch.hasFocus()) {
            binding.ukisRoomsSearch.postDelayed({ binding.ukisRoomsSearch.showKeyboard() }, DELAY_KEYBOARD)
        }
        viewModel.getDrafts(reloadRooms = isNeedReloadRoomsIfResume)
        viewModel.unsubscribeRoom()
    }

    override fun onDestroyView() {
        currentScrollPos =
            (binding.rvRooms.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        super.onDestroyView()
    }

    override fun setScrollState(recyclerView: RecyclerView?) {
        super.setScrollState(binding.rvRooms)
    }

    fun hideSearchKeyboard() {
        binding.ukisRoomsSearch.hideKeyboard()
    }

    fun resetUserSearch() {
        scrollToTop = true
        binding.vgRoomsPlaceholder.gone()
        binding.ukisRoomsSearch.clear()
    }

    private fun initObservables() {
        viewModel.userSettings.observe(viewLifecycleOwner) { settingsState ->
            if (settingsState.all { it != null }) settingAdapter?.submitList(settingsState, ::checkSettingsScroll)
        }

        viewModel.liveRoomsViewEvent
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleEvents)
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.roomsPagingList.distinctUntilChanged().observe(viewLifecycleOwner, roomsPagedObserver)
    }

    private fun hideSwipedItems(recyclerView: RecyclerView) {
        for (i in (recyclerView.adapter?.itemCount ?: 0) downTo 0) {
            val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView
            if (itemView != null && itemView.scrollX > 0) {
                itemView.scrollTo(0, 0)
            }
        }
    }

    private fun checkSettingsScroll() {
        val recyclerView = binding.rvRooms
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
        val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (visiblePosition <= 0 || recyclerView.findViewHolderForAdapterPosition(visiblePosition) !is ChatRoomsPagedAdapterV2.RoomsViewHolder) {
            recyclerView.scrollToPosition(0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchInput() {
        val isChatListSearchDisabled = !featureTogglesContainer.chatSearchFeatureToggle.isEnabled
        if (isChatListSearchDisabled) {
            binding.root.setTransition(R.id.hidden_state)
            return
        } else {
            binding.root.setTransition(R.id.hidden_transition)
        }
        binding.vgSearchbarContainer.setOnTouchListener(object : View.OnTouchListener {
            private val gestureDetector: GestureDetector = GestureDetector(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }

                    override fun onFling(
                        e1: MotionEvent?,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        resetUserSearch()
                        hideSearchBarInputAnimated()
                        return true
                    }
                })

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event)
            }
        })


        binding.ukisRoomsSearch.doAfterSearchTextChanged { text ->
            scrollToTop = text.isEmpty()
            viewModel.search(text.trim())
            switchSearchBarTransition(isEnabled = text.isBlank())
        }
        binding.ukisRoomsSearch.setCloseButtonClickedListener {
            requireContext().hideKeyboard(binding.ukisRoomsSearch)
        }

        saveSearchQueryWhenBackFromMessages()
    }

    private fun saveSearchQueryWhenBackFromMessages() {
        val searchQuery = viewModel.getSearchQuery()
        if (isBackFromMessagesScreen && searchQuery.isNotEmpty()) {
            binding.ukisRoomsSearch.searchInputText = searchQuery
        }
        isBackFromMessagesScreen = false
    }

    private fun initRecycler() {
        binding.rvRooms.addDividerDecoratorLeftPadding(DIVIDER_PADDING)
        binding.rvRooms.setHasFixedSize(true)
        binding.rvRooms.itemAnimator = recyclerAnimator
        binding.rvRooms.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideSearchKeyboard()
                    hideSwipedItems(recyclerView)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val itemPos = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if (!recyclerView.canScrollVertically(-1)) {
                    meeraRoomsFragmentInteraction?.onSetTabLayoutElevation(
                        isElevated = false,
                        isTop = itemPos == 0
                    )
                } else {
                    meeraRoomsFragmentInteraction?.onSetTabLayoutElevation(
                        isElevated = true,
                        isTop = itemPos == 0
                    )
                }
            }
        })
        settingAdapter = ChatSettingsAdapter(
            messagesSettingClickListener = {},
            chatRequestClickListener = {},
        )
        meeraRoomsAdapter = MeeraRoomsAdapter(object : MeeraRoomsAdapter.RoomCellListener {
            override fun onRoomClicked(item: UiKitRoomCellConfig) {
                navigateChatMessagesFragment(item)
            }

            override fun onDeleteRoomClicked(item: UiKitRoomCellConfig) {
                handleDeleteRoom(item, onResetSwipedItems = { binding.rvRooms.resetSwipedItems() })
            }

            override fun onChangeMuteClicked(item: UiKitRoomCellConfig) {
                viewModel.changeMuteState(item)
                binding.rvRooms.resetSwipedItems()
            }
        })
        meeraRoomsAdapter?.registerAdapterDataObserver(object : AnyChangeDataObserver() {
            override fun changesTriggered() {
                updatePlaceholderVisibility()
            }
        })
        binding.rvRooms.adapter = meeraRoomsAdapter
        (binding.rvRooms.itemAnimator as? SimpleItemAnimator?)?.supportsChangeAnimations

        setItemTouchHelper(
            recyclerView = binding.rvRooms,
            buttonsLimit = BUTTONS_COUNT,
            adapter = requireNotNull(meeraRoomsAdapter),
            onSwipeComplete = { onItemSwipeCompleted() }
        )
    }

    private fun navigateChatMessagesFragment(item: UiKitRoomCellConfig) {
        val args = Bundle().apply {
            putSerializable(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.COMMUNICATION)
            putParcelable(
                IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                    initType = ChatInitType.FROM_LIST_ROOMS,
                    roomId = item.id
                )
            )
            putSerializable(ARG_CHAT_TRANSIT_FROM, MeeraChatFragment.TransitFrom.OTHER)
        }
        isBackFromMessagesScreen = true
        findNavController().safeNavigate(R.id.action_mainChatFragment_to_meeraChatFragment, args)
    }

    private fun updatePlaceholderVisibility() {
        val isEmptySearch = binding.ukisRoomsSearch.searchInputText.isNullOrBlank()
        val isEmptyAdapter = meeraRoomsAdapter?.itemCount == 0
        binding.vgRoomsPlaceholder.isVisible = !isEmptySearch && isEmptyAdapter
    }

    private fun switchSearchBarTransition(isEnabled: Boolean) {
        if (isEnabled) {
            binding.root.setTransition(R.id.hidden_transition)
        } else {
            binding.root.setTransition(R.id.default_state)
        }
    }

    private fun hideSearchBarInputAnimated() {
        binding.root.setTransition(R.id.hidden_transition)
        binding.root.transitionToStart()
    }

    private fun handleEvents(event: ChatRoomsViewEvent) {
        when (event) {
            is ChatRoomsViewEvent.ShowSwipeToShowChatSearchEvent -> showSwipeDownToOpenSearchTooltip()
            else -> Unit
        }
    }

    private fun showSwipeDownToOpenSearchTooltip() {
        meeraRoomsFragmentInteraction?.onShowSwipeDownToSearchTooltip()
    }

}
