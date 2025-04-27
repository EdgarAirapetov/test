package com.numplates.nomera3.modules.redesign.fragments.main.map.friends

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.safeNavigate
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.meera.uikit.widgets.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogMapFriendsBinding
import com.numplates.nomera3.modules.chat.MeeraChatFragment
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.comments.ui.fragment.MeeraPostFragmentV2
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val LIST_BOTTOM_PADDING = 340
private const val CL_ROOT_HEIGHT = 400
//private const val CHAT_TRANSITION_DELAY = 500L

class MeeraMapFriendsListsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), IOnBackPressed {

    private var isOpened: Boolean = false
    var uiActionListener: ((MapFriendsListUiAction) -> Unit)? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_dialog_map_friends, this, false)
        .apply(::addView)
        .let(MeeraDialogMapFriendsBinding::bind)

    private var eventPostBehavior: BottomSheetBehavior<View>? = null

    private var uiModel: MapFriendsListUiModel? = null
    private var interceptTouchEvents = false
    private var contentFragment: MeeraPostFragmentV2? = null
    private var isFirstStart = true
    private var lastScrollPosition = 0
    private var wasKeyboardExpanded = false
    private var findFriend = false

    init {
        initListeners()
        onMeasured {
            createEventPostBottomSheetBehaviour()
        }
        binding?.clTopDrag?.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun addKeyboardListener() {
        if (isFirstStart || keyboardHeightProvider == null) {
            keyboardHeightProvider = KeyboardHeightProvider(binding.root)
        }
        startKeyboardListener()
    }

    private fun startKeyboardListener() {
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { height ->
            val isOpen = height > 0
            val listener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    context?.hideKeyboard(rootView)
                    super.onScrollStateChanged(recyclerView, newState)
                }
            }
            if (isOpen) {
                eventPostBehavior?.isDraggable = true
                wasKeyboardExpanded = isOpen
                eventPostBehavior?.state = STATE_EXPANDED
                binding?.rvParticipantsListItems?.removeSnapHelper()
                binding?.rvParticipantsListItems?.setPadding(0, 0, 0, dpToPx(0))
                doDelayed(400) {
                    binding?.rvParticipantsListItems?.addOnScrollListener(listener)
                }
            } else {
                if (eventPostBehavior?.state == STATE_EXPANDED) {
                    binding.rvParticipantsListItems.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    binding.rvParticipantsListItems.requestLayout()
                }

                binding?.rvParticipantsListItems?.removeOnScrollListener(listener)
            }
        }
    }

    fun setState(snippetState: SnippetState.StableSnippetState) {
        binding?.isFriendSearch?.forceBtnCloseVisibility(false)
        eventPostBehavior?.state = snippetState.behaviorValue
        turnOffProfile()
    }

    private fun createEventPostBottomSheetBehaviour() {
        eventPostBehavior = BottomSheetBehavior.from<View>(binding.vgMapListsBottomsheet).apply {
            skipCollapsed = false
            isHideable = true
            peekHeight = resources.getDimensionPixelSize(R.dimen.map_friends_lists_height)
            state = STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_COLLAPSED || newState == STATE_HALF_EXPANDED) {
                        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Main) {
                            delay(200)
                            isDraggable = false
                        }
                    } else {
                        isDraggable = true
                    }
                    when (newState) {
                        STATE_COLLAPSED -> {
                            if (findFriend) {
                                state = STATE_HIDDEN
                            } else {
                                state = STATE_HALF_EXPANDED
                            }
                        }

                        STATE_EXPANDED -> {
                            uiActionListener?.invoke(MapFriendsListUiAction.HideWidget)
                            binding?.isFriendSearch?.forceBtnCloseVisibility(true)
                            binding?.rvParticipantsListItems?.removeSnapHelper()
                            binding?.rvParticipantsListItems?.setPadding(0, 0, 0, 0)

                            binding.vgMapListsBottomsheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                            binding.vgMapListsBottomsheet.requestLayout()
                            binding.rvParticipantsListItems.updateStateBottomSheet(STATE_EXPANDED)
                        }

                        STATE_HALF_EXPANDED -> {
                            uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                            hideKeyboard()
                            binding?.root?.setMargins(16.dp, 0, 16.dp, 0)
                            binding?.isFriendSearch?.forceBtnCloseVisibility(false)
                            binding?.rvParticipantsListItems?.addSnapHelper()

                            binding.rvParticipantsListItems.layoutParams.height = LIST_BOTTOM_PADDING.dp
                            binding.rvParticipantsListItems.requestLayout()
                            binding.rvParticipantsListItems.updateStateBottomSheet(STATE_HALF_EXPANDED)
                            peekHeight = resources.getDimensionPixelSize(R.dimen.map_friends_lists_height)
                            turnOffProfile()
                        }

                        BottomSheetBehavior.STATE_DRAGGING -> {
                            if (binding.rvParticipantsListItems?.paddingBottom != 0) {
                                binding.rvParticipantsListItems?.setPadding(0, 0, 0, dpToPx(0))
                            }
                        }

                        STATE_HIDDEN -> {
                            isOpened = false
                            wasKeyboardExpanded = false
                            hideKeyboard()
                            uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                            lastScrollPosition = 0
                            binding.isFriendSearch.clear()
                            if (!isFirstStart) {
                                uiActionListener?.invoke(MapFriendsListUiAction.Close)
                            }
                            keyboardHeightProvider?.release()
                        }
                    }
                }

                override fun onSlide(view: View, offset: Float) = Unit
            })
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return interceptTouchEvents || super.onInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return interceptTouchEvents || super.onTouchEvent(event)
    }

    override fun onBackPressed(): Boolean {
        return when {
            eventPostBehavior?.state == STATE_EXPANDED -> {
                eventPostBehavior?.isDraggable = false
                eventPostBehavior?.state = STATE_COLLAPSED
                true
            }

            else -> false
        }
    }

    fun onScreenshotTaken() {
        contentFragment?.onScreenshotTaken()
    }

    fun getState() = eventPostBehavior?.state

    fun setUiModel(uiModel: MapFriendsListUiModel, listener: ((MapFriendsListUiAction) -> Unit)) {

        this.uiModel = uiModel
        if (binding.rvParticipantsListItems.adapter == null) {
            this.uiActionListener = listener
            binding.rvParticipantsListItems.init(
                listener
            )
        }

        binding?.rvParticipantsListItems?.setItems(uiModel.items)
        binding?.rvParticipantsListItems?.scrollToPosition(0)

        findFriend = uiModel.items.contains(MapFriendListItem.FindFriendItemUiModel)
        binding?.isFriendSearch?.isVisible = !findFriend
        if (!isOpened) return
        if (findFriend) {
            binding.clRoot.layoutParams.height = CL_ROOT_HEIGHT.dp
            binding.clRoot.requestLayout()
            eventPostBehavior?.maxHeight = CL_ROOT_HEIGHT.dp
            eventPostBehavior?.state = STATE_EXPANDED
        } else {
            binding.clRoot.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.clRoot.requestLayout()
            eventPostBehavior?.maxHeight = ViewGroup.LayoutParams.MATCH_PARENT
            if (!wasKeyboardExpanded) eventPostBehavior?.state = STATE_HALF_EXPANDED
        }
    }

    private fun initListeners() {
        with(binding) {
            isFriendSearch.doAfterSearchTextChanged { search ->
                uiActionListener?.invoke(MapFriendsListUiAction.SearchFriends(search))
            }

            isFriendSearch.setCloseButtonClickedListener {
                eventPostBehavior?.state = STATE_HALF_EXPANDED
            }
        }
        addRecyclerViewPaginator()
    }

    private fun addRecyclerViewPaginator() {
        RecyclerViewPaginator(
            recyclerView = binding.rvParticipantsListItems,
            onLast = {
                uiModel?.isLastPage.isTrue()
            },
            isLoading = {
                uiModel?.isLoadingNextPage.isTrue()
            },
            loadMore = {
                uiActionListener?.invoke(MapFriendsListUiAction.LoadNextPageRequested)
            },
        ).apply {
            endWithAuto = true
        }
    }

    fun open() {
        isOpened = true
        eventPostBehavior?.isDraggable = false
        addKeyboardListener()
        isFirstStart = false

        binding.rvParticipantsListItems.getFirstItem()?.let {
            val uiAction = MapFriendsListUiAction.MapFriendListItemSelected(it, position = 0)
            uiActionListener?.invoke(uiAction)
        }
    }

    fun close() {
        eventPostBehavior?.state = STATE_HIDDEN
    }

    fun turnOffProfile() {
        binding?.root2?.setPadding(0, 0, 0, 0)
        if (binding?.root2?.isVisible == true) {
            uiActionListener?.invoke(MapFriendsListUiAction.UpdateSelectedUser)

            binding?.root2?.isVisible = false
            binding?.root2?.animateAlpha(
                from = ALPHA_VISIBLE,
                to = ALPHA_INVISIBLE,
                duration = EVENT_POST_ENTER_ANIM_DURATION_MS
            )
            binding?.root3?.animateAlpha(
                from = ALPHA_INVISIBLE,
                to = ALPHA_VISIBLE,
                duration = EVENT_POST_ENTER_ANIM_DURATION_MS
            )
            postDelayed({
                scrollToPosition(lastScrollPosition)
            }, 400)
        }
    }

    fun sendMessage(userId: Long) {
        hideKeyboard()
        openRedesignChatFragment(userId)
    }

    private fun openRedesignChatFragment(userId: Long) {
        NavigationManager.getManager().topNavController
            .safeNavigate(
                resId = R.id.action_global_meeraChatFragment,
                bundle = bundleOf(
                    IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = userId,
                        isDraggable = true,
                        isCorner = true,
                        fromMap = true
                    )
                )
            )
        // Delay for fragment putting in FM after navigate
        Handler(Looper.getMainLooper()).postDelayed({
            val chatFragment =
                NavigationManager.getManager().topNavHost?.childFragmentManager?.fragments?.get(0) as? MeeraChatFragment
            chatFragment?.lifecycle?.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    super.onDestroy(owner)
                    chatFragment.lifecycle.removeObserver(this)
                    NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility =
                        UiKitNavigationBarViewVisibilityState.GONE
                    uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                }
            })
        }, 200)
    }

    fun saveScrollPosition() {
        lastScrollPosition =
            (binding.rvParticipantsListItems.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                ?: 0
    }

    fun scrollToPosition(position: Int) {
        hideKeyboard()

        (binding.rvParticipantsListItems.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
            position,
            binding?.rvParticipantsListItems?.paddingTop ?: 0
        )
    }

    companion object {
        private const val ALPHA_VISIBLE = 1f
        private const val ALPHA_INVISIBLE = 0f
        private const val EVENT_POST_ENTER_ANIM_DURATION_MS = 500L
    }
}
