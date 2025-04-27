package com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

private const val DEFAULT_VALUE = 0

interface AmplitudeMediaKeyboardAnalytic {
    fun logMediaPanelOpen(how: AmplitudeMediaKeyboardHowProperty, defaultBlock: AmplitudeMediaKeyboardDefaultBlockProperty)
    fun logMediaPanelClose(how: AmplitudeMediaKeyboardHowProperty)
    fun logMediaPanelMediaOpen(how: AmplitudeMediaKeyboardHowProperty)
    fun logMediaPanelGifOpen(how: AmplitudeMediaKeyboardHowProperty)
    fun logGifSend(where: AmplitudeMediaKeyboardWhereProperty)
    fun logPanelPull(where: AmplitudeMediaKeyboardWhereProperty)
    fun logAddFavorite(
        where: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        userId: Long,
        stickerCategory: String? = null,
        stickerId: Int? = null
    )
    fun logDeleteFavorite(
        where: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        userId: Long,
        stickerCategory: String? = null,
        stickerId: Int? = null
    )
    fun logClearRecentStickers()
    fun logDeleteRecentSticker(stickerId: Int)
}

class AmplitudeMediaKeyboardAnalyticImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMediaKeyboardAnalytic {

    override fun logMediaPanelOpen(
        how: AmplitudeMediaKeyboardHowProperty,
        defaultBlock: AmplitudeMediaKeyboardDefaultBlockProperty
    ) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_OPEN,
            properties = {
                it.apply {
                    addProperty(how)
                    addProperty(defaultBlock)
                }
            }
        )
    }

    override fun logMediaPanelClose(how: AmplitudeMediaKeyboardHowProperty) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_CLOSE,
            properties = {
                it.apply {
                    addProperty(how)
                }
            }
        )
    }

    override fun logMediaPanelMediaOpen(how: AmplitudeMediaKeyboardHowProperty) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_MEDIA_OPEN,
            properties = {
                it.apply {
                    addProperty(how)
                }
            }
        )
    }

    override fun logMediaPanelGifOpen(how: AmplitudeMediaKeyboardHowProperty) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_GIF_OPEN,
            properties = {
                it.apply {
                    addProperty(how)
                }
            }
        )
    }

    override fun logGifSend(where: AmplitudeMediaKeyboardWhereProperty) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.GIF_SEND,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }

    override fun logPanelPull(where: AmplitudeMediaKeyboardWhereProperty) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.PANEL_PULL,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }

    override fun logAddFavorite(
        where: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        userId: Long,
        stickerCategory: String?,
        stickerId: Int?
    ) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_FAVORITE_ADD,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(mediaTypeProperty)
                    addProperty(AmplitudeMediaKeyboardConstants.USER_ID, userId)
                    addProperty(AmplitudeMediaKeyboardConstants.STICKER_CATEGORY, stickerCategory ?: DEFAULT_VALUE)
                    addProperty(AmplitudeMediaKeyboardConstants.STICKER_ID, stickerId ?: DEFAULT_VALUE)
                }
            }
        )
    }

    override fun logDeleteFavorite(
        where: AmplitudeMediaKeyboardFavoriteWhereProperty,
        mediaTypeProperty: AmplitudeMediaKeyboardMediaTypeProperty,
        userId: Long,
        stickerCategory: String?,
        stickerId: Int?
    ) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_FAVORITE_DELETE,
            properties = {
                it.apply {
                    addProperty(where)
                    addProperty(mediaTypeProperty)
                    addProperty(AmplitudeMediaKeyboardConstants.USER_ID, userId)
                    addProperty(AmplitudeMediaKeyboardConstants.STICKER_CATEGORY, stickerCategory ?: DEFAULT_VALUE)
                    addProperty(AmplitudeMediaKeyboardConstants.STICKER_ID, stickerId ?: DEFAULT_VALUE)
                }
            }
        )
    }

    override fun logClearRecentStickers() {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_RECENT_STICKER_DELETE
        )
    }

    override fun logDeleteRecentSticker(stickerId: Int) {
        delegate.logEvent(
            eventName = AmplitudeMediaKeyboardEventName.MEDIA_PANEL_RECENT_ONE_STICKER_DELETE,
            properties = {
                it.apply {
                    addProperty(AmplitudeMediaKeyboardConstants.STICKER_ID, stickerId)
                }
            }
        )
    }
}
