package com.numplates.nomera3.modules.gifservice.ui

import android.net.Uri
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel

sealed class GifMenuDelegateEvents {
    object OnShowSoftwareKeyboard : GifMenuDelegateEvents()
    object OnHideSoftwareKeyboard : GifMenuDelegateEvents()

    object OnDialogExpanded : GifMenuDelegateEvents()
    object OnDialogStateCollapsed : GifMenuDelegateEvents()

    data class OnDisplayGifMenuIcon(val showNewStickerPacksIcon: Boolean) : GifMenuDelegateEvents()
    object OnDisplayKeyboardIcon : GifMenuDelegateEvents()
    class OnClickGif(val gifs: List<Uri>, val aspect: Double) : GifMenuDelegateEvents()
    class OnKeyboardHeightChanged(val height: Int) : GifMenuDelegateEvents()
    data class OnStickerPackViewed(val stickerPack: MediaKeyboardStickerPackUiModel) : GifMenuDelegateEvents()
}
