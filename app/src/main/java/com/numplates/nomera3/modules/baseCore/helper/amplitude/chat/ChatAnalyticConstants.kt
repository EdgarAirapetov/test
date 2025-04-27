package com.numplates.nomera3.modules.baseCore.helper.amplitude.chat

import com.meera.application_api.analytic.model.AmplitudeName

enum class ChatAmplitudeEventName(private val event: String) : AmplitudeName {

    /**
     * Пользователь тапнул на кнопку "Показать" на заблюренной картинке в запросах на переписку
     */
    BLUR_MEDIA_SHOW("blur media show"),
    UNSENT_MESSAGE_COPY("unsent message copy"),
    MESSAGE_RESEND_MENU_SHOWED("message resend menu open"),
    MESSAGE_RESEND("message resend"),
    UNSENT_MESSAGE_DELETED("unsent message delete"),
    ALL_MESSAGES_RESEND("all message resend");

    override val eventName: String
        get() = event
}

object ChatAmplitudeConstants {
    const val PHOTO_COUNT = "photo count"
    const val VIDEO_COUNT = "video count"
    const val GIF_COUNT = "gif count"
    const val RESEND_MESSAGES_COUNT = "message count"
    const val HAVE_TEXT = "have text"
    const val HAVE_PIC = "have pic"
    const val HAVE_VIDEO = "have video"
    const val HAVE_GIFT = "have gift"
    const val HAVE_AUDIO = "have audio"
    const val HAVE_MEDIA = "have media"
    const val GROUP_CHAT = "group chat"
    const val FROM = "from"
    const val TO = "to"
}
