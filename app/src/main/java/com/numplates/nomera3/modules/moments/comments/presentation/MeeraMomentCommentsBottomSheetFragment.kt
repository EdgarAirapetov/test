package com.numplates.nomera3.modules.moments.comments.presentation

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentCommentsBottomSheetBinding
import com.numplates.nomera3.databinding.MeeraMomentCommentsBottomSheetCreateCommentBlockBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsCloseType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener

private const val COLLAPSED_HEIGHT_RATIO = 1.6
private const val NO_COMMENT_ID = 0L

/**
 * @property momentCommentsBottomSheetSetupUtil – отвечает за поведение шторки (отступы, открытия, закрытие, анимации)
 * @property commentTreeController – отвечает за recycler-список комментариев
 * @property commentMenuController – отвечает за шторку-действий при лонгтапе на комментарий
 * @property commentCreateController – отвечает за создания комментария
 */
class MeeraMomentCommentsBottomSheetFragment :
    UiKitBottomSheetDialog<MeeraMomentCommentsBottomSheetBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraMomentCommentsBottomSheetBinding
        get() = MeeraMomentCommentsBottomSheetBinding:: inflate

    private val viewModel by viewModels<MomentCommentsBottomSheetViewModel> { App.component.getViewModelFactory() }

    private val momentCommentsBottomSheetSetupUtil = MeeraMomentCommentsBottomSheetSetupUtil()
    private var commentTreeController: MeeraMomentCommentTreeView? = null
    private var commentMenuController: MeeraMomentCommentBottomMenuView? = null
    private val commentCreateController = MeeraMomentCommentCreateView()

    private val expandedStateHeight = getScreenHeight()
    private val collapsedStateHeight = (expandedStateHeight / COLLAPSED_HEIGHT_RATIO).toInt()

    private var bottomSheetRootView: View? = null

    private var momentItem: MomentItemUiModel? = null
    private var momentCommentToOpenId: Long? = null
    private var commentsListener: CommentsListener? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var isKeyboardOpen: Boolean = false
    private var dialogListener: BottomSheetDialogEventsListener? = null

    fun setCommentsListener(listener: CommentsListener) {
        this.commentsListener = listener
    }
    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.bottom_sheet_moment_comment_title))

    fun setListener(dialogListener: BottomSheetDialogEventsListener) {
        this.dialogListener = dialogListener
    }

    private val bottomSheetCloseUtil = BottomSheetCloseUtil(object : BottomSheetCloseUtil.Listener {
        override fun bottomSheetClosed(method: BottomSheetCloseUtil.BottomSheetCloseMethod) {
            when (method) {
                BottomSheetCloseUtil.BottomSheetCloseMethod.SWIPE -> AmplitudePropertyProfileStatisticsCloseType.CLOSE_SWIPE
                BottomSheetCloseUtil.BottomSheetCloseMethod.TAP_OUTSIDE -> AmplitudePropertyProfileStatisticsCloseType.TAP
                BottomSheetCloseUtil.BottomSheetCloseMethod.CLOSE_BUTTON -> AmplitudePropertyProfileStatisticsCloseType.CLOSE
                BottomSheetCloseUtil.BottomSheetCloseMethod.BACK_BUTTON -> AmplitudePropertyProfileStatisticsCloseType.TAP
            }
        }
    })

    //TODO dialogListener?.onCreateDialog() перенесен в метод onCreate для решения проблемы с паузой момента
    // во время открытия боттом щита комментариев при запуске момента https://nomera.atlassian.net/browse/BR-26931
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        momentItem = arguments?.getParcelable(MOMENT_ITEM_MODEL)
            ?: error("Ошибка в параметрах ${this::class.java.simpleName}")

        val commentId = arguments?.getLong(MOMENT_COMMENT_TO_OPEN_ID)
        if (commentId != NO_COMMENT_ID) momentCommentToOpenId = commentId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialogListener?.onCreateDialog()
        bottomSheetCloseUtil.reset()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        bottomSheetCloseUtil.onCancel()
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogListener?.onDismissDialog()
        commentsListener?.onCommentsCountChange(commentTreeController?.commentAdapter?.itemCount ?: 0)
        super.onDismiss(dialog)
        bottomSheetCloseUtil.onDismiss()
        viewModel.handleDismiss()
        commentTreeController?.onDispose()
        momentCommentsBottomSheetSetupUtil.onDispose()
        commentMenuController?.onDismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setOnShowListener { dialogInterface ->
                val momentItem = momentItem
                    ?: error("Невозможно выполнить инициализацию. Модель момента должна быть инициализирована")
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val bottomSheetView = bottomSheetDialog.findViewById<View>(
                    R.id.design_bottom_sheet
                ) ?: return@setOnShowListener
                val rootView = bottomSheetDialog.findViewById<View>(
                    R.id.container
                ) ?: return@setOnShowListener
                val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)

                val mainBinding = contentBinding ?: return@setOnShowListener
                val createCommentBinding =
                    MeeraMomentCommentsBottomSheetCreateCommentBlockBinding.inflate(
                        LayoutInflater.from(context)
                    )

                listenViewEvent()
                initKeyboardListener()
                setupBottomSheetCustomLogic(
                    bottomSheetDialog = bottomSheetDialog,
                    createCommentBinding = createCommentBinding,
                    mainBinding = mainBinding,
                    bottomSheetBehavior = bottomSheetBehavior
                )

                setupBottomSheetBehavior(bottomSheetView = bottomSheetView)
                setupBottomSheetHeight(bottomSheetView)
                setupCommentCreateBlock(
                    createCommentBinding = createCommentBinding,
                    bottomSheetDialog = bottomSheetDialog,
                    bottomSheetBehavior = bottomSheetBehavior
                )
                setupCommentViewTree(
                    bottomSheetDialog = bottomSheetDialog
                )
                setupCommentMenu(
                    bottomSheetRootView = rootView
                )

                bottomSheetRootView = rootView

                viewModel.init(
                    momentItem = momentItem,
                    commentToOpenId = momentCommentToOpenId
                )

                if (momentCommentToOpenId != null) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun listenViewEvent() {
        viewModel.getViewEvent().observe(viewLifecycleOwner) { viewEvent ->
            when (viewEvent) {
                is MomentsCommentViewEvent.CommentRestricted -> {
                    commentTreeController?.handleCommentRestricted()
                    commentCreateController.handleCommentNotAvailable()
                    viewEvent.message?.apply {
                        showCommonError(this, requireView())
                    }
                }

                is MomentsCommentViewEvent.ErrorInnerPagination -> {
                    commentTreeController?.handleInnerCommentError(viewEvent.data)
                    showCommonError(getText(R.string.no_internet), requireView())
                }

                is MomentsCommentViewEvent.UpdateCommentReactionMeera -> {
                    commentTreeController?.handleUpdateReaction(
                        viewEvent.position,
                        viewEvent.reactionUpdate
                    )
                }

                is MomentsCommentViewEvent.EnableComments -> {
                    commentCreateController.handleEnableWriteComment()
                }

                is MomentsCommentViewEvent.NoInternetAction -> {
                    showCommonError(getText(R.string.internet_connection_problem_action), requireView())
                    commentCreateController.handleEnableWriteComment()
                }

                is MomentsCommentViewEvent.ErrorPublishMomentComment -> {
                    showCommonError(getText(R.string.comment_send_error), requireView())
                    commentCreateController.handleEnableWriteComment()
                }

                is MomentsCommentViewEvent.NewCommentSuccess -> {
                    commentTreeController?.handleNewComment(
                        beforeMyComment = viewEvent.beforeMyComment,
                        hasIntersection = viewEvent.hasIntersection,
                        needSmoothScroll = viewEvent.needSmoothScroll,
                        needToShowLastFullComment = viewEvent.needToShowLastFullComment
                    )
                }

                is MomentsCommentViewEvent.NewInnerCommentSuccess -> {
                    commentTreeController?.handleNewInnerComment(
                        parentId = viewEvent.parentId,
                        chunk = viewEvent.chunk
                    )
                }

                is MomentsCommentViewEvent.ComplainSuccess ->
                    showCommonSuccessMessage(getText(R.string.complain_send), requireView())

                is MomentsCommentViewEvent.MarkCommentForDeletion -> {
                    val originalComment = commentTreeController?.handleMarkCommentAsDeleted(
                        commentID = viewEvent.commentID,
                        whoDeleteComment = viewEvent.whoDeleteComment
                    ) ?: return@observe
                    commentMenuController?.handleMarkCommentAsDeleted(
                        originalComment = originalComment,
                        whoDeleteComment = viewEvent.whoDeleteComment
                    )
                }

                is MomentsCommentViewEvent.CancelDeleteComment -> {
                    commentTreeController?.handleCancelCommentDeletion(viewEvent.originalComment)
                }

                is MomentsCommentViewEvent.DeleteComment -> {
                    commentCreateController.handleDeleteComment()
                }

                is MomentsCommentViewEvent.ErrorDeleteComment -> {
                    showCommonError(getText(R.string.no_internet), requireView())
                    commentTreeController?.handleErrorDeleteComment(viewEvent.comment)
                }

                is MomentsCommentViewEvent.OnAddUserToBlocked -> {
                    commentMenuController?.handleUserBlocked(viewEvent.userId)
                    showCommonSuccessMessage(getText(R.string.you_blocked_user), requireView())
                }

                is MomentsCommentViewEvent.ShowTextError -> {
                    commentTreeController?.handleError()
                    showCommonError(
                        viewEvent.message ?: getString(R.string.reaction_unknown_error),
                        requireView()
                    )
                }

                else -> Unit
            }
        }
    }

    private fun setupBottomSheetCustomLogic(
        bottomSheetDialog: BottomSheetDialog,
        createCommentBinding: MeeraMomentCommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: MeeraMomentCommentsBottomSheetBinding,
        bottomSheetBehavior: BottomSheetBehavior<View>
    ) {
        momentCommentsBottomSheetSetupUtil.setup(
            fragment = this,
            bottomDialog = bottomSheetDialog,
            createCommentBinding = createCommentBinding,
            mainBinding = mainBinding,
            callback = object : MeeraMomentCommentsBottomSheetSetupUtil.Callback {
                override fun onChangeSuggestionListPeekHeight(peekHeight: Int, keyboardOpen: Boolean) {
                    commentCreateController.setSuggestionMenuPeekHeight(peekHeight)
                    isKeyboardOpen = keyboardOpen
                }

                override fun onBottomSheetStateChanged(newState: Int) {
                    checkOpenedKeyboard(newState, bottomSheetBehavior)
                    bottomSheetCloseUtil.onStateChanged(newState)
                }
            })
    }

    private fun initKeyboardListener() {
        keyboardHeightProvider?.release()
        contentBinding?.root?.let { root ->
            keyboardHeightProvider = KeyboardHeightProvider(root)
            keyboardHeightProvider?.observer = { height ->
                isKeyboardOpen = height > 0
            }
        }
    }

    private fun checkOpenedKeyboard(newState: Int, bottomSheetBehavior: BottomSheetBehavior<View>) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            if (isKeyboardOpen) {
                requireContext().hideKeyboard(requireView())
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun setupCommentMenu(bottomSheetRootView: View) {
        val momentItem = momentItem
            ?: error("Невозможно выполнить действие. Модель момента должна быть инициализирована")
        val adapter = commentTreeController?.commentAdapter ?: return

        commentMenuController = MeeraMomentCommentBottomMenuView(
            bottomSheetRootView = bottomSheetRootView,
            viewModelController = viewModel.commentBottomMenuController,
            momentItem = momentItem,
            fragment = this,
            commentAdapter = adapter,
            callback = object : MeeraMomentCommentBottomMenuView.Callback {
                override fun onCommentReply(comment: CommentEntityResponse) {
                    commentCreateController.handleCommentReply(comment)
                }

                override fun onShowMessage(message: Int) {
                    showCommonSuccessMessage(getText(message), requireView())
                }
            }
        )
    }

    private fun setupCommentViewTree(bottomSheetDialog: BottomSheetDialog) {
        contentBinding?.let {contentBinding ->
            commentTreeController = MeeraMomentCommentTreeView(
                viewModelController = viewModel.commentTreeModelController,
                binding = contentBinding,
                bottomSheetDialog = bottomSheetDialog,
                fragment = this,
                featureToggleContainer = viewModel.getFeatureToggleContainer(),
                callback = object : MeeraMomentCommentTreeView.Callback {
                    override fun onReplyComment(comment: CommentEntityResponse) {
                        commentCreateController.handleCommentReply(comment)
                    }

                    override fun onCommentShowMenu(comment: CommentEntityResponse, position: Int) {
                        commentMenuController?.show(comment)
                    }

                    override fun onRefresh() {
                        commentMenuController?.handleRefresh()
                    }
                }
            )
        }
    }

    private fun setupCommentCreateBlock(
        createCommentBinding: MeeraMomentCommentsBottomSheetCreateCommentBlockBinding,
        bottomSheetDialog: BottomSheetDialog,
        bottomSheetBehavior: BottomSheetBehavior<View>
    ) {
        val momentItem = momentItem
            ?: error("Невозможно выполнить действие. Модель момента должна быть инициализирована")

        commentCreateController.init(
            momentItem = momentItem,
            fragment = this,
            binding = createCommentBinding,
            bottomSheetDialog = bottomSheetDialog,
            callback = object : MeeraMomentCommentCreateView.Callback {
                override fun onMessageCreate(message: String, parentCommentId: Long) {
                    viewModel.commentCreateModelController.sendCommentToServer(
                        moment = momentItem,
                        message = message,
                        parentCommentId = parentCommentId
                    )

                    expandBottomSheet()
                }

                override fun onInputTextSelected() {
                    expandBottomSheet()
                }

                private fun expandBottomSheet() {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                override fun onScrollDownButtonPress() {
                    expandBottomSheet()
                    commentTreeController?.handleScrollToLastPosition(needSmoothScroll = false)
                }
            }
        )
    }

    private fun setupBottomSheetBehavior(bottomSheetView: View) {
        BottomSheetBehavior.from(bottomSheetView).apply {
            isHideable = true
            skipCollapsed = false
            peekHeight = collapsedStateHeight
            hideFriction = 0.01F
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    private fun setupBottomSheetHeight(bottomSheetView: View) {
        val viewGroupLayoutParams = bottomSheetView.layoutParams
        viewGroupLayoutParams?.height = expandedStateHeight
        bottomSheetView.layoutParams = viewGroupLayoutParams
    }

    companion object {
        const val TAG = "MomentCommentBottomSheetTag"
        private const val MOMENT_ITEM_MODEL = "MOMENT_MODEL"
        private const val MOMENT_COMMENT_TO_OPEN_ID = "MOMENT_COMMENT_TO_OPEN_ID"

        fun show(
            model: MomentItemUiModel,
            fragmentManager: FragmentManager,
            commentToOpenId: Long? = null,
            listener: BottomSheetDialogEventsListener,
            commentsListener: CommentsListener
        ) {
            val alreadyCreatedFragment = fragmentManager.findFragmentByTag(TAG)
            if (alreadyCreatedFragment != null) return

            val fragment = MeeraMomentCommentsBottomSheetFragment()
            fragment.arguments = bundleOf(
                MOMENT_ITEM_MODEL to model,
                MOMENT_COMMENT_TO_OPEN_ID to commentToOpenId
            )
            fragment.setListener(listener)
            fragment.setCommentsListener(commentsListener)
            fragment.show(fragmentManager, TAG)
        }
    }

    interface CommentsListener {
        fun onCommentsCountChange(commentCount: Int) {}
    }
}
