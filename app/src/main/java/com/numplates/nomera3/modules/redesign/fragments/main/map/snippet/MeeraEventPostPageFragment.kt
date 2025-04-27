package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.isTrue
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.navigation.UiKitNavigationBarViewVisibilityState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostPageBinding
import com.numplates.nomera3.modules.comments.ui.fragment.MeeraPostFragmentV2
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPage
import com.numplates.nomera3.modules.maps.ui.events.snippet.EventSnippetPageContent
import com.numplates.nomera3.modules.maps.ui.events.snippet.PageBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.model.MapMode
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPageContent
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraEventSnippetViewController
import com.numplates.nomera3.modules.redesign.fragments.main.map.MeeraMapSnippetHost
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer


const val SNIPPET_DEFAULT_HEIGHT = 590
private const val SNIPPET_STATE_CHANGE_DELAY = 500
private const val SNIPPET_HALF_COLLAPSED_PEEK_HEIGHT = 180
private const val DELAY_BOTTOM_SHEET_CALLBACK = 500L
private const val EXPANDED_SNIPPET_MIN_OFFSET = 0.1F

class MeeraEventPostPageFragment : MeeraBaseDialogFragment(
    layout = R.layout.fragment_post_page,
    behaviourConfigState = ScreenBehaviourState.Empty
), EventSnippetPage, ScreenshotTakenListener {

    private var returningBackFromEvent: Boolean = false
    private val viewModel: MeeraEventPostPageViewModel by viewModels { App.component.getViewModelFactory() }

    private var isCollapsed = false

    private var eventOpenedFromRoad: Boolean = false
    private var pageIndex: Int? = null
    private var postId: Long? = null
    private val binding by viewBinding(FragmentPostPageBinding::bind)

    private var lastStateChangedTime = 0L
    private var currentSnippetState = EventSnippetState.VISIBLE
    var snippetPeekHeight = 0
    private val snippetVisibleOffset: Float
        get() {
            val maxHeight = binding.msbcvPostPageBottomsheetContainer.measuredHeight
            return (snippetPeekHeight - getSnippetHeight()).toFloat() / (maxHeight - getSnippetHeight()).toFloat()
        }
    private val snippetHalfHiddenOffset: Float
        get() {
            return - (snippetPeekHeight - SNIPPET_HALF_COLLAPSED_PEEK_HEIGHT.dp).toFloat() / snippetPeekHeight.toFloat()
        }

    private var contentFragment: MeeraPostFragmentV2? = null

    private var behavior: BottomSheetBehavior<*>? = null
    private var bottomSheetCallback: PageBottomSheetCallback? = null

    private var eventSnippetViewController: MeeraEventSnippetViewController? = null
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.isApplyNavigationConfig = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        setupController()
        extractParameters()
        setupBottomSheetBehavior()
        setupPageUi()
        setupPageContent()
    }

    override fun onScreenshotTaken() {
        if (getSnippetState() == SnippetState.Closed) return
        (childFragmentManager.fragments.firstOrNull() as? ScreenshotTakenListener)?.onScreenshotTaken()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetCallback?.let {
            behavior?.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
    }

    override fun onStart() {
        super.onStart()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted.not() }
            .forEach { it.onStartFragment() }
    }

    override fun onStop() {
        super.onStop()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted }
            .forEach { it.onStopFragment() }
    }

    override fun onDestroyPage() {
        childFragmentManager.findFragmentById(R.id.msbcv_post_page_bottomsheet_container)?.let { contentFragment ->
            childFragmentManager.beginTransaction()
                .remove(contentFragment)
                .commitNow()
        }
    }

    override fun onBackFromEventClicked() {
        this.returningBackFromEvent = true
        behavior?.isHideable = true
        setSnippetState(SnippetState.Closed)
    }

    override fun setSnippetState(snippetState: SnippetState.StableSnippetState) {
        if (snippetState == SnippetState.Closed) {
            behavior?.isHideable = true
        }
        behavior?.state = snippetState.behaviorValue
    }

    override fun setSnippetHeight(height: Int) {
        val pageIndex = pageIndex ?: return
        if (behavior?.peekHeight != height) {
            snippetPeekHeight = height
            eventSnippetViewController?.onPageHeightChanged(pageIndex = pageIndex, height = height)
        }
        val snippetState = when {
            currentSnippetState == EventSnippetState.HALF_HIDDEN -> SnippetState.HalfCollapsedPreview
            else -> SnippetState.fromBehaviorValue(behavior?.state ?: STATE_COLLAPSED)
        }
        if (snippetState == SnippetState.Preview) {
            behavior?.peekHeight = height
        }
    }

    override fun updateEventSnippetPageContent(postUIEntity: PostUIEntity) {
        getPageContent()?.onEventPostUpdated(postUIEntity)
    }

    override fun onEventPostUpdated(postUIEntity: PostUIEntity) {
        val pageIndex = pageIndex ?: return
        eventSnippetViewController?.updateEventPost(pageIndex = pageIndex, post = postUIEntity)
    }

    override fun onUserDeletedOwnPost() {
        eventSnippetViewController?.onUserRemovedOwnPost()
    }

    override fun onSelectPage() {
        getPageContent()?.onPageSelected()
    }

    override fun getSnippetState(): SnippetState = behavior?.state
        ?.let(SnippetState::fromBehaviorValue)
        ?: SnippetState.Closed

    override fun getPageIndex(): Int? = pageIndex

    override fun getSnippetHeight(): Int = behavior?.peekHeight ?: 0

    override fun getSnippetView(): View? = view

    fun getPostId(): Long? = postId

    private fun setupController() {
        eventSnippetViewController = (parentFragment as? MeeraMapSnippetHost)?.getEventSnippetViewController()
    }

    private fun extractParameters() {
        pageIndex = arguments?.getInt(KEY_PAGE_INDEX)
        postId = arguments?.getLong(KEY_POST_ID)
    }

    private fun setupPageContent() {
        val item = postId?.let(viewModel::getEventPostItem) ?: return
        val post = item.eventObject.eventPost
        eventOpenedFromRoad = post.openedFromRoad
        contentFragment = MeeraPostFragmentV2().apply {
            arguments = Bundle().apply {
                putLong(IArgContainer.ARG_FEED_POST_ID, post.postId)
                putBoolean(IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE, item.updateWhenCreated)
                putBoolean(IArgContainer.ARG_FROM_MAP, true)
                putParcelable(IArgContainer.ARG_FEED_POST, post)
            }
        }
        contentFragment?.lifecycle?.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
//                if (contentFragment.isFragmentStarted.not()) {
//                    contentFragment.onStartFragment()
//                }
            }

            override fun onStop(owner: LifecycleOwner) {
//                if (contentFragment.isFragmentStarted) {
//                    contentFragment.onStopFragment()
//                }
                super.onStop(owner)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)
                super.onDestroy(owner)
            }
        })
        contentFragment?.let {
            childFragmentManager.beginTransaction()
                .replace(R.id.msbcv_post_page_bottomsheet_container, it)
                .commit()
        }
    }

    private fun setupBottomSheetBehavior() {
        val params = binding.msbcvPostPageBottomsheetContainer.layoutParams as CoordinatorLayout.LayoutParams
        val needToAnimateSnippetEnter =
            pageIndex == 0 && eventSnippetViewController?.consumeNeedToAnimatePageEnter().isTrue()
        behavior = PageBottomSheetBehavior<View>(requireContext()).apply {
            val currentSnippetPeekHeight = postId?.let(viewModel::getEventPostItem)?.snippetHeight ?: 0
            peekHeight = currentSnippetPeekHeight
            snippetPeekHeight = currentSnippetPeekHeight
            isHideable = true
            state = if (needToAnimateSnippetEnter) {
                STATE_HIDDEN
            } else {
                STATE_COLLAPSED
            }
            PageBottomSheetCallback().let {
                bottomSheetCallback = it
                doDelayed(DELAY_BOTTOM_SHEET_CALLBACK) {
                    addBottomSheetCallback(it)
                }
            }
        }
        params.behavior = behavior
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding.msbcvPostPageBottomsheetContainer.layoutParams = params
        binding.msbcvPostPageBottomsheetContainer.post {
            if (needToAnimateSnippetEnter) {
                behavior?.state = STATE_COLLAPSED
                val pageIndex = pageIndex ?: return@post
                eventSnippetViewController?.onSnippetState(
                    pageIndex = pageIndex,
                    snippetState = SnippetState.Preview,
                    changeMapControls = !returningBackFromEvent
                )
            }
            collapseToSnippet()
        }
    }

    private fun setupPageUi() {
        NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().stateVisibility = UiKitNavigationBarViewVisibilityState.GONE
    }

    private fun getPageContent(): EventSnippetPageContent? =
        (childFragmentManager.findFragmentById(R.id.msbcv_post_page_bottomsheet_container) as? EventSnippetPageContent)

    private inner class PageBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val pageIndex = pageIndex ?: return
            val snippetState = when {
                currentSnippetState == EventSnippetState.HALF_HIDDEN && newState == STATE_HIDDEN -> SnippetState.Closed
                currentSnippetState == EventSnippetState.HALF_HIDDEN -> SnippetState.HalfCollapsedPreview
                else -> SnippetState.fromBehaviorValue(newState)
            }
            (childFragmentManager
                .findFragmentById(R.id.msbcv_post_page_bottomsheet_container) as? MapSnippetPageContent)
                ?.onSnippetStateChanged(snippetState)
            eventSnippetViewController?.onSnippetState(
                pageIndex = pageIndex,
                snippetState = snippetState,
                changeMapControls = !returningBackFromEvent
            )
            when (newState) {
                BottomSheetBehavior.STATE_EXPANDED -> {
                    contentFragment?.onStateChanged(newState)
                    getPageContent()?.onSnippetStateChanged(false)
                    behavior?.skipCollapsed = false
                    behavior?.isHideable = false
                    isCollapsed = false
                }

                STATE_COLLAPSED -> {
                    contentFragment?.onStateChanged(newState)
                    if (currentSnippetState == EventSnippetState.HALF_HIDDEN) return
                    collapseToSnippet()
                    isCollapsed = true
                }

                STATE_HIDDEN -> {
                    if (eventOpenedFromRoad) {
                        (parentFragment as? MeeraMapSnippetHost)?.setMapMode(MapMode.Main)
                    }
                }
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (returningBackFromEvent) return
            if (System.currentTimeMillis() - lastStateChangedTime < SNIPPET_STATE_CHANGE_DELAY) return
            if (slideOffset > EXPANDED_SNIPPET_MIN_OFFSET && currentSnippetState == EventSnippetState.VISIBLE) {
                getPageContent()?.onSnippetStateChanged(false)
            }
            if (!eventOpenedFromRoad) return
            when (currentSnippetState) {
                EventSnippetState.HALF_HIDDEN -> {
                    if (slideOffset >= snippetVisibleOffset) {
                        lastStateChangedTime = System.currentTimeMillis()
                        setVisibleState()
                    }
                }
                EventSnippetState.VISIBLE -> {
                    if (slideOffset <= snippetHalfHiddenOffset) {
                        lastStateChangedTime = System.currentTimeMillis()
                        setHalfCollapsedState()
                    }
                }
            }
        }
    }

    private fun collapseToSnippet() {
        if (isVisible) {
            getPageContent()?.onSnippetStateChanged(true)
            behavior?.isHideable = true
            behavior?.skipCollapsed = false
        }
    }

    private fun setVisibleState() {
        currentSnippetState = EventSnippetState.VISIBLE
        behavior?.apply {
            peekHeight = snippetPeekHeight
            state = STATE_COLLAPSED
            isHideable = true
        }
    }

    private fun setHalfCollapsedState() {
        currentSnippetState = EventSnippetState.HALF_HIDDEN
        behavior?.apply {
            peekHeight = SNIPPET_HALF_COLLAPSED_PEEK_HEIGHT.dp
            state = STATE_COLLAPSED
            isHideable = false
        }
    }

    companion object {
        private const val KEY_POST_ID = "KEY_POST_ID"
        private const val KEY_PAGE_INDEX = "KEY_PAGE_INDEX"

        fun newInstance(postId: Long, pageIndex: Int) = MeeraEventPostPageFragment().apply {
            arguments = Bundle().apply {
                putLong(KEY_POST_ID, postId)
                putInt(KEY_PAGE_INDEX, pageIndex)
            }
        }
    }
}

enum class EventSnippetState {
    HALF_HIDDEN, VISIBLE
}
