package com.numplates.nomera3.modules.chat.helpers.voicemessage

import android.media.MediaPlayer
import android.widget.SeekBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.presentation.audio.VoiceMessageView
import timber.log.Timber


class VoiceMessagePlayer(
    private val lifecycle: Lifecycle,
    private val callback: VoiceMessagePlayerCallback
): LifecycleObserver {

    private val player by lazy { MediaPlayer() }
    private var unheardMessages: HashMap<String, Int> = hashMapOf()

    private var lastPlayingMessage: MessageEntity? = null
    private var lastPlayingMessageView: VoiceMessageView? = null

    init {
        lifecycle.addObserver(this)
    }

    fun voicePlayClick(
        message: MessageEntity?,
        voiceView: VoiceMessageView,
        onPlayCompleted: () -> Unit
    ) {
        voiceView.voiceHandler.removeCallbacksAndMessages(null)

        if (!ifExistsLocalFile(voiceView)) {
            message?.let { callback.onDownloadVoiceMessage(it) }
        } else {
            prepareVoiceMessage(message, voiceView)
            playControlVoiceMessage(voiceView, onPlayCompleted)
        }

        listenPlayProgressChange(voiceView)
    }

    fun onBindVoiceMessage(
        message: MessageEntity,
        voiceView: VoiceMessageView?,
        isIncomingMessage: Boolean
    ) {
        if (lastPlayingMessage?.msgId == message.msgId && player.isPlaying) {
            setVoiceViewPlayState(voiceView)
            voiceView?.isPlaying = true
            player.start()
        } else {
            setVoiceViewToStartPosition(message, voiceView, isIncomingMessage)
        }
    }

    private fun setVoiceViewToStartPosition(
        message: MessageEntity,
        voiceView: VoiceMessageView?,
        isIncomingMessage: Boolean
    ) {
        val listOfAmplitudes = message.attachment.waveForm
        voiceView?.apply {
            setPlayButton()
            setView(isIncomingMessage, listOfAmplitudes)
            voiceHandler.removeCallbacksAndMessages(null)
        }
    }

    fun setPlayButtonInLastPlayingMessage() {
        if (lastPlayingMessageView?.isPlaying == true) {
            lastPlayingMessageView?.setPlayButton()
        }
    }

    private fun ifExistsLocalFile(voiceView: VoiceMessageView): Boolean {
        val isEmptyFilePath = voiceView.downloadedFilePath?.isEmpty() ?: true
        return !(voiceView.downloadedFilePath == null || isEmptyFilePath)
    }

    fun stopPlayer() {
        player.pause()
        lastPlayingMessageView?.setPlayButton()
    }

    fun stopAndDestroyPlayer() {
        runCatching {
            player.apply {
                reset()
                prepare()
                stop()
                release()
            }
        }
    }

    private fun prepareVoiceMessage(
        message: MessageEntity?,
        voiceView: VoiceMessageView
    ) {
        if (!voiceView.isPlaying) {
            try {
                if (lastPlayingMessage?.msgId == message?.msgId && player.isPlaying) {
                    setVoiceViewPlayState(voiceView)
                    return
                }

                player.apply {
                    stop()
                    reset()
                    setDataSource(voiceView.downloadedFilePath)
                    prepare()
                }

                voiceView.apply {
                    visualizer.max = player.duration
                    localDuration = player.duration
                    isPlaying = true
                }

                lastPlayingMessage = message
                lastPlayingMessageView = voiceView
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setVoiceViewPlayState(voiceView: VoiceMessageView?) {
        voiceView?.apply {
            visualizer.max = player.duration
            localDuration = player.duration
            isPlaying = true
            setPauseButton()
            voiceHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun playControlVoiceMessage(
        voiceView: VoiceMessageView,
        onPlayCompleted: () -> Unit
    ) {
        Timber.e("playControlVoiceMessage")

        try {
            if (!player.isPlaying) {
                playVoiceMessage(voiceView, onPlayCompleted)
            } else {
                pauseVoiceMessage(voiceView)
            }
        } catch (e: Exception) {
            Timber.e("Internal error when playing voice message:${e.message}")
        }
    }

    private fun playVoiceMessage(
        voiceView: VoiceMessageView,
        onPlayCompleted: () -> Unit
    ) {
        Timber.e("playVoiceMessage")

        voiceView.getUnheardMessagePosition()?.let { currPos ->
            if (currPos > 0) {
                player.apply {
                    reset()
                    setDataSource(voiceView.downloadedFilePath)
                    prepare()
                    seekTo(currPos)
                    start()
                }
                startPlay(voiceView, onPlayCompleted)
            } else {
                startPlay(voiceView, onPlayCompleted)
                player.start()
            }
        } ?: kotlin.run {
            startPlay(voiceView, onPlayCompleted)
            player.start()
        }
    }

    private fun startPlay(
        voiceView: VoiceMessageView,
        onPlayCompleted: () -> Unit
    ) {
        Timber.e("startPlay")

        callback.keepScreen(isEnable = true)
        handleVoicePlayComplete(voiceView, onPlayCompleted)
        voiceView.setPauseButton()
        voiceView.isPlaying = true
        setAudioVisualizer(player, voiceView)
    }

    private fun handleVoicePlayComplete(
        voiceView: VoiceMessageView,
        onPlayCompleted: () -> Unit
    ) {
        Timber.e("handleVoicePlayComplete")

        player.setOnCompletionListener {
            callback.keepScreen(isEnable = false)
            onPlayCompleted.invoke()
            voiceView.setUnheardMessagePosition(0)
            voiceView.apply {
                setPlayButton()
                visualizer.drawBar(voiceView.visualizer.max)
                isPlaying = false
                voiceHandler.removeCallbacksAndMessages(null)
            }
        }
    }

    private fun setAudioVisualizer(
        player: MediaPlayer,
        voiceView: VoiceMessageView
    ) {
        val visualizerRunnable = VoiceMessageVisualizerRunnable(player, voiceView)
        voiceView.post(visualizerRunnable)
    }

    private fun pauseVoiceMessage(voiceView: VoiceMessageView) {
        callback.keepScreen(isEnable = false)
        player.pause()
        voiceView.setPlayButton()
        voiceView.voiceHandler.removeCallbacksAndMessages(null)

        val currentPosition = player.currentPosition
        val totalDuration = player.duration

        if (totalDuration - currentPosition != 0) {
            voiceView.setUnheardMessagePosition(currentPosition)
        } else {
            voiceView.setUnheardMessagePosition(0)
        }
    }

    private fun listenPlayProgressChange(voiceView: VoiceMessageView) {
        voiceView.visualizer.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, fromUser: Boolean) {
                if (fromUser) player.seekTo(i)
                voiceView.visualizer.drawBar(i)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
    }

    private fun VoiceMessageView.setUnheardMessagePosition(position: Int) {
        unheardMessages[this.downloadedFilePath ?: ""] = position
    }

    private fun VoiceMessageView.getUnheardMessagePosition() =
        unheardMessages[this.downloadedFilePath ?: ""]

}
