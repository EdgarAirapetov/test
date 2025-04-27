package com.numplates.nomera3.modules.chat.mediakeyboard.data.entity

import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.gifservice.ui.DRAWABLE_WIDGETS
import com.numplates.nomera3.modules.gifservice.ui.DRAWABlE_RECENT_STICKERS

data class MediaKeyboardTab(
    @DrawableRes
    val drawableId: Int? = null,
    var checked: Boolean = false,
    var playAnimation: Boolean = false,
    val stickerPack: MediaKeyboardStickerPackUiModel? = null,
    val isDivider: Boolean = false
) {
    val isRecentStickersTab
        get() = drawableId == DRAWABlE_RECENT_STICKERS

    val isWidgetsTab
        get() = drawableId == DRAWABLE_WIDGETS
}

val MediaKeyboardTab.isRegularTab
    get() = drawableId != null && drawableId != DRAWABlE_RECENT_STICKERS

val MediaKeyboardTab.isStickerPackTab
    get() = stickerPack != null
