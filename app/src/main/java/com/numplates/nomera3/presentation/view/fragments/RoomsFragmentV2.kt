package com.numplates.nomera3.presentation.view.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.common.COLOR_STATUSBAR_LIGHT_NAVBAR
import com.meera.core.common.LIGHT_STATUSBAR
import com.meera.core.extensions.clearText
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.userRole
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.databinding.FragmentRoomsNewBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.IOnDialogClickedNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chat.requests.ui.addDividerDecorator
import com.numplates.nomera3.modules.chat.requests.ui.fragment.ChatRequestFragment
import com.numplates.nomera3.modules.chat.ui.ActivityInteractChatActions
import com.numplates.nomera3.modules.chat.ui.ActivityInteractionChatCallback
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_BLACKLIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COUNT_USERS_WHITELIST
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PRIVACY_TYPE_VALUE
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatRoomsPagedAdapterV2
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatSettingsAdapter
import com.numplates.nomera3.presentation.view.fragments.privacysettings.PersonalMessagesFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.viewmodel.RoomsViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatRoomsViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SettingsPrivateMessagesState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

private const val DELAY_KEYBOARD = 400L
private const val LIST_ANIMATION_TIME = 100L

class RoomsFragmentV2 : BaseFragmentNew<FragmentRoomsNewBinding>(), IOnDialogClickedNew {

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    private val roomsViewModel by viewModels<RoomsViewModel> { App.component.getViewModelFactory() }

    private var chatRoomsPagedAdapter: ChatRoomsPagedAdapterV2? = null
    private var settingAdapter: ChatSettingsAdapter? = null
    private var scrollToTop: Boolean = false

    private var activityCallback: ActivityInteractionChatCallback? = null

    private val recyclerAnimator by lazy {
        DefaultItemAnimator().apply {
            changeDuration = LIST_ANIMATION_TIME
            addDuration = LIST_ANIMATION_TIME
            removeDuration = LIST_ANIMATION_TIME
            moveDuration = LIST_ANIMATION_TIME
        }
    }

    private val roomsPagedObserver = Observer<PagedList<DialogEntity>> { roomsPaged ->
        Timber.d("ROOMS_LOG roomsPagedObserver RoomCOUNT:${roomsPaged.size}")
        binding?.loadingProgress?.gone()
        chatRoomsPagedAdapter?.submitList(roomsPaged) {
            binding?.rvRooms?.itemAnimator = recyclerAnimator
            if (scrollToTop) {
                scrollToTop = false
                binding?.rvRooms?.scrollToPosition(0)
            }
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRoomsNewBinding
        get() = FragmentRoomsNewBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setActivityCallback(context)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated called.")
        initRecycler()
        binding?.loadingProgress?.visible()
        initLiveObservables()
        initSearchInput()
        if (savedInstanceState == null) {
            roomsViewModel.getDrafts()
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding?.search?.hasFocus() == true) {
            binding?.search?.postDelayed({ binding?.search?.showKeyboard() }, DELAY_KEYBOARD)
        }
    }

    override fun onDestroyView() {
        removeObservables()
        settingAdapter = null
        chatRoomsPagedAdapter = null
        super.onDestroyView()
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        Timber.d("onReturnTransitionFragment()")
        roomsViewModel.getDrafts(reloadRooms = true)
        roomsViewModel.unsubscribeRoom()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d("onHiddenChanged()")
        if (!hidden) {
            roomsViewModel.getDrafts()
        }
    }

    override fun onRoomClicked(dialog: DialogEntity?) {
        if (dialog == null) return
        activityCallback?.onGetActionFromChat(ActivityInteractChatActions.HideAppHints)
        roomsViewModel.triggerGoToChat(dialog.roomId)
    }

    override fun onRoomLongClicked(dialog: DialogEntity?) {
        if (dialog?.companion?.userRole == UserRole.SUPPORT_USER) return
        MeeraMenuBottomSheet(context).apply {
            addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
                handleDeleteRoom(dialog)
            }
        }.show(childFragmentManager)
    }

