package com.numplates.nomera3.modules.gifservice.ui.entity

import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyEmoji
import com.meera.core.extensions.empty

data class GifEmojiEntity(
    val itemType: GifEmojiItemType = GifEmojiItemType.TEXT,
    val emojiQuery: GiphyEmoji = GiphyEmoji.SEARCH,
    val emojiText: String? = String.empty(),
    @DrawableRes val emojiDrawableRes: Int? = null,
    var isSelected: Boolean = false
)

enum class GifEmojiItemType(val key: Int) {
    TEXT(1), IMAGE(2)
}