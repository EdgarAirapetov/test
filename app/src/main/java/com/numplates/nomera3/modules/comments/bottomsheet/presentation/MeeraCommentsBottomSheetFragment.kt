package com.numplates.nomera3.modules.comments.bottomsheet.presentation

import android.content.DialogInterface
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.layouts.intercept.InterceptTouchLayout
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.CommentsBottomSheetBinding
import com.numplates.nomera3.databinding.CommentsBottomSheetCreateCommentBlockBinding
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.menu.CommentMenuItem
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.util.BottomPanelContainerCallback
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.util.CommentBottomSheetKeyboardHeightWatcher
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.viewcontroller.CommentCreateViewController
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.viewcontroller.CommentTreeViewController
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.ICommentsActionsCallback
import com.numplates.nomera3.modules.comments.ui.entity.CommentSeparatorEntity
import com.numplates.nomera3.modules.comments.ui.viewholder.CommentViewHolderPlayAnimation
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import kotlinx.coroutines.launch


private const val COLLAPSED_HEIGHT_RATIO = 1.6
private const val DELAY_DELETE_COMMENT_SECONDS = 5L
private const val BEHAVIOR_HIDE_FRICTION = 0.01F
private val REACTION_BUBBLE_VERTICAL_OFFSET = 26.dp
private val DEFAULT_TOAST_BOTTOM_PADDING = 16.dp

private const val ELEVATION_SNACKBAR = 50F

