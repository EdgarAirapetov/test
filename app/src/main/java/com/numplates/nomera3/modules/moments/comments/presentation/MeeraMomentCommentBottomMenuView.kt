package com.numplates.nomera3.modules.moments.comments.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.extensions.stringNullable
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.comments.ui.adapter.MeeraCommentAdapter
import com.numplates.nomera3.modules.comments.ui.entity.CommentUIType
import com.numplates.nomera3.modules.comments.ui.fragment.WhoDeleteComment
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

private const val DELAY_DELETE_COMMENT_SEC = 5
private const val CLIP_DATA_TEXT = "text"

class MeeraMomentCommentBottomMenuView(
    private val bottomSheetRootView: View,
    private val commentAdapter: MeeraCommentAdapter,
    private val viewModelController: MomentCommentMenuModel,
    private val momentItem: MomentItemUiModel,
    private val fragment: BottomSheetDialogFragment,
    private val callback: Callback
) {
    private val act = (fragment.requireActivity() as? MeeraAct)
        ?: error("Ошибка аргументов. Не могу инициализировать MomentCommentBottomMenuView")
    private val userUid = act.app.appSettings.get().readUID()
    private val context = fragment.requireContext()

    private var currentBottomMenu: MeeraMenuBottomSheet? = null
    private var blockedUsersList = mutableSetOf<Long>()
    private var undoSnackBar: NSnackbar? = null

    fun onDismiss() {
        undoSnackBar?.dismissNoCallbacks()
    }

    fun handleRefresh() {
        blockedUsersList.clear()
    }

    fun handleUserBlocked(userId: Long) {
        blockedUsersList.add(userId)
    }

    fun handleMarkCommentAsDeleted(originalComment: CommentUIType, whoDeleteComment: WhoDeleteComment) {
        showDeleteCommentCountdownToastNew(
            onClosedManually = { isDialogForceClosed ->
                if (isDialogForceClosed) {
                    viewModelController.cancelDeleteComment(originalComment)
                } else {
                    viewModelController.deleteComment(
                        momentItemId = momentItem.id,
                        comment = originalComment,
                        whoDeleteComment = whoDeleteComment
                    )
                }
            }
        )
    }

    fun show(comment: CommentEntityResponse) {
        if (momentItem.isUserBlackListMe) return

        val isMeAuthor = comment.uid == userUid
        val commentText = comment.text ?: ""
        val isAuthor = momentItem.userId == userUid

        if (isAuthor) {
            showBottomOwnerAction(
                comment = comment,
                commentText = commentText,
                isMeAuthor = isMeAuthor,
                commentAuthorId = comment.uid,
                isAuthor = isAuthor
            )
        } else {
            if (isMeAuthor) {
                showBottomOwnerAction(
                    comment = comment,
                    commentText = commentText,
                    isMeAuthor = isMeAuthor,
                    commentAuthorId = comment.uid,
                    isAuthor = isAuthor
                )
            } else {
                showBottomCommonAction(
                    comment = comment,
                    commentText = commentText
                )
            }
        }
    }

    private fun showBottomCommonAction(
        comment: CommentEntityResponse,
        commentText: String,
    ) {
        val menu = MeeraMenuBottomSheet(context)
        menu.setListener(object : BottomSheetDialogEventsListener {
            override fun onDismissDialog(closeTypes: CloseTypes?) {
                currentBottomMenu = null
            }
        })

        if (viewModelController.paginationHelper.needToShowReplyBtn) {
            menu.addItem(R.string.reply_txt, R.drawable.ic_reply_purple_new) {
                callback.onCommentReply(comment)
            }
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
            val clipboardManager =
                act.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText(CLIP_DATA_TEXT, commentText)
            clipboardManager?.setPrimaryClip(clipData)
            callback.onShowMessage(R.string.comment_text_copied)
        }

        menu.addItem(R.string.comment_complain, R.drawable.ic_send_error) {
            viewModelController.complainComment(comment.id)
        }

        menu.show(fragment.childFragmentManager)

        currentBottomMenu = menu
    }

    private fun showBottomOwnerAction(
        comment: CommentEntityResponse,
        commentText: String,
        isMeAuthor: Boolean,
        commentAuthorId: Long,
        isAuthor: Boolean
    ) {
        val menu = MeeraMenuBottomSheet(act.baseContext)
        menu.setListener(object : BottomSheetDialogEventsListener {
            override fun onDismissDialog(closeTypes: CloseTypes?) {
                currentBottomMenu = null
            }
        })

        menu.addItem(R.string.reply_txt, R.drawable.ic_reply_purple_new) {
            callback.onCommentReply(comment)
        }

        menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
            val clipboardManager =
                act.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
            val clipData = ClipData.newPlainText(CLIP_DATA_TEXT, commentText)
            clipboardManager?.setPrimaryClip(clipData)
            callback.onShowMessage(R.string.comment_text_copied)
        }

        menu.addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
            val whoDeleteComment = getWhoDeletedComment(isAuthor, isMeAuthor) ?: return@addItem
            val originalComment = commentAdapter.findCommentById(comment.id) ?: return@addItem
            viewModelController.markAsDeleteComment(
                originalComment = originalComment,
                whoDeleteComment = whoDeleteComment
            )
        }

        if (!isMeAuthor) {
            menu.addItem(R.string.comment_complain, R.drawable.ic_send_error) {
                viewModelController.complainComment(comment.id)
            }

            if (!blockedUsersList.contains(commentAuthorId)) {
                menu.addItem(R.string.settings_privacy_block_user, R.drawable.ic_block_user_red) {
                    viewModelController.blockUser(userUid, commentAuthorId)
                }
            }
        }

        menu.show(act.supportFragmentManager)

        currentBottomMenu = menu
    }

    private fun getWhoDeletedComment(
        isPostAuthor: Boolean,
        isCommentAuthor: Boolean
    ): WhoDeleteComment? {
        if (isPostAuthor && !isCommentAuthor) return WhoDeleteComment.POST_AUTHOR
        if (isPostAuthor && isCommentAuthor) return WhoDeleteComment.BOTH_POST_COMMENT_AUTHOR
        if (!isPostAuthor && isCommentAuthor) return WhoDeleteComment.COMMENT_AUTHOR
        return null
    }

    private fun showDeleteCommentCountdownToastNew(onClosedManually: (Boolean) -> Unit) {
        undoSnackBar?.dismissNoCallbacks()
        undoSnackBar = NSnackbar.with(act)
            .inView(bottomSheetRootView)
            .text(act.stringNullable(R.string.comment_deleted))
            .description(act.stringNullable(R.string.touch_to_delete))
            .durationIndefinite()
            .button(act.stringNullable(R.string.general_cancel))
            .dismissManualListener { onClosedManually(true) }
            .timer(DELAY_DELETE_COMMENT_SEC) { onClosedManually(false) }
            .show()
    }

    interface Callback {
        fun onCommentReply(comment: CommentEntityResponse)
        fun onShowMessage(message: Int)
    }
}
