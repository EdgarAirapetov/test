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
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetBinding
import com.numplates.nomera3.databinding.MomentCommentsBottomSheetCreateCommentBlockBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics.AmplitudePropertyProfileStatisticsCloseType
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.utils.bottomsheet.BottomSheetCloseUtil
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.utils.NToast

private const val COLLAPSED_HEIGHT_RATIO = 1.6
private const val NO_COMMENT_ID = 0L

/**
 * @property momentCommentsBottomSheetSetupUtil – отвечает за поведение шторки (отступы, открытия, закрытие, анимации)
 * @property commentTreeController – отвечает за recycler-список комментариев
 * @property commentMenuController – отвечает за шторку-действий при лонгтапе на комментарий
 * @property commentCreateController – отвечает за создания комментария
 */
class MomentCommentsBottomSheetFragment :
    BaseBottomSheetDialogFragment<MomentCommentsBottomSheetBinding>() {

    private val viewModel by viewModels<MomentCommentsBottomSheetViewModel> { App.component.getViewModelFactory() }

    private val momentCommentsBottomSheetSetupUtil = MomentCommentsBottomSheetSetupUtil()
    private var commentTreeController: MomentCommentTreeView? = null
    private var commentMenuController: MomentCommentBottomMenuView? = null
    private val commentCreateController = MomentCommentCreateView()

    private val expandedStateHeight = getScreenHeight()
    private val collapsedStateHeight = (expandedStateHeight / COLLAPSED_HEIGHT_RATIO).toInt()

    private var bottomSheetRootView: View? = null

    private var momentItem: MomentItemUiModel? = null
    private var momentCommentToOpenId: Long? = null
    private var commentsListener: CommentsListener? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var isKeyboardOpen: Boolean = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MomentCommentsBottomSheetBinding
        get() = MomentCommentsBottomSheetBinding::inflate

    fun setCommentsListener(listener: CommentsListener) {
        this.commentsListener = listener
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

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
        dialogListener?.onCreateDialog()
        momentItem = arguments?.getParcelable(MOMENT_ITEM_MODEL)
            ?: error("Ошибка в параметрах ${this::class.java.simpleName}")

        val commentId = arguments?.getLong(MOMENT_COMMENT_TO_OPEN_ID)
        if (commentId != NO_COMMENT_ID) momentCommentToOpenId = commentId
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bottomSheetCloseUtil.reset()
    }

    override fun onBackKeyPressed() {
        super.onBackKeyPressed()
        bottomSheetCloseUtil.onBackButtonPressed()
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

                val mainBinding = binding ?: return@setOnShowListener
                val createCommentBinding =
                    MomentCommentsBottomSheetCreateCommentBlockBinding.inflate(
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
                setupBottomSheetClickListeners()

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

    private fun setupBottomSheetClickListeners() {
        binding?.ivMomentCommentsCloseButton?.setThrottledClickListener {
            dismiss()
        }
    }

    private fun listenViewEvent() {
        viewModel.getViewEvent().observe(viewLifecycleOwner) { viewEvent ->
            when (viewEvent) {
                is MomentsCommentViewEvent.CommentRestricted -> {
                    commentTreeController?.handleCommentRestricted()
                    commentCreateController.handleCommentNotAvailable()
                    viewEvent.message?.apply { showTextError(this) }
                }

                is MomentsCommentViewEvent.ErrorInnerPagination -> {
                    commentTreeController?.handleInnerCommentError(viewEvent.data)
                    showTextError(getString(R.string.no_internet))
                }

                is MomentsCommentViewEvent.UpdateCommentReaction -> {
                    commentTreeController?.handleUpdateReaction(
                        viewEvent.position,
                        viewEvent.reactionUpdate
                    )
                }

                is MomentsCommentViewEvent.EnableComments -> {
                    commentCreateController.handleEnableWriteComment()
                }

                is MomentsCommentViewEvent.NoInternetAction -> {
                    showTextError(getString(R.string.internet_connection_problem_action))
                    commentCreateController.handleEnableWriteComment()
                }

                is MomentsCommentViewEvent.ErrorPublishMomentComment -> {
                    showTextError(getString(R.string.comment_send_error))
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
                    showTextMessage(getString(R.string.complain_send))

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
                    showTextError(getString(R.string.no_internet))
                    commentTreeController?.handleErrorDeleteComment(viewEvent.comment)
                }

                is MomentsCommentViewEvent.OnAddUserToBlocked -> {
                    commentMenuController?.handleUserBlocked(viewEvent.userId)
                    showTextMessage(getString(R.string.you_blocked_user))
                }

                is MomentsCommentViewEvent.ShowTextError -> {
                    commentTreeController?.handleError()
                    showTextError(viewEvent.message)
                }

                else -> Unit
            }
        }
    }

    private fun setupBottomSheetCustomLogic(
        bottomSheetDialog: BottomSheetDialog,
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
        mainBinding: MomentCommentsBottomSheetBinding,
        bottomSheetBehavior: BottomSheetBehavior<View>
    ) {
        momentCommentsBottomSheetSetupUtil.setup(
            fragment = this,
            bottomDialog = bottomSheetDialog,
            createCommentBinding = createCommentBinding,
            mainBinding = mainBinding,
            callback = object : MomentCommentsBottomSheetSetupUtil.Callback {
                override fun onChangeSuggestionListPeekHeight(peekHeight: Int) {
                    commentCreateController.setSuggestionMenuPeekHeight(peekHeight)
                }

                override fun onBottomSheetStateChanged(newState: Int) {
                    checkOpenedKeyboard(newState, bottomSheetBehavior)
                    bottomSheetCloseUtil.onStateChanged(newState)
                }
            })
    }

    private fun initKeyboardListener() {
        keyboardHeightProvider?.release()
        binding?.root?.let { root ->
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

        commentMenuController = MomentCommentBottomMenuView(
            bottomSheetRootView = bottomSheetRootView,
            viewModelController = viewModel.commentBottomMenuController,
            momentItem = momentItem,
            fragment = this,
            commentAdapter = adapter,
            callback = object : MomentCommentBottomMenuView.Callback {
                override fun onCommentReply(comment: CommentEntityResponse) {
                    commentCreateController.handleCommentReply(comment)
                }

                override fun onShowMessage(message: String) {
                    showTextMessage(message)
                }
            }
        )
    }

    private fun setupCommentViewTree(bottomSheetDialog: BottomSheetDialog) {
        commentTreeController = MomentCommentTreeView(
            viewModelController = viewModel.commentTreeModelController,
            binding = binding!!,
            bottomSheetDialog = bottomSheetDialog,
            fragment = this,
            featureToggleContainer = viewModel.getFeatureToggleContainer(),
            callback = object : MomentCommentTreeView.Callback {
                override fun onReplyComment(comment: CommentEntityResponse) {
                    commentCreateController.handleCommentReply(comment)
                }

                override fun onShowScrollDownButton() {
                    commentCreateController.handleShowScrollButtonDown()
                }

                override fun onHideScrollDownButton() {
                    commentCreateController.handleHideScrollButtonDown()
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

    private fun setupCommentCreateBlock(
        createCommentBinding: MomentCommentsBottomSheetCreateCommentBlockBinding,
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
            callback = object : MomentCommentCreateView.Callback {
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

    private fun showTextMessage(message: String) {
        NToast.with(act)
            .inView(bottomSheetRootView)
            .text(message)
            .typeSuccess()
            .show()
    }

    private fun showTextError(message: String?) {
        val resultMessage = message ?: getString(R.string.reaction_unknown_error)

        NToast.with(act)
            .inView(bottomSheetRootView)
            .typeError()
            .text(resultMessage)
            .show()
    }

    companion object {
        private const val MOMENT_ITEM_MODEL = "MOMENT_MODEL"
        private const val MOMENT_COMMENT_TO_OPEN_ID = "MOMENT_COMMENT_TO_OPEN_ID"
        private const val TAG = "MomentCommentBottomSheetTag"

        fun show(
            model: MomentItemUiModel,
            fragmentManager: FragmentManager,
            commentToOpenId: Long? = null,
            listener: BottomSheetDialogEventsListener,
            commentsListener: CommentsListener
        ) {
            val alreadyCreatedFragment = fragmentManager.findFragmentByTag(TAG)
            if (alreadyCreatedFragment != null) return

            val fragment = MomentCommentsBottomSheetFragment()
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