    fun onStartRoomsFragment() {
        if (!isAdded) return
        Timber.d("onStartRoomsFragment()")
        binding?.rvRooms?.itemAnimator = null
        roomsViewModel.getMessageSettings()
    }

    fun scrollAndRefresh() {
        Timber.d("Scroll and refresh called.")
        scrollToTop = true
        doDelayed(300) {
            roomsViewModel.getRooms()
        }
    }

    fun hideSearchKeyboard() {
        binding?.search?.hideKeyboard()
    }

    fun resetUserSearch() {
        scrollToTop = true
        binding?.vgRoomsPlaceholder?.gone()
        binding?.search?.clearText()
    }

    private fun initLiveObservables() {
        roomsViewModel.userSettings.observe(viewLifecycleOwner) { settingsState ->
            if (settingsState.all { it != null }) settingAdapter?.submitList(settingsState, ::checkSettingsScroll)
        }

        roomsViewModel.liveRoomsViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach(::handleEvents)
            .launchIn(lifecycleScope)

        // View rooms by page (1st init)
        roomsViewModel.roomsPagingList.observeForever(roomsPagedObserver)
    }

    private fun checkSettingsScroll() {
        val recyclerView = binding?.rvRooms ?: return
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
            binding?.root?.setTransition(R.id.hidden_state)
            return
        } else {
            binding?.root?.setTransition(R.id.hidden_transition)
        }
        binding?.vgSearchbarContainer?.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector: GestureDetector = GestureDetector(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }

