package com.numplates.nomera3.modules.feed.ui.entity

enum class PostTypeEnum(val value: Int) {
    IMAGE(0),
    GIF(1),
    AUDIO(2),
    VIDEO(3),
    AVATAR_VISIBLE(4),
    AVATAR_HIDDEN(5),
    UNKNOWN(-1);

    companion object {
        fun valueOf(value: Int): PostTypeEnum {
            return when (value) {
                IMAGE.value -> IMAGE
                GIF.value -> GIF
                AUDIO.value -> AUDIO
                VIDEO.value -> VIDEO
                AVATAR_VISIBLE.value -> AVATAR_VISIBLE
                AVATAR_HIDDEN.value -> AVATAR_HIDDEN
                else -> UNKNOWN
            }
        }
    }
}
