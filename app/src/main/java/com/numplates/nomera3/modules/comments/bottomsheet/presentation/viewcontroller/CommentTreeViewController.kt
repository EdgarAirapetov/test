package com.numplates.nomera3.modules.comments.bottomsheet.presentation.viewcontroller

import android.annotation.SuppressLint
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.fadeOut
import com.meera.core.extensions.gone
import com.meera.core.extensions.setListener
import com.meera.core.extensions.visible
import com.meera.core.utils.pagination.RecyclerPaginationUtil
import com.numplates.nomera3.databinding.CommentsBottomSheetBinding
import com.numplates.nomera3.modules.chat.helpers.replymessage.ReplySwipeController
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipeControllerActions
import com.numplates.nomera3.modules.chat.helpers.replymessage.SwipingItemType
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.adapter.MeeraCommentAdapter
import com.numplates.nomera3.modules.comments.ui.entity.CommentChunk
import com.numplates.nomera3.modules.comments.ui.entity.CommentEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.comments.ui.util.PaginationHelper
import com.numplates.nomera3.modules.comments.ui.util.SpeedyLinearLayoutManager
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.presentation.view.ui.swiperefresh.SwipyRefreshLayoutDirection
import kotlin.math.max

private const val DELAY_SCROLLING = 200L
private const val PROGRESS_HIDE_DURATION_MS = 150L
private const val EMPTY_COMMENTS_SHOW_DURATION_MS = 450L
private const val EMPTY_COMMENTS_SHOW_DELAY_MS = 200L
private const val SCROLL_DOWN_BUTTON_VISIBILITY_DELAY_MS = 100L
private const val EMPTY_COMMENTS_HIDE_DURATION_MS = 100L
private const val SCROLL_DOWN_BUTTON_HIDE_BEFORE_ITEM_COUNT = 3

/**
 * View-контроллер отвечающий за отображение комментариев
 */
