package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel

data class MediaKeyboardRecentAdapterItem(
    val type: ItemType,
    val model: MediakeyboardFavoriteRecentUiModel? = null
) {

    enum class ItemType {
        SHIMMER, RECENT
    }

}
