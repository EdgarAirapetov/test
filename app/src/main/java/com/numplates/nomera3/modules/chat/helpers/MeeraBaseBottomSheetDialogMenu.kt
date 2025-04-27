package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.dialog.userRole
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.userprofile.UserRole
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.cell.CellLeftElement
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.databinding.MeeraClickMessageDialogBinding
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import kotlin.properties.Delegates


private const val MEERA_CHAT_BASE_BOTTOM_SHEET_DIALOG_MENU_TAG = "MeeraBaseBottomSheetDialogMenu"


class MeeraBaseBottomSheetDialogMenu : UiKitBottomSheetDialog<MeeraClickMessageDialogBinding>() {

    private var onClickAction: (ChatActions?) -> Unit = {}
    private var data: MeeraClickMessageDialogData by Delegates.notNull()
    private val items = mutableListOf<MeeraClickMessageDialogCellItem>()
    private var dismissListener: MeeraMenuBottomSheet.Listener? = null
    private val errorTitleColor = R.color.uiKitColorAccentWrong

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraClickMessageDialogBinding
        get() = MeeraClickMessageDialogBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dismissListener = context as? MeeraMenuBottomSheet.Listener ?: parentFragment as? MeeraMenuBottomSheet.Listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        dismissListener?.onDismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        items.clear()
        val menuItems = when (data.menuType) {
            MeeraBaseBottomSheetDialogType.CLICK_MESSAGE -> configureClickMessageMenu(data)
            MeeraBaseBottomSheetDialogType.FORBID_CHAT_REQUEST_MENU -> configureForbidMenu()
            MeeraBaseBottomSheetDialogType.DIALOG_MORE -> configureDialogMoreMenu(data)
            MeeraBaseBottomSheetDialogType.GROUP_MORE -> configureGroupMoreMenu(data)
            MeeraBaseBottomSheetDialogType.COMPLETE_REMOVE_MESSAGE -> configureCompleteRemoveMessageMenu(data)
        }
        showMenuFromConfiguration(menuItems)
    }

    fun show(
        fm: FragmentManager,
        data: MeeraClickMessageDialogData,
        onClickAction: (ChatActions?) -> Unit
    ): MeeraBaseBottomSheetDialogMenu {
        val dialog = MeeraBaseBottomSheetDialogMenu()
        dialog.data = data
        dialog.onClickAction = onClickAction
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_CHAT_BASE_BOTTOM_SHEET_DIALOG_MENU_TAG)
        return dialog
    }

    private fun configureClickMessageMenu(data: MeeraClickMessageDialogData): List<MeeraClickMessageDialogCellItem> {
        val message = data.message ?: error("Message data must not be null")
        val room = data.room ?: error("Room data must not be null")

        if (data.isUploadMediaProgress) {
            setDownloadMenuItem(message)
            setDeleteMenuItem(room, message)
            return items
        }
        if (message.isNeedToResend()) {
            setResendMenuItem(
                message = message,
                unsentMessageCounter = data.unsentMessageCounter
            )
        } else {
            setReplyMenuItem(room, message)
            setFavoritesMenuItem(
                message = message,
                mediaPreview = data.mediaPreview
            )
            setDownloadMenuItem(message)
            setForwardMenuItem(room, message)
        }
        setCopyMenuItem(message)
        setEditMenuItem(room, message)
        setSharingMenuItem(message)
        setDeleteMenuItem(room, message)
        setupMediaPreviewIfRequired(message)
        return items
    }

    private fun setupMediaPreviewIfRequired(message: MessageEntity) {
        val media = data.mediaPreview?.media
        val isMsgMediaOrSticker = message.isMediaMessage() || message.isStickerMessage()
        if (isMsgMediaOrSticker && media != null) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val heightOfTopMenuPart = 64.dp
            val heightOfOneMenuItem = 48.dp
            val approxHeight = heightOfTopMenuPart + (items.size * heightOfOneMenuItem)
            val chatAction = ChatActions.SetupMediaPreview(
                media = media,
                isMeeraMenu = true,
                menuHeight = approxHeight
            )
            onClickAction.invoke(chatAction)
        }
    }

    private fun configureForbidMenu(): List<MeeraClickMessageDialogCellItem> {
        addMenuItem(
            R.string.profile_dots_menu_disallow_messages,
            R.drawable.ic_outlined_message_off_m,
            action = ChatActions.ForbidCompanionToChat
        )
        addMenuItem(
            R.string.general_block,
            R.drawable.ic_outlined_user_block_m,
            action = ChatActions.BlockCompanionFromChatRequest
        )
        addMenuItem(
            R.string.complaints_block_and_report_user,
            R.drawable.ic_outline_attention_m,
            action = ChatActions.OpenChatComplaintRequestMenu
        )
        return items
    }

    private fun configureDialogMoreMenu(data: MeeraClickMessageDialogData): List<MeeraClickMessageDialogCellItem> {
        val companion = data.companion
        val isCompanionNotBlocked = (companion?.blacklistedByMe?.toBoolean()?.not()) ?: true
        val companionUid = companion?.userId ?: error("Companion UserId must not be null")

        if (isCompanionNotBlocked) {
            configureDialogMoreMenuIfCanCompanionChat(companion, companionUid)
            onClickAction.invoke(ChatActions.ClearMessageEditor)
            addMenuItem(
                R.string.general_block,
                R.drawable.ic_outlined_user_block_m,
                action = ChatActions.BlockUser(companionUid),
                titleColor = errorTitleColor
            )
            addMenuItem(
                R.string.complaints_block_and_report_user,
                R.drawable.ic_outline_attention_m,
                action = ChatActions.BlockReportUserFromChat,
                titleColor = errorTitleColor
            )
        } else {
            addMenuItem(
                R.string.general_unblock,
                R.drawable.ic_outlined_user_m,
                action = ChatActions.UnblockUser(companionUid)
            )
            addMenuItem(
                R.string.user_complain,
                R.drawable.ic_outline_attention_m,
                action = ChatActions.OpenChatComplaintMenu,
                titleColor = errorTitleColor
            )
        }
        return items
    }

    private fun configureDialogMoreMenuIfCanCompanionChat(companion: UserChat, companionUid: Long) {
        val canCompanionChat = (companion.settingsFlags?.userCanChatMe?.toBoolean()) ?: true
        if (canCompanionChat) {
            addMenuItem(
                R.string.profile_dots_menu_disallow_messages,
                R.drawable.ic_outlined_message_off_m,
                action = ChatActions.DisableChat(companionUid),
                titleColor = errorTitleColor
            )
        } else {
            addMenuItem(
                R.string.profile_dots_menu_allow_messages,
                R.drawable.ic_outlined_message_m,
                action = ChatActions.EnableChat(companionUid)
            )
        }
    }

    private fun configureGroupMoreMenu(data: MeeraClickMessageDialogData): List<MeeraClickMessageDialogCellItem> {
        val roomId = data.room?.roomId
        val creatorId = data.room?.creator?.userId
        val isFeatureGroupComplaintsAvailable = data.featureToggles?.chatGroupComplaintsFeatureToggle?.isEnabled == true
            && data.ownUserId != data.room?.creator?.userId
            && data.room?.blocked == false

        val deleteCellPosition = if (isFeatureGroupComplaintsAvailable) CellPosition.TOP else CellPosition.ALONE
        if (data.ownUserId != creatorId) {
            addMenuItem(
                R.string.menu_chat_leave_and_delete_creator,
                R.drawable.ic_outlined_delete_m,
                cellPosition = deleteCellPosition,
                titleColor = errorTitleColor,
                onClick = {
                    showConfirmDeleteAndLeaveChatDialog {
                        onClickAction.invoke(
                            ChatActions.RemoveRoom(
                                roomId = roomId,
                                isBoth = false,
                                isGroupChat = true
                            )
                        )
                    }
                }
            )
        } else {
            addMenuItem(
                R.string.general_delete,
                R.drawable.ic_outlined_delete_m,
                cellPosition = deleteCellPosition,
                titleColor = errorTitleColor,
                onClick = {
                    showConfirmDeleteChatDialog {
                        onClickAction.invoke(
                            ChatActions.RemoveRoom(
                                roomId = roomId,
                                isBoth = false,
                                isGroupChat = true
                            )
                        )
                    }
                }
            )
        }
        if (isFeatureGroupComplaintsAvailable) {
            addMenuItem(
                R.string.general_complaint,
                R.drawable.ic_outline_attention_m,
                cellPosition = CellPosition.BOTTOM,
                titleColor = errorTitleColor,
                onClick = { onClickAction.invoke(ChatActions.ComplaintGroupChat(roomId)) }
            )
        }
        return emptyList()
    }

    private fun configureCompleteRemoveMessageMenu(
        data: MeeraClickMessageDialogData
    ): List<MeeraClickMessageDialogCellItem> {
        val roomId = data.message?.roomId ?: error("Room must not be null")
        val messageId = data.message.msgId
        addMenuItem(
            title = R.string.message_delete_txt,
            icon = R.drawable.ic_outlined_delete_m,
            cellPosition = CellPosition.ALONE,
            titleColor = errorTitleColor,
            onClick = {
                onClickAction.invoke(ChatActions.CompletelyRemoveMessage(roomId, messageId))
            }
        )
        return emptyList()
    }

    private fun showMenuFromConfiguration(menuItems: List<MeeraClickMessageDialogCellItem>) {
        menuItems.forEachIndexed { index, item ->
            val cell = UiKitCell(requireContext())
            item.title?.let { cell.setTitleValue(getString(item.title)) }
            item.titleString?.let { text -> cell.setTitleValue(text) }
            cell.setLeftIcon(item.icon)
            cell.cellLeftElement = CellLeftElement.ICON
            cell.cellBackgroundColor = R.color.uiKitColorBackgroundSecondary
            cell.cellPosition = when {
                index == 0 && menuItems.size == 1 -> CellPosition.ALONE
                index == 0 && menuItems.size > 1 -> CellPosition.TOP
                index == menuItems.size - 1 -> CellPosition.BOTTOM
                else -> CellPosition.MIDDLE
            }

            if (item.titleColor != null) cell.cellLeftIconAndTitleColor = item.titleColor
            cell.setThrottledClickListener {
                onClickAction.invoke(item.clickAction)
                dismiss()
            }
            contentBinding?.root?.addView(cell)
        }
    }

    private fun setResendMenuItem(
        message: MessageEntity,
        unsentMessageCounter: Int
    ) {
        val resendAction = ChatActions.ResendSingleMessage(message)
        addMenuItem(R.string.chat_resend_message, R.drawable.ic_outlined_resend_m, resendAction)
        if (unsentMessageCounter > 1) {
            val title = getString(R.string.chat_resend_some_messages, unsentMessageCounter)
            addMenuItem(
                title = null,
                icon = R.drawable.ic_outlined_resend_m,
                action = resendAction,
                titleString = title
            )
        }
    }

    private fun setFavoritesMenuItem(
        message: MessageEntity,
        mediaPreview: MediaPreviewUiModel? = null
    ) {
        val isMsgMediaOrSticker = message.isMediaMessage() || message.isStickerMessage()
        when {
            isMsgMediaOrSticker && mediaPreview?.isAdded == true -> {
                val action = ChatActions.RemoveFromFavorites(mediaPreview)
                addMenuItem(
                    R.string.remove_from_favorites,
                    R.drawable.ic_outlined_fav_off_m,
                    action,
                    errorTitleColor
                )
            }

            isMsgMediaOrSticker -> {
                val lottieUrl = (mediaPreview?.media as? MediaUiModel.StickerMediaUiModel?)?.lottieUrl
                val action = ChatActions.AddToFavoritesToMessage(
                    message = message,
                    lottieUrl = lottieUrl
                )
                addMenuItem(R.string.add_to_favorites, R.drawable.ic_outlined_fav_m, action)
            }
        }
    }

    private fun setReplyMenuItem(room: DialogEntity, message: MessageEntity) {
        if (message.isValidForReply() && message.sent) {
            val isPersonalChatAllowed = isPersonalChatBlocked(room).not()
            val action = if (isPersonalChatAllowed && room.blocked == false) ChatActions.ReplyMessage(message) else null
            addMenuItem(R.string.road_reply_comment, R.drawable.ic_outlined_reply_m, action)
        }
    }

    private fun setDownloadMenuItem(message: MessageEntity) {
        if (message.isMediaMessage()) {
            val downloadAction = ChatActions.DownloadImageVideoAttachment(message)
            addMenuItem(R.string.save_image, R.drawable.ic_outlined_download_m, downloadAction)
        }
    }

    private fun setForwardMenuItem(room: DialogEntity, message: MessageEntity) {
        if (message.isValidForForwarding()) {
            val action = if (roomIsNotBlocked(room)) ChatActions.OnMessageForward(message) else null
            addMenuItem(R.string.general_forward, R.drawable.ic_outlined_repost_m, action)
        }
    }

    private fun setCopyMenuItem(message: MessageEntity) {
        if (message.isImageOrGifMessage() && message.tagSpan?.text?.isBlank() == true) {
            val action = ChatActions.CopyImageAttachment(message)
            addMenuItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m, action)
        } else if (message.isValidForCopy()) {
            val action = ChatActions.CopyMessageContent(message, data.messageView)
            addMenuItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m, action)
        } else if (message.attachment.type == TYPING_TYPE_AUDIO && !data.menuPayload?.voiceRecognizedText.isNullOrBlank()) {
            val action = ChatActions.CopyMessageContent(message, data.messageView)
            addMenuItem(R.string.text_copy_txt, R.drawable.ic_outlined_copy_m, action)
        }
    }

    private fun setEditMenuItem(room: DialogEntity, message: MessageEntity) {
        if (data.isEditMessageAvailable) {
            val action = if (roomIsNotBlocked(room)) ChatActions.MessageEdit(message) else null
            addMenuItem(R.string.chat_edit_message, R.drawable.ic_outlined_edit_m, action)
        }
    }

    private fun setSharingMenuItem(message: MessageEntity) {
        if (message.isValidForContentSharing()) {
            val action = ChatActions.ShareMessageContent(message)
            addMenuItem(R.string.image_share, R.drawable.ic_outlined_share_m, action)
        }
    }

    private fun setDeleteMenuItem(room: DialogEntity, message: MessageEntity) {
        if (room.companion.userRole != UserRole.SUPPORT_USER) {
            val action = ChatActions.MessageDelete(message)
            addMenuItem(R.string.message_delete_txt, R.drawable.ic_outlined_delete_m, action, errorTitleColor)
        }
    }

    private fun addMenuItem(
        @StringRes title: Int?,
        @DrawableRes icon: Int,
        action: ChatActions?,
        @ColorRes titleColor: Int? = null,
        titleString: String? = null
    ) {
        items.add(
            MeeraClickMessageDialogCellItem(
                icon = icon,
                title = title,
                titleString = titleString,
                clickAction = action,
                titleColor = titleColor
            )
        )
    }

    private fun addMenuItem(
        @StringRes title: Int?,
        @DrawableRes icon: Int,
        cellPosition: CellPosition,
        @ColorRes titleColor: Int? = null,
        titleString: String? = null,
        onClick: () -> Unit = {}
    ) {
        val cell = UiKitCell(requireContext())
        title?.let { cell.setTitleValue(getString(title)) }
        titleString?.let { text -> cell.setTitleValue(text) }
        cell.setLeftIcon(icon)
        cell.cellLeftElement = CellLeftElement.ICON
        cell.cellBackgroundColor = R.color.uiKitColorBackgroundSecondary
        cell.cellPosition = cellPosition
        if (titleColor != null) cell.cellLeftIconAndTitleColor = titleColor
        cell.setThrottledClickListener { onClick.invoke() }
        contentBinding?.root?.addView(cell)
    }

    private fun isPersonalChatBlocked(room: DialogEntity?): Boolean {
        val companion = room?.companion
        return companion?.blacklistedMe == true.toInt()
            || companion?.blacklistedByMe == true.toInt()
            || companion?.settingsFlags?.iCanChat != true.toInt()
            || companion.settingsFlags?.userCanChatMe != true.toInt()
            || companion.settingsFlags?.isInChatBlackList == true.toInt()
    }

    private fun roomIsNotBlocked(room: DialogEntity) = room.blocked == false

    private fun MessageEntity.isNeedToResend(): Boolean = !this.sent && this.isResendAvailable

    private fun showConfirmDeleteAndLeaveChatDialog(onClick: () -> Unit) {
        showConfirmDialog(
            headerText = R.string.dialog_are_you_sure_to_leave_and_remove_chat_title,
            descriptionText = R.string.dialog_are_you_sure_to_leave_and_remove_chat_content,
            onClick = onClick
        )
    }

    private fun showConfirmDeleteChatDialog(onClick: () -> Unit) {
        showConfirmDialog(
            headerText = R.string.rooms_delete_title,
            descriptionText = R.string.rooms_delete_room,
            onClick = onClick
        )
    }

    private fun showConfirmDialog(
        @StringRes headerText: Int,
        @StringRes descriptionText: Int,
        onClick: () -> Unit
    ) {
        MeeraConfirmDialogBuilder()
            .setHeader(headerText)
            .setDescription(descriptionText)
            .setTopBtnText(R.string.yes)
            .setBottomBtnText(R.string.no)
            .setCancelable(true)
            .setTopClickListener {
                onClick()
                dismiss()
            }
            .setBottomClickListener { dismiss() }
            .show(childFragmentManager)
    }

}

data class MeeraClickMessageDialogData(
    val room: DialogEntity? = null,
    val message: MessageEntity? = null,
    val messageView: View? = null,
    val unsentMessageCounter: Int = 0,
    val mediaPreview: MediaPreviewUiModel? = null,
    val isEditMessageAvailable: Boolean = false,
    val menuPayload: ChatBottomMenuPayload? = null,
    val isUploadMediaProgress: Boolean = false,
    val companion: UserChat? = null,
    val ownUserId: Long? = null,
    val featureToggles: FeatureTogglesContainer? = null,
    val menuType: MeeraBaseBottomSheetDialogType = MeeraBaseBottomSheetDialogType.CLICK_MESSAGE
)

data class MeeraClickMessageDialogCellItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int? = null,
    val titleString: String? = null,
    val clickAction: ChatActions? = null,
    @ColorRes val titleColor: Int? = null,
)

enum class MeeraBaseBottomSheetDialogType {
    CLICK_MESSAGE,
    FORBID_CHAT_REQUEST_MENU,
    DIALOG_MORE,
    GROUP_MORE,
    COMPLETE_REMOVE_MESSAGE
}
