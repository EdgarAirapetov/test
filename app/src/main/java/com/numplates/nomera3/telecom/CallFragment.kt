package com.numplates.nomera3.telecom

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetFileDescriptor
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.Toast
import androidx.annotation.RawRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.register
import com.meera.core.extensions.setTint
import com.meera.core.extensions.tryCatch
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.utils.getNotificationPendingIntentFlag
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.CALL_NOTIFICATION_CHANNEL_ID
import com.numplates.nomera3.CALL_NOTIFICATION_ID
import com.numplates.nomera3.INCOMING_CALL_KEY
import com.numplates.nomera3.R
import com.numplates.nomera3.TYPE_CALL_KEYS
import com.numplates.nomera3.WEBRTC_SET_CAMERA_DISABLED
import com.numplates.nomera3.WEBRTC_SET_CAMERA_ENABLED
import com.numplates.nomera3.WEBRTC_SET_MIC_DISABLED
import com.numplates.nomera3.WEBRTC_SET_MIC_ENABLED
import com.numplates.nomera3.databinding.FragmentCallBinding
import com.numplates.nomera3.modules.audioswitch.ui.AudioSwitchBottomSheet
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallCanceller
import com.numplates.nomera3.modules.calls.presentation.CallViewEvent
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.callback.IOnKeyDown
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showForSpeakerButton
import com.numplates.nomera3.presentation.viewmodel.CallViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.appspot.apprtc.AppRTCClient
import org.appspot.apprtc.PeerConnectionClient
import org.appspot.apprtc.UnhandledExceptionHandler
import org.webrtc.Camera1Enumerator
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraEnumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.EglBase
import org.webrtc.FileVideoCapturer
import org.webrtc.IceCandidate
import org.webrtc.Logging
import org.webrtc.PeerConnectionFactory
import org.webrtc.RendererCommon
import org.webrtc.SessionDescription
import org.webrtc.StatsReport
import org.webrtc.VideoCapturer
import org.webrtc.VideoFileRenderer
import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import timber.log.Timber
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject
import kotlin.math.abs

private const val DELAY_SEND_DATA_CHANNEL_MESSAGE = 2000L

/**
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/3170205699   --- sounds
 * https://disk.yandex.ru/d/OEhe2FF6_5goOg  --- sound resources
 */
