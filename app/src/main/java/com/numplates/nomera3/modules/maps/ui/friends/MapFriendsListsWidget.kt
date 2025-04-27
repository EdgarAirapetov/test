package com.numplates.nomera3.modules.maps.ui.friends

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.onMeasured
import com.meera.core.extensions.visible
import com.meera.core.keyboard.KeyboardEventListener
import com.meera.core.utils.checkAppRedesigned
import com.meera.uikit.widgets.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.DialogMapFriendsBinding
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendListItem
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


private const val LIST_STUB_COUNT = 4
private const val LIST_ITEM_HEIGHT = 88
private const val LIST_BOTTOM_PADDING = 340
private const val CHAT_TRANSITION_DELAY = 500L
private const val CHAT_TAG = "CHAT_TAG"

class MapFriendsListsWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle), IOnBackPressed {

    var uiActionListener: ((MapFriendsListUiAction) -> Unit)? = null
    private var keyListener: KeyboardEventListener? = null
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.dialog_map_friends, this, false)
        .apply(::addView)
        .let(DialogMapFriendsBinding::bind)
    private var eventPostBehavior: BottomSheetBehavior<View>? = null

    private var uiModel: MapFriendsListUiModel? = null
    private var interceptTouchEvents = false
    private var contentFragment: PostFragmentV2? = null
    private var lastState = BottomSheetBehavior.STATE_HALF_EXPANDED
    private var isFirstStart = true
    private var lastScrollPosition = 0

    init {
        initListeners()
        onMeasured {
            createEventPostBottomSheetBehaviour()
        }
    }

    private fun addKeyboardListener() {
        if (!isFirstStart || keyListener != null) return
        keyListener = KeyboardEventListener(context as AppCompatActivity) { isOpen ->
            val listener = object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    context?.hideKeyboard(rootView)
                    super.onScrollStateChanged(recyclerView, newState)
                }
            }
            if (isOpen) {
                eventPostBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                binding?.rvParticipantsListItems?.removeSnapHelper()
                val params = CoordinatorLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                binding.rvParticipantsListItems?.layoutParams = params
                doDelayed(400) {
                    binding?.rvParticipantsListItems?.addOnScrollListener(listener)
                }
            } else {
                binding?.rvParticipantsListItems?.removeOnScrollListener(listener)
            }
            eventPostBehavior?.isDraggable = !isOpen
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
            state = BottomSheetBehavior.STATE_HIDDEN
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {


                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Main) {
                            delay(200)
                            isDraggable = false
                        }
                    } else {
                        isDraggable = true
                    }

                    when (newState) {
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            hideKeyboard()
                            uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                            binding?.root?.setMargins(16.dp, 0, 16.dp, 0)
                            binding?.isFriendSearch?.forceBtnCloseVisibility(false)
                            binding?.rvParticipantsListItems?.addSnapHelper()
                            val params = binding.rvParticipantsListItems.layoutParams
                            params?.height = dpToPx(LIST_BOTTOM_PADDING)
                            binding.rvParticipantsListItems?.layoutParams = params
                            turnOffProfile()
                        }

                        BottomSheetBehavior.STATE_EXPANDED -> {
                            uiActionListener?.invoke(MapFriendsListUiAction.HideWidget)
                            binding?.isFriendSearch?.forceBtnCloseVisibility(true)
                            binding?.rvParticipantsListItems?.removeSnapHelper()
                            val params = CoordinatorLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            binding.rvParticipantsListItems?.layoutParams = params
                        }

                        BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                            uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                            hideKeyboard()
                            binding?.root?.setMargins(16.dp, 0, 16.dp, 0)
                            binding?.isFriendSearch?.forceBtnCloseVisibility(false)
                            binding?.rvParticipantsListItems?.addSnapHelper()
                            val params = binding.rvParticipantsListItems.layoutParams
                            params?.height = dpToPx(LIST_BOTTOM_PADDING)
                            binding.rvParticipantsListItems?.layoutParams = params
                            peekHeight = resources.getDimensionPixelSize(R.dimen.map_friends_lists_height)
                            turnOffProfile()
                        }

                        BottomSheetBehavior.STATE_DRAGGING -> {
                            if (binding.rvParticipantsListItems?.paddingBottom != 0) {
//                                val params = RecyclerView.LayoutParams(
//                                    ViewGroup.LayoutParams.MATCH_PARENT,
//                                    ViewGroup.LayoutParams.MATCH_PARENT
//                                )
//                                binding.rvParticipantsListItems?.layoutParams = params
//                                binding.rvParticipantsListItems?.setPadding(0, 0, 0, dpToPx(0))
                            }
                        }

                        BottomSheetBehavior.STATE_HIDDEN -> {
                            hideKeyboard()
                            uiActionListener?.invoke(MapFriendsListUiAction.ShowWidget)
                            lastScrollPosition = 0
                            binding.isFriendSearch.clear()
                            uiActionListener?.invoke(MapFriendsListUiAction.Close)
                        }
                    }
                    lastState = newState
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
        val fragmentManager = findFragment<Fragment>().childFragmentManager
        val chatFragment = fragmentManager.findFragmentByTag(CHAT_TAG) as? ChatFragmentNew

        return when {
            chatFragment?.gifMenuDelegate?.getBehavior() != BottomSheetBehavior.STATE_HIDDEN -> {
                return (chatFragment?.gifMenuDelegate?.hideGifMenuWhenBackPressed() ?: true)
            }
            eventPostBehavior?.state == BottomSheetBehavior.STATE_EXPANDED -> {
                eventPostBehavior?.isDraggable = false
                eventPostBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
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

        if (uiModel.updatePosition) {
            findViewTreeLifecycleOwner()?.lifecycleScope?.launch(Dispatchers.Main) {
                delay(200)
//                binding?.rvParticipantsListItems?.scrollToPosition(0)
            }
        }
        binding?.isFriendSearch?.isVisible = !uiModel.items.contains(MapFriendListItem.FindFriendItemUiModel)
    }

    @Suppress("detekt:UnusedPrivateMember")
    private fun getFriendListPadding(): Int {
        return runCatching {
            binding.rvParticipantsListItems.height - (LIST_STUB_COUNT * LIST_ITEM_HEIGHT)
        }.getOrElse { LIST_BOTTOM_PADDING.dp }
    }

    private fun initListeners() {
        with(binding) {
            isFriendSearch.doAfterSearchTextChanged { search ->
                uiActionListener?.invoke(MapFriendsListUiAction.SearchFriends(search))
            }

            isFriendSearch.setCloseButtonClickedListener {
                eventPostBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
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
        eventPostBehavior?.isDraggable = false
        addKeyboardListener()
        eventPostBehavior?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        isFirstStart = false
        keyListener?.onResume(context as AppCompatActivity)
    }

    fun close() {
        eventPostBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        keyListener?.onPause(context as AppCompatActivity)
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
        checkAppRedesigned(
            isRedesigned = {
                postDelayed({ openRedesignChatFragment(userId) }, CHAT_TRANSITION_DELAY)
            },
            isNotRedesigned = {
                postDelayed({ openChatFragment(userId) }, CHAT_TRANSITION_DELAY)
            }
        )
    }

    private fun openRedesignChatFragment(userId: Long) {
        val fragmentManager = findFragment<Fragment>().childFragmentManager

        val contentFragment = ChatFragmentNew().apply {
            arguments = Bundle().apply {
                putParcelable(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = userId,
                        isCorner = true
                    )
                )
            }
        }
        contentFragment.userSnippet = WeakReference(this)

        contentFragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (contentFragment.isFragmentStarted.not()) {
                    contentFragment.onStartFragment()
                    binding?.root2?.setPadding(0, 0, 0, 10.dp)
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (contentFragment.isFragmentStarted) {
                    contentFragment.onStopFragment()
                }
                super.onStop(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                super.onDestroy(owner)
            }
        })

        binding?.root3?.animateAlpha(
            from = ALPHA_VISIBLE,
            to = ALPHA_INVISIBLE,
            duration = EVENT_POST_ENTER_ANIM_DURATION_MS
        )
        binding?.root?.setMargins(0, 0, 0, 0)
        fragmentManager.beginTransaction()
            .replace(R.id.root2, contentFragment)
            .runOnCommit {
                binding?.root2?.alpha = ALPHA_INVISIBLE
                binding?.root2?.visible()
                binding?.root2?.animateAlpha(
                    from = ALPHA_INVISIBLE,
                    to = ALPHA_VISIBLE,
                    duration = EVENT_POST_ENTER_ANIM_DURATION_MS
                ) {
                    eventPostBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    eventPostBehavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.map_friends_lists_height)
                }
            }
            .commit()
    }

    private fun openChatFragment(userId: Long) {
        val fragmentManager = findFragment<Fragment>().childFragmentManager

        val chatFragment = ChatFragmentNew().apply {
            arguments = Bundle().apply {
                putParcelable(
                    IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                        initType = ChatInitType.FROM_PROFILE,
                        userId = userId,
                        isCorner = true
                    )
                )
            }
        }
        chatFragment.userSnippet = WeakReference(this)

        chatFragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (chatFragment.isFragmentStarted.not()) {
                    chatFragment.onStartFragment()
                    binding?.root2?.setPadding(0, 0, 0, 10.dp)
                }
            }

            override fun onStop(owner: LifecycleOwner) {
                if (chatFragment.isFragmentStarted) {
                    chatFragment.onStopFragment()
                }
                super.onStop(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                super.onDestroy(owner)
            }
        })

        binding?.root3?.animateAlpha(
            from = ALPHA_VISIBLE,
            to = ALPHA_INVISIBLE,
            duration = EVENT_POST_ENTER_ANIM_DURATION_MS
        )
        binding?.root?.setMargins(0, 0, 0, 0)
        fragmentManager.beginTransaction()
            .replace(R.id.root2, chatFragment, CHAT_TAG)
            .runOnCommit {
                binding?.root2?.alpha = ALPHA_INVISIBLE
                binding?.root2?.visible()
                binding?.root2?.animateAlpha(
                    from = ALPHA_INVISIBLE,
                    to = ALPHA_VISIBLE,
                    duration = EVENT_POST_ENTER_ANIM_DURATION_MS
                ) {
                    eventPostBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    eventPostBehavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.map_friends_lists_height)

                }
            }
            .commit()
    }

    fun saveScrollPosition() {
        lastScrollPosition = (binding.rvParticipantsListItems.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition() ?: 0
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
