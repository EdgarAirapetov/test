package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.core.utils.getDurationSeconds
import com.meera.uikit.widgets.chat.voice.UiKitVoiceConfig
import com.meera.uikit.widgets.chat.voice.VoiceButtonState
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject
import kotlin.math.min

private const val MIN_AMPLITUDES_SIZE = 24

class AudioMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val attachment = message.attachments?.attachments?.first()
        val recognizedText = attachment?.audioRecognizedText ?: String.empty()
        val duration = attachment?.duration ?: 0.0
        return MessageConfigWrapperUiModel.Voice(
            UiKitVoiceConfig(
                showAudioDecryption = false,
                isMe = message.isMy,
                audioDurationStr = getDurationSeconds(duration.toInt()),
                voiceButtonState = when (1) {    // TODO: NOT Implemented
                    1 -> VoiceButtonState.Download
                    2 -> VoiceButtonState.Default
                    3 -> VoiceButtonState.Pause
                    else -> VoiceButtonState.InfinityProgressDownload
                },
                voiceAmplitude = getAmplitudes(message),
                audioDecryptionText = recognizedText,
                statusConfig = getMessageStatusConfig(message),
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
                headerConfig = getMessageHeaderNameConfig(message, isGroupChat)
            )
        )
    }

    /**
     * Retrieves and processes the amplitude data for a voice message.
     *
     * This function extracts the waveform data (amplitudes) from a [MessageUiModel] representing a voice message.
     * It ensures that the returned list of amplitudes has at least [MIN_AMPLITUDES_SIZE] elements. If the original
     * waveform data has fewer elements, it generates additional amplitude values by averaging adjacent existing values.
     *
     * @param message The [MessageUiModel] containing the voice message data.
     * @return A [List] of [Int] representing the processed amplitudes. The list will have at least [MIN_AMPLITUDES_SIZE] elements.
     *
     * @throws NoSuchElementException if the message does not contain any attachments or if the first attachment does not have a waveform.
     *
     * @see MessageUiModel
     * @see MIN_AMPLITUDES_SIZE
     */
    private fun getAmplitudes(message: MessageUiModel): List<Int> {
        val attachment = message.attachments?.attachments?.first()
        val amplitudes = attachment?.waveForm ?: emptyList()
        val diff = min(MIN_AMPLITUDES_SIZE, amplitudes.size)
        if (diff < MIN_AMPLITUDES_SIZE) {
            val delta = MIN_AMPLITUDES_SIZE - amplitudes.size
            val divider = MIN_AMPLITUDES_SIZE / delta
            val generated = mutableListOf<Int>()
            for (i in amplitudes.indices) {
                generated.add(amplitudes[i])
                if (i != 0 && i % divider == 0) {
                    generated.add((amplitudes[i - 1] + amplitudes[i]) / 2)
                }
            }
            return generated
        } else {
            return amplitudes
        }
    }
}