class CallFragment : BaseFragmentNew<FragmentCallBinding>(),
    CallProgressEvents,
    AppRTCClient.SignalingEvents,
    PeerConnectionClient.PeerConnectionEvents,
    IOnKeyDown,
    IOnBackPressed,
    AudioSwitchBottomSheet.Listener {

    companion object {

        private const val TAG = "CallRTCClient"

        const val EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS"
        const val EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA"
        const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE"
        const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH"
        const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT"

        // Peer connection statistics callback period in ms.
        private const val STAT_CALLBACK_PERIOD = 1000
    }


    private var uuId: String? = ""
    private val callViewModel: CallViewModel by viewModels<CallViewModel>() { App.component.getViewModelFactory() }

    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var androidAudioManager: AudioManager? = null
    private var isIncomingCall = false
    private var callUser: UserChat? = null
    private var callAccepted: Boolean = false
    private var canDisconnect = true

    private lateinit var callAnimationDrawable: AnimatedVectorDrawable
    private lateinit var callAnimationCallback: Animatable2.AnimationCallback

    private val callTimerHandler = Handler(Looper.getMainLooper())
    private val compositeDisposable = CompositeDisposable()
    private var rxPermissions: RxPermissions? = null

    private val remoteProxyRenderer = ProxyVideoSink()
    private val localProxyVideoSink = ProxyVideoSink()
    private var peerConnectionClient: PeerConnectionClient? = null
    private var appRtcClient: AppRTCClient? = null
    private var signalingParameters: AppRTCClient.SignalingParameters? = null
    private var pipRenderer: CallVideoView? = null
    private var fullscreenRenderer: CallVideoView? = null
    private var videoFileRenderer: VideoFileRenderer? = null
    private val remoteSinks = ArrayList<VideoSink>()
    private var logToast: Toast? = null
    private var commandLineRun: Boolean = false
    private var activityRunning: Boolean = false
    private var roomConnectionParameters: AppRTCClient.RoomConnectionParameters? = null
    private var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters? = null
    private var iceConnected: Boolean = false
    private var isError: Boolean = false
    private var callStartedTimeMs: Long = 0
    private var connectionUpTimeMs:Long = 0
    private var micEnabled = false
    private var screencaptureEnabled = false
    private var frontCamera = true
    private var roomId: Long? = null

    private var mediaPlayer: MediaPlayer? = null
    private var videoCapturer: VideoCapturer? = null

    private var streamAudioType: Int = AudioManager.STREAM_VOICE_CALL
    private lateinit var vibrator: Vibrator

    private var notificationManager: NotificationManager? = null

    private val roomUrl = "https://api2.dev.noomera.ru" // just place, it isn't used
    var uri: Uri? = null
    private val loopback = false
    lateinit var eglBase: EglBase

    // Video call enabled flag.
    private var callConnected = false
    private var isDisconnected = false
    private var incomingVideoEnabled = false
    private var outgoingVideoEnabled = false
    private var callWasInterrupted = false

    // Use screencapture option.
    private val useScreencapture = false

    // Use Camera2 option.
    private val useCamera2 = true

    // Get default codecs.
    private val videoCodec = "VP8"
    private val audioCodec = "OPUS"

    // Check HW codec flag.
    private val hwCodec = true

    // Check Capture to texture.
    private val captureToTexture = true

    // Check FlexFEC.
    private val flexfecEnabled = false

    // Check Disable Audio Processing flag.
    private val noAudioProcessing = false

    private val aecDump = false

    private val saveInputAudioToFile = false

    // Check OpenSL ES enabled flag.
    private val useOpenSLES = false

    // Check Disable built-in AEC flag.
    private val disableBuiltInAEC = false

    // Check Disable built-in AGC flag.
    private val disableBuiltInAGC = false

    // Check Disable built-in NS flag.
    private val disableBuiltInNS = false

    // Check Disable gain control
    private val disableWebRtcAGCAndHPF = false

    // Get video resolution from settings.
    private var videoWidth = 0
    private var videoHeight = 0

    // Get camera fps from settings.
    private var cameraFps = 0

    // Get video and audio start bitrate.
    private val videoStartBitrate = 1700
    private val audioStartBitrate = 32

    private val tracing = false

    // Check Enable RtcEventLog.
    private val rtcEventLogEnabled = false

    private val useLegacyAudioDevice = false

    // Get datachannel options
    private val dataChannelEnabled = true /* SL__: true*/

    private val ordered = true
    private val negotiated = false
    private val maxRetrMs = -1
    private val maxRetr = -1
    private val tempId = -1
    private val protocol = ""

    private var signalingService: SignalingService? = null
    private var ringtoneAudioFocusManager: RingtoneAudioFocusManager? = null

    @Inject
    lateinit var audioSwitch: AudioSwitch

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок(Экран-вызова%2C-Динамик)
     * - текст: Громкая связь включена / Громкая связь выключена
     * - поведение: подсказка открывается каждый раз когда пользователь кликает на кнопку динамика
     * - время отображения: 1 сек
     * */
    private var speakerTurnedOnTooltipJob: Job? = null
    private val speakerTurnedOnTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_speaker_turned_on)
    }

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок(Экран-вызова%2C-Динамик)
     * - текст: Громкая связь включена / Громкая связь выключена
     * - поведение: подсказка открывается каждый раз когда пользователь кликает на кнопку динамика
     * - время отображения: 1 сек
     * */
    private var speakerTurnedOffTooltipJob: Job? = null
    private val speakerTurnedOffTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_speaker_turned_off)
    }

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок(Экран-вызова%2C-Микрофон)
     * - текст: Микрофон включен / Микрофон выключен
     * - поведение: подсказка открывается каждый раз когда пользователь кликает на кнопку микрофона
     * - время отображения: 1 сек
     * */
    private var micTurnedOffTooltipJob: Job? = null
    private val micTurnedOffTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_mic_turned_off)
    }

    /**
     * - документация: https://nomera.atlassian.net/wiki/spaces/NOM/pages/1218248715#Звонок(Экран-вызова%2C-Микрофон)
     * - текст: Микрофон включен / Микрофон выключен
     * - поведение: подсказка открывается каждый раз когда пользователь кликает на кнопку микрофона
     * - время отображения: 1 сек
     * */
    private var micTurnedOnTooltipJob: Job? = null
    private val micTurnedOnTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_mic_turned_on)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCallBinding
        get() = FragmentCallBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ringtoneAudioFocusManager = RingtoneAudioFocusManager(requireContext().applicationContext)
        ringtoneAudioFocusManager?.requestAudioFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.e("Call fragment onViewCreated()")
        act?.dismissAllDialogs(act?.supportFragmentManager)
        signalingService = act.signalingServiceConnectionWrapper.get().signalingService
        signalingService?.registerCallProgressClient(this@CallFragment)

        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(ringtoneAudioFocusManager?.getCallRingtoneAudioAttributes())
        }

        androidAudioManager = (requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        powerManager = act.getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            act.localClassName
        )

        isIncomingCall = arguments?.getInt(TYPE_CALL_KEYS) == INCOMING_CALL_KEY
        callUser = arguments?.getParcelable(IArgContainer.ARG_USER_MODEL)
        arguments?.getBoolean(IArgContainer.ARG_CALL_ACCEPTED)?.let {
            callAccepted = it
        }

        setSpeakerDisabled()

        binding?.tvUserName?.text = callUser?.name

        if (isIncomingCall) {
            binding?.tvTypeCall?.text = resources.getString(R.string.incoming_call)
            binding?.tvTypeCallHeader?.text = resources.getString(R.string.incoming_call)
            binding?.llIncomingCall?.visible()
            binding?.llOutgoingCall?.gone()
        } else {
            binding?.tvTypeCall?.text = resources.getString(R.string.outgoing_call)
            binding?.tvTypeCallHeader?.text = resources.getString(R.string.outgoing_call)
            binding?.llIncomingCall?.gone()
            binding?.llOutgoingCall?.visible()
        }

        binding?.ivCallBackground?.visible()
        binding?.ivChangeCamera?.gone()
        binding?.enableVideoSwitch?.isChecked = false

        // Срабытывает, если мы сами звоним а потом нажимаем кнопку отмены звонка
        binding?.flButtonClose?.setOnClickListener {
            context?.vibrate()
            act?.hideHints()
            Timber.e("REJECT_CALL [flButtonClose] connectionUpTimeMs:$connectionUpTimeMs")
            if (connectionUpTimeMs == 0L) playSoundReject() else playSoundStopCallAndDisconnect()
            trackEndedCall()
        }

        // Срабатывает, если мы нажимаем кнопку отмены во время входящего звонка
        binding?.llButtonIncomingClose?.setOnClickListener {
            context?.vibrate()
            act?.hideHints()
            Timber.e("REJECT_CALL [llButtonIncomingClose]")
            playSoundReject()
            callViewModel.logCallCancel(AmplitudePropertyCallCanceller.CALLED)
        }

        binding?.flButtonMic?.setOnClickListener {
            if (micEnabled) {
                setMicDisable()
                micEnabledAppHint()
            } else {
                setMicEnable()
                micDisabledAppHint()
            }
            context?.vibrate()
        }

        binding?.flButtonOutput?.setOnClickListener {
            selectDeviceOutput()
            context?.vibrate()
        }

        // Кнопка приёма звонка
        binding?.llButtonIncomingReplyCall?.setOnClickListener {
            Timber.e("[fl_button_incoming_reply_call] ${signalingService.toString()}")
            if (isDisconnected || canDisconnect.not()) return@setOnClickListener
            if (callViewModel.isInternetConnected()) {
                mediaPlayer?.stop()
                callAccepted = true
                signalingService?.callProgressAccept()
                context?.vibrate()
                audioSwitch.activate()
                startCallView()
                showConnectionStringStatus()
                selectEarPhone()
                playSoundConnect(isLooping = true)
            } else {
                Timber.e("DISCONNECT")
                disconnect()
                NToast.with(act).text(getString(R.string.no_internet_connection)).show()
            }
        }

        binding?.ivUserAvatar?.let {
            Glide.with(this)
                    .load(callUser?.avatarSmall)
                    .placeholder(getDrawable(it.context, R.drawable.fill_8_round))
                    .apply(RequestOptions.circleCropTransform())
                    .into(it)
        }


        binding?.enableVideoSwitch?.setOnCheckedChangeListener { button, _ ->
            view.post {
                if (button.isChecked) {
                    peerConnectionClient?.startVideoSource()
                    outgoingVideoEnabled = true
                    peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_ENABLED)
                    binding?.cvFloatingVideo?.visible()
                    binding?.pipVideoView?.visible()
                    tryCatch {
                        if (wakeLock!!.isHeld)
                            wakeLock?.release()
                    }
                    if (callConnected) binding?.ivChangeCamera?.visible()
                } else {
                    outgoingVideoEnabled = false
                    peerConnectionClient?.stopVideoSource()
                    peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_DISABLED)
                    binding?.pipVideoView?.gone()
                    binding?.cvFloatingVideo?.gone()
                    binding?.ivChangeCamera?.gone()
                    tryCatch {
                        wakeLock?.acquire()
                    }
                }
                switchCameraOutput(button.isChecked)
            }
        }


        binding?.ivChangeCamera?.setOnClickListener {
            frontCamera = !frontCamera
            pipRenderer?.setMirror(frontCamera)
            // Блокируем кнопку переключения до тех пор пока не получим сигнал об успешной смене камеры
            binding?.ivChangeCamera?.isEnabled = false
            peerConnectionClient?.switchCamera(object: CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(p0: Boolean) {
                    act.runOnUiThread {
                        binding?.ivChangeCamera?.isEnabled = true
                    }
                }

                override fun onCameraSwitchError(p0: String?) {
                    act.runOnUiThread {
                        binding?.ivChangeCamera?.isEnabled = true
                    }
                }

            })
        }

        var dXView = 0f
        var dYView = 0f
        var touchXView = 0f
        var touchYView = 0f
        var movePipWindow = false
        binding?.cvFloatingVideo?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchXView = v.x
                    touchYView = v.y
                    dXView = touchXView - event.rawX
                    dYView = touchYView - event.rawY
                    movePipWindow = false
                }
                MotionEvent.ACTION_MOVE -> {

                    val positionXView = event.rawX + dXView
                    val positionYView = event.rawY + dYView

                    if (movePipWindow
                            || abs(touchXView.toInt() - positionXView.toInt()) > 50
                            || abs(touchYView.toInt() - positionYView.toInt()) > 50) {
                        movePipWindow = true
                        v.x = positionXView
                        v.y = positionYView
                    }
                }
            }
            true
        }

        uri = Uri.parse(roomUrl)

        setCallAnimation()
        setIncomingCallVibrate()
        createCallNotification()
        setPermissions()
        observeViewEvents()
    }

    override fun onStart() {
        super.onStart()
        Timber.d("CallFragment onStart()")
        act.navigatorViewPager.setAllowedSwipeDirection(NavigatorViewPager.SwipeDirection.NONE)
        if (!wakeLock!!.isHeld) {
            tryCatch {
                wakeLock?.acquire()
            }
        }
        audioSwitch.start { audioDevices, selectedAudioDevice ->
            //audioDevices.forEach { device -> Timber.d("CALL_LOGS availableAudioDevice: $device") }
            //Timber.d("CALL_LOGS selectedAudioDevice: $selectedAudioDevice")
            onAudioManagerDevicesChanged(selectedAudioDevice)
        }

        playCallRingtone()
        playProgressConnectSoundWhenPushTransit()
    }

    private fun playProgressConnectSoundWhenPushTransit() {
        if (isIncomingCall && callAccepted) {
            setAudioAttributes()
            prepareMediaPlayerBeforePlay(R.raw.sound_call_connected, isLooping = true)
            mediaPlayer?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        runCatching {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
        rxPermissions = null
        audioSwitch.stop()
    }

    private fun showConnectionStringStatus() {
        binding?.tvCallTime?.visible()
        binding?.tvCallTime?.text = getString(R.string.general_connect)
    }

    private fun trackEndedCall() {
        if (callAccepted || callStartedTimeMs > 0) {
            callViewModel.logCall(
                callStartedTimeMs,
                isIncomingCall,
                outgoingVideoEnabled || incomingVideoEnabled
            )
        } else {
            callViewModel.logCallCancel(AmplitudePropertyCallCanceller.CALLER)
        }
    }

    override fun onHideHints() {
        super.onHideHints()
        act?.hideHints()
        hideTooltips()
    }

    private fun micEnabledAppHint() {
        hideTooltips()

        micTurnedOnTooltipJob = lifecycleScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            micTurnedOnTooltip?.isTouchable = false
            micTurnedOnTooltip?.isOutsideTouchable = false
            binding?.flButtonOutput?.let {
                micTurnedOnTooltip?.showForSpeakerButton(
                        fragment = this@CallFragment,
                        view = it,
                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                )
            }
            delay(TooltipDuration.MIC_ON_OFF)
            micTurnedOnTooltip?.dismiss()
        }
    }

    private fun micDisabledAppHint() {
        hideTooltips()

        micTurnedOffTooltipJob = lifecycleScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            micTurnedOffTooltip?.isTouchable = false
            micTurnedOffTooltip?.isOutsideTouchable = false

            binding?.flButtonOutput?.let {
                micTurnedOffTooltip?.showForSpeakerButton(
                        fragment = this@CallFragment,
                        view = it,
                        gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                )
            }
            delay(TooltipDuration.MIC_ON_OFF)
            micTurnedOffTooltip?.dismiss()
        }
    }

    override fun callIsStopped() {
        Timber.e("[CallAct] CallIsStopped")
        vibrator.cancel()
        view?.post {
            playSoundStopCallAndDisconnect()
        }
    }

    override fun callIsRejected() {
        Timber.e("[CallAct] CallIsRejected")
        vibrator.cancel()
        view?.post {
            Timber.e("REJECT_CALL")
            playSoundReject()
        }
    }

    override fun callIsAccepted(roomIdInProc: Long, uuid: String?) {
        Timber.e("START_CALL")
        this.uuId = uuid
        this.roomId = roomIdInProc
        vibrator.cancel()
        showConnectionStringStatus()
        initWebrtcCall()
        startCallView()
    }

    override fun lineIsBusy() {
        Timber.e("[CallAct] LineIsBusy")
        vibrator.cancel()
        if (!isIncomingCall) {
            view?.post {
                Timber.e("REJECT_CALL")
                playSoundReject()
            }
        }
    }

    override fun onDeviceSelected(device: AudioDevice) {
        // Timber.d("CALL_LOGS onDeviceSelected:$device")
        audioSwitch.selectDevice(device)
        audioSwitch.activate()
    }

    private fun initCallActivity() {
        binding?.flCallAccessPermissions?.gone()

        Thread.setDefaultUncaughtExceptionHandler(UnhandledExceptionHandler(act))

        iceConnected = false
        signalingParameters = null

        // Create UI controls.
        pipRenderer = binding?.pipVideoView
        fullscreenRenderer = binding?.fullscreenVideoView

        remoteSinks.add(remoteProxyRenderer)

        eglBase = EglBase.create()

        // Create video renderers.
        pipRenderer?.init(eglBase.eglBaseContext, null)
        pipRenderer?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        val saveRemoteVideoToFile = act.intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)
        // When saveRemoteVideoToFile is set we save the video from the remote to a file.
        if (saveRemoteVideoToFile != null) {
            val videoOutWidth = act.intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0)
            val videoOutHeight = act.intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0)
            try {
                videoFileRenderer = VideoFileRenderer(
                        saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.eglBaseContext)
                remoteSinks.add(videoFileRenderer!!)
            } catch (e: IOException) {
                throw RuntimeException(
                        "Failed to open video file for output: $saveRemoteVideoToFile", e)
            }
        }
        fullscreenRenderer?.init(eglBase.eglBaseContext, null)
        fullscreenRenderer?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        fullscreenRenderer?.setZOrderMediaOverlay(false)

        pipRenderer?.setZOrderMediaOverlay(true)
        pipRenderer?.setEnableHardwareScaler(true /* enabled */)
        pipRenderer?.setMirror(true)
        fullscreenRenderer?.setEnableHardwareScaler(true /* enabled */)

        if (uri == null) {
            logAndToast(getString(org.appspot.apprtc.R.string.missing_url))
            Timber.e("Didn't get any URL in intent!")
            return
        }

        screencaptureEnabled = useScreencapture
        // If capturing format is not specified for screencapture, use screen resolution.
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            val displayMetrics = displayMetrics
            videoWidth = displayMetrics.widthPixels
            videoHeight = displayMetrics.heightPixels
        }
        var dataChannelParameters: PeerConnectionClient.DataChannelParameters? = null
        if (dataChannelEnabled) {
            dataChannelParameters = PeerConnectionClient.DataChannelParameters(
                    ordered,
                    maxRetrMs,
                    maxRetr,
                    protocol,
                    negotiated,
                    tempId)
        }
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters(true, loopback,
                tracing, videoWidth, videoHeight, cameraFps,
                videoStartBitrate, videoCodec,
                hwCodec,
                flexfecEnabled,
                audioStartBitrate, audioCodec,
                noAudioProcessing,
                aecDump,
                saveInputAudioToFile,
                useOpenSLES,
                disableBuiltInAEC,
                disableBuiltInAGC,
                disableBuiltInNS,
                disableWebRtcAGCAndHPF,
                rtcEventLogEnabled,
                useLegacyAudioDevice, dataChannelParameters)
        commandLineRun = commandLineRun //todo что??


        // Create connection parameters.
        val urlParameters = act.intent.getStringExtra(EXTRA_URLPARAMETERS)
        roomConnectionParameters = AppRTCClient.RoomConnectionParameters(uri.toString(), "", loopback, urlParameters)

        // Create peer connection client.
        peerConnectionClient = PeerConnectionClient(
                act.applicationContext, eglBase, peerConnectionParameters!!, this@CallFragment)
        val options = PeerConnectionFactory.Options()
        if (loopback) {
            options.networkIgnoreMask = 0
        }
        peerConnectionClient!!.createPeerConnectionFactory(options)

        activityRunning = true

        val parameters = AppRTCClient.SignalingParameters(
                ArrayList(),
                true,
                null, null, null, null, null)
        videoCapturer = createVideoCapturer()
        peerConnectionClient?.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, parameters)
        peerConnectionClient?.setVideoEnabled(true)
        peerConnectionClient?.stopVideoSource()
        localProxyVideoSink.setTarget(pipRenderer)
        remoteProxyRenderer.setTarget(fullscreenRenderer)
        if (callAccepted) {
            signalingService?.callProgressAccept()
            context?.vibrate()
            startCallView()
        }
    }

    private fun initWebrtcCall() {
        Timber.e("INIT Web rtc call")
        signalingService?.registerWebRtcClient(this@CallFragment)
        appRtcClient = signalingService
        startCall()
    }

    /**
     * Check audio connected devices and show popup with options
     */
    private fun showAvailableDevices() {
        if (AudioSwitchBottomSheet.isShowed(childFragmentManager)) return
        if (deviceHasExtraOutput()) {
            AudioSwitchBottomSheet.showBottomMenu(
                fm = childFragmentManager,
                devices = audioSwitch.availableAudioDevices.toList(),
                selected = audioSwitch.selectedAudioDevice,
            )
        }
    }

    private fun setMicDisable() {
        view?.post {
            if (peerConnectionClient != null) {
                binding?.flButtonMic?.isSelected = false
                binding?.ivMic?.isSelected = false
                peerConnectionClient?.setAudioEnabled(true)
                micEnabled = false
            }
        }
    }

    private fun setMicEnable() {
        view?.post {
            if (peerConnectionClient != null) {
                binding?.flButtonMic?.isSelected = true
                binding?.ivMic?.isSelected = true
                peerConnectionClient?.setAudioEnabled(false)
                micEnabled = true
            }
        }
    }

    private fun setRemoteVideoDisable() {
        view?.post {
            if (peerConnectionClient != null) {
                incomingVideoEnabled = false
                configIncomingVideo()
            }
        }
    }

    private fun setRemoteVideoEnable() {
        view?.post {
            if (peerConnectionClient != null) {
                incomingVideoEnabled = true
                configIncomingVideo()
            }
        }
    }

    private fun startCallView() {
        stopCallAnimation()
        binding?.vectorAnimationCall?.gone()
        binding?.llIncomingCall?.gone()
        binding?.llOutgoingCall?.visible()
    }

    private fun configIncomingVideo() {
        if (incomingVideoEnabled) {
            binding?.ivUserAvatar?.gone()
            binding?.tvTypeCall?.gone()
            binding?.tvUserName?.gone()
            binding?.tvCallTime?.gone()
            binding?.tvTypeCallHeader?.visible()
            binding?.tvCallTimeHeader?.visible()
            binding?.ivCallBackground?.gone()
        } else {
            binding?.ivUserAvatar?.visible()
            binding?.tvTypeCall?.visible()
            binding?.tvUserName?.visible()
            binding?.tvCallTime?.visible()
            binding?.tvTypeCallHeader?.gone()
            binding?.tvCallTimeHeader?.gone()
            binding?.ivCallBackground?.visible()
        }
        switchCameraOutput(incomingVideoEnabled)
    }

    @SuppressLint("ResourceType")
    private fun setCallAnimation() {
        callAnimationDrawable = act.getDrawable(R.animator.avd_anim_call) as AnimatedVectorDrawable
        binding?.vectorAnimationCall?.setImageDrawable(callAnimationDrawable)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            callAnimationCallback = object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(drawable: Drawable?) {
                    super.onAnimationEnd(drawable)
                    binding?.vectorAnimationCall?.post { callAnimationDrawable.start() }
                }
            }
            callAnimationDrawable.registerAnimationCallback(callAnimationCallback)
        }
        callAnimationDrawable.start()
    }

    private fun stopCallAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            callAnimationDrawable.clearAnimationCallbacks()
        }
        callAnimationDrawable.stop()
    }

    private fun startCallTimer() {
        callTimerHandler.postDelayed(object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run() {
                val sdf = SimpleDateFormat("mm:ss")
                val resultdate = Date(System.currentTimeMillis() - connectionUpTimeMs)
                val time = sdf.format(resultdate)
                view?.post {
                    binding?.tvCallTimeHeader?.text = time
                    binding?.tvCallTime?.text = time
                }
                callTimerHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private val displayMetrics: DisplayMetrics
        @TargetApi(17)
        get() {
            val displayMetrics = DisplayMetrics()
            val windowManager = act.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            return displayMetrics
        }

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Logging.d(TAG, "Dropping frame in proxy because target is null.")
                return
            }
            target!!.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

    private fun useCamera2(): Boolean {
        return Camera2Enumerator.isSupported(context) && useCamera2
    }

    private fun captureToTexture(): Boolean {
        return captureToTexture
    }

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        // First, try to find front facing camera
        Logging.d(TAG, "Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)

                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        return null
    }

    override fun onDestroy() {
        Timber.e("DISCONNECT")
        vibrator.cancel()
        notificationManager?.cancel(CALL_NOTIFICATION_ID)
        callTimerHandler.removeCallbacksAndMessages(null)
        Thread.setDefaultUncaughtExceptionHandler(null)
        act.unregisterReceiver(callReceiver)
        disconnect()
        if (logToast != null) {
            logToast!!.cancel()
        }
        activityRunning = false
        super.onDestroy()
    }

    private fun startCall() {
        if (appRtcClient == null || roomConnectionParameters == null) {
            Timber.e("AppRTC client is not allocated for a call.")
            return
        }
        callStartedTimeMs = System.currentTimeMillis()
        appRtcClient?.connectToRoom(roomConnectionParameters)
    }

    // Should be called from UI thread
    private fun callConnected() {
        Timber.e("CALL_LOGS ......CALL_CONNECTED: delay=${System.currentTimeMillis() - callStartedTimeMs} ms")
        callConnected = true

        if (isIncomingCall) {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        } else {
            playSoundConnect(isLooping = false)
        }

        if (callWasInterrupted) {
            callWasInterrupted = false
        } else {
            connectionUpTimeMs = System.currentTimeMillis()
        }

        binding?.tvCallTime?.visible()

        if (peerConnectionClient == null || isError) {
            Timber.d("Call is connected in closed or error state")
        }

        peerConnectionClient?.enableStatsEvents(true, STAT_CALLBACK_PERIOD)

        if (outgoingVideoEnabled) {
            sendMessageCameraEnabled()
        } else {
            sendMessageCameraDisabled()
        }

        uuId?.let { uuid ->
            roomId?.let { id ->
                callViewModel.onCallStarted(uuid, id)
            }
        }
        startCallTimer()
    }

    private fun sendMessageCameraEnabled() {
        doDelayed(DELAY_SEND_DATA_CHANNEL_MESSAGE){
            Timber.e("SEND Message: WEBRTC_SET_CAMERA_ENABLED")
            peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_ENABLED)
            peerConnectionClient?.stopVideoSource()
            peerConnectionClient?.startVideoSource()
            binding?.ivChangeCamera?.visible()
        }
    }

    private fun sendMessageCameraDisabled() {
        doDelayed(DELAY_SEND_DATA_CHANNEL_MESSAGE) {
            Timber.e("SEND Message: WEBRTC_SET_CAMERA_DISABLED")
            peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_DISABLED)
            peerConnectionClient?.stopVideoSource()
            binding?.ivChangeCamera?.gone()
        }
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private fun onAudioManagerDevicesChanged(selectedDevice: AudioDevice?) {
        //Timber.e("CALL_LOGS onAudionDeviceCHANGED:$selectedDevice")
        val selectedBackground = selectedDevice !is AudioDevice.Earpiece
        val tintColor = when (selectedDevice) {
            is AudioDevice.Earpiece -> R.color.white
            else -> R.color.ui_purple
        }
        binding?.flButtonOutput?.isSelected = selectedBackground
        binding?.ivDevice?.setTint(tintColor)
        val imageRes = when (selectedDevice) {
            is AudioDevice.Speakerphone -> R.drawable.ic_switch_audio_speakerphone
            is AudioDevice.Earpiece -> R.drawable.ic_switch_audio_speakerphone
            is AudioDevice.BluetoothHeadset -> R.drawable.ic_switch_audio_bluetooth_headset
            is AudioDevice.WiredHeadset -> R.drawable.ic_switch_audio_wired_headset
            else -> -1
        }
        binding?.ivDevice?.setImageResource(imageRes)
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    @Synchronized
    fun disconnect() {
        ringtoneAudioFocusManager?.releaseAudioFocus()

        if (isDisconnected) return
        isDisconnected = true

        videoCapturer?.stopCapture()
        videoCapturer?.dispose()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            callAnimationDrawable.unregisterAnimationCallback(callAnimationCallback)
            callAnimationDrawable.reset()
        }

        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null

        notificationManager?.cancel(CALL_NOTIFICATION_ID)

        if (callConnected)
            signalingService?.callProgressStop()
        else
            signalingService?.callProgressReject()

        activityRunning = false
        remoteProxyRenderer.setTarget(null)
        localProxyVideoSink.setTarget(null)
        if (appRtcClient != null) {
            appRtcClient!!.disconnectFromRoom()
            appRtcClient = null
        }
        if (pipRenderer != null) {
            pipRenderer!!.release()
            pipRenderer = null
        }
        if (videoFileRenderer != null) {
            videoFileRenderer!!.release()
            videoFileRenderer = null
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer!!.release()
            fullscreenRenderer = null
        }
        if (peerConnectionClient != null) {
            peerConnectionClient!!.close()
            peerConnectionClient = null
        }
        Timber.e("------------------------- DISCONNECT. FINISH ---------------------------------")
        uuId?.let { uuid ->
            roomId?.let { id ->
                callViewModel.onCallFinished(uuid, id)
            }
        }
        act.onCallFinished()
    }

    private fun disconnectWithErrorMessage(errorMessage: String) {
        Timber.e("Critical error: $errorMessage")
    }

    // Log |msg| and Toast about it.
    private fun logAndToast(msg: String) {
        Timber.e("CALL_LOG :::::  $msg")
    }

    private fun reportError(description: String) {
        view?.post {
            if (!isError) {
                isError = true
                disconnectWithErrorMessage(description)
            }
        }
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        val videoFileAsCamera = act.intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA)
        when {
            videoFileAsCamera != null -> {
                try {
                    videoCapturer = FileVideoCapturer(videoFileAsCamera)
                } catch (e: IOException) {
                    reportError("Failed to open video file for emulated camera")
                    Timber.e("Failed to open video file for emulated camera")
                    return null
                }
            }
            useCamera2() -> {
                if (!captureToTexture()) {
                    reportError(getString(org.appspot.apprtc.R.string.camera2_texture_only_error))
                    return null
                }

                Logging.d(TAG, "Creating capturer using camera2 API.")
                videoCapturer = createCameraCapturer(Camera2Enumerator(context))
            }
            else -> {
                Logging.d(TAG, "Creating capturer using camera1 API.")
                videoCapturer = createCameraCapturer(Camera1Enumerator(captureToTexture()))
            }
        }
        if (videoCapturer == null) {
            reportError("Failed to open camera")
            return null
        }
        return videoCapturer
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    @SuppressLint("BinaryOperationInTimber")
    private fun onConnectedToRoomInternal(params: AppRTCClient.SignalingParameters) {
        Timber.e("!!!!! onConnectedToRoomInternal")
        val delta = System.currentTimeMillis() - callStartedTimeMs
        signalingParameters = params
        logAndToast("Creating peer connection, delay=" + delta + "ms")
        videoCapturer = createVideoCapturer()

        peerConnectionClient?.createPeerConnection(
                localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters)
        if (signalingParameters!!.initiator) {
            Timber.d(" Creating OFFER...: ${signalingParameters?.initiator}")
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
            peerConnectionClient?.createOffer()
        } else {
            Timber.d("Creating ANSWER...: ${signalingParameters?.initiator}  " +
                "params.offerSdp: ${params.offerSdp}  ice candidates: ${params.iceCandidates}")
            if (params.offerSdp != null) {
                peerConnectionClient?.setRemoteDescription(params.offerSdp)
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient?.createAnswer()
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (iceCandidate in params.iceCandidates) {
                    peerConnectionClient?.addRemoteIceCandidate(iceCandidate)
                }
            }
        }
    }

    override fun onConnectedToRoom(params: AppRTCClient.SignalingParameters) {
        Timber.e("!!!!! onConnectedToRoom")
        view?.post { onConnectedToRoomInternal(params) }
    }

    override fun onRemoteDescription(sdp: SessionDescription) {
        Timber.e("!!!!! onRemoteDescription")
        val delta = System.currentTimeMillis() - callStartedTimeMs
        view?.post(Runnable {
            if (peerConnectionClient == null) {
                Timber.e("Received remote SDP for non-initilized peer connection.")
                return@Runnable
            }

            peerConnectionClient!!.setRemoteDescription(sdp)
            if (!signalingParameters!!.initiator) {
                logAndToast("Creating ANSWER...")
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient!!.createAnswer()
            }
        })
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate) {
        Timber.e("CALL_FRAGMENT onRemoteIceCandidate() CANDIDATE:${candidate.serverUrl}")
        view?.post(Runnable {
            if (peerConnectionClient == null) {
                Timber.e("Received ICE candidate for a non-initialized peer connection.")
                return@Runnable
            }
            peerConnectionClient?.addRemoteIceCandidate(candidate)
        })
    }

    override fun onRemoteIceCandidatesRemoved(candidates: Array<IceCandidate>) {
        Timber.e("!!!!! onRemoteIceCandidatesRemoved")
        view?.post(Runnable {
            if (peerConnectionClient == null) {
                Timber.e("Received ICE candidate removals for a non-initialized peer connection.")
                return@Runnable
            }
            peerConnectionClient!!.removeRemoteIceCandidates(candidates)
        })
    }

    override fun onChannelClose() {
        Timber.e("!!!!! onChannelClose")
        view?.post {
            logAndToast("Remote end hung up; dropping PeerConnection")
        }
    }

    override fun onChannelError(description: String) {
        Timber.e("!!!!! onChannelError")
        reportError(description)
        Timber.e("REJECT_CALL")
        playSoundReject()
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    override fun onLocalDescription(sdp: SessionDescription) {
        Timber.e("!!!!! onLocalDescription")
        val delta = System.currentTimeMillis() - callStartedTimeMs
        view?.post {
            if (appRtcClient != null) {
                logAndToast("Sending " + sdp.type + ", delay=" + delta + "ms")
                if (signalingParameters!!.initiator) {
                    appRtcClient!!.sendOfferSdp(sdp)
                } else {
                    appRtcClient!!.sendAnswerSdp(sdp)
                }
            }
            if (peerConnectionParameters!!.videoMaxBitrate > 0) {
                Timber.d("Set video maximum bitrate: ${peerConnectionParameters?.videoMaxBitrate}")
                peerConnectionClient!!.setVideoMaxBitrate(peerConnectionParameters?.videoMaxBitrate)
            }
        }
    }

    // обрабатывать каждый раз, когда обнаруживается кандидат ICE
    // Затем вам нужно отправить кандидата своему контакту через свой сигнальный механизм
    override fun onIceCandidate(candidate: IceCandidate) {
        Timber.e("CALL_FRAGMENT onIceCandidate() AppRtc client:$appRtcClient CANDIDATE:${candidate.serverUrl}")
        view?.post {
            if (appRtcClient != null) {
                Timber.d("SEND Local ICE Candidate:${candidate.serverUrl}")
                appRtcClient?.sendLocalIceCandidate(candidate)
            }
        }
    }

    override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {
        Timber.e("CALL_FRAGMENT onIceCandidatesRemoved")
        view?.post {
            if (appRtcClient != null) {
                appRtcClient?.sendLocalIceCandidateRemovals(candidates)
            }
        }
    }

    override fun onConnectionEstablished() {
        Timber.d("CALL_LOGS CALL_FRAGMENT => CONGRATS!!! onIceConnected()")
        view?.post {
            Timber.d("ICE connected, delay=${System.currentTimeMillis() - callStartedTimeMs} ms")
            callAccepted = true
            iceConnected = true
            callConnected()
        }
    }

    override fun onDisconnect() {
        callWasInterrupted = true
        playSoundReconnecting()
    }

    override fun onConnectionClosed() {
        Timber.e("CALL_LOGS onConnectionClosed()")
        iceConnected = false
        view?.post {
            playSoundStopCallAndDisconnect()
        }
    }

    private fun observeViewEvents() {
        callViewModel.viewEventFlow.onEach { event ->
            Timber.e("OBSERVE_CALL Events:$event")
            when (event) {
                is CallViewEvent.StopCall -> {
                    doDelayed(100) {
                        playSoundStopCallAndDisconnect()
                        trackEndedCall()
                    }
                }
                is CallViewEvent.RejectCall -> playSoundReject()
                else -> event.toString()
            }
        }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    override fun onPeerConnectionStatsReady(reports: Array<StatsReport>) = Unit

    override fun onPeerConnectionError(description: String) {
        Timber.e("!!!!! onPeerConnectionError $description")
        view?.post {
            Timber.e("REJECT_CALL")
            playSoundReject()
        }
    }

    override fun onDataChannelMessage(message: String?) {
        Timber.d("DATA_CHANNEL: $message")
        when (message) {
            WEBRTC_SET_CAMERA_DISABLED -> setRemoteVideoDisable()
            WEBRTC_SET_CAMERA_ENABLED -> setRemoteVideoEnable()
            WEBRTC_SET_MIC_DISABLED -> Timber.d("REMOTE_MIC_IS_DISABLED")
            WEBRTC_SET_MIC_ENABLED -> Timber.d("REMOTE_MIC_IS_ENABLED")
        }
    }


    private val callReceiver = object : CallBroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            signalingService?.callProgressStop()
            Timber.d("REJECT_CALL")
            playSoundReject()
            super.onReceive(context, intent)
        }
    }

    private fun createCallNotification() {
        val intentFilter = IntentFilter(CallBroadcastReceiver.BROADCAST_INTENT)
        callReceiver.register(
            context = act,
            filter = intentFilter
        )
        val broadcastIntent = Intent(CallBroadcastReceiver.BROADCAST_INTENT)
        val broadcastPendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, getNotificationPendingIntentFlag())
        val i = Intent(context, Act::class.java)
        val callActivityIntent = PendingIntent.getActivity(context, 0, i, getNotificationPendingIntentFlag())
        notificationManager = act.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = androidx.core.app.NotificationCompat.Builder(requireContext(), CALL_NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(callUser?.name)
                .setSmallIcon(R.drawable.phone_icon)
                .setColor(this.resources.getColor(R.color.ui_purple))
                .setContentIntent(callActivityIntent)
                .setContentText(getString(R.string.current_call))
                .setOngoing(true)
                .addAction(R.drawable.close_call, getString(R.string.end_call), broadcastPendingIntent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notifiactionChannel: NotificationChannel? = null
            notifiactionChannel = NotificationChannel(
                CALL_NOTIFICATION_CHANNEL_ID,
                "NOOMEERA TELEPHONY",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager?.createNotificationChannel(notifiactionChannel)
        } else {
            builder.setContentTitle(callUser?.name)
                    .setAutoCancel(true)
        }
        builder.priority = androidx.core.app.NotificationCompat.PRIORITY_LOW
        notificationManager?.notify(CALL_NOTIFICATION_ID, builder.build())
    }

    private fun setPermissions() {
        rxPermissions = RxPermissions(act)
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
        }.toTypedArray()

        rxPermissions?.request(*permissions)
            ?.subscribe(
                { granted ->
                    if (granted) {
                        initCallActivity()
                    } else {
                        showAccessPermissionsScreen(permissions)
                    }
                },
                { error -> Timber.e(error) }
            )
            ?.also { compositeDisposable.add(it) }
    }

    private fun showAccessPermissionsScreen(permissions: Array<String>) {
        binding?.flCallAccessPermissions?.visible()
        binding?.btnCallAllowAccess?.setOnClickListener {
            handleClickCallAllowAccess(permissions)
        }
        binding?.tvCallCancelAccess?.setOnClickListener {
            Timber.e("REJECT_CALL")
            playSoundReject()
        }
    }

    private fun handleClickCallAllowAccess(permissions: Array<String>) {
        val isShowRequestRationale = isShowRequestCallPermissionsRationale(*permissions)
        if (!isShowRequestRationale) {
            requireContext().openSettingsScreen()
        } else {
            setPermissions()
        }
    }

    private fun isShowRequestCallPermissionsRationale(vararg permissions: String): Boolean {
        permissions.forEach { permission ->
            val rationale = shouldShowRequestPermissionRationale(permission)
            if (!rationale) {
                return false
            }
        }
        return true
    }

    private fun setIncomingCallVibrate() {
        if (isIncomingCall) {
            val pattern = longArrayOf(0, 200, 100, 200, 1000)
            val audioAttributes = ringtoneAudioFocusManager?.getCallRingtoneAudioAttributes()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0), audioAttributes)
            } else {
                vibrator.vibrate(pattern, 0, audioAttributes)
            }
        }
    }

    private fun playCallRingtone() {
        if (isIncomingCall && !iceConnected) {
            if (!callAccepted) {
                playIncomeCallRingtone()
            }
        } else {
            if (!callAccepted) {
                playOutgoingCallRingtone()
            }
        }
    }

    private fun toggleSpeaker() {
        view?.post {
            val deviceToSelect = when (audioSwitch.selectedAudioDevice) {
                is AudioDevice.Earpiece -> audioSwitch.availableAudioDevices.find { it is AudioDevice.Speakerphone }
                is AudioDevice.Speakerphone -> audioSwitch.availableAudioDevices.find { it is AudioDevice.Earpiece }
                else -> null
            }
            if (deviceToSelect != null) {
                audioSwitch.selectDevice(deviceToSelect)
                audioSwitch.activate()
            }
        }
    }

    private fun selectSpeakerPhone() {
        audioSwitch.availableAudioDevices.find { it is AudioDevice.Speakerphone }?.let { speakerPhone ->
            onDeviceSelected(speakerPhone)
        }
    }

    private fun selectEarPhone() {
        audioSwitch.availableAudioDevices.find { it is AudioDevice.Earpiece }?.let { speakerPhone ->
            onDeviceSelected(speakerPhone)
        }
    }

    private fun switchCameraOutput(isEnabled: Boolean) {
        when {
            isEnabled && deviceHasExtraOutput()-> showAvailableDevices()
            isEnabled -> selectSpeakerPhone()
            else -> Unit
        }
    }

    private fun selectDeviceOutput() {
        if (deviceHasExtraOutput()) {
            showAvailableDevices()
        } else {
            toggleSpeaker()
        }
    }

    private fun deviceHasExtraOutput(): Boolean {
        return audioSwitch.availableAudioDevices.size > 1 &&
            audioSwitch.availableAudioDevices.any { it is AudioDevice.BluetoothHeadset } ||
            audioSwitch.availableAudioDevices.any { it is AudioDevice.WiredHeadset }
    }

    private fun hideTooltips() {
        speakerTurnedOnTooltipJob?.cancel()
        speakerTurnedOnTooltip?.dismiss()

        speakerTurnedOffTooltipJob?.cancel()
        speakerTurnedOffTooltip?.dismiss()

        micTurnedOffTooltipJob?.cancel()
        micTurnedOffTooltip?.dismiss()

        micTurnedOnTooltipJob?.cancel()
        micTurnedOnTooltip?.dismiss()
    }

    private fun setSpeakerDisabled() {
        runCatching {
            if (wakeLock?.isHeld == true) {
                wakeLock?.acquire()
            }
        }
        if (mediaPlayer != null && mediaPlayer!!.isPlaying && !callConnected) {
            playCallRingtone()
        }
    }

    private fun playIncomeCallRingtone() {
        Timber.d("CALL_SOUND_LOG Play INCOME RingTone")
        selectSpeakerPhone()
        playFirstRingtone(R.raw.sound_incoming_call, isCheckSilentMode = true)
    }

    private fun playOutgoingCallRingtone() {
        Timber.d("CALL_SOUND_LOG play outgoing sound")
        playFirstRingtone(R.raw.sound_outgoing_call, isCheckSilentMode = false)
    }

    private fun playFirstRingtone(@RawRes ringtoneRes: Int, isCheckSilentMode: Boolean) {
        setAudioAttributes()
        prepareMediaPlayerBeforePlay(ringtoneRes, isLooping = true, isCheckSilentMode)
        mediaPlayer?.start()
    }

    private fun setAudioAttributes() {
        mediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setLegacyStreamType(streamAudioType).build())
        androidAudioManager?.getStreamMaxVolume(streamAudioType)?.let { maxVolume ->
            androidAudioManager?.setStreamVolume(streamAudioType, maxVolume, 0)
        }
    }

    private fun playSoundConnect(isLooping: Boolean) {
        prepareMediaPlayerBeforePlay(R.raw.sound_call_connected, isLooping = isLooping)
        mediaPlayer?.start()
        ringtoneAudioFocusManager?.releaseAudioFocus()
    }

    //TODO https://nomera.atlassian.net/browse/BR-19415
    private fun playSoundReconnecting() {
        try {
            prepareMediaPlayerBeforePlay(R.raw.sound_call_reconnected, isLooping = true)
            mediaPlayer?.start()
        } catch (e:Exception) {
            Timber.e(e.message)
        }
    }

    private fun playSoundReject() {
        if (canDisconnect) {
            canDisconnect = false
            prepareMediaPlayerBeforePlay(R.raw.sound_call_rejected, isLooping = false)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                Timber.e("DISCONNECT")
                disconnect()
                canDisconnect = true
            }
        }
        ringtoneAudioFocusManager?.releaseAudioFocus()
    }

    private fun playSoundStopCallAndDisconnect() {
        Timber.d("PLAY Sound Stop call >>>>>>> and DISCONNECT isDisconnected:$isDisconnected canDisconnect:$canDisconnect")
        if (!isDisconnected) {
            runCatching {
                canDisconnect = false
                prepareMediaPlayerBeforePlay(R.raw.sound_end_call, isLooping = false)
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener {
                    disconnect()
                    canDisconnect = true
                }
            }.onFailure {
                Timber.e(it)
                disconnect()
            }
        }
    }

    private fun prepareMediaPlayerBeforePlay(
        @RawRes soundRawResource: Int,
        isLooping: Boolean,
        isCheckSilentMode: Boolean = false
    ) {
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        val afd = resources.openRawResourceFd(soundRawResource) as AssetFileDescriptor
        mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer?.prepare()
        if (isCheckSilentMode) checkSilentModeWhenPlayingSound()
        mediaPlayer?.isLooping = isLooping
    }

    private fun checkSilentModeWhenPlayingSound() {
        if (playSoundWhenSilentMode()) unMutePlayer() else mutePlayer()
    }

    private fun playSoundWhenSilentMode(): Boolean {
        return when (androidAudioManager?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> false
            else -> true
        }
    }

    private fun mutePlayer() {
        mediaPlayer?.setVolume(0f, 0f)
    }

    private fun unMutePlayer() {
        mediaPlayer?.setVolume(1f, 1f)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (isIncomingCall && mediaPlayer!!.isPlaying) {
                    vibrator.cancel()
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    true
                } else {
                    false
                }
            }
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (isIncomingCall && mediaPlayer!!.isPlaying) {
                    mediaPlayer?.stop()
                    mediaPlayer?.reset()
                    vibrator.cancel()
                    true
                } else {
                    false
                }
            }
            else -> false
        }

    }

    override fun onBackPressed(): Boolean {
        hideTooltips()
        ringtoneAudioFocusManager?.releaseAudioFocus()
        if (!isDisconnected) {
            Timber.e("DISCONNECT")
            disconnect()
        }
        return true
    }

}
