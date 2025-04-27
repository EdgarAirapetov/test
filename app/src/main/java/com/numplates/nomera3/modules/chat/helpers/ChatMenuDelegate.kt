package com.numplates.nomera3.modules.chat.helpers

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.checkAppRedesigned
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.InRedesignExists
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MediaKeyboardPreviewLayoutBinding
import com.numplates.nomera3.modules.chat.MoreMenuBottomSheetBuilder
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.MediaPreviewMenuBottomSheet
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet


private const val PREVIEW_MARGIN_SMALL = 160


class ChatMenuDelegate(
    private val fragment: Fragment,
    private val binding: MediaKeyboardPreviewLayoutBinding?,
    private val featureToggles: FeatureTogglesContainer,
    private val isFromMoments: Boolean = false,
    private val onAction: (ChatActions) -> Unit
) {

    @InRedesignExists(description = "Редизайн в классе MeeraMediaPreviewBottomSheetDialog")
    fun showMediaPreview(
        mediaPreview: MediaPreviewUiModel,
        deleteRecentClickListener: (Int) -> Unit = {}
    ) {
        var isShowSendWhenFavoriteRecent = false
        var isShowSendWhenSticker = false
        var isShowRemoveFromFavorites = false
        var isShowAddToFavorites = false
        var isShowFromRecent = false

        val context = fragment.context ?: return
        binding?.apply {
            onAction(ChatActions.SetupMediaPreview(mediaPreview.media))

            val menu = MediaPreviewMenuBottomSheet(fragment.context)
            if (mediaPreview.favoriteRecentModel != null) {
                isShowSendWhenFavoriteRecent = true
                menu.addItem(
                    title = if (isFromMoments) R.string.editor_widget_add else R.string.general_send,
                    icon = if (isFromMoments) R.drawable.iconadd_purple else R.drawable.ic_send
                ) {
                    onAction(ChatActions.SendFavoriteRecent(mediaPreview.favoriteRecentModel, mediaPreview.type))
                }
            }
            if (mediaPreview.media is MediaUiModel.StickerMediaUiModel && menu.isEmpty) {
                isShowSendWhenSticker = true
                menu.addItem(
                    title = if (isFromMoments) R.string.editor_widget_add else R.string.general_send,
                    icon = if (isFromMoments) R.drawable.iconadd_purple else R.drawable.ic_send
                ) {
                    val favoriteRecentModel = MediakeyboardFavoriteRecentUiModel(
                        id = mediaPreview.media.favoriteId ?: mediaPreview.media.stickerId ?: return@addItem,
                        type = MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER,
                        url = mediaPreview.media.stickerUrl,
                        preview = mediaPreview.media.stickerUrl,
                        lottieUrl = mediaPreview.media.lottieUrl,
                        webpUrl = mediaPreview.media.webpUrl,
                        stickerId = mediaPreview.media.stickerId,
                        favoriteId = mediaPreview.media.favoriteId?.toLong()
                    )
                    onAction(ChatActions.SendFavoriteRecent(favoriteRecentModel, mediaPreview.type))
                }
            }
            when {
                mediaPreview.isAdded -> {
                    isShowRemoveFromFavorites = true
                    menu.addItem(
                        title = context.getString(R.string.remove_from_favorites),
                        icon = R.drawable.ic_delete_media_from_favorites,
                        color = R.color.color_reaction_default
                    ) {
                        onAction(ChatActions.RemoveFromFavorites(mediaPreview))
                    }
                }
                !mediaPreview.isAdded -> {
                    isShowAddToFavorites = true
                    menu.addItem(
                        title = R.string.add_to_favorites,
                        icon = R.drawable.ic_add_to_favorite
                    ) {
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
                }
            }
            if (mediaPreview.type == MediaPreviewType.RECENT) {
                isShowFromRecent = true
                menu.addItem(
                    title = context.getString(R.string.remove_from_recents),
                    icon = R.drawable.ic_delete_from_recents,
                    color = R.color.color_reaction_default
                ) {
                    mediaPreview.media.id?.let(deleteRecentClickListener)
                }
            }

            ivMediaPreview.setMargins(bottom = PREVIEW_MARGIN_SMALL.dp)
            vgVideoPreview.setMargins(bottom = PREVIEW_MARGIN_SMALL.dp)
            vgMediaPreview.visible()

            vMediaPreviewBackground.setThrottledClickListener {
                menu.dismiss()
                vgMediaPreview.gone()
            }
            menu.show(fragment.childFragmentManager)
        }
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun openDialogMoreMenu(companion: UserChat?) {
        val isCompanionNotBlocked = (companion?.blacklistedByMe?.toBoolean()?.not()) ?: true
        val actionMenuBuilder = fragment.context?.let { MoreMenuBottomSheetBuilder(it) }
        val companionUid = companion?.userId
        val actionMenu = actionMenuBuilder?.apply {
            if (isCompanionNotBlocked) {
                val canCompanionChat = (companion?.settingsFlags?.userCanChatMe?.toBoolean()) ?: true
                if (canCompanionChat) {
                    addForbidMessagesItem {
                        companionUid?.let { id ->
                            onAction(ChatActions.DisableChat(id))
                        }
                    }
                } else {
                    addAllowMessagesItem {
                        companionUid?.let { id ->
                            onAction(ChatActions.EnableChat(id))
                        }
                    }
                }
                addBlockUserItem {
                    onAction(ChatActions.ClearMessageEditor)
                    companion?.userId?.let { id ->
                        onAction(ChatActions.BlockUser(id))
                    }
                }
                addBlockReportItem {
                    onAction(ChatActions.BlockReportUserFromChat)
                }
            } else {
                addUnblockUserItem {
                    companionUid?.let { id ->
                        onAction(ChatActions.UnblockUser(id))
                    }
                }
                addReportUserItem {
                    onAction(ChatActions.OpenChatComplaintMenu)
                }
            }
        }?.build()
        actionMenu?.show(fragment.childFragmentManager)
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun openForbidChatRequestMenu() {
        MeeraMenuBottomSheet(fragment.context).apply {
            addItem(
                title = R.string.profile_dots_menu_disallow_messages,
                icon = R.drawable.ic_disallow_message,
                click = { onAction(ChatActions.ForbidCompanionToChat) }
            )
            addItem(
                title = R.string.general_block,
                icon = R.drawable.ic_block_user_red,
                click = { onAction(ChatActions.BlockCompanionFromChatRequest) }
            )
            addItem(
                title = R.string.complaints_block_and_report_user,
                icon = R.drawable.ic_report_profile,
                click = { onAction(ChatActions.OpenChatComplaintRequestMenu) }
            )
        }.show(fragment.childFragmentManager)
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
                    .setNegativeButton(ctx.getString(R.string.chat_message_edit_failed_accept), null)
                    .show()
            }
        )
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun showGroupChatSettingsDialog(
        roomId: Long?,
        ownUserId: Long,
        room: DialogEntity?
    ) {
        val creatorId = room?.creator?.userId
        val menu = MeeraMenuBottomSheet(fragment.context)
        if (ownUserId != creatorId) {
            // TODO uncomment later https://nomera.atlassian.net/browse/BR-21337
//            menu.addItem(R.string.menu_chat_leave_member, R.drawable.ic_group_leave) {
//                leaveChatDialog(roomId)
//            }
            menu.addItem(R.string.menu_chat_leave_and_delete_creator, R.drawable.ic_group_exit_delete) {
                deleteAndLeaveChatDialog(roomId)
            }
        } else {
            menu.addItem(R.string.general_delete, R.drawable.ic_group_exit_delete) {
                startDeleteChatGroup(roomId)
            }
        }
        checkFeatureGroupChatComplaintsAvailable(
            room = room,
            ownUserId = ownUserId,
            isFeatureAvailable = {
                menu.addItem(R.string.general_complaint, R.drawable.ic_report_profile) {
                    onAction(ChatActions.ComplaintGroupChat(roomId))
                }
            }
        )

        menu.show(fragment.childFragmentManager)
    }

    fun deleteMessageDialog(
        message: MessageEntity?,
        ownUserId: Long,
        onClickRemove: () -> Unit
    ) {
        val ctx = fragment.requireContext()
        if (message?.creator?.userId == ownUserId) {
            ConfirmDialogBuilder()
                .setHeader(ctx.getString(R.string.chat_remove_message))
                .setDescription(ctx.getString(R.string.confirm_delete_messgage_desc))
                .setTopBtnText(ctx.getString(R.string.delete_from_myself))
                .setVertical(true)
                .setMiddleBtnText(ctx.getString(R.string.delete_from_everyone))
                .setBottomBtnText(ctx.getString(R.string.remove_subscriber_dialog_cancel))
                .setTopClickListener {
                    onClickRemove.invoke()
                    onAction(
                        ChatActions.RemoveMessage(
                            message = message,
                            isBoth = false
                        )
                    )
                }
                .setMiddleClickListener {
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
            ConfirmDialogBuilder()
                .setHeader(ctx.getString(R.string.chat_remove_message))
                .setDescription(ctx.getString(R.string.chat_remove_message_for_you))
                .setLeftBtnText(ctx.getString(R.string.remove_subscriber_dialog_cancel))
                .setRightBtnText(ctx.getString(R.string.remove_subscriber_dialog_delete))
                .setRightClickListener {
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
        val ctx = fragment.requireContext()
        if (isMessageEditActive) {
            checkAppRedesigned(
                isRedesigned = {
                   showMeeraConfirmDialog(
                       headerText = R.string.chat_edit_message_abort_title,
                       descriptionText = R.string.chat_edit_message_abort_message,
                       onOkButtonClick = {
                           onAction(ChatActions.ClearMessageEditor)
                           onEditAborted.invoke()
                       }
                   )
                },
                isNotRedesigned = {
                    AlertDialog.Builder(ctx)
                        .setCancelable(false)
                        .setTitle(ctx.getString(R.string.chat_edit_message_abort_title))
                        .setMessage(ctx.getString(R.string.chat_edit_message_abort_message))
                        .setPositiveButton(ctx.getString(R.string.chat_edit_message_abort_allow)) { _, _ ->
                            onAction(ChatActions.ClearMessageEditor)
                            onEditAborted.invoke()
                        }
                        .setNegativeButton(ctx.getString(R.string.chat_edit_message_abort_deny)) { _, _ -> }
                        .show()
                }
            )
        } else {
            onEditAborted.invoke()
        }
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    private fun deleteAndLeaveChatDialog(roomId: Long?) {
        val ctx = fragment.requireContext()
        ConfirmDialogBuilder()
            .setHeader(ctx.getString(R.string.dialog_are_you_sure_to_leave_and_remove_chat_title))
            .setDescription(ctx.getString(R.string.dialog_are_you_sure_to_leave_and_remove_chat_content))
            .setLeftBtnText(ctx.getString(R.string.dialog_are_you_sure_to_leave_and_remove_chat_confirmation))
            .setLeftClickListener {
                onAction(ChatActions.RemoveRoom(
                    roomId = roomId,
                    isBoth = false,
                    isGroupChat = true
                ))
            }
            .setRightBtnText(ctx.getString(android.R.string.cancel))
            .show(fragment.childFragmentManager)
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    fun showCompletelyRemoveMessageDialog(roomId: Long, message: MessageEntity) {
        MeeraMenuBottomSheet(fragment.context).apply {
            addItem(R.string.message_delete_txt, R.drawable.ic_chat_delete_message) {
                onAction(ChatActions.CompletelyRemoveMessage(roomId, message.msgId))
            }
        }.show(fragment.childFragmentManager)
    }

    @InRedesignExists(description = "Редизайн в классе MeeraBaseBottomSheetDialogMenu")
    private fun startDeleteChatGroup(roomId: Long?) {
        val ctx = fragment.requireContext()
        ConfirmDialogBuilder()
            .setHeader(ctx.getString(R.string.rooms_delete_title))
            .setDescription(ctx.getString(R.string.rooms_delete_room))
            .setLeftBtnText(ctx.getString(R.string.delete))
            .setLeftClickListener {
                onAction(ChatActions.RemoveRoom(
                    roomId = roomId,
                    isBoth = false,
                    isGroupChat = true
                ))
            }
            .setRightBtnText(ctx.getString(android.R.string.cancel))
            .show(fragment.childFragmentManager)
    }

    private fun checkFeatureGroupChatComplaintsAvailable(
        room: DialogEntity?,
        ownUserId: Long,
        isFeatureAvailable: () -> Unit
    ) {
        if (featureToggles.chatGroupComplaintsFeatureToggle.isEnabled
            && ownUserId != room?.creator?.userId
            && room?.blocked == false
        ) {
            isFeatureAvailable()
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
