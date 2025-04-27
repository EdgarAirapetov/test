package com.numplates.nomera3.presentation.viewmodel.viewevents

import android.content.Intent
import android.view.View
import com.meera.db.models.DraftUiModel
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.model.ChatBackgroundType
import com.numplates.nomera3.modules.chat.ui.model.PlayMeeraMessageDataModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.PlayMessageDataUiModel
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult
import java.util.LinkedList
import java.util.UUID

sealed class ChatMessageViewEvent {

    class OnRefreshRoomData(val room: DialogEntity?): ChatMessageViewEvent()

    object OnSuccessSentMessage : ChatMessageViewEvent()

    class ActionSendMessage(
        val messageId: String,
        val isSentError: Boolean,
        val resultMessage: String?
    ) : ChatMessageViewEvent()

    object ErrorRemoveMessage : ChatMessageViewEvent()

    object OnSuccessDeleteRoom : ChatMessageViewEvent()

    object OnFailureDeleteRoom : ChatMessageViewEvent()

    object OnFailureDeleteRoomNotCreatedYet : ChatMessageViewEvent()

    class OnLastInputMessage(val lastText: String) : ChatMessageViewEvent()

    object PlayReceivedMessage : ChatMessageViewEvent()

    object OnEventDeleteRoom : ChatMessageViewEvent()
    class OnEventDeletedFromRoom(val needToShow: Boolean) : ChatMessageViewEvent()

    // After call check rest messages set unread message count
    class SetUnreadRestMessageCount(val count: Long): ChatMessageViewEvent()

    data class OnScrollToMessage(val messageId: String): ChatMessageViewEvent()

    class OnSetUnsentMessages(val unsentMessageCount: Int): ChatMessageViewEvent()

    class UpdateCompanionAsUnblocked(val userId: Long): ChatMessageViewEvent()

    object OnFailEnableChatMessages : ChatMessageViewEvent()

    class UpdateBirthdayTextSpannable(val listRanges: List<IntRange>): ChatMessageViewEvent()

    object OnSuccessSubscribedToUser : ChatMessageViewEvent()
    object OnFailSubscribeToUser : ChatMessageViewEvent()

    class OnUpdateCompanionInfo(val companionData: UserChat): ChatMessageViewEvent()
    class OnWorkSubmitted(val workUuid: UUID): ChatMessageViewEvent()
    class OnEditMessageWorkSubmitted(val workUuid: UUID): ChatMessageViewEvent()
    class BlockUserResult(val isSuccess: Boolean): ChatMessageViewEvent()
    class BlockReportResult(val isBlockSuccess: Boolean, val reportResult: ComplaintFlowResult): ChatMessageViewEvent()
    class OnDraftFound(val draft: DraftUiModel): ChatMessageViewEvent()
    object OnEditMessageSuccess : ChatMessageViewEvent()
    class OnEditMessageError(val isEditTooLate: Boolean): ChatMessageViewEvent()
    class CheckEditedMediaItems(val urls: List<String>): ChatMessageViewEvent()
    class OnAddedToFavorites(val mediaUrl: String): ChatMessageViewEvent()
    class OnGetRoomDataById(val roomData: DialogEntity, val isChatTransitFromPush: Boolean = false): ChatMessageViewEvent()
    object StartMessageEditing: ChatMessageViewEvent()
    object FinishMessageEditing: ChatMessageViewEvent()

    class OnSetupAddToFavoritesAnimation(val lottieUrl: String?): ChatMessageViewEvent()
    object OnBlockCompanionFromChatRequest: ChatMessageViewEvent()
    object OnBlockReportUserFromChat: ChatMessageViewEvent()
    class OnCopyMessageContent(val message: MessageEntity, val messageView: View?): ChatMessageViewEvent()
    class OnDownloadImageVideoAttachment(val message: MessageEntity): ChatMessageViewEvent()
    class OnCopyImageAttachment(val message: MessageEntity): ChatMessageViewEvent()
    class OnShareMessageContent(val message: MessageEntity): ChatMessageViewEvent()
    object OnForbidCompanionToChat: ChatMessageViewEvent()
    class OnMessageDelete(val message: MessageEntity): ChatMessageViewEvent()
    class OnMessageEdit(val message: MessageEntity): ChatMessageViewEvent()
    class OnMessageForward(val message: MessageEntity): ChatMessageViewEvent()
    object OnOpenChatComplaintMenu: ChatMessageViewEvent()
    object OnOpenChatComplaintRequestMenu: ChatMessageViewEvent()
    class OnMessageReply(val message: MessageEntity): ChatMessageViewEvent()
    class ShowMessageReplyTooltip(val message: MessageUiModel): ChatMessageViewEvent()
    class OnResendAllMessages(val unsentMessageCounter: Int): ChatMessageViewEvent()
    class OnResendSingleMessage(val message: MessageEntity): ChatMessageViewEvent()
    class OnSendFavoriteRecent(val favoriteRecentUiModel: MediakeyboardFavoriteRecentUiModel, val type: MediaPreviewType): ChatMessageViewEvent()
    class OnSetupMediaPreview(val media: MediaUiModel, val isMeeraMenu: Boolean = false, val menuHeight: Int? = null): ChatMessageViewEvent()
    class OnSetChatBackground(val backgroundType: ChatBackgroundType): ChatMessageViewEvent()
    class OnGreetingStickerFound(val sticker: MediaKeyboardStickerUiModel?) : ChatMessageViewEvent()
    class ShowDialogComplaintGroupChat(val roomId: Long?): ChatMessageViewEvent()
    object OnGroupChatBlocked: ChatMessageViewEvent()

    class OnPlayVoiceMessage(
        val startPos: Int,
        val messagesQueue: LinkedList<PlayMessageDataUiModel>
    ): ChatMessageViewEvent()

    class OnPlayMeeraVoiceMessage(
        val startPos: Int,
        val messagesQueue: LinkedList<PlayMeeraMessageDataModel>
    ): ChatMessageViewEvent()

    object OnShowMaxSelectedMediaCountErrorMessage: ChatMessageViewEvent()

    object OnHideBottomDownloadMediaProgress: ChatMessageViewEvent()

    class OnShareContent(
        val intent: Intent,
        val isDismissProgress: Boolean = false
    ): ChatMessageViewEvent()

    object OnFailShareContent: ChatMessageViewEvent()

    class OnCopyAttachmentImageMessage(val attachment: MessageAttachment): ChatMessageViewEvent()
    class ShowMeeraCompletelyRemoveMessageDialog(val message: MessageEntity): ChatMessageViewEvent()

}
