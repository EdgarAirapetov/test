package com.numplates.nomera3.modules.chat.helpers

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.CHAT_VOICE_MESSAGE_EXTENSION
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentChatBinding
import com.numplates.nomera3.modules.chat.toolbar.ui.NavSwipeDirection
import com.numplates.nomera3.modules.chat.views.VoiceMessagePreviewView
import com.numplates.nomera3.modules.chat.views.scaleAlphaAnimateVoiceBtn
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit

private const val VOICE_BTN_SCALE_UP_FACTOR = 2.2f
private const val ORIGIN_ANIM = 1.0f
private const val ALPHA_INVISIBLE_ANIM = 0.0f
private const val ALPHA_VISIBLE_ANIM = 1.0f
private const val MARGIN_START_AFTER_COMPLETE_ANIMATE_VOICE_BTN = 8
private const val BLINK_BUTTON_SCALE_UP_ANIMATION_FACTOR = 1.7f
private const val RED_DOT_SCALE_UP_ANIMATION_FACTOR = 1.3f

private const val AUDIO_NUM_CHANNELS = 1
private const val AUDIO_ENCODING_BITRATE = 128000
private const val AUDIO_SAMPLING_RATE = 44100

private const val TIMER_FORMAT = "00.00"

private const val SMALL_LIST_AMPLITUDES_SIZE = 11
private const val MIDDLE_LIST_AMPLITUDES_SIZE = 19
private const val LARGE_LIST_AMPLITUDES_SIZE = 36

private const val DELAY_BEFORE_RECORD_BTN_APPEARANCE_MS = 500L
private const val DELAY_SEND_TYPING_STATUS = 3000L
private const val DELAY_UPDATE_VOICE_TIMER = 100L
private const val DELAY_RECORD_PREVIEW_AMPLITUDES = 50L
private const val DELAY_RECORD_MESSAGE_AMPLITUDES = 100L
private const val DELAY_UPDATE_PREVIEW_PROGRESS_BAR = 100L
private const val DELAY_GARBAGE_ANIMATION = 300L


