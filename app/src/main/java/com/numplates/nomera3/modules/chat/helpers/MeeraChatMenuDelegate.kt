package com.numplates.nomera3.modules.chat.helpers

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.utils.checkAppRedesigned
import com.meera.db.models.message.MessageEntity
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.InRedesignExists
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.action.ChatActions

class MeeraChatMenuDelegate(
    private val fragment: Fragment,
    private val isFromMoments: Boolean = false,
    private val onAction: (ChatActions) -> Unit
) {

    @InRedesignExists(description = "Редизайн в классе MeeraMediaPreviewBottomSheetDialog")
    fun showMediaPreview(
        mediaPreview: MediaPreviewUiModel,
        onDismiss: () -> Unit = {},
        deleteRecentClickListener: (Int) -> Unit = {}
    ) {
        var isShowSendWhenFavoriteRecent = false
        var isShowSendWhenSticker = false
        var isShowRemoveFromFavorites = false
        var isShowAddToFavorites = false
        var isShowFromRecent = false

        onAction(ChatActions.SetupMediaPreview(media = mediaPreview.media, isMeeraMenu = true))
        if (mediaPreview.favoriteRecentModel != null) {
            isShowSendWhenFavoriteRecent = true
        }
        if (mediaPreview.media is MediaUiModel.StickerMediaUiModel && mediaPreview.favoriteRecentModel == null) {
            isShowSendWhenSticker = true
        }
        when {
            mediaPreview.isAdded -> {
                isShowRemoveFromFavorites = true
            }

            !mediaPreview.isAdded -> {
                isShowAddToFavorites = true
            }
        }
        if (mediaPreview.type == MediaPreviewType.RECENT) {
            isShowFromRecent = true
        }

        showMeeraMediaMenu(
            isFromMoments = isFromMoments,
            isShowSendWhenFavoriteRecent = isShowSendWhenFavoriteRecent,
            isShowSendWhenSticker = isShowSendWhenSticker,
            isShowRemoveFromFavorites = isShowRemoveFromFavorites,
            isShowAddToFavorites = isShowAddToFavorites,
            isShowFromRecent = isShowFromRecent,
            mediaPreview = mediaPreview,
            deleteRecentClickListener = deleteRecentClickListener,
            onDismiss = onDismiss
        )
    }

    private fun showMeeraMediaMenu(
        isFromMoments: Boolean,
        isShowSendWhenFavoriteRecent: Boolean,
        isShowSendWhenSticker: Boolean,
        isShowRemoveFromFavorites: Boolean,
        isShowAddToFavorites: Boolean,
        isShowFromRecent: Boolean,
        mediaPreview: MediaPreviewUiModel,
        deleteRecentClickListener: (Int) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        val dialog = MeeraMediaPreviewBottomSheetDialog()
        dialog.show(
            fm = fragment.childFragmentManager,
            data = MeeraMediaPreviewBottomSheetDialogData(
                isFromMoments = isFromMoments,
                isShowSend = isShowSendWhenFavoriteRecent || isShowSendWhenSticker,
                isShowAddToFavorites = isShowAddToFavorites,
                isShowRemoveFromFavorites = isShowRemoveFromFavorites,
                isShowRemoveFromRecent = isShowFromRecent
            ),
            onClick = { action ->
                when (action) {
                    is MeeraMediaPreviewBottomSheetDialogClickAction.OnClickSend -> {
                        if (mediaPreview.favoriteRecentModel != null) {
                            onAction(
                                ChatActions.SendFavoriteRecent(
                                    mediaPreview.favoriteRecentModel,
                                    mediaPreview.type
                                )
                            )
                        } else if (mediaPreview.media is MediaUiModel.StickerMediaUiModel) {
                            val favoriteRecentModel = MediakeyboardFavoriteRecentUiModel(
                                id = mediaPreview.media.favoriteId ?: mediaPreview.media.stickerId
                                ?: return@show,
                                type = MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER,
                                url = mediaPreview.media.stickerUrl,
                                preview = mediaPreview.media.stickerUrl,
                                lottieUrl = mediaPreview.media.lottieUrl,
                                webpUrl = mediaPreview.media.webpUrl,
                                stickerId = mediaPreview.media.stickerId,
                                favoriteId = mediaPreview.media.favoriteId?.toLong()
                            )
                            onAction(
                                ChatActions.SendFavoriteRecent(
                                    favoriteRecentModel,
                                    mediaPreview.type
                                )
                            )
                        }
                    }

                    is MeeraMediaPreviewBottomSheetDialogClickAction.OnClickAddToFavorites -> {
                        val mediaUrl = when (mediaPreview.media) {
                            is MediaUiModel.GifMediaUiModel -> mediaPreview.media.url
                            is MediaUiModel.ImageMediaUiModel -> mediaPreview.media.url
                            is MediaUiModel.VideoMediaUiModel -> mediaPreview.media.preview ?: mediaPreview.media.url
                            is MediaUiModel.StickerMediaUiModel -> mediaPreview.media.stickerUrl
                        }
                        onAction(
                            ChatActions.AddToFavorites(
                                mediaPreview = mediaPreview,
                                mediaUrl = mediaUrl,
                                lottieUrl = (mediaPreview.media as? MediaUiModel.StickerMediaUiModel?)?.lottieUrl
                            )
                        )
                    }

                    is MeeraMediaPreviewBottomSheetDialogClickAction.OnClickRemoveFromFavorites -> {
                        onAction(ChatActions.RemoveFromFavorites(mediaPreview))
                    }

                    is MeeraMediaPreviewBottomSheetDialogClickAction.OnClickRemoveFromRecent -> {
                        mediaPreview.media.id?.let(deleteRecentClickListener)
                    }
                }
            },
            onDismiss = { onDismiss.invoke() }
        )
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun showMessageEditErrorAlert() {
        checkAppRedesigned(
            isRedesigned = {
                showMeeraConfirmDialog(
                    headerText = R.string.chat_message_edit_failed_title,
                    descriptionText = R.string.chat_message_edit_failed_text,
                    onOkButtonClick = {
                        onAction(ChatActions.SendEditedMessageWithConditionsCheck)
                    }
                )
            },
            isNotRedesigned = {
                val ctx = fragment.requireContext()
                AlertDialog.Builder(ctx)
                    .setCancelable(false)
                    .setTitle(ctx.getString(R.string.chat_message_edit_failed_title))
                    .setMessage(ctx.getString(R.string.chat_message_edit_failed_text))
                    .setPositiveButton(ctx.getString(R.string.chat_message_edit_failed_retry)) { _, _ ->
                        onAction(ChatActions.SendEditedMessageWithConditionsCheck)
                    }
                    .setNegativeButton(
                        ctx.getString(R.string.chat_message_edit_failed_accept),
                        null
                    )
                    .show()
            }
        )
    }

    fun deleteMessageDialog(
        message: MessageEntity?,
        ownUserId: Long,
        onClickRemove: () -> Unit
    ) {
        val ctx = fragment.requireContext()
        if (message?.creator?.userId == ownUserId) {
            MeeraConfirmDialogBuilder()
                .setHeader(ctx.getString(R.string.chat_remove_message))
                .setDescription(ctx.getString(R.string.confirm_delete_messgage_desc))
                .setTopBtnText(ctx.getString(R.string.delete_from_myself))
                .setBottomBtnText(ctx.getString(R.string.delete_from_everyone))
                .setTopBtnType(ButtonType.FILLED_ERROR)
                .setBottomBtnType(ButtonType.OUTLINE_ERROR)
                .setTopClickListener {
                    onClickRemove.invoke()
                    onAction(
                        ChatActions.RemoveMessage(
                            message = message,
                            isBoth = false
                        )
                    )
                }
                .setBottomClickListener {
                    onClickRemove.invoke()
                    onAction(
                        ChatActions.RemoveMessage(
                            message = message,
                            isBoth = true
                        )
                    )
                }
                .show(fragment.childFragmentManager)
        } else {
            MeeraConfirmDialogBuilder()
                .setHeader(ctx.getString(R.string.chat_remove_message))
                .setDescription(ctx.getString(R.string.chat_remove_message_for_you))
                .setTopBtnText(R.string.general_delete)
                .setBottomBtnText(R.string.general_close)
                .setTopClickListener {
                    onClickRemove.invoke()
                    onAction(
                        ChatActions.RemoveMessage(
                            message = message,
                            isBoth = false
                        )
                    )
                }
                .show(fragment.childFragmentManager)
        }
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun abortMessageEdit(
        isMessageEditActive: Boolean,
        onEditAborted: () -> Unit
    ) {
        if (isMessageEditActive) {
            showMeeraConfirmDialog(
                headerText = R.string.chat_edit_message_abort_title,
                descriptionText = R.string.chat_edit_message_abort_message,
                onOkButtonClick = {
                    onAction(ChatActions.ClearMessageEditor)
                    onEditAborted.invoke()
                }
            )
        } else {
            onEditAborted.invoke()
        }
    }

    private fun showMeeraConfirmDialog(
        @StringRes headerText: Int,
        @StringRes descriptionText: Int,
        onOkButtonClick: () -> Unit = {},
        onCancelButtonClick: () -> Unit = {}
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(headerText)
            .setDescription(descriptionText)
            .setTopBtnText(R.string.yes)
            .setBottomBtnText(R.string.no)
            .setCancelable(true)
            .setTopClickListener {
                onOkButtonClick()
            }
            .setBottomClickListener {
                onCancelButtonClick()
            }
            .show(fragment.childFragmentManager)
    }
}
