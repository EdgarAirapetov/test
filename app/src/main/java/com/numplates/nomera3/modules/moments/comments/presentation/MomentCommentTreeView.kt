package com.numplates.nomera3.modules.moments.comments.presentation

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.fadeOut
import com.meera.core.extensions.gone
import com.meera.core.extensions.setListener
import com.meera.core.extensions.visible
import com.meera.core.utils.layouts.intercept.InterceptTouchFrameLayout
import com.meera.core.utils.pagination.RecyclerPaginationUtil
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetBinding
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
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
import com.numplates.nomera3.modules.comments.ui.util.SpeedyLinearLayoutManager
import com.numplates.nomera3.modules.comments.ui.viewholder.CommentViewHolderPlayAnimation
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.util.getMyReaction
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import kotlin.math.max


private const val DELAY_SCROLLING = 200L
private const val PROGRESS_HIDE_DURATION_MS = 150L
private const val EMPTY_COMMENTS_SHOW_DURATION_MS = 450L
private const val EMPTY_COMMENTS_SHOW_DELAY_MS = 200L
private const val SCROLL_DOWN_BUTTON_VISIBILITY_DELAY_MS = 100L
private const val EMPTY_COMMENTS_HIDE_DURATION_MS = 100L
private const val SCROLL_DOWN_BUTTON_HIDE_BEFORE_ITEM_COUNT = 3
private val REACTION_BUBBLE_VERTICAL_OFFSET_PX = (-26).dp

/**
 * View-контроллер отвечающий за отображение комментариев
 */