class MeeraCommentsBottomSheetFragment : UiKitBottomSheetDialog<CommentsBottomSheetBinding>(),
    MeeraMenuBottomSheet.Listener {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> CommentsBottomSheetBinding
        get() = CommentsBottomSheetBinding::inflate

    private val viewModel by viewModels<CommentsBottomSheetViewModel> { App.component.getViewModelFactory() }

    private val expandedStateHeight = getScreenHeight()
    private val collapsedStateHeight = (expandedStateHeight / COLLAPSED_HEIGHT_RATIO).toInt()

    private var commentCreateController: CommentCreateViewController? = null
    private var commentTreeController: CommentTreeViewController? = null
    private var keyboardHeightWatcher: CommentBottomSheetKeyboardHeightWatcher? = null
    private var bottomSheetCallback: BottomPanelContainerCallback? = null
    private var viewContainer: BottomSheetDialogViewContainer? = null

    private var undoSnackbar: UiKitSnackBar? = null
    private var createCommentBinding : CommentsBottomSheetCreateCommentBlockBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewContainer = initViewContainer() ?: return
        val commentsInfo = arguments?.getParcelable<BottomSheetCommentsInfoUiModel>(COMMENTS_INFO_ARG) ?: return
        val commentToOpenId =
            arguments?.getLong(COMMENT_TO_OPEN_ARG, COMMENTS_BOTTOM_SHEET_INVALID_COMMENT_ID) ?: return
        createCommentBinding = CommentsBottomSheetCreateCommentBlockBinding.inflate(LayoutInflater.from(context))
        setGeneralBottomSheetBehavior(viewContainer.bottomSheetBehavior)
        rootBinding?.tvBottomSheetDialogLabel?.text = getString(R.string.meera_commentaries)
        disableWindowAdjust()
        initObservers()
        initViewModel(commentsInfo, commentToOpenId)
        view.post {
            createCommentBinding?.apply {
                setupAllDialogBehavior(viewContainer, this)
                setupCommentCreateBlock(commentsInfo, viewContainer, this)
                setupCommentTreeBlock()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super<UiKitBottomSheetDialog>.onDismiss(dialog)
        undoSnackbar?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        enableWindowAdjust()
        commentTreeController?.onDispose()
        commentTreeController = null
        commentCreateController = null
        keyboardHeightWatcher?.onDispose()
        keyboardHeightWatcher = null
        bottomSheetCallback?.let {
            viewContainer?.bottomSheetBehavior?.removeBottomSheetCallback(it)
        }
        bottomSheetCallback = null
        viewContainer = null
    }

    override fun onCancelByUser(menuTag: String?) {
        if (menuTag == COMMENTS_BOTTOM_SHEET_MENU_TAG) {
            viewModel.onCommentAction(CommentViewAction.CommentMenuCancelAction)
        }
    }

    private fun disableWindowAdjust() {
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    private fun enableWindowAdjust() {
        requireActivity().window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.commentEffect.collect(::handleCommentEffect)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.liveComments.collect { commentChunk ->
                    val chunk = commentChunk ?: return@collect
                    val isCommentable = viewModel.contentItem?.isCommentsEnabled ?: return@collect
                    commentTreeController?.handleLiveComments(chunk, isCommentable)
                }
            }
        }
    }

    private fun initViewModel(
        commentInfo: BottomSheetCommentsInfoUiModel,
        commentToOpenId: Long
    ) {
        viewModel.init(commentInfo, commentToOpenId)
    }

    private fun setupAllDialogBehavior(
        viewContainer: BottomSheetDialogViewContainer,
        createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding
    ) {
        initKeyboardWatcher(
            viewContainer = viewContainer,
            createCommentBinding = createCommentBinding
        )

        initBottomPanelContainerCallback(
            bottomContainer = viewContainer.bottomContainer,
            bottomSheetBehavior = viewContainer.bottomSheetBehavior
        )

        extendInputTouchFocus(createCommentBinding)
        setGeneralBottomSheetBehavior(viewContainer.bottomSheetBehavior)
        setBottomSheetHeight(viewContainer.bottomSheetView)
    }

    private fun setupCommentCreateBlock(
        commentsInfo: BottomSheetCommentsInfoUiModel,
        viewContainer: BottomSheetDialogViewContainer,
        createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding
    ) {
        commentCreateController = CommentCreateViewController().also { controller ->
            controller.init(
                commentsInfo = commentsInfo,
                fragment = this,
                binding = createCommentBinding,
                viewContainer = viewContainer,
                callback = object : CommentCreateViewController.Callback {
                    override fun onMessageCreate(message: String, parentCommentId: Long) {
                        viewModel.onCommentAction(CommentViewAction.SendCommentToServer(message, parentCommentId))
                        expandBottomSheet()
                    }

                    override fun onInputTextSelected() {
                        expandBottomSheet()
                    }

                    override fun onScrollDownButtonPress() {
                        expandBottomSheet()
                        commentTreeController?.handleScrollToLastPosition(false)
                    }
                }
            )
        }
    }

    private fun setupCommentTreeBlock() {
        val commentsAdapterCallback = CommentBottomSheetListener()
        contentBinding?.let { binding ->
            commentTreeController = CommentTreeViewController(
                commentObserver = viewModel.commentObserver,
                paginationHelper = viewModel.paginationHelper,
                commentsAdapterCallback = commentsAdapterCallback,
                binding = binding,
                fragment = this,
                callback = object : CommentTreeViewController.Callback {
                    override fun addCommentsBefore() {
                        viewModel.onCommentAction(CommentViewAction.AddCommentsBefore)
                    }

                    override fun addCommentsAfter() {
                        viewModel.onCommentAction(CommentViewAction.AddCommentsAfter)
                    }

                    override fun addInnerComment(commentSeparatorEntity: CommentSeparatorEntity) {
                        viewModel.onCommentAction(CommentViewAction.AddInnerComment(commentSeparatorEntity))
                    }

                    override fun onCommentReplySwipe(comment: CommentEntityResponse) = needAuthToNavigate {
                        viewModel.onCommentAction(CommentViewAction.ReplyToComment(comment))
                    }

                    override fun onShowScrollDownButton() {
//                        commentCreateController?.handleShowScrollButtonDown()
                    }

                    override fun onHideScrollDownButton() {
//                        commentCreateController?.handleHideScrollButtonDown()
                    }
                }
            ).also { controller ->
                viewModel.paginationHelper.isLoadingAfterCallback = {
                    controller.handleLoadingAfterProgress(it)
                }
                viewModel.paginationHelper.isLoadingBeforeCallback = {
                    controller.handleLoadingBeforeProgress(it)
                }
            }
        }
    }

    private fun handleCommentEffect(effect: CommentViewEffect) {
        when (effect) {
            CommentViewEffect.EnableComments -> onCommentsEnabled()
            CommentViewEffect.CommentRestricted -> onCommentsRestricted()

            is CommentViewEffect.MarkCommentForDeletion -> onMarkCommentForDeletion(effect)
            is CommentViewEffect.CancelDeleteComment -> onCancelDeleteComment(effect)
            is CommentViewEffect.DeleteComment -> onDeleteComment(effect)
            is CommentViewEffect.ErrorDeleteComment -> onErrorDeleteComment(effect)

            is CommentViewEffect.NewCommentSuccess -> onNewCommentSuccess(effect)
            CommentViewEffect.ErrorPublishComment -> onErrorPublishComment()
            is CommentViewEffect.NewInnerCommentSuccess -> onNewInnerCommentSuccess(effect)
            is CommentViewEffect.ErrorInnerPagination -> onErrorInnerPagination(effect)
            CommentViewEffect.OnScrollToBottom -> onScrollToBottom()

            is CommentViewEffect.OnReplyToComment -> onReplyToComment(effect)
            is CommentViewEffect.OpenCommentMenu -> onOpenCommentMenu(effect)
            is CommentViewEffect.UpdateCommentReaction -> onUpdateCommentReaction(effect)

            is CommentViewEffect.ShowTextError -> onShowTextErrorEffect(effect)
            CommentViewEffect.OnAddUserToBlocked -> showToastMessage(getString(R.string.meera_you_blocked_user))
            CommentViewEffect.ComplainSuccess -> showToastMessage(getString(R.string.road_complaint_send_success))
            CommentViewEffect.OnCopyCommentText -> showToastMessage(getString(R.string.comment_text_copied),
                iconState = AvatarUiState.WarningIconState)
            CommentViewEffect.NoInternet -> showToastMessage(getString(R.string.no_internet),
                iconState = AvatarUiState.ErrorIconState)
            CommentViewEffect.NoInternetAction -> showToastMessage(
                getString(R.string.internet_connection_problem_action),
                iconState = AvatarUiState.ErrorIconState
            )

            else -> Unit
        }
    }

    private fun onCommentsRestricted() {
        commentTreeController?.handleCommentRestricted()
        commentCreateController?.handleCommentNotAvailable()
    }

    private fun onCommentsEnabled() {
        commentCreateController?.handleEnableWriteComment()
    }

    private fun onMarkCommentForDeletion(effect: CommentViewEffect.MarkCommentForDeletion) {
        val originalComment =
            commentTreeController?.handleMarkCommentAsDeleted(effect.commentID, effect.whoDeleteComment) ?: return
        showDeleteCommentCountdownToastNew { deleteCancelled ->
            if (deleteCancelled) {
                viewModel.onCommentAction(CommentViewAction.CancelDeleteComment(originalComment))
            } else {
                viewModel.onCommentAction(
                    CommentViewAction.DeleteComment(
                        commentId = effect.commentID,
                        originalComment = originalComment,
                        whoDeleteComment = effect.whoDeleteComment
                    )
                )
            }
        }
    }

    private fun onCancelDeleteComment(effect: CommentViewEffect.CancelDeleteComment) {
        commentTreeController?.handleCancelCommentDeletion(effect.originalComment)
    }

    private fun onDeleteComment(effect: CommentViewEffect.DeleteComment) {
        commentCreateController?.handleDeleteComment(effect.commentID)
    }

    private fun onErrorDeleteComment(effect: CommentViewEffect.ErrorDeleteComment) {
        showToastMessage(getString(R.string.no_internet), iconState = AvatarUiState.ErrorIconState)
        commentTreeController?.handleErrorDeleteComment(effect.comment)
    }

    private fun onNewCommentSuccess(effect: CommentViewEffect.NewCommentSuccess) {
        commentTreeController?.handleNewComment(
            beforeMyComment = effect.beforeMyComment,
            hasIntersection = effect.hasIntersection,
            needSmoothScroll = effect.needSmoothScroll,
            needToShowLastFullComment = effect.needToShowLastFullComment
        )
    }

    private fun onErrorPublishComment() {
        commentCreateController?.handleEnableWriteComment()
        showToastMessage(getString(R.string.comment_send_error), iconState = AvatarUiState.ErrorIconState)
    }

    private fun onNewInnerCommentSuccess(effect: CommentViewEffect.NewInnerCommentSuccess) {
        commentTreeController?.handleNewInnerComment(
            parentId = effect.parentId,
            chunk = effect.chunk
        )
    }

    private fun onErrorInnerPagination(effect: CommentViewEffect.ErrorInnerPagination) {
        commentTreeController?.handleInnerCommentError(effect.data)
        showToastMessage(getString(R.string.no_internet), iconState = AvatarUiState.ErrorIconState)
    }

    private fun onScrollToBottom() {
        commentTreeController?.handleScrollToLastPosition(false)
    }

    private fun onReplyToComment(effect: CommentViewEffect.OnReplyToComment) {
        commentCreateController?.handleCommentReply(effect.comment)
    }

    private fun onOpenCommentMenu(effect: CommentViewEffect.OpenCommentMenu) {
        val menu = MeeraMenuBottomSheet(context)

        effect.menuItems.forEach {
            menu.addItem(it.titleResId, it.iconResId, iconAndTitleColor = it.iconAndTitleColor) {
                when (it) {
                    is CommentMenuItem.AddComplaintForComment ->
                        viewModel.onCommentAction(CommentViewAction.AddComplaintForCommentMenuAction(it.commentId))

                    is CommentMenuItem.BlockUser -> viewModel.onCommentAction(CommentViewAction.BlockUserMenuAction(it.commentAuthorId))
                    is CommentMenuItem.CopyMessage -> viewModel.onCommentAction(
                        CommentViewAction.CopyMessageMenuAction(
                            it.comment
                        )
                    )

                    is CommentMenuItem.ReplyToComment -> viewModel.onCommentAction(
                        CommentViewAction.ReplyToCommentMenuAction(
                            it.comment
                        )
                    )

                    is CommentMenuItem.DeleteComment -> {
                        if (it.whoDeleteComment == null) return@addItem
                        val commentUiEntity = commentTreeController?.findCommentById(it.commentId) ?: return@addItem
                        viewModel.onCommentAction(
                            CommentViewAction.MarkAsDeletedCommentMenuAction(
                                commentUiEntity,
                                it.whoDeleteComment
                            )
                        )
                    }

                    else -> Unit
                }
            }
        }

        menu.showWithTag(childFragmentManager, COMMENTS_BOTTOM_SHEET_MENU_TAG)
    }

    private fun onUpdateCommentReaction(effect: CommentViewEffect.UpdateCommentReaction) {
        commentTreeController?.handleUpdateReaction(
            effect.position,
            effect.reactionUpdate
        )
    }

    private fun onShowTextErrorEffect(effect: CommentViewEffect.ShowTextError) {
        commentTreeController?.handleError()
        showToastMessage(effect.message, iconState = AvatarUiState.ErrorIconState)
    }

    private fun initViewContainer(): BottomSheetDialogViewContainer? {
        val bottomSheetDialog = dialog as BottomSheetDialog? ?: return null
        val bottomSheetView = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) ?: return null
        val bottomContainer = bottomSheetDialog.findViewById<ViewGroup>(R.id.fl_bottom_container) ?: return null
        val rootView = bottomSheetDialog.findViewById<ViewGroup>(R.id.container) ?: return null
        viewContainer = BottomSheetDialogViewContainer(
            bottomSheetDialog = bottomSheetDialog,
            bottomSheetView = bottomSheetView,
            bottomSheetBehavior = bottomSheetDialog.behavior,
            bottomContainer = bottomContainer,
            rootContainer = rootView
        )
        return viewContainer
    }

    private fun initKeyboardWatcher(
        viewContainer: BottomSheetDialogViewContainer,
        createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding
    ) {
        contentBinding?.let { binding ->
            keyboardHeightWatcher = CommentBottomSheetKeyboardHeightWatcher { peekHeight: Int ->
                commentCreateController?.setSuggestionMenuPeekHeight(peekHeight)
            }.also {
                it.observeKeyboardHeight(
                    bottomContainer = viewContainer.bottomContainer,
                    rootContainer = viewContainer.rootContainer,
                    bottomSheetDialog = viewContainer.bottomSheetDialog,
                    createCommentBinding = createCommentBinding,
                    mainBinding = binding,
                )
            }
        }
    }

    private fun initBottomPanelContainerCallback(
        bottomContainer: ViewGroup,
        bottomSheetBehavior: BottomSheetBehavior<out View>
    ) {
        bottomSheetCallback = BottomPanelContainerCallback(bottomContainer, bottomSheetBehavior) { newState: Int ->
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> undoSnackbar?.dismiss()
                BottomSheetBehavior.STATE_COLLAPSED -> requireContext().hideKeyboard(requireView())
            }
        }.also {
            bottomSheetBehavior.addBottomSheetCallback(it)
        }
    }

    private fun extendInputTouchFocus(createCommentBinding: CommentsBottomSheetCreateCommentBlockBinding) {
        createCommentBinding.vgInputLayoutContainer.setThrottledClickListener {
            createCommentBinding.etWriteComment.requestFocus()
        }
    }

    private fun setGeneralBottomSheetBehavior(behavior: BottomSheetBehavior<out View>) {
        behavior.isHideable = true
        behavior.skipCollapsed = false
        behavior.peekHeight = collapsedStateHeight
        behavior.hideFriction = BEHAVIOR_HIDE_FRICTION
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setBottomSheetHeight(bottomSheetView: View) {
        bottomSheetView.updateLayoutParams {
            height = expandedStateHeight
        }
    }

    private fun expandBottomSheet() {
        viewContainer?.bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun showDeleteCommentCountdownToastNew(onClosedManually: (Boolean) -> Unit) {
        undoSnackbar?.dismiss()
        undoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.comment_deleted),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_COMMENT_SECONDS,
                        onTimerFinished = { onClosedManually.invoke(false) }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        onClosedManually.invoke(true)
                        undoSnackbar?.dismiss()
                    }
                ),
                dismissOnClick = false,
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                paddingState = PaddingState(
                    bottom = countMessageInputBottomPadding()
                )
            )
        ).apply {
            ViewCompat.setElevation(this.view, ELEVATION_SNACKBAR)
        }
        undoSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        undoSnackbar?.show()
    }

    private fun showToastMessage(messageString: String?, iconState: AvatarUiState = AvatarUiState.SuccessIconState) =
        doOnUIThread {
            val resultMessage = messageString ?: getString(R.string.reaction_unknown_error)
            undoSnackbar = UiKitSnackBar.make(
                view = requireView(),
                params = SnackBarParams(
                    snackBarViewState = SnackBarContainerUiState(
                        messageText = resultMessage,
                        avatarUiState = iconState,
                    ),
                    duration = BaseTransientBottomBar.LENGTH_SHORT,
                    dismissOnClick = true,
                    paddingState = PaddingState(
                        bottom = countMessageInputBottomPadding()
                    )
                )
            ).apply {
                ViewCompat.setElevation(this.view, ELEVATION_SNACKBAR)
            }
            undoSnackbar?.show()
        }

    private fun countMessageInputBottomPadding(): Int {
        val createMessageBlockHeight = createCommentBinding?.vgCreateBlockMainContainer?.height ?: 0
        return createMessageBlockHeight + DEFAULT_TOAST_BOTTOM_PADDING
    }

    class BottomSheetDialogViewContainer(
        val bottomSheetDialog: BottomSheetDialog,
        val bottomSheetView: View,
        val bottomSheetBehavior: BottomSheetBehavior<out View>,
        val bottomContainer: ViewGroup,
        val rootContainer: ViewGroup,
    )

    inner class CommentBottomSheetListener : ICommentsActionsCallback {
        override fun onReactionBadgeClick(comment: CommentEntityResponse) {
            needAuthToNavigate {
                MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
                    entityId = comment.id,
                    entityType = ReactionsEntityType.COMMENT
                ) { destination ->
                    when (destination) {
                        is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                            openUserFragment(destination.userEntity.userId)
                        }
                    }
                }.show(childFragmentManager)
            }
        }

        override fun onCommentDoubleClick(comment: CommentEntityResponse) {
            val item = viewModel.contentItem ?: return
            val toolsProvider = activity as? ActivityToolsProvider ?: return

            commentTreeController?.commentAdapter?.playCommentAnimation(
                commentId = comment.id,
                animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
            )

            val reactionSource = MeeraReactionSource.CommentBottomSheet(
                postId = item.contentId,
                commentId = comment.id,
                originEnum = item.baseScreenOrigin
            )
            toolsProvider
                .getMeeraReactionBubbleViewController()
                .onSelectDefaultReaction(
                    reactionSource = reactionSource,
                    currentReactionsList = comment.reactions,
                    forceDefault = true
                )
        }

        override fun onCommentPlayClickAnimation(commentId: Long) {
            commentTreeController?.commentAdapter?.playCommentAnimation(
                commentId = commentId,
                animation = CommentViewHolderPlayAnimation.PlayLikeOnDoubleClickAnimation
            )
        }

        override fun onCommentLongClick(comment: CommentEntityResponse, position: Int) {
            viewModel.onCommentAction(CommentViewAction.OpenMenuForComment(comment))
        }

        override fun onCommentLikeClick(comment: CommentEntityResponse) {
            val item = viewModel.contentItem ?: return
            val toolsProvider = activity as? ActivityToolsProvider ?: return
            val reactionSource = MeeraReactionSource.CommentBottomSheet(
                postId = item.contentId,
                commentId = comment.id,
                originEnum = item.baseScreenOrigin
            )
            toolsProvider
                .getMeeraReactionBubbleViewController()
                .onSelectDefaultReaction(
                    reactionSource = reactionSource,
                    currentReactionsList = comment.reactions,
                    forceDefault = false
                )
        }

        override fun onCommentLinkClick(url: String?) {
            val activity = activity as? MeeraAct ?: return
            activity.emitDeeplinkCall(url)
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
            val item = viewModel.contentItem ?: return
            val toolsProvider = activity as? ActivityToolsProvider ?: return
            val bottomSheetRootContainer = viewContainer?.rootContainer ?: return
            val interceptTouchLayout = bottomSheetRootContainer as? InterceptTouchLayout?
            val containerInfo = MeeraReactionBubbleViewController.ContainerInfo(
                container = bottomSheetRootContainer,
                bypassLayouts = if (interceptTouchLayout != null) listOf(interceptTouchLayout) else listOf()
            )
            val reactionSource = MeeraReactionSource.CommentBottomSheet(
                postId = item.contentId,
                commentId = commentId,
                originEnum = item.baseScreenOrigin
            )
            val reactionController = toolsProvider.getMeeraReactionBubbleViewController()

            showPoint.y = showPoint.y - REACTION_BUBBLE_VERTICAL_OFFSET

            reactionController.showReactionBubble(
                reactionSource = reactionSource,
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = currentReactionsList,
                contentActionBarType = MeeraContentActionBar.ContentActionBarType.DEFAULT,
                containerInfo = containerInfo,
                isForceAdd = true,
                showMorningEvening = false
            )
        }

        override fun onCommentProfileClick(comment: CommentEntityResponse) {
            openUserFragment(comment.uid)
        }

        override fun onCommentReplyClick(comment: CommentEntityResponse) {
            viewModel.onCommentAction(CommentViewAction.ReplyToComment(comment))
        }

        override fun onCommentMention(userId: Long) {
            needAuthToNavigate {
                openUserFragment(userId)
            }
        }

        override fun onHashtagClicked(hashtag: String?) {
            openHashtagFragment(hashtag)
        }

        override fun onBirthdayTextClicked() {
            val activity = activity as? Act ?: return
            activity.showFireworkAnimation()
        }

        private fun openUserFragment(userId: Long) {
            findNavController().safeNavigate(R.id.action_global_userInfoFragment, Bundle().apply {
                putSerializable(IArgContainer.ARG_USER_ID, userId)
            })
        }

        private fun openHashtagFragment(hashtag: String?) {
            val activity = activity as? Act ?: return

            activity.addFragment(
                HashtagFragment(), Act.LIGHT_STATUSBAR,
                Arg(IArgContainer.ARG_HASHTAG, hashtag)
            )
        }

    }

    companion object {
        const val COMMENTS_INFO_ARG = "COMMENTS_INFO"
        const val COMMENT_TO_OPEN_ARG = "COMMENT_TO_OPEN_ARG"
        private const val COMMENTS_BOTTOM_SHEET_TAG = "COMMENTS_BOTTOM_SHEET_TAG"
        private const val COMMENTS_BOTTOM_SHEET_MENU_TAG = "COMMENTS_BOTTOM_SHEET_MENU_TAG"

        fun showForPost(
            post: PostUIEntity,
            fragmentManager: FragmentManager,
            postOriginEnum: DestinationOriginEnum? = null,
            commentToOpenId: Long? = null
        ) {
            val fragment = MeeraCommentsBottomSheetFragment()
            val commentsInfo = BottomSheetCommentsInfoUiModel(
                contentId = post.postId,
                contentUserId = post.user?.userId!!,
                isUserBlackListMe = post.user.blackListedMe,
                isCommentsEnabled = post.isAllowedToComment,
                commentsOrigin = BottomSheetCommentsOrigin.VIDEO_POST,
                baseScreenOrigin = postOriginEnum
            )
            val bundle = Bundle().apply {
                putParcelable(COMMENTS_INFO_ARG, commentsInfo)
                if (commentToOpenId != null) putLong(COMMENT_TO_OPEN_ARG, commentToOpenId)
            }
            fragment.arguments = bundle
            fragment.show(fragmentManager, COMMENTS_BOTTOM_SHEET_TAG)
        }
    }

}
