package com.numplates.nomera3.presentation.viewmodel.viewevents

import android.view.View
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.helpers.ChatBottomMenuPayload
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel

sealed class ChatMediaKeyboardEvent {
    data object ExpandMediaKeyboardEvent : ChatMediaKeyboardEvent()
    data object CheckMediaKeyboardBehavior : ChatMediaKeyboardEvent()
    data class ShowMediaPreview(
        val mediaPreview: MediaPreviewUiModel? = null,
        val message: MessageEntity? = null,
        val view: View? = null,
        val bottomMenuPayload: ChatBottomMenuPayload? = null,
        val deleteRecentClickListener: (Int) -> Unit = {}
    ) : ChatMediaKeyboardEvent()
    data class OnStickersLoaded(
        val stickerPacks: List<MediaKeyboardStickerPackUiModel>,
        val recentStickers: List<MediakeyboardFavoriteRecentUiModel>
        ): ChatMediaKeyboardEvent()
    class OnSendFavoriteRecent(
        val favoriteRecentUiModel: MediakeyboardFavoriteRecentUiModel,
        val type: MediaPreviewType
    ) : ChatMediaKeyboardEvent()
}
