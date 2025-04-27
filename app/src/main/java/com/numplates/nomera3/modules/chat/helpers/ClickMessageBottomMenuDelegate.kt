package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.meera.core.extensions.copyToClipBoard
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.toInt
import com.meera.core.extensions.visible
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.dialog.userRole
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.databinding.FragmentChatBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.MediaPreviewMenuBottomSheet
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

const val PREVIEW_MARGIN_LARGE = 400

class ClickMessageBottomMenuDelegate(
    private val fragment: Fragment,
    private val binding: FragmentChatBinding?,
    private val featureToggles: FeatureTogglesContainer,
    private val onAction: (ChatActions) -> Unit
) {

    fun showMeeraBottomMenu(
        message: MessageEntity?,
        room: DialogEntity?,
        messageView: View? = null,
        unsentMessageCounter: Int = 0,
        mediaPreview: MediaPreviewUiModel? = null,
        isEditMessageAvailable: Boolean = false,
        menuPayload: ChatBottomMenuPayload? = null,
        isUploadMediaProgress: Boolean = false
    ) {
        if (message != null && room != null) {
            val dialogMenu = MeeraBaseBottomSheetDialogMenu()
            dialogMenu.show(
                fm = fragment.childFragmentManager,
                data = MeeraClickMessageDialogData(
                    message = message,
                    room = room,
                    messageView = messageView,
                    unsentMessageCounter = unsentMessageCounter,
                    mediaPreview = mediaPreview,
                    isEditMessageAvailable = isEditMessageAvailable,
                    menuPayload = menuPayload,
                    isUploadMediaProgress = isUploadMediaProgress,
                    menuType = MeeraBaseBottomSheetDialogType.CLICK_MESSAGE
                ),
                onClickAction = { menuAction ->
                    if (menuAction != null) onAction.invoke(menuAction)
                }
            )
        }
    }

    fun openMeeraForbidChatRequestMenu() {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(menuType = MeeraBaseBottomSheetDialogType.FORBID_CHAT_REQUEST_MENU),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun openMeeraDialogMoreMenu(companion: UserChat?){
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                companion = companion,
                menuType = MeeraBaseBottomSheetDialogType.DIALOG_MORE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun openMeeraGroupChatSettingsDialog(
        room: DialogEntity?,
        ownUserId: Long
    ) {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                room = room,
                ownUserId = ownUserId,
                featureToggles = featureToggles,
                menuType = MeeraBaseBottomSheetDialogType.GROUP_MORE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }

    fun showMeeraCompletelyRemoveMessageDialog(message: MessageEntity) {
        val dialogMenu = MeeraBaseBottomSheetDialogMenu()
        dialogMenu.show(
            fm = fragment.childFragmentManager,
            data = MeeraClickMessageDialogData(
                message = message,
                menuType = MeeraBaseBottomSheetDialogType.COMPLETE_REMOVE_MESSAGE
            ),
            onClickAction = { menuAction ->
                if (menuAction != null) onAction.invoke(menuAction)
            }
        )
    }


    fun showBottomMenu(
        room: DialogEntity?,
        message: MessageEntity?,
        messageView: View? = null,
        unsentMessageCounter: Int = 0,
        mediaPreview: MediaPreviewUiModel? = null,
        isEditMessageAvailable: Boolean = false,
        menuPayload: ChatBottomMenuPayload? = null,
        isUploadMediaProgress: Boolean = false
    ): MeeraMenuBottomSheet {
        val ctx = fragment.requireContext()
        val menu = MediaPreviewMenuBottomSheet(ctx)

        message?.let {
            if (isUploadMediaProgress) return buildMediaLoadingInProgressMenu(menu, message)

            val isMsgMediaOrSticker = message.isMediaMessage() || message.isStickerMessage()
            message.ifNotResendProgress {

                if (!message.isNeedToResend()) {
                    replyMenuItem(menu, room, message)
                    favoritesMenuItem(
                        context = ctx,
                        menu = menu,
                        message = message,
                        mediaPreview = mediaPreview
                    )
                } else if (message.isNeedToResend()) {
                    resendMenuItem(
                        menu = menu,
                        message = message,
                        unsentMessageCounter = unsentMessageCounter
                    )
                }

                downloadMenuItem(
                    menu = menu,
                    message = message,
                )

                if (message.isValidForForwarding()) {
                    menu.addItem(R.string.general_forward, R.drawable.ic_share_purple_new) {
                        room?.isNotBlocked { onAction(ChatActions.OnMessageForward(message)) }
                    }
                }

                if (message.isImageOrGifMessage() && message.tagSpan?.text?.isBlank() == true) {
                    menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
                        onAction(ChatActions.CopyImageAttachment(message))
                    }
                }

                copyMessageContentMenuItem(
                    menu = menu,
                    message = message,
                    messageView = messageView
                )
            }

            editMessageMenuItem(
                menu = menu,
                room = room,
                message = message,
                isEditMessageAvailable = isEditMessageAvailable
            )

            copyVoiceMessageRecognizedText(
                menu = menu,
                message = message,
                messageView = messageView,
                menuPayload = menuPayload
            )

            if (message.isValidForContentSharing()) {
                menu.addItem(R.string.image_share, R.drawable.ic_repost_more, isDismissMenu = false) {
                    onAction(ChatActions.ShareMessageContent(message))
                }
            }

            deleteMessageMenuItem(
                menu = menu,
                message = message,
                companionUserRole = room?.companion?.userRole
            )

            if (isMsgMediaOrSticker) {
                mediaPreview?.media?.let {
                    onAction(ChatActions.SetupMediaPreview(it))
                }
                binding?.layoutMediaKeyboardPreview?.apply {
                    ivMediaPreview.setMargins(bottom = PREVIEW_MARGIN_LARGE.dp)
                    vgVideoPreview.setMargins(bottom = PREVIEW_MARGIN_LARGE.dp)
                    vgMediaPreview.visible()
                }
            }
            menu.show(fragment.childFragmentManager)
        }
        return menu
    }

    private fun buildMediaLoadingInProgressMenu(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity
    ): MediaPreviewMenuBottomSheet {
        downloadMenuItem(
            menu = menu,
            message = message,
        )
        menu.addItem(R.string.message_delete_txt, R.drawable.ic_chat_delete_message) {
            onAction(ChatActions.MessageDelete(message))
        }
        menu.show(fragment.childFragmentManager)
        return menu
    }

    private fun replyMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        room: DialogEntity?,
        message: MessageEntity
    ) {
        val isPersonalChatAllowed = isPersonalChatBlocked(room).not()
        if (message.isValidForReply() && message.sent) {
            menu.addItem(R.string.road_reply_comment, R.drawable.ic_reply_purple_new) {
                if (isPersonalChatAllowed) room?.isNotBlocked {
                    onAction(ChatActions.ReplyMessage(message))
                }
            }
        }
    }

    private fun favoritesMenuItem(
        context: Context,
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
        mediaPreview: MediaPreviewUiModel? = null
    ) {
        val isMsgMediaOrSticker = message.isMediaMessage() || message.isStickerMessage()
        when {
            isMsgMediaOrSticker && mediaPreview?.isAdded == true -> {
                menu.addItem(
                    title = context.getString(R.string.remove_from_favorites),
                    icon = R.drawable.ic_delete_media_from_favorites,
                    color = R.color.color_reaction_default
                ) {
                    onAction(ChatActions.RemoveFromFavorites(mediaPreview))
                }
            }
            isMsgMediaOrSticker -> {
                menu.addItem(R.string.add_to_favorites, R.drawable.ic_add_to_favorite) {
                    val lottieUrl = (mediaPreview?.media as? MediaUiModel.StickerMediaUiModel?)?.lottieUrl
                    onAction(ChatActions.AddToFavoritesToMessage(
                        message = message,
                        lottieUrl = lottieUrl
                    ))

                }
            }
        }
    }

    private fun resendMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
        unsentMessageCounter: Int
    ) {
        menu.addItem(R.string.chat_resend_message, R.drawable.ic_resend_message) {
            onAction(ChatActions.ResendSingleMessage(message))
        }
        if (unsentMessageCounter > 1) {
            menu.addItem(
                R.string.chat_resend_some_messages, R.drawable.ic_resend_message,
                unsentMessageCounter
            ) {
                onAction(ChatActions.ResendAllMessages(unsentMessageCounter))
            }
        }
    }

    private fun downloadMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
    ) {
        if (message.isMediaMessage()) {
            menu.addItem(R.string.save_image, R.drawable.image_download_menu_item) {
                onAction(ChatActions.DownloadImageVideoAttachment(message))
            }
        }
    }

    private fun copyMessageContentMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
        messageView: View? = null,
    ) {
        if (message.isValidForCopy()) {
            menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
                val copyText = message.tagSpan?.text?.trim() ?: String.empty()
                if (copyText.isNotEmpty()) {
                    menu.context?.copyToClipBoard(copyText) {
                        onAction(ChatActions.CopyMessageContent(message, messageView))
                    }
                }
            }
        }
    }

    private fun copyVoiceMessageRecognizedText(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
        messageView: View? = null,
        menuPayload: ChatBottomMenuPayload?
    ) {
        val recognizedText = menuPayload?.voiceRecognizedText ?: String.empty()
        if (message.attachment.type == TYPING_TYPE_AUDIO && recognizedText.isNotEmpty()) {
            menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
                menu.context?.copyToClipBoard(recognizedText) {
                    onAction(ChatActions.CopyMessageContent(message, messageView))
                }
            }
        }
    }

    private fun editMessageMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        room: DialogEntity?,
        message: MessageEntity,
        isEditMessageAvailable: Boolean
    ) {
        if (isEditMessageAvailable) {
            menu.addItem(
                title = fragment.requireContext().getString(R.string.chat_edit_message),
                icon = R.drawable.ic_chat_edit_message,
                click = {
                    room?.isNotBlocked { onAction(ChatActions.MessageEdit(message)) }
                }
            )
        }
    }

    private fun deleteMessageMenuItem(
        menu: MediaPreviewMenuBottomSheet,
        message: MessageEntity,
        companionUserRole: UserRole?
    ) {
        if (companionUserRole != UserRole.SUPPORT_USER) {
            menu.addItem(R.string.message_delete_txt, R.drawable.ic_chat_delete_message) {
                onAction(ChatActions.MessageDelete(message))
            }
        }
    }

    private fun MessageEntity.ifNotResendProgress(block: () -> Unit) {
        if (!this.isResendProgress) {
            block()
        }
    }

    private fun MessageEntity.isNeedToResend(): Boolean = !this.sent && this.isResendAvailable

    private fun DialogEntity.isNotBlocked(block: () -> Unit) {
        if (this.blocked == false) {
            block.invoke()
        }
    }

    private fun isPersonalChatBlocked(room: DialogEntity?): Boolean {
        val companion = room?.companion
        return companion?.blacklistedMe == true.toInt()
            || companion?.blacklistedByMe == true.toInt()
            || companion?.settingsFlags?.iCanChat != true.toInt()
            || companion?.settingsFlags?.userCanChatMe != true.toInt()
            || companion?.settingsFlags?.isInChatBlackList == true.toInt()
    }

}

data class ChatBottomMenuPayload(
    val voiceRecognizedText: String? = null
)