class VoiceMessageRecordDelegate(
    private val fragment: Fragment,
    private val binding: FragmentChatBinding?,
    private val callback: VoiceMessageRecordCallback
) : LifecycleObserver {

    private val lifecycle
        get() = fragment.viewLifecycleOwner

    private var hasPermissions = false

    private var mediaRecorder: MediaRecorder? = null
    private var recordingAudioFileName: String = String.empty()
    private var recordedAudioFile: File? = null
    private var startVoiceRecordTimer = 0L

    private val voiceRecordListenerHandler = Handler(Looper.getMainLooper())
    private val voiceRecordStatusUpdateHandler = Handler(Looper.getMainLooper())
    private val recordPreviewAmplitudesHandler = Handler(Looper.getMainLooper())
    private val recordMessageAmplitudesHandler = Handler(Looper.getMainLooper())
    private val updatePlayProgressBarHandler = Handler(Looper.getMainLooper())

    private var motionLayout: MotionLayout? = null

    private var isRecordButtonPressed = false
    private var isLockRecordButton = false
    private var isPlayedGarbageAnimation = false
    private var isLockSendMessage = false

    private var btnBlinkAnimation: ObjectAnimator? = null
    private var redDotAnimation: ObjectAnimator? = null
    private var startTvSwipeToCancel: ObjectAnimator? = null

    private val previewListAmplitudes = mutableListOf<Int>()
    private val messageListAmplitudes = mutableListOf<Int>()

    private val player by lazy { MediaPlayer() }
    private var recordDuration: Long = 0

    fun run() {
        fragment.lifecycle.addObserver(this)
        binding?.apply {
            motionLayout = voiceRecordMotionContainer
            setIsEnabledMotionLayout(isEnabled = false)
            visibleVoiceRecordBtn()
            handleMotionLayoutTouchListener(binding = this)
            handleMotionLayoutTransition()
            handleClickLockButton(binding = this)
            handleClickCancelButton(binding = this)
            lavVoiceGarbage.addAnimatorListener(lottieGarbageAnimationListener)
        }
    }

    fun isRecordingProcess(): Boolean {
        return isShowPreview() || mediaRecorder != null
    }

    fun isLockButtonVisible() = binding?.btnLock?.visibility == View.VISIBLE

    fun initChatInput() {
        binding?.apply {
            btnLock.gone()
            btnVoiceMessage.visible()
            sendMessageContainer.layoutGroupChatChatbox.visible()
            if (!isShowPreview()) voiceRecordProcessContainer.root.gone()
        }
        visibleVoiceRecordBtn()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleMotionLayoutTouchListener(binding: FragmentChatBinding) {
        binding.btnVoiceMessage.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    return@setOnTouchListener actionDownTouchEvent(binding)

                MotionEvent.ACTION_UP ->
                    actionUpTouchEvent(binding)
            }
            false
        }

        motionLayout?.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                actionUpTouchEvent(binding)
            }
            false
        }
    }

    private fun actionDownTouchEvent(binding: FragmentChatBinding): Boolean {
        checkPermissions()
        isRecordButtonPressed = true

        when {
            isShowPreview() -> {
                setIsEnabledMotionLayout(isEnabled = false)
                return true
            }

            isLockRecordButton -> {
                isLockRecordButton = false
                return true
            }
        }
        handleTouchDown(binding)
        return false
    }

    private fun actionUpTouchEvent(binding: FragmentChatBinding) {
        if (isRecordButtonPressed) {
            handleTouchUp(binding)
            isRecordButtonPressed = false
        }
        callback.releaseRecordBtn()
    }

    /**
     * Stop recording and show preview
     */
    private fun handleClickLockButton(binding: FragmentChatBinding) {
        binding.btnLock.click {
            stopAllRecordAndShowPreview()
        }
    }

    private fun stopAllRecordAndShowPreview() {
        stopRecordVoiceMessage()
        setStopRecordAndShowPreviewState()
        showPreview()
    }

    /**
     * Stop recording and remove already recorded file
     */
    private fun handleClickCancelButton(binding: FragmentChatBinding) {
        binding.tvCancelText.click {
            playLottieGarbageAnimation()
            stopAllRecordAndSetStartState()
        }
    }

    private fun stopAllRecordAndSetStartState() {
        stopRecordVoiceMessage()
        setStopRecordState()
        deleteTempAudioFile()
        callback.onFinishRecordingProcess()
    }

    private fun handleTouchDown(binding: FragmentChatBinding) {
        callback.tapRecordBtn()
        binding.apply {
            hasPermissions {
                setIsEnabledMotionLayout(isEnabled = true)
                when {
                    !isShowPreview() && mediaRecorder == null -> {
                        goneLottieGarbageAnimation()
                        voiceRecordProcessContainer.vgVoiceRecordTimer.visible()
                        visibleLockButton()
                        hideSendTextContainer()
                        startAllRecordProcessAnimations(binding = this)
                        startRecordVoiceMessage()
                    }

                    !isShowPreview() && mediaRecorder != null -> {
                        isLockRecordButton = false
                        setIsEnabledMotionLayout(isEnabled = false)
                    }

                    isShowPreview() -> {
                        stopLottieGarbageAnimationDelayed()
                    }
                }
            }
        }
    }

    private fun handleTouchUp(binding: FragmentChatBinding) {
        hasPermissions {
            motionLayout?.getTransition(R.id.horizontal_transition)?.isEnabled = true
            motionLayout?.transitionToState(R.id.start)

            if (!isLockRecordButton) {
                stopRecordVoiceMessage()
                stopAllRecordProcessAnimations(binding)
                hidePreview()
                if (!isLockSendMessage) {
                    val optimizedAmplitudes = optimizeAmplitudesForMessage(
                        duration = recordDuration,
                        amplitudes = messageListAmplitudes
                    )
                    binding.btnVoiceMessageIcon.setImageResource(R.drawable.ic_mic_voice_record)
                    setIsEnabledMotionLayout(isEnabled = false)
                    player.pause()
                    callback.sendVoiceMessage(
                        filePath = recordingAudioFileName,
                        amplitudes = optimizedAmplitudes,
                        durationSec = TimeUnit.MILLISECONDS.toSeconds(retrieveDurationData())
                    )
                    callback.onFinishRecordingProcess()
                } else {
                    isLockSendMessage = false
                }
            } else {
                Timber.d("TOUCH UP -> Show preview mode")
            }
        }
    }

    private fun retrieveDurationData(): Long {
        val uri = Uri.parse(recordingAudioFileName)
        val mediaRetriever = MediaMetadataRetriever()
        mediaRetriever.setDataSource(fragment.requireContext(), uri)
        val durationStr = mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        return durationStr?.toLong() ?: 0L
    }

    private fun optimizeAmplitudesForMessage(
        duration: Long,
        amplitudes: List<Int>
    ): List<Int> {
        if (amplitudes.isNullOrEmpty()) return emptyList()

        val tempAmplitudeList = arrayListOf<Int>()
        var currentArraySize = 0
        when {
            duration / 1000 <= 5 -> currentArraySize = SMALL_LIST_AMPLITUDES_SIZE
            duration / 1000 in 6..30 -> currentArraySize = MIDDLE_LIST_AMPLITUDES_SIZE
            duration / 1000 > 30 -> currentArraySize = LARGE_LIST_AMPLITUDES_SIZE
        }
        try {
            val batchArray = amplitudes.chunked(amplitudes.size / (currentArraySize - 1))
            val loopCondition =
                if (batchArray.size >= currentArraySize) currentArraySize else batchArray.size
            for (i in 0 until loopCondition) {
                var valueAmplitude = 0
                for (b in 0 until batchArray[i].size) valueAmplitude += batchArray[i][b]
                val averageAmplitude = valueAmplitude / batchArray[i].size
                tempAmplitudeList.add(averageAmplitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            tempAmplitudeList.add(0)
        }
        return tempAmplitudeList
    }

    /**
     * Stop recording without show preview
     */
    private fun setStopRecordState() {
        binding?.apply {
            goneLockButton()
            btnVoiceMessageIcon.setImageResource(R.drawable.ic_mic_voice_record)
            voiceRecordProcessContainer.tvVoiceRecordTimer.text = TIMER_FORMAT
            setStartRecordBtnState()
            stopAllRecordProcessAnimations(binding)
            voiceRecordListenerHandler.removeCallbacks(updateVoiceTimerRunnable)
            isLockRecordButton = false
        }
    }

    /**
     * Stop recording with show preview
     */
    private fun setStopRecordAndShowPreviewState() {
        binding?.apply {
            goneLockButton()
            voiceRecordProcessContainer.vgVoiceRecordTimer.gone()
            tvCancelText.gone()
            voiceBlink.gone()
            stopAllRecordProcessAnimations(binding)
            btnVoiceMessage.btnVoiceScaleDownAnimate()
            voiceRecordListenerHandler.removeCallbacks(updateVoiceTimerRunnable)
            isLockRecordButton = false
        }
    }

    private fun handleClickGarbageWhenPreviewMode(binding: FragmentChatBinding) {
        binding.lavVoiceGarbage.click {
            binding.btnVoiceMessageIcon.setImageResource(R.drawable.ic_mic_voice_record)
            hidePreview()
            playLottieGarbageAnimation()
            deleteTempAudioFile()
        }
    }

    private fun showPreview() {
        binding?.apply {
            val previewView = voiceRecordProcessContainer.viewVoiceMessagePreview
            previewView.visible()
            preparePreviewView(previewView)
            visibleLottieGarbageAnimation()
            handleClickGarbageWhenPreviewMode(binding = this)
            callback.allowSwipeDirectionNavigator(NavSwipeDirection.NONE)
        }
    }

    private fun hidePreview() {
        binding?.voiceRecordProcessContainer?.viewVoiceMessagePreview?.gone()
        setStartRecordBtnState()
        callback.allowSwipeDirectionNavigator(NavSwipeDirection.LEFT)
    }

    private fun isShowPreview() = binding
        ?.voiceRecordProcessContainer?.viewVoiceMessagePreview?.visibility == View.VISIBLE

    private fun handleMotionLayoutTransition() {
        motionLayout?.setTransitionListener(object : MotionLayout.TransitionListener {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) = Unit

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                handleMLTransitionChange(startId, endId, progress)
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                handleMLTransitionComplete(currentId)
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) = Unit
        })
    }

    private fun handleMLTransitionChange(
        startId: Int,
        endId: Int,
        progress: Float
    ) {
        if (startId == R.id.start && endId == R.id.end) {
            binding?.btnVoiceMessage?.apply {
                scaleX = VOICE_BTN_SCALE_UP_FACTOR - progress
                scaleY = VOICE_BTN_SCALE_UP_FACTOR - progress
            }
        }
    }

    private fun handleMLTransitionComplete(currentId: Int) {
        if (currentId == R.id.start) {
            when {
                isLockRecordButton -> setIsEnabledMotionLayout(isEnabled = false)
                !isRecordButtonPressed && !isRecordingProcess() -> binding?.btnVoiceMessage?.apply {
                    scaleX = 1.0f
                    scaleY = 1.0f
                }
            }
        }

        if (currentId == R.id.top_lock) {
            isLockRecordButton = true
            binding?.btnLock?.setLockMode()
            binding?.btnVoiceMessageIcon?.apply {
                scaleAlphaAnimateVoiceBtn(alpha = 0.5f, scale = 0.5f, duration = 200) {
                    setImageResource(R.drawable.ic_send_voice_record)
                    setMargins(start = MARGIN_START_AFTER_COMPLETE_ANIMATE_VOICE_BTN.dp)
                    scaleAlphaAnimateVoiceBtn(1.4f, 1.4f, duration = 100)
                }
            }
            stopTvSwipeToCancel(
                binding = binding,
                animator = startTvSwipeToCancel,
                isShowLeftIcon = false
            )
        }
        if (currentId == R.id.end) {
            isLockSendMessage = true
            isLockRecordButton = false

            binding?.btnVoiceMessage?.btnVoiceScaleDownAnimate()
            setIsEnabledMotionLayout(isEnabled = false)
            goneVoiceRecordBtn()
            binding?.tvCancelText?.gone()
            binding?.voiceBlink?.gone()
            showSendTextContainer()
            playLottieGarbageAnimation()
            motionLayout?.transitionToState(R.id.start)
            setStartRecordBtnState()
            lifecycle.doDelayed(DELAY_BEFORE_RECORD_BTN_APPEARANCE_MS) {
                visibleVoiceRecordBtn()
                stopAllRecordProcessAnimations(binding)
            }
        }
    }

    private fun setStartRecordBtnState() {
        binding?.apply {
            btnVoiceMessage.btnVoiceScaleDownAnimate {
                goneLockButton()
                voiceRecordProcessContainer.vgVoiceRecordTimer.gone()
                showSendTextContainer()
                if (!isPlayedGarbageAnimation) {
                    checkIsGarbageAnimatingAndStop {
                        lavVoiceGarbage.gone()
                        sendMessageContainer.lavAddToFavorites.visible()
                    }
                }
                callback.onFinishRecordingProcess()
            }
        }
    }

    private fun visibleLockButton() {
        binding?.btnLock?.startAppearAnimation()
        callback.onLockButtonIsVisible(isVisible = true)
    }

    private fun goneLockButton() {
        binding?.btnLock?.clearLockMode()
        callback.onLockButtonIsVisible(isVisible = false)
    }

    private fun visibleLottieGarbageAnimation() {
        binding?.apply {
            lavVoiceGarbage.frame = 0
            lavVoiceGarbage.visible()
        }
    }

    private fun stopLottieGarbageAnimationDelayed(block: () -> Unit = {}) {
        binding?.lavVoiceGarbage?.postDelayed({
            checkIsGarbageAnimatingAndStop(block)
        }, DELAY_GARBAGE_ANIMATION)
    }

    private fun checkIsGarbageAnimatingAndStop(block: () -> Unit = {}) {
        binding?.apply {
            if (lavVoiceGarbage.isAnimating) {
                isPlayedGarbageAnimation = false
                lavVoiceGarbage.cancelAnimation()
                block()
            } else {
                block()
            }
        }
    }

    private fun goneLottieGarbageAnimation() {
        stopLottieGarbageAnimationDelayed {
            binding?.lavVoiceGarbage?.gone()
        }
    }

    private fun playLottieGarbageAnimation() {
        binding?.apply {
            lavVoiceGarbage.takeIf { it.isAnimating.not() }?.apply {
                visible()
                playAnimation()
                isPlayedGarbageAnimation = true
                voiceRecordProcessContainer.vgVoiceRecordTimer.gone()
            }
        }
    }

    private val lottieGarbageAnimationListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            goneLottieGarbageAnimation()
            binding?.sendMessageContainer?.lavAddToFavorites?.visible()
            setIsEnabledMotionLayout(isEnabled = false)
            isPlayedGarbageAnimation = false
        }
    }

    fun visibleVoiceRecordBtn() {
        motionLayout?.setAlphaVoiceRecordBtn(ALPHA_VISIBLE_ANIM)
        binding?.btnVoiceMessage?.visible()
    }

    fun goneVoiceRecordBtn() {
        motionLayout?.setAlphaVoiceRecordBtn(ALPHA_INVISIBLE_ANIM)
        binding?.btnVoiceMessage?.gone()
    }

    private fun MotionLayout.setAlphaVoiceRecordBtn(alpha: Float) {
        this.getConstraintSet(R.id.start)?.setAlpha(R.id.btn_voice_message, alpha)
        this.getConstraintSet(R.id.top_lock)?.setAlpha(R.id.btn_voice_message, alpha)
    }

    private fun View.btnVoiceScaleUpAnimate(complete: () -> Unit = {}) {
        this.animate()
            .scaleX(VOICE_BTN_SCALE_UP_FACTOR)
            .scaleY(VOICE_BTN_SCALE_UP_FACTOR)
            .withEndAction { complete() }
            .duration = 300

        binding?.btnVoiceMessageIcon?.animate()
            ?.scaleX(1.4f)
            ?.scaleY(1.4f)
            ?.duration = 300
    }

    private fun View.btnVoiceScaleDownAnimate(complete: () -> Unit = {}) {
        this.animate()
            .scaleX(ORIGIN_ANIM)
            .scaleY(ORIGIN_ANIM)
            .withEndAction { complete() }
            .duration = 233

        binding?.btnVoiceMessageIcon?.animate()
            ?.scaleX(ORIGIN_ANIM)
            ?.scaleY(ORIGIN_ANIM)
            ?.duration = 100
    }

    private fun startAllRecordProcessAnimations(binding: FragmentChatBinding?) {
        binding?.apply {
            btnVoiceMessage.btnVoiceScaleUpAnimate {
                binding.voiceBlink.visible()
                binding.voiceBlink.animate()
                    .scaleX(1.4f)
                    .scaleY(1.4f)
                    .withEndAction {
                        btnBlinkAnimation = startAnimationBlinkBtn(this)
                    }
                    .duration = 500
            }
            redDotAnimation = startAnimationRedDot(this)
            startTvSwipeToCancel = startAnimationTvSwipeToCancel(this)
        }
    }

    private fun stopAllRecordProcessAnimations(binding: FragmentChatBinding?) {
        binding?.let {
            stopAnimationBlinkBtn(binding, btnBlinkAnimation)
            stopAnimationRedDot(binding, redDotAnimation)
            stopTvSwipeToCancel(binding, startTvSwipeToCancel)
        }
    }

    private fun startAnimationBlinkBtn(binding: FragmentChatBinding): ObjectAnimator {
        return scalePulseAnimation(
            view = binding.voiceBlink,
            scaleFactor = BLINK_BUTTON_SCALE_UP_ANIMATION_FACTOR,
            duration = 500,
            repeatDelay = 500
        )
    }

    private fun stopAnimationBlinkBtn(binding: FragmentChatBinding?, animator: ObjectAnimator?) {
        animator?.cancel()
        animator?.removeAllListeners()
        binding?.voiceBlink?.apply {
            animate().scaleX(ORIGIN_ANIM).scaleY(ORIGIN_ANIM).duration = 50
            gone()
        }
    }

    private fun startAnimationRedDot(binding: FragmentChatBinding): ObjectAnimator {
        return scalePulseAnimation(
            view = binding.voiceRecordProcessContainer.ivRedDotRecordProgressIndicator,
            scaleFactor = RED_DOT_SCALE_UP_ANIMATION_FACTOR,
            duration = 500,
            repeatDelay = 700
        )
    }

    private fun stopAnimationRedDot(binding: FragmentChatBinding, animator: ObjectAnimator?) {
        val view = binding.voiceRecordProcessContainer.ivRedDotRecordProgressIndicator
        animator?.cancel()
        animator?.removeAllListeners()
        view.animate().scaleX(ORIGIN_ANIM).scaleY(ORIGIN_ANIM).duration = 100
    }

    private fun scalePulseAnimation(
        view: View,
        scaleFactor: Float,
        duration: Long,
        repeatDelay: Long
    ): ObjectAnimator {
        view.visible()
        val scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, scaleFactor),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleFactor)
        )
        return infiniteLoopAnimation(
            objectAnimator = scaleAnim,
            duration = duration,
            repeatDelay = repeatDelay
        )
    }

    private fun startAnimationTvSwipeToCancel(binding: FragmentChatBinding): ObjectAnimator {
        val view = binding.tvCancelText
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_back_grey_16, 0, 0, 0)
        view.visible()
        val moveX = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -40.0f),
        )
        return infiniteLoopAnimation(
            objectAnimator = moveX,
            duration = 700,
            repeatDelay = 500
        )
    }

    private fun infiniteLoopAnimation(
        objectAnimator: ObjectAnimator,
        duration: Long,
        repeatDelay: Long
    ): ObjectAnimator {
        objectAnimator.apply {
            setDuration(duration)
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            addListener(object : AnimatorListenerAdapter() {
                var isFirstStep = true
                override fun onAnimationRepeat(animation: Animator) {
                    if (!isFirstStep) {
                        animation?.pause()
                        fragment.lifecycle.doDelayed(repeatDelay) {
                            animation?.resume()
                            isFirstStep = true
                        }
                    }
                    isFirstStep = false
                }
            })
            start()
        }
        return objectAnimator
    }

    private fun stopTvSwipeToCancel(
        binding: FragmentChatBinding?,
        animator: ObjectAnimator?,
        isShowLeftIcon: Boolean = true
    ) {
        animator?.cancel()
        animator?.removeAllListeners()
        binding?.tvCancelText?.animate()?.translationX(ORIGIN_ANIM)?.duration = 200
        if (!isShowLeftIcon) {
            binding?.tvCancelText?.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }
    }

    private fun hideSendTextContainer() {
        binding?.apply {
            sendMessageContainer.lavAddToFavorites.invisible()
            sendMessageContainer.vgChatInputField.invisible()
            voiceRecordProcessContainer.llVoiceRecord.visible()
        }
    }

    private fun showSendTextContainer() {
        binding?.sendMessageContainer?.vgChatInputField?.visible()
    }

    private fun startRecordVoiceMessage() {
        val dir = fragment.context?.cacheDir.toString()
        val currentTimeMs = System.currentTimeMillis()
        recordingAudioFileName = "$dir/$currentTimeMs$CHAT_VOICE_MESSAGE_EXTENSION"
        try {
            releaseMediaRecorder()
            recordedAudioFile = File(recordingAudioFileName)
            recordedAudioFile?.let { file -> if (file.exists()) file.delete() }
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(fragment.requireContext())
            } else {
                MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
                setAudioChannels(AUDIO_NUM_CHANNELS)
                setAudioEncodingBitRate(AUDIO_ENCODING_BITRATE)
                setAudioSamplingRate(AUDIO_SAMPLING_RATE)
                setOutputFile(recordingAudioFileName)
                prepare()
                start()
            }
            startVoiceRecordTimer = SystemClock.uptimeMillis()
            updateVoiceTimerRunnable.run()
            updateStatusVoiceRecordRunnable.run()
            recordPreviewAmplitudesRunnable.run()
            recordMessageAmplitudesRunnable.run()
            callback.allowSwipeDirectionNavigator(NavSwipeDirection.NONE)
        } catch (e: Exception) {
            Timber.e("ERROR when record voice message:${e.message}")
        }
    }

    private fun stopRecordVoiceMessage() {
        voiceRecordListenerHandler.removeCallbacks(updateVoiceTimerRunnable)
        voiceRecordStatusUpdateHandler.removeCallbacks(updateStatusVoiceRecordRunnable)
        recordPreviewAmplitudesHandler.removeCallbacks(recordPreviewAmplitudesRunnable)
        recordMessageAmplitudesHandler.removeCallbacks(recordMessageAmplitudesRunnable)
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Timber.e("ERROR Stop Media recorder:${e.message}")
        }
        mediaRecorder = null
        callback.allowSwipeDirectionNavigator(NavSwipeDirection.LEFT)
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.reset()
        mediaRecorder?.release()
    }

    private val updateVoiceTimerRunnable = object : Runnable {

        @SuppressLint("SetTextI18n")
        override fun run() {
            fun str(vararg args: Any?) = String.format("%02d", *args)

            try {
                recordDuration = SystemClock.uptimeMillis() - startVoiceRecordTimer
                var seconds = (recordDuration / 1000).toInt()
                val milliseconds = (recordDuration % 1000).toInt()
                val min = seconds / 60
                seconds %= 60
                binding?.voiceRecordProcessContainer
                    ?.tvVoiceRecordTimer?.text = str(min) + ":" + str(seconds)
                callback.onUpdateVoiceTimer(seconds, milliseconds)
            } finally {
                voiceRecordListenerHandler.postDelayed(this, DELAY_UPDATE_VOICE_TIMER)
            }
        }
    }

    private val updateStatusVoiceRecordRunnable = object : Runnable {
        override fun run() {
            try {
                callback.sendTypingStatus()
            } finally {
                voiceRecordStatusUpdateHandler.postDelayed(this, DELAY_SEND_TYPING_STATUS)
            }
        }
    }

    private val recordPreviewAmplitudesRunnable = object : Runnable {
        override fun run() {
            try {
                val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                previewListAmplitudes.add(maxAmplitude)
            } finally {
                recordPreviewAmplitudesHandler.postDelayed(this, DELAY_RECORD_PREVIEW_AMPLITUDES)
            }
        }
    }

    private val recordMessageAmplitudesRunnable = object : Runnable {
        override fun run() {
            try {
                val maxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                messageListAmplitudes.add(maxAmplitude)
            } finally {
                recordMessageAmplitudesHandler.postDelayed(this, DELAY_RECORD_MESSAGE_AMPLITUDES)
            }
        }
    }

    private fun preparePreviewView(previewView: VoiceMessagePreviewView) {
        previewView.showPreviewBars(previewListAmplitudes)
        try {
            player.apply {
                stop()
                reset()
                setDataSource(recordingAudioFileName)
                prepare()
            }

            previewView.apply {
                setProgress(0)
                setMaxProgress(player.duration)
                setDuration(player.duration.toLong())
                onPlayClicked = { isPlay ->
                    if (isPlay) {
                        player.start()
                    } else {
                        player.pause()
                    }
                    updatePlayProgressBarHandler.post(UpdateProgressBar(previewView))
                }
                onProgressChangedByUser = { progress -> player.seekTo(progress) }
            }

            player.setOnCompletionListener {
                previewView.setStopState()
                updatePlayProgressBarHandler.removeCallbacksAndMessages(null)
            }
        } catch (e: Exception) {
            Timber.e("ERROR When play preview:${e.message}")
        }
    }

    private fun deleteTempAudioFile() {
        recordedAudioFile?.let { file -> if (file.exists()) file.delete() }
        updatePlayProgressBarHandler.removeCallbacksAndMessages(null)
    }

    inner class UpdateProgressBar(private val previewView: VoiceMessagePreviewView) : Runnable {

        override fun run() {
            try {
                previewView.setProgress(player.currentPosition)
                updatePlayProgressBarHandler.postDelayed(this, DELAY_UPDATE_PREVIEW_PROGRESS_BAR)
            } catch (e: Exception) {
                updatePlayProgressBarHandler.removeCallbacksAndMessages(null)
                e.printStackTrace()
            }
        }

    }

    private fun setIsEnabledMotionLayout(isEnabled: Boolean) {
        motionLayout?.apply {
            getTransition(R.id.horizontal_transition)?.isEnabled = isEnabled
            getTransition(R.id.vertical_transition)?.isEnabled = isEnabled
        }
    }

    private fun checkPermissions() {
        fragment.context?.let { ctx ->
            hasPermissions = hasPermissions(
                ctx,
                Manifest.permission.RECORD_AUDIO,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (!hasPermissions) callback.requestPermissions()
        }
    }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        permissions.forEach { permission ->
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun hasPermissions(block: () -> Unit) {
        if (hasPermissions) {
            block.invoke()
        }
    }
}