                    override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                        resetUserSearch()
                        hideSearchBarInputAnimated()
                        return true
                    }

                    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                        binding?.search?.showKeyboard()
                        return true
                    }
                })

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(event)
            }
        })
        binding?.search?.doAfterTextChanged { text ->
            binding?.ivClearInput?.isVisible = !text.isNullOrBlank()
            roomsViewModel.search(binding?.search?.text.toString().trim())
            switchSearchBarTransition(isEnabled = text.isNullOrBlank())
        }
        binding?.search?.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSearchKeyboard()
                    return true
                }
                return false
            }
        })
        binding?.ivClearInput?.click { resetUserSearch() }
    }

    private fun initRecycler() {
        binding?.rvRooms?.addDividerDecorator()
        binding?.rvRooms?.setHasFixedSize(true)
        binding?.rvRooms?.itemAnimator = recyclerAnimator
        binding?.rvRooms?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideSearchKeyboard()
                }
            }
        })
        settingAdapter = ChatSettingsAdapter(
            messagesSettingClickListener = ::openMessagesSettings,
            chatRequestClickListener = ::openChatRequestScreen,
        )
        chatRoomsPagedAdapter = ChatRoomsPagedAdapterV2(
            userId = roomsViewModel.getUserUid(),
            featureToggles = getFeatureToggles(),
            onDialogClicked = this,
        )
        chatRoomsPagedAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            private fun updatePlaceholderVisibility() {
                val isEmptySearch = binding?.search?.text.isNullOrEmpty()
                val isEmptyAdapter = chatRoomsPagedAdapter?.itemCount == 0
                Timber.d("isEmptySearch: $isEmptySearch; isEmptyAdapter: $isEmptyAdapter;")
                binding?.vgRoomsPlaceholder?.isVisible = !isEmptySearch && isEmptyAdapter
                binding?.vgNoRoomsPlaceholder?.isVisible = isEmptySearch && isEmptyAdapter
            }

            override fun onChanged() = updatePlaceholderVisibility()
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = updatePlaceholderVisibility()
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updatePlaceholderVisibility()
            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = updatePlaceholderVisibility()
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updatePlaceholderVisibility()
        })
        val concatAdapter = ConcatAdapter(settingAdapter, chatRoomsPagedAdapter)
        binding?.rvRooms?.adapter = concatAdapter
        (binding?.rvRooms?.itemAnimator as? SimpleItemAnimator?)?.supportsChangeAnimations
    }

    private fun switchSearchBarTransition(isEnabled: Boolean) {
        if (isEnabled) {
            binding?.root?.setTransition(R.id.hidden_transition)
        } else {
            binding?.root?.setTransition(R.id.default_state)
        }
    }

    private fun hideSearchBarInputAnimated() {
        binding?.root?.setTransition(R.id.hidden_transition)
        binding?.root?.transitionToStart()
    }

    private fun removeObservables() {
        roomsViewModel.roomsPagingList.removeObserver(roomsPagedObserver)
    }

    private fun gotoChatFragment(dialog: DialogEntity?) {
        onActivityInteraction?.onAddFragment(
            fragment = ChatFragmentNew(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(
                IArgContainer.ARG_WHERE_CHAT_OPEN to AmplitudePropertyWhere.COMMUNICATION,
                IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                    initType = ChatInitType.FROM_LIST_ROOMS,
                    roomId = dialog?.roomId
                )
            )
        )
    }

    private fun handleDeleteRoom(dialog: DialogEntity?) {
        if (dialog?.type == ROOM_TYPE_DIALOG) {
            arrayOf(getString(R.string.chat_remove_for, dialog.companion.name))

            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.rooms_delete_title))
                .setMessage(getString(R.string.rooms_delete_room))
                .setPositiveButton(R.string.yes) { dlg, _ ->
                    roomsViewModel.removeRoom(dialog.roomId, isBoth = false)
                    dlg.dismiss()
                }
                .setNegativeButton(R.string.no) { d, _ -> d.cancel() }
                .show()
        } else if (dialog?.type == ROOM_TYPE_GROUP) {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.rooms_delete_title))
                .setMessage(getString(R.string.rooms_delete_room))
                .setPositiveButton(R.string.yes) { dlg, _ ->
                    roomsViewModel.removeRoomGroupDialog(dialog)
                    dlg.dismiss()
                }
                .setNegativeButton(R.string.no) { d, _ -> d.cancel() }
                .show()
        }
    }

    override fun onAvatarClicked(dialog: DialogEntity?) {
        when (dialog?.type) {
            ROOM_TYPE_DIALOG -> {
                onActivityInteraction?.onAddFragment(
                    fragment = UserInfoFragment(),
                    isLightStatusBar = COLOR_STATUSBAR_LIGHT_NAVBAR,
                    mapArgs = hashMapOf(
                        IArgContainer.ARG_USER_ID to dialog.companion.userId,
                        IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.CHAT.property)

                )
            }
            ROOM_TYPE_GROUP -> Unit
        }
    }

    private fun handleEvents(event: ChatRoomsViewEvent) {
        when (event) {
            is ChatRoomsViewEvent.OnShowCreateGroupChatAppHint ->
                event.hint?.let {
                    activityCallback?.onGetActionFromChat(ActivityInteractChatActions.ShowAppHint(it))
                }
            is ChatRoomsViewEvent.OnNavigateToChatEvent ->
                gotoChatFragment(event.roomData)
            else -> Unit
        }
    }

    private fun openMessagesSettings(state: SettingsPrivateMessagesState?) {
        onActivityInteraction?.onAddFragment(
            fragment = PersonalMessagesFragment(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(
                ARG_PRIVACY_TYPE_VALUE to state?.settingsType?.key,
                ARG_COUNT_USERS_BLACKLIST to state?.blackListCount,
                ARG_COUNT_USERS_WHITELIST to state?.whiteListCount
            )
        )
    }

    private fun openChatRequestScreen() {
        onActivityInteraction?.onAddFragment(
            fragment = ChatRequestFragment(),
            isLightStatusBar = LIGHT_STATUSBAR
        )
    }

    private fun setActivityCallback(context: Context) {
        try {
            activityCallback = context as ActivityInteractionChatCallback
        } catch (e: ClassCastException) {
            Timber.e(e)
            throw ClassCastException("$context must implement interface ActivityInteractionChatCallback")
        }
    }

    private fun getFeatureToggles(): FeatureTogglesContainer =
        (activity?.application as FeatureTogglesContainer)
}
