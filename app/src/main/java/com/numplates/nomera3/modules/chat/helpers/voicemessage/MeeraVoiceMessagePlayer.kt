package com.numplates.nomera3.modules.chat.helpers.voicemessage

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.meera.uikit.widgets.chat.voice.VoiceButtonState
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import timber.log.Timber
import java.io.File
import kotlin.math.round

class MeeraVoiceMessagePlayer(
    val context: Context,
    private val lifecycle: Lifecycle,
    private val callback: MeeraVoiceMessagePlayerCallback
): DefaultLifecycleObserver {

    private val player by lazy { MediaPlayer() }
    private var unheardMessages: HashMap<String, Int> = hashMapOf()
    private val playListenerHandler = Handler(Looper.getMainLooper())

    init {
        lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        stopAndDestroyPlayer()
    }

    fun clickPlay(
        cell: UiKitVoiceView,
        data: ChatMessageDataUiModel,
        url: String,
        onPlayCompleted: () -> Unit
    ) {
        playListenerHandler.removeCallbacksAndMessages(null)

        val message = data.messageData
        if (isExistsLocalFile(message.roomId, url)) {
            updateChatVoiceButtonConfig(cell, data, VoiceButtonState.Pause)
            prepareMessagePlay(url)
            playVoiceMessage(cell, url) {
                playListenerHandler.removeCallbacksAndMessages(null)
                cell.setAmplitudeProgress(0)
                setUnheardMessagePosition(url, position = 0)
                updateChatVoiceButtonConfig(cell, data, VoiceButtonState.Default)
                onPlayCompleted.invoke()
            }
        } else {
            Timber.d("MEERA_VOICE_MSG media file NOT exists need download file")
            callback.onDownloadVoiceMessage(data.messageData)
        }
    }

    fun pauseVoiceMessage(url: String) {
        callback.keepScreen(isEnable = false)
        player.pause()

        val currentPosition = player.currentPosition
        val totalDuration = player.duration

        if (totalDuration - currentPosition != 0) {
            setUnheardMessagePosition(url, position = currentPosition)
        } else {
            setUnheardMessagePosition(url, position = 0)
        }
    }

    fun pausePlayer() {
        player.pause()
    }

    fun handlePlayProgress(cell: UiKitVoiceView, progress: Int) {
        if (player.isPlaying) {
            cell.setAmplitudeProgress(progress)
            val seekPos = player.duration / 100 * progress
            player.seekTo(seekPos)
        } else {
            Timber.e("MEERA_VOICE_MSG [VoicePlayer] set Voice Message progress manually progress:$progress NOT-PLAY")
        }
    }

    private fun stopAndDestroyPlayer() {
        runCatching {
            player.apply {
                reset()
                prepare()
                stop()
                release()
            }
        }
    }

    private fun prepareMessagePlay(url: String) {
        runCatching {
            player.apply {
                stop()
                reset()
                setDataSource(url)
                prepare()
            }
        }.onFailure {
            Timber.e("ERROR when prepare MediaPlayer before play voice message")
        }
    }

    private fun playVoiceMessage(
        cell: UiKitVoiceView,
        url: String,
        onPlayCompleted: () -> Unit
    ) {
        runCatching {
            val unheardPosition = getUnheardMessagePosition(url)
            if (unheardPosition == null) {
                startPlay(cell, onPlayCompleted)
            } else {
                if (unheardPosition > 0) {
                    player.apply {
                        reset()
                        setDataSource(url)
                        prepare()
                        seekTo(unheardPosition)
                        start()
                    }
                }
                startPlay(cell, onPlayCompleted)
            }
        }.onFailure { e ->
            Timber.e("MEERA_VOICE_MSG Internal error when playing voice message:${e.message}")
        }
    }

    private fun startPlay(
        cell: UiKitVoiceView,
        onPlayCompleted: () -> Unit
    ) {
        player.setOnCompletionListener {
            callback.keepScreen(isEnable = false)
            onPlayCompleted.invoke()
        }
        player.start()
        setAudioVisualizer(cell)
    }

    private fun setAudioVisualizer(cell: UiKitVoiceView) {
        playListenerHandler.post(UpdateProgressBar(cell))
    }

    private fun isExistsLocalFile(roomId: Long, url: String): Boolean {
        val fileName = Uri.parse(url).lastPathSegment
        val storageDir = File(
            context.getExternalFilesDir(null),
            "$CHAT_VOICE_MESSAGES_PATH/$roomId"
        )
        val audioFile = File(storageDir, fileName)
        return audioFile.exists()
    }

    private fun UiKitVoiceView.setAmplitudeProgress(progress: Int) {
        getAmplitudeVisualizer().progress = progress
        getAmplitudeVisualizer().drawBar(progress)
    }

    private fun setUnheardMessagePosition(url: String, position: Int) {
        unheardMessages[url] = position
    }

    private fun getUnheardMessagePosition(url: String) =
        unheardMessages[url]

    inner class UpdateProgressBar(val cell: UiKitVoiceView): Runnable {
        override fun run() {
            try {
                val progress = round((player.currentPosition / player.duration.toDouble()) * 100.0).toInt()
                cell.setAmplitudeProgress(progress)
                playListenerHandler.postDelayed(this, 100L)
            } catch (e: Exception) {
                playListenerHandler.removeCallbacksAndMessages(null)
                e.printStackTrace()
            }
        }
    }

}

fun setChatVoiceButtonConfig(
    cell: UiKitVoiceView,
    data: ChatMessageDataUiModel,
    buttonState: VoiceButtonState
) {
    val viewConfig = (data.messageConfig as MessageConfigWrapperUiModel.Voice).config
    val updConfig = viewConfig.copy(voiceButtonState = buttonState)
    cell.setConfig(updConfig)
}

fun updateChatVoiceButtonConfig(
    cell: UiKitVoiceView,
    data: ChatMessageDataUiModel,
    buttonState: VoiceButtonState
) {
    val viewConfig = (data.messageConfig as MessageConfigWrapperUiModel.Voice).config
    val updConfig = viewConfig.copy(voiceButtonState = buttonState)
    cell.updateButtonStateConfig(updConfig)
}
