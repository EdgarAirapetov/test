package com.numplates.nomera3.modules.baseCore.helper.amplitude.chat

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst.HAVE_GIF
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.FROM
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.GROUP_CHAT
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.HAVE_AUDIO
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.HAVE_MEDIA
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.HAVE_PIC
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.HAVE_VIDEO
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.ChatAmplitudeConstants.TO
import javax.inject.Inject
import org.json.JSONObject

class AnalyticMessageParams(
    val haveText: Boolean,
    val havePic: Boolean,
    val haveVideo: Boolean,
    val haveGif: Boolean,
    val haveAudio: Boolean,
    val haveMedia: Boolean,
    val groupChat: Boolean,
    val from: String,
    val to: String
)

interface AmplitudeChatAnalytic {

    fun onBlurMediaShowChatRequest(photoCount: Int, gifCount: Int, videoCount: Int)
    fun onAllMessagesResend(messagesCount: Int)
    fun unsentMessageCopy()
    fun onMessageResendMenuShowed()
    fun onMessageResendClicked(paramsAnalytic: AnalyticMessageParams)
    fun onDeletedUnsentMessageClicked(paramsAnalytic: AnalyticMessageParams)
}


class AmplitudeChatAnalyticImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeChatAnalytic {

    override fun onBlurMediaShowChatRequest(
        photoCount: Int,
        gifCount: Int,
        videoCount: Int
    ) {
        delegate.logEvent(
            eventName = ChatAmplitudeEventName.BLUR_MEDIA_SHOW,
            properties = {
                it.apply {
                    addProperty(ChatAmplitudeConstants.PHOTO_COUNT, photoCount)
                    addProperty(ChatAmplitudeConstants.GIF_COUNT, gifCount)
                    addProperty(ChatAmplitudeConstants.VIDEO_COUNT, videoCount)
                }
            }
        )
    }

    override fun onAllMessagesResend(messagesCount: Int) {
        delegate.logEvent(
            eventName = ChatAmplitudeEventName.ALL_MESSAGES_RESEND,
            properties = {
                it.apply {
                    addProperty(ChatAmplitudeConstants.RESEND_MESSAGES_COUNT, messagesCount)
                }
            }
        )
    }

    override fun unsentMessageCopy() = delegate.logEvent(eventName = ChatAmplitudeEventName.UNSENT_MESSAGE_COPY)

    override fun onMessageResendMenuShowed() =
        delegate.logEvent(eventName = ChatAmplitudeEventName.MESSAGE_RESEND_MENU_SHOWED)

    override fun onMessageResendClicked(paramsAnalytic: AnalyticMessageParams) = delegate.logEvent(
        eventName = ChatAmplitudeEventName.MESSAGE_RESEND,
        properties = { setMessageParams(it, paramsAnalytic) }
    )

    override fun onDeletedUnsentMessageClicked(paramsAnalytic: AnalyticMessageParams) = delegate.logEvent(
        eventName = ChatAmplitudeEventName.UNSENT_MESSAGE_DELETED,
        properties = { setMessageParams(it, paramsAnalytic) }
    )

    private fun setMessageParams(jsonObj: JSONObject, paramsAnalytic: AnalyticMessageParams): JSONObject {
        return jsonObj.apply {
            addProperty(HAVE_VIDEO, paramsAnalytic.haveVideo)
            addProperty(HAVE_PIC, paramsAnalytic.havePic)
            addProperty(HAVE_MEDIA, paramsAnalytic.haveMedia)
            addProperty(HAVE_GIF, paramsAnalytic.haveGif)
            addProperty(FROM, paramsAnalytic.from)
            addProperty(TO, paramsAnalytic.to)
            addProperty(HAVE_AUDIO, paramsAnalytic.haveAudio)
            addProperty(GROUP_CHAT, paramsAnalytic.groupChat)
        }
    }
}
