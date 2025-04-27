package com.numplates.nomera3.modules.maps.ui.events.snippet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.isTrue
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentPostPageBinding
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetHost
import com.numplates.nomera3.modules.maps.ui.view.MapSnippetPageContent
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer

class EventPostPageFragment : BaseFragmentNew<FragmentPostPageBinding>(), EventSnippetPage, ScreenshotTakenListener {

    private val viewModel: EventPostPageViewModel by viewModels { App.component.getViewModelFactory() }

    private var pageIndex: Int? = null
    private var postId: Long? = null

    private var behavior: BottomSheetBehavior<*>? = null
    private var bottomSheetCallback: PageBottomSheetCallback? = null

    private var eventSnippetViewController: EventSnippetViewController? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPostPageBinding
        get() = FragmentPostPageBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, null)
        setupController()
        extractParameters()
        setupBottomSheetBehavior()
        setupPageContent()
    }

    override fun onScreenshotTaken() {
        if (getSnippetState() != SnippetState.Expanded) return
        (childFragmentManager.fragments.firstOrNull() as? ScreenshotTakenListener)?.onScreenshotTaken()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetCallback?.let {
            behavior?.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
    }

    override fun onStartFragment() {
        super.onStartFragment()
        childFragmentManager.fragments
            .filterIsInstance<BaseFragmentNew<*>>()
            .filter { it.isFragmentStarted.not() }
            .forEach { it.onStartFragment() }
    }

    override fun onStopFragment() {
        super.onStopFragment()
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

    override fun setSnippetState(snippetState: SnippetState.StableSnippetState) {
        behavior?.state = snippetState.behaviorValue
    }

    override fun setSnippetHeight(height: Int) {
        val pageIndex = pageIndex ?: return
        if (behavior?.peekHeight != height) {
            eventSnippetViewController?.onPageHeightChanged(pageIndex = pageIndex, height = height)
        }
        behavior?.peekHeight = height
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
        eventSnippetViewController = (parentFragment as? MapSnippetHost)?.getEventSnippetViewController()
    }

    private fun extractParameters() {
        pageIndex = arguments?.getInt(KEY_PAGE_INDEX)
        postId = arguments?.getLong(KEY_POST_ID)
    }

    private fun setupPageContent() {
        val item = postId?.let(viewModel::getEventPostItem) ?: return
        val post = item.eventObject.eventPost
        val contentFragment = PostFragmentV2().apply {
            arguments = Bundle().apply {
                putLong(IArgContainer.ARG_FEED_POST_ID, post.postId)
                putBoolean(IArgContainer.ARG_FEED_POST_NEED_TO_UPDATE, item.updateWhenCreated)
                putParcelable(IArgContainer.ARG_FEED_POST, post)
            }
        }
        contentFragment.lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStart(owner: LifecycleOwner) {
                super.onStart(owner)
                if (contentFragment.isFragmentStarted.not()) {
                    contentFragment.onStartFragment()
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
        childFragmentManager.beginTransaction()
            .replace(R.id.msbcv_post_page_bottomsheet_container, contentFragment)
            .commit()
    }

    private fun setupBottomSheetBehavior() {
        val params = binding?.msbcvPostPageBottomsheetContainer?.layoutParams as CoordinatorLayout.LayoutParams
        val needToAnimateSnippetEnter =
            pageIndex == 0 && eventSnippetViewController?.consumeNeedToAnimatePageEnter().isTrue()
        behavior = PageBottomSheetBehavior<View>(requireContext()).apply {
            peekHeight = postId?.let(viewModel::getEventPostItem)?.snippetHeight ?: 0
            isHideable = true
            state = if (needToAnimateSnippetEnter) {
                BottomSheetBehavior.STATE_HIDDEN
            } else {
                BottomSheetBehavior.STATE_COLLAPSED
            }
            PageBottomSheetCallback().let {
                bottomSheetCallback = it
                addBottomSheetCallback(it)
            }
        }
        params.behavior = behavior
        params.height = ViewGroup.LayoutParams.MATCH_PARENT
        binding?.msbcvPostPageBottomsheetContainer?.layoutParams = params
        if (needToAnimateSnippetEnter) {
            binding?.msbcvPostPageBottomsheetContainer?.post { behavior?.state = BottomSheetBehavior.STATE_COLLAPSED }
        }
    }

    private fun getPageContent(): EventSnippetPageContent? =
        (childFragmentManager.findFragmentById(R.id.msbcv_post_page_bottomsheet_container) as? EventSnippetPageContent)

    private inner class PageBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            val pageIndex = pageIndex ?: return
            val snippetState = SnippetState.fromBehaviorValue(newState)
            (childFragmentManager
                .findFragmentById(R.id.msbcv_post_page_bottomsheet_container) as? MapSnippetPageContent)
                ?.onSnippetStateChanged(snippetState)
            eventSnippetViewController?.onSnippetState(pageIndex = pageIndex, snippetState = snippetState)
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
    }

    companion object {
        private const val KEY_POST_ID = "KEY_POST_ID"
        private const val KEY_PAGE_INDEX = "KEY_PAGE_INDEX"

        fun newInstance(postId: Long, pageIndex: Int) = EventPostPageFragment().apply {
            arguments = Bundle().apply {
                putLong(KEY_POST_ID, postId)
                putInt(KEY_PAGE_INDEX, pageIndex)
            }
        }
    }
}