class CommentTreeViewController(
    private val commentObserver: (MutableList<CommentUIType>) -> Unit,
    private val commentsAdapterCallback: ICommentsActionsCallback,
    private val paginationHelper: PaginationHelper,
    private val binding: CommentsBottomSheetBinding,
    private val fragment: Fragment,
    private val callback: Callback
) {

    val commentAdapter: MeeraCommentAdapter
    private var itemTouchHelper: ItemTouchHelper? = null

    init {
        commentAdapter = createCommentAdapter()
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)

            if (commentAdapter.itemCount > 0) {
                hideEmptyPlaceCommentsHolder()
            }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)

            if (commentAdapter.itemCount == 0) {
                showEmptyPlaceCommentsHolder()
            }
        }

        override fun onChanged() {
            super.onChanged()
            if (commentAdapter.itemCount == 0) {
                showEmptyPlaceCommentsHolder()
            }
        }
    }

    init {
        initCommentAdapter()
        initRecycler()
        initSwipeController()
        initRefreshListener()
        initOnScrollListener()
    }

    fun findCommentById(commentID: Long): CommentUIType? = commentAdapter.findCommentById(commentID)

    fun handleMarkCommentAsDeleted(commentID: Long, whoDeleteComment: WhoDeleteComment): CommentUIType? {
        return commentAdapter.replaceCommentByDeletion(commentID = commentID, whoDeleteComment = whoDeleteComment)
    }

    fun handleCancelCommentDeletion(originalComment: CommentUIType) {
        commentAdapter.restoreComment(originalComment)
    }

    fun handleErrorDeleteComment(comment: CommentUIType) {
        commentAdapter.restoreComment(comment)
    }

    fun handleUpdateReaction(position: Int, reactionUpdate: MeeraReactionUpdate) {
        commentAdapter.notifyItemChanged(position, reactionUpdate)
    }

    fun handleInnerCommentError(commentSeparatorEntity: CommentSeparatorEntity) {
        commentAdapter.stopProgressInnerPagination(commentSeparatorEntity)
    }

    fun handleNewComment(
        beforeMyComment: List<CommentUIType>,
        hasIntersection: Boolean,
        needSmoothScroll: Boolean,
        needToShowLastFullComment: Boolean
    ) {
        val commentRecyclerView = binding.rvMomentComments
        val itemAnimator = commentRecyclerView.itemAnimator
        commentRecyclerView.itemAnimator = null
        val index = commentAdapter.itemCount

        if (needToShowLastFullComment) {
            showFullLastComment(beforeMyComment)
        }

        commentAdapter.addItemsNext(beforeMyComment)

        commentRecyclerView.scrollToLastPosition(needSmoothScroll)

        // TODO принудительные-задержки, нужно от них избавляться
        // TODO задача на ТехДолг: https://nomera.atlassian.net/browse/BR-17558
        if (!hasIntersection) {
            fragment.doDelayed(50) {
                commentAdapter.removeItemsBefore(index = index)
            }
        }

        fragment.doDelayed(200) {
            commentRecyclerView.itemAnimator = itemAnimator
        }

        fragment.doDelayed(400) {
            handleScrollDownBtnVisibility()
        }
    }

    fun handleNewInnerComment(parentId: Long, chunk: CommentChunk) {
        val itemAnimator = binding.rvMomentComments.itemAnimator
        binding.rvMomentComments.itemAnimator = null

        showFullLastComment(chunk.items)

        commentAdapter.addItemsNext(parentId, chunk) {
            binding.rvMomentComments.smoothScrollToPosition(it)
        }

        fragment.doDelayed(200) {
            binding.rvMomentComments.itemAnimator = itemAnimator
        }
    }

    fun handleLiveComments(commentChunk: CommentChunk, isCommentable: Boolean) {
        hideProgressBar()
        when (commentChunk.order) {
            OrderType.AFTER -> {
                commentAdapter.addItemsNext(commentChunk)
            }
            OrderType.BEFORE -> {
                commentAdapter.addItemsPrevious(commentChunk)
            }
            OrderType.INITIALIZE -> {
                commentAdapter.refresh(commentChunk) { scroll ->
                    scrollToPositionAfterNewComments(scroll)
                }
                if (commentChunk.items.isEmpty() && isCommentable) {
                    showEmptyPlaceCommentsHolder()
                } else {
                    hideEmptyPlaceCommentsHolder()
                }
            }
        }
    }

    fun handleLoadingBeforeProgress(hasStarted: Boolean) {
        if (hasStarted) {
            commentAdapter.addLoadingProgressBefore()
        } else {
            commentAdapter.removeLoadingProgressBefore()
        }
    }

    fun handleLoadingAfterProgress(hasStarted: Boolean) {
        if (hasStarted) {
            commentAdapter.addLoadingProgressAfter()
        } else {
            commentAdapter.removeLoadingProgressAfter()
        }
    }

    fun handleScrollToLastPosition(needSmoothScroll: Boolean) {
        binding.rvMomentComments.scrollToLastPosition(needSmoothScroll)
    }

    fun handleCommentRestricted() {
        hideProgressBar()
    }

    fun handleError() {
        hideProgressBar()
    }

    fun onDispose() {
        commentAdapter.release()
        commentAdapter.collectionUpdateListener = {}
    }

    private fun showEmptyPlaceCommentsHolder() {
        binding.vgPostCommentFirstHolder.clearAnimation()
        binding.vgPostCommentFirstHolder.alpha = 0F
        binding.vgPostCommentFirstHolder.visible()
        binding.vgPostCommentFirstHolder
            .animate()
            .alpha(1F)
            .setStartDelay(EMPTY_COMMENTS_SHOW_DELAY_MS)
            .setDuration(EMPTY_COMMENTS_SHOW_DURATION_MS)
            .start()
    }

    private fun hideEmptyPlaceCommentsHolder() {
        binding.vgPostCommentFirstHolder.clearAnimation()
        binding.vgPostCommentFirstHolder
            .animate()
            .alpha(0F)
            .setDuration(EMPTY_COMMENTS_HIDE_DURATION_MS)
            .setListener(
                onAnimationEnd = {
                    binding.vgPostCommentFirstHolder.gone()
                }
            )
            .start()
    }

    private fun hideProgressBar() {
        binding.pbMomentCommentsSheet.fadeOut(PROGRESS_HIDE_DURATION_MS)
    }

    private fun RecyclerView.scrollToLastPosition(needSmoothScroll: Boolean) {
        val adapter = adapter ?: return
        val lastPosition = max(adapter.itemCount - 1, 0)

        if (needSmoothScroll) {
            smoothScrollToPosition(lastPosition)
        } else {
            scrollToPosition(lastPosition)
        }
    }

    private fun showFullLastComment(comments: List<CommentUIType>) {
        if (comments.isNotEmpty()) {
            val lastComment = comments.last()
            if (lastComment is CommentEntity) {
                lastComment.isShowFull = true
            }
        }
    }

    private fun handleScrollDownBtnVisibility() {
        val layoutManager = binding.rvMomentComments.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        val pos = lastVisiblePosition + 1
        val maxPos = commentAdapter.itemCount - SCROLL_DOWN_BUTTON_HIDE_BEFORE_ITEM_COUNT

        if (pos <= maxPos) {
            callback.onShowScrollDownButton()
        } else {
            callback.onHideScrollDownButton()
        }
    }

    private fun initRefreshListener() {
        binding.vgRefreshComments.setOnRefreshListener {
            if (it == SwipyRefreshLayoutDirection.BOTTOM) {
                callback.addCommentsAfter()
                binding.rvMomentComments.smoothScrollToPosition(commentAdapter.itemCount)
            }

            binding.vgRefreshComments.isRefreshing = false
        }
    }

    private fun createCommentAdapter(): MeeraCommentAdapter {
        return MeeraCommentAdapter(
            commentListCallback = commentsAdapterCallback,
        ) {
            callback.addInnerComment(it)
        }
    }

    private fun initCommentAdapter() {
        commentAdapter.collectionUpdateListener = commentObserver
        commentAdapter.innerSeparatorItemClickListener = {
            fragment.doDelayed(SCROLL_DOWN_BUTTON_VISIBILITY_DELAY_MS) {
                handleScrollDownBtnVisibility()
            }
        }

        commentAdapter.registerAdapterDataObserver(adapterDataObserver)
    }

    private fun initSwipeController() {
        val messageSwipeController = ReplySwipeController(
            fragment.requireContext(),
            SwipingItemType.POST_COMMENT,
            object : SwipeControllerActions {
                override fun onReply(absoluteAdapterPosition: Int) {
                    commentAdapter.getItem(absoluteAdapterPosition)?.comment?.let { comment ->
                        callback.onCommentReplySwipe(comment)
                    }
                }
            })

        itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper?.attachToRecyclerView(binding.rvMomentComments)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initRecycler() {
        val commentRecyclerViewLayoutManager = SpeedyLinearLayoutManager(fragment.context)

        binding.rvMomentComments.apply {
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
            layoutManager = commentRecyclerViewLayoutManager
            adapter = commentAdapter
        }

        binding.rvMomentComments.addOnScrollListener(
            object : RecyclerPaginationUtil(commentRecyclerViewLayoutManager) {
                override fun loadBefore() {
                    callback.addCommentsBefore()
                }

                override fun loadAfter() {
                    callback.addCommentsAfter()
                }

                override fun isTopPage(): Boolean =
                    paginationHelper.isTopPage

                override fun isBottomPage(): Boolean =
                    paginationHelper.isLastPage

                override fun isLoadingAfter(): Boolean =
                    paginationHelper.isLoadingAfter

                override fun isLoadingBefore(): Boolean =
                    paginationHelper.isLoadingBefore
            }
        )

        binding.rvMomentComments.setOnTouchListener { v, event ->
            if (!binding.rvMomentComments.canScrollVertically(-1)
                && !binding.rvMomentComments.canScrollVertically(1)
            ) {
                v.parent.requestDisallowInterceptTouchEvent(true)
                v.onTouchEvent(event)
                true
            } else false
        }

        binding.rvMomentComments.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                rv.parent.requestDisallowInterceptTouchEvent(true);
                return false;
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
        })
    }

    private fun initOnScrollListener() {
        binding.rvMomentComments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                handleScrollDownBtnVisibility()
            }
        })
    }

    private fun scrollToPositionAfterNewComments(scrollPosition: Int) {
        fragment.viewLifecycleOwner.doDelayed(DELAY_SCROLLING) {
            val isLifecycleValid =
                fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED
                )

            if (isLifecycleValid) {
                binding.rvMomentComments.scrollToPosition(scrollPosition + 1)
                paginationHelper.isTopPage = false
            }
        }
    }

    interface Callback {
        fun addCommentsBefore()
        fun addCommentsAfter()
        fun addInnerComment(commentSeparatorEntity: CommentSeparatorEntity)
        fun onCommentReplySwipe(comment: CommentEntityResponse)
        fun onShowScrollDownButton()
        fun onHideScrollDownButton()
    }
}
