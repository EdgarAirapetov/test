package com.numplates.nomera3.modules.chat.ui.action

import android.net.Uri
import android.view.View
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.holidays.ui.entity.RoomType


sealed interface ChatActions {
    class SetupChat(val data: ChatInitData?): ChatActions
    class SetRoomData(val room: DialogEntity): ChatActions
    object LoadStickerPacks: ChatActions
    class PrepareRoomAfterUpdateRoomData(val roomId: Long): ChatActions
    class LoadMessages(val room: DialogEntity): ChatActions
    class SetupMediaPreview(val media: MediaUiModel, val isMeeraMenu: Boolean = false, val menuHeight: Int? = null): ChatActions
    class OnEditorOpen(val uri: Uri) : ChatActions
    class OnPhotoEdits(val nmrAmplitude: NMRPhotoAmplitude?): ChatActions
    class OnVideoEdits(val nmrAmplitude: NMRVideoAmplitude?): ChatActions
    class SendFavoriteRecent(val favoriteRecentUiModel: MediakeyboardFavoriteRecentUiModel, val type: MediaPreviewType): ChatActions
    object BlockReportUserFromChat: ChatActions
    object OpenChatComplaintRequestMenu: ChatActions
    object OpenChatComplaintMenu: ChatActions
    object ForbidCompanionToChat: ChatActions
    object BlockCompanionFromChatRequest: ChatActions
    class OnMessageForward(val message: MessageEntity): ChatActions
    class ReplyMessage(val message: MessageEntity): ChatActions
    class ResendSingleMessage(val message: MessageEntity): ChatActions
    class ResendAllMessages(val unsentMessageCounter: Int): ChatActions
    class DownloadImageVideoAttachment(val message: MessageEntity): ChatActions
    class CopyImageAttachment(val message: MessageEntity): ChatActions
    class ShareMessageContent(val message: MessageEntity): ChatActions
    class CopyMessageContent(val message: MessageEntity, val messageView: View?): ChatActions
    class MessageEdit(val message: MessageEntity): ChatActions
    class MessageDelete(val message: MessageEntity): ChatActions
    class RemoveMessage(val message: MessageEntity?, val isBoth: Boolean): ChatActions
    class CompletelyRemoveMessage(val roomId: Long, val messageId: String): ChatActions

    class RemoveOnlyNetworkMessage(val roomId: Long, val messageId: String): ChatActions
    class AddToFavoritesToMessage(val message: MessageEntity, val lottieUrl: String?): ChatActions
    class AddToFavorites(val mediaPreview: MediaPreviewUiModel, val mediaUrl: String, val lottieUrl: String?): ChatActions
    class RemoveFromFavorites(val mediaPreview: MediaPreviewUiModel): ChatActions
    object UnsentCopyMessageClicked: ChatActions
    class UnsentMessageDelete(val message: MessageEntity, val roomType: String, val companion: UserChat?): ChatActions
    object ClearMessageEditor: ChatActions
    class EnableChat(val companionUid: Long): ChatActions
    class DisableChat(val companionUid: Long): ChatActions
    class BlockUser(val companionUid: Long): ChatActions
    class UnblockUser(val companionUid: Long): ChatActions
    object SendEditedMessageWithConditionsCheck: ChatActions
    class RemoveRoom(val roomId: Long?, val isBoth: Boolean, val isGroupChat: Boolean): ChatActions
    class OnSetChatBackground(val room: DialogEntity?, val user: UserChat?, val roomType: RoomType): ChatActions
    object InitGreetings : ChatActions
    class SendFakeMessages(val roomId: Long?, val messageText: String): ChatActions
    class ComplaintGroupChat(val roomId: Long?): ChatActions
    class PlayVoiceMessages(val message: MessageEntity?, val position: Int): ChatActions
    class PlayMeeraVoiceMessage(val message: MessageUiModel?, val position: Int): ChatActions
    class InProgressResendSendMessage(val roomId: Long?): ChatActions
    class ShareContent(val types: ShareContentTypes): ChatActions
    class CopyImageMessageAttachment(val message: MessageEntity, val attachmentsIndex: Int): ChatActions
    class OnFavoriteRecentLongClick(
        val model: MediakeyboardFavoriteRecentUiModel,
        val type: MediaPreviewType,
        val deleteRecentClickListener: (Int) -> Unit
    ) : ChatActions
    object ReloadRecentStickers : ChatActions

    class MapAttachmentsData(val attachments: List<String>): ChatActions
}


sealed class ShareContentTypes {
    class TextContent(val message: MessageEntity): ShareContentTypes()
    class SingleMedia(val message: MessageEntity, val isShareText: Boolean): ShareContentTypes()
    class MultipleMedias(val message: MessageEntity): ShareContentTypes()
}