class MomentCommentTreeView(
    private val viewModelController: MomentCommentTreeModel,
    private val binding: MomentCommentsBottomSheetBinding,
    private val bottomSheetDialog: BottomSheetDialog,
    private val fragment: Fragment,
    private val featureToggleContainer: FeatureTogglesContainer,
    private val callback: Callback,
) {
    private val commentsAdapterCallback = object : ICommentsActionsCallback {
        override fun onReactionBadgeClick(comment: CommentEntityResponse) {
            if (featureToggleContainer.detailedReactionsForCommentsFeatureToggle.isEnabled) {
                MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                    entityId = comment.id,
                    entityType = ReactionsEntityType.MOMENT_COMMENT
                ).show(fragment.childFragmentManager)
            } else {
                val sortedReactions = comment.reactions.sortedByDescending { reaction -> reaction.count }
                val menu = ReactionsStatisticBottomMenu(fragment.context)
                menu.addTitle(R.string.post_comment_reactions, sortedReactions.reactionCount())
                sortedReactions.forEachIndexed { index, value ->
                    menu.addReaction(value, index != sortedReactions.size - 1)
                }
                menu.show(fragment.childFragmentManager)
            }
        }

        override fun onCommentLikeClick(comment: CommentEntityResponse) {
            val moment = viewModelController.getMomentItem() ?: return
            val activity = fragment.activity as? MeeraAct ?: return
            val toolsProvider = activity as? ActivityToolsProvider ?: return
            val reactionSource = MeeraReactionSource.MomentComment(
                momentId = moment.id,
                commentUserId = comment.user.userId,
                momentUserId = moment.userId,
                commentId = comment.id
            )

            toolsProvider
                .getMeeraReactionBubbleViewController()
                .onSelectDefaultReaction(
                    reactionSource = reactionSource,
                    currentReactionsList = comment.reactions,
                    forceDefault = false,
                )
        }

        override fun onCommentLinkClick(url: String?) {
            val act = fragment.activity as? Act ?: return
            act.openLink(url)
        }

        override fun onCommentLongClick(comment: CommentEntityResponse, position: Int) =
            callback.onCommentShowMenu(comment, position)

        override fun onCommentProfileClick(comment: CommentEntityResponse) {
            val activity = fragment.activity as? Act ?: return
//            activity.addFragmentIgnoringAuthCheck(
//                UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//                Arg(IArgContainer.ARG_USER_ID, comment.uid),
//                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.MOMENT.property)
//            )
            //TODO ROAD FIX navigate userInfoFragment
        }

        override fun onCommentReplyClick(comment: CommentEntityResponse) {
            handleCommentReply(comment)
        }

        override fun onCommentMention(userId: Long) {
            val activity = fragment.activity as? Act ?: return
            activity.addFragment(
                UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.MOMENT.property)
            )
        }

        override fun onHashtagClicked(hashtag: String?) {
            val activity = fragment.activity as? Act ?: return

            activity.addFragment(
                HashtagFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_HASHTAG, hashtag)
            )
        }

        override fun onCommentShowReactionBubble(
            commentId: Long,
            commentUserId: Long,
            showPoint: Point,
            viewsToHide: List<View>,
            reactionTip: TextView,
            currentReactionsList: List<ReactionEntity>,
            isMoveUpAnimationEnabled: Boolean
        ) {
            val moment = viewModelController.getMomentItem() ?: return
            val activity = fragment.activity as? Act ?: return
            val dialogRootContainer =
                bottomSheetDialog.findViewById<InterceptTouchFrameLayout>(
                    R.id.container
                ) ?: return
            val reactionSource = MeeraReactionSource.MomentComment(
                momentId = moment.id,
                commentUserId = commentUserId,
                momentUserId = moment.userId,
                commentId = commentId
            )
            val correctedShowPoint =
                Point(showPoint.x, showPoint.y + REACTION_BUBBLE_VERTICAL_OFFSET_PX)

            activity.getMeeraReactionBubbleViewController().showReactionBubble(
                reactionSource = reactionSource,
                showPoint = correctedShowPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = currentReactionsList,
                contentActionBarType = MeeraContentActionBar.ContentActionBarType.DEFAULT,
                containerInfo = MeeraReactionBubbleViewController.ContainerInfo(
                    container = dialogRootContainer,
                    bypassLayouts = listOf(dialogRootContainer),
                ),
                showMorningEvening = false
            )
        }

        override fun onBirthdayTextClicked() {
            val activity = fragment.activity as? Act ?: return

            activity.showFireworkAnimation()
        }

        override fun onCommentDoubleClick(comment: CommentEntityResponse) {
            val moment = viewModelController.getMomentItem() ?: return
            val activity = fragment.activity as? MeeraAct ?: return
            val toolsProvider = activity as? ActivityToolsProvider ?: return
            val isCurrentUserAlreadySetLike = comment.reactions.getMyReaction() == ReactionType.GreenLight

            commentAdapter.playCommentAnimation(
                commentId = comment.id,
                animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
            )

            if (!isCurrentUserAlreadySetLike) {
                val reactionSource = MeeraReactionSource.MomentComment(
                    momentId = moment.id,
                    commentUserId = comment.user.userId,
                    momentUserId = moment.userId,
                    commentId = comment.id
                )

                toolsProvider
                    .getMeeraReactionBubbleViewController()
                    .onSelectDefaultReaction(
                        reactionSource = reactionSource,
                        currentReactionsList = comment.reactions,
                        forceDefault = true
                    )
            }
        }

        override fun onCommentPlayClickAnimation(commentId: Long) {
            commentAdapter.playCommentAnimation(
                commentId = commentId,
                animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
            )
        }
    }

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
        initOnScrollListener()
        listenLiveComments()
    }

    fun handleMarkCommentAsDeleted(commentID: Long, whoDeleteComment: WhoDeleteComment): CommentUIType? {
        return commentAdapter.replaceCommentByDeletion(commentID = commentID, whoDeleteComment = whoDeleteComment)
    }

    fun handleCancelCommentDeletion(originalComment: CommentUIType) {
        commentAdapter.restoreComment(originalComment)
    }

    fun handleErrorDeleteComment(comment: CommentUIType) {
        commentAdapter.restoreComment(comment)
    }

    fun handleUpdateReaction(position: Int, reactionUpdate: ReactionUpdate) {
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
        commentAdapter.removeObserver()
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

    private fun createCommentAdapter(): MeeraCommentAdapter {
        return MeeraCommentAdapter(
            commentListCallback = commentsAdapterCallback,
        ) {
            viewModelController.addInnerComment(it)
        }
    }

    private fun initCommentAdapter() {
        commentAdapter.collectionUpdateListener = viewModelController.commentObserver
        commentAdapter.innerSeparatorItemClickListener = {
            fragment.doDelayed(SCROLL_DOWN_BUTTON_VISIBILITY_DELAY_MS) {
                handleScrollDownBtnVisibility()
            }
        }

        commentAdapter.registerAdapterDataObserver(adapterDataObserver)

        viewModelController.paginationHelper.isLoadingBeforeCallback = {
            if (it) {
                commentAdapter.addLoadingProgressBefore()
            } else {
                commentAdapter.removeLoadingProgressBefore()
            }
        }

        viewModelController.paginationHelper.isLoadingAfterCallback = {
            if (it) {
                commentAdapter.addLoadingProgressAfter()
            } else {
                commentAdapter.removeLoadingProgressAfter()
            }
        }
    }

    private fun initSwipeController() {
        val messageSwipeController = ReplySwipeController(
            fragment.requireContext(),
            SwipingItemType.POST_COMMENT,
            object : SwipeControllerActions {
                override fun onReply(absoluteAdapterPosition: Int) {
                    commentAdapter.getItem(absoluteAdapterPosition)?.comment?.let { comment ->
                        handleCommentReply(comment)
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
                    viewModelController.addCommentsBefore()
                }

                override fun loadAfter() {
                    viewModelController.addCommentsAfter()
                }

                override fun isTopPage(): Boolean =
                    viewModelController.paginationHelper.isTopPage

                override fun isBottomPage(): Boolean =
                    viewModelController.paginationHelper.isLastPage

                override fun isLoadingAfter(): Boolean =
                    viewModelController.paginationHelper.isLoadingAfter

                override fun isLoadingBefore(): Boolean =
                    viewModelController.paginationHelper.isLoadingBefore
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

    private fun handleCommentReply(comment: CommentEntityResponse) = fragment.needAuth {
        val momentItem = viewModelController.getMomentItem()

        if (momentItem?.isUserBlackListMe == true) return@needAuth

        callback.onReplyComment(comment)
    }

    private fun listenLiveComments() {
        viewModelController.getLiveComments().observe(fragment.viewLifecycleOwner) { commentChunk ->
            when (commentChunk.order) {
                OrderType.AFTER -> {
                    commentAdapter.addItemsNext(commentChunk)
                }

                OrderType.BEFORE -> {
                    commentAdapter.addItemsPrevious(commentChunk)
                }

                OrderType.INITIALIZE -> {
                    hideProgressBar()
                    commentAdapter.refresh(commentChunk) { scroll ->
                        scrollToPositionAfterNewComments(scroll)
                    }
                    if (commentChunk.items.isEmpty() && viewModelController.getIsMomentCommentable()) {
                        showEmptyPlaceCommentsHolder()
                    } else {
                        hideEmptyPlaceCommentsHolder()
                    }
                }
            }
        }
    }

    private fun scrollToPositionAfterNewComments(scrollPosition: Int) {
        fragment.viewLifecycleOwner.doDelayed(DELAY_SCROLLING) {
            val isLifecycleValid =
                fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(
                    Lifecycle.State.STARTED
                )

            if (isLifecycleValid) {
                binding.rvMomentComments.scrollToPosition(scrollPosition + 1)
                viewModelController.paginationHelper.isTopPage = false
            }
        }
    }

    interface Callback {
        fun onReplyComment(comment: CommentEntityResponse)
        fun onShowScrollDownButton()
        fun onHideScrollDownButton()
        fun onCommentShowMenu(comment: CommentEntityResponse, position: Int)
        fun onRefresh()
    }
}
