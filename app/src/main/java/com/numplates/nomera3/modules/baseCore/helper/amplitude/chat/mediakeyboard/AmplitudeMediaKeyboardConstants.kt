package com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeMediaKeyboardHowProperty(
    private val property: String
) : AmplitudeProperty {
    BUTTON_ADD_MEDIA("button add media"),
    BUTTON_MEDIA_KEYBOARD("button media keyboard"),
    SWIPE("swipe"),
    ICON("icon"),
    TAP_BUTTON("tap button");

    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudeMediaKeyboardConstants.HOW
}

enum class AmplitudeMediaKeyboardWhereProperty(
    private val property: String
) : AmplitudeProperty {
    GIF_BLOCK("gif block"),
    GIF_SEARCH("gif search"),
    CATEGORIES_GIF("categories gif"),
    MEDIA_BLOCK("media block"),
    GIF("gif");

    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudeMediaKeyboardConstants.WHERE
}

enum class AmplitudeMediaKeyboardDefaultBlockProperty(
    private val property: String
) : AmplitudeProperty {
    MEDIA_BLOCK("media block"),
    GIF("gif");

    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudeMediaKeyboardConstants.DEFAULT_BLOCK

}

enum class AmplitudeMediaKeyboardFavoriteWhereProperty(
    private val property: String
) : AmplitudeProperty {
    CHAT_SCREEN("chat screen"),
    CHAT_MEDIA_SCREEN("chat media screen"),
    GIPHY("giphy"),
    RECENT_STICKERS("recent stickers"),
    STICKERPACK("stickerpack"),
    FAVORITE("favorite"),
    OTHER("other");


    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudeMediaKeyboardConstants.WHERE

}

enum class AmplitudeMediaKeyboardMediaTypeProperty(
    private val property: String
) : AmplitudeProperty {
    PHOTO("photo"),
    VIDEO("video"),
    GIF_GALLERY("gif gallery"),
    GIF_GIPHY("gif giphy"),
    STICKER("sticker");


    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudeMediaKeyboardConstants.MEDIA_TYPE

}

enum class AmplitudeMediaKeyboardEventName(
    private val event: String
) : AmplitudeName {
    MEDIA_PANEL_OPEN("media panel open"),
    MEDIA_PANEL_CLOSE("media panel close"),
    MEDIA_PANEL_MEDIA_OPEN("mp media open"),
    MEDIA_PANEL_GIF_OPEN("mp gif open"),
    GIF_SEND("gif send"),
    PANEL_PULL("panel pull"),
    MEDIA_PANEL_FAVORITE_ADD("mp favorite add"),
    MEDIA_PANEL_FAVORITE_DELETE("mp favorite delete"),
    MEDIA_PANEL_RECENT_STICKER_DELETE("mp recent stickers delete"),
    MEDIA_PANEL_RECENT_ONE_STICKER_DELETE("mp recent one sticker delete");

    override val eventName: String
        get() = event
}

object AmplitudeMediaKeyboardConstants {
    const val HOW = "how"
    const val WHERE = "where"
    const val DEFAULT_BLOCK = "default block"
    const val MEDIA_TYPE = "media type"

    const val USER_ID = "user id"
    const val STICKER_CATEGORY = "stiker category"
    const val STICKER_ID = "stiker id"
}
