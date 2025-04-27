package com.numplates.nomera3.telecom

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.AssetFileDescriptor
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
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.register
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.db.models.dialog.UserChat
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.CALL_NOTIFICATION_CHANNEL_ID
import com.numplates.nomera3.CALL_NOTIFICATION_ID
import com.numplates.nomera3.INCOMING_CALL_KEY
import com.numplates.nomera3.R
import com.numplates.nomera3.TYPE_CALL_KEYS
import com.numplates.nomera3.WEBRTC_ROOM_URL
import com.numplates.nomera3.WEBRTC_SET_CAMERA_DISABLED
import com.numplates.nomera3.WEBRTC_SET_CAMERA_ENABLED
import com.numplates.nomera3.WEBRTC_SET_MIC_DISABLED
import com.numplates.nomera3.WEBRTC_SET_MIC_ENABLED
import com.numplates.nomera3.databinding.MeeraFragmentCallBinding
import com.numplates.nomera3.modules.audioswitch.ui.AudioSwitchBottomSheet
import com.numplates.nomera3.modules.audioswitch.ui.MeeraAudioSwitchBottomSheet
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallCanceller
import com.numplates.nomera3.modules.calls.presentation.CallViewEvent
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.viewmodel.CallViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import com.twilio.audioswitch.AudioDevice
import com.twilio.audioswitch.AudioSwitch
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import kotlin.math.abs
import kotlin.properties.Delegates

private const val NOTIFICATION_CHANNEL_NAME = "MEERA TELEPHONY"
private const val USE_SCREEN_CAPTURE = false
private const val STAT_CALLBACK_PERIOD = 1000
private const val CALL_TIMER_DELAY_MS = 1000L
private const val DELAY_SEND_DATA_CHANNEL_MESSAGE = 2000L
private const val STREAM_AUDIO_TYPE = AudioManager.STREAM_VOICE_CALL
private const val CALL_TIMER_TEXT_FORMAT = "mm:ss"

private const val DATA_CHANNEL_ENABLED = true

private const val DATA_CHANNEL_ORDERED = true
private const val DATA_CHANNEL_NEGOTIATED = false
private const val DATA_CHANNEL_MAX_RETRY_MS = -1
private const val DATA_CHANNEL_MAX_RETRY = -1
private const val DATA_CHANNEL_TEMP_ID = -1
private const val DATA_CHANNEL_PROTOCOL = ""

private const val PEER_CONNECTION_LOOPBACK = false
private const val PEER_CONNECTION_TRACING = false
private const val PEER_CONNECTION_CAMERA_FPS = 0
private const val PEER_CONNECTION_VIDEO_START_BITRATE = 1700
private const val PEER_CONNECTION_AUDIO_START_BITRATE = 32
private const val PEER_CONNECTION_VIDEO_CODEC = "VP8"
private const val PEER_CONNECTION_AUDIO_CODEC = "OPUS"
private const val PEER_CONNECTION_HW_CODEC = true
private const val PEER_CONNECTION_FLEX_FEC_ENABLED = false
private const val PEER_CONNECTION_NO_AUDIO_PROCESSING = false
private const val PEER_CONNECTION_AEC_DUMP = false
private const val PEER_CONNECTION_SAVE_INPUT_AUDIO_TO_FILE = false
private const val PEER_CONNECTION_USE_OPEN_SL_ES = false
private const val PEER_CONNECTION_DISABLE_BUILT_IN_AEC = false
private const val PEER_CONNECTION_DISABLE_BUILT_IN_AGC = false
private const val PEER_CONNECTION_DISABLE_BUILT_IN_NS = false
private const val PEER_CONNECTION_DISABLE_WEB_RTC_AGC_AND_HPF = false
private const val PEER_CONNECTION_RTC_EVENT_LOG_ENABLED = false
private const val PEER_CONNECTION_USE_LEGACY_AUDIO_DEVICE = false

private const val USE_CAMERA_2 = true
private const val CAPTURE_TO_TEXTURE = true
private const val TOOLTIP_MARGIN_BOTTOM = 176
private const val INCOMING_CALL_BUTTONS_MARGIN = 40
private const val INCOMING_CALL_DEFAULT_BUTTONS_MARGIN = 16
private const val HALF_SCREEN_PERCENT = 50
private const val DELAY_BEFORE_STOP_CALL = 100L

const val EXTRA_URLPARAMETERS = "org.appspot.apprtc.URLPARAMETERS"
const val EXTRA_VIDEO_FILE_AS_CAMERA = "org.appspot.apprtc.VIDEO_FILE_AS_CAMERA"
const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE"
const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_WIDTH"
const val EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT = "org.appspot.apprtc.SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT"


/**
 * https://www.figma.com/design/wyLhqHbHkvWWjLHznv6Wz8/Social-Chat-New?node-id=5060-71306&t=13nek5cWoeSSgOpO-1
 */
class MeeraCallFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_call,
    behaviourConfigState = ScreenBehaviourState.Calls
),
    CallProgressEvents,
    PeerConnectionClient.PeerConnectionEvents,
    AppRTCClient.SignalingEvents,
    AudioSwitchBottomSheet.Listener {

    interface CallsOnActivityInteraction {
        fun onCallFragmentAction(actions: CallFragmentActions)
    }

    sealed interface CallFragmentActions {
        data object OnCallFinished : CallFragmentActions
        data object DismissAllDialogs : CallFragmentActions
    }

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentCallBinding::bind)
    private val callViewModel: CallViewModel by viewModels<CallViewModel> { App.component.getViewModelFactory() }
    private var activityCallback: CallsOnActivityInteraction? = null
    private var signalingService: MeeraSignalingService? = null
    private var connectionWrapper: MeeraSignalingServiceConnectionWrapper? = null
    private var ringtoneAudioFocusManager: RingtoneAudioFocusManager? = null
    private var notificationManager: NotificationManager by Delegates.notNull()
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private var androidAudioManager: AudioManager? = null
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioSwitch: AudioSwitch by Delegates.notNull()
    private val callTimerHandler = Handler(Looper.getMainLooper())

    private var isIncomingCall = false
    private var isVideoCallOn = false
    private var callUser: UserChat? = null
    private var callAccepted: Boolean = false

    private var roomId: Long? = null
    private var uuId: String? = String.empty()
    private var uri = Uri.parse(WEBRTC_ROOM_URL)
    private var iceConnected: Boolean = false
    private var signalingParameters: AppRTCClient.SignalingParameters? = null
    private var fullscreenRenderer: CallVideoView? = null
    private var pipRenderer: CallVideoView? = null
    private val remoteSinks = ArrayList<VideoSink>()
    private val remoteProxyRenderer = ProxyVideoSink()
    private val localProxyVideoSink = ProxyVideoSink()
    private var eglBase: EglBase by Delegates.notNull()
    private var videoFileRenderer: VideoFileRenderer? = null
    private var screencaptureEnabled = false
    private var videoWidth = 0
    private var videoHeight = 0
    private var peerConnectionParameters: PeerConnectionClient.PeerConnectionParameters? = null
    private var roomConnectionParameters: AppRTCClient.RoomConnectionParameters? = null
    private var peerConnectionClient: PeerConnectionClient? = null
    private var activityRunning: Boolean = false
    private var videoCapturer: VideoCapturer? = null
    private var incomingVideoEnabled = false
    private var appRtcClient: AppRTCClient? = null
    private var callStartedTimeMs: Long = 0
    private var connectionUpTimeMs: Long = 0
    private var callConnected = false
    private var isDisconnected = false
    private var callWasInterrupted = false
    private var outgoingVideoEnabled = false
    private var canDisconnect = true
    private var isError: Boolean = false
    private var micEnabled = false
    private var isSpeakerEnabled = false
    private var frontCamera = true
    private var tooltipSnackbar: UiKitSnackBar? = null
    private var errorSnackbar: UiKitSnackBar? = null

    private val callReceiver = object : CallBroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            signalingService?.callProgressStop()
            playSoundReject()
            super.onReceive(context, intent)
        }
    }

    private val displayMetrics: DisplayMetrics
        get() {
            val displayMetrics = DisplayMetrics()
            val windowManager = requireActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
            return displayMetrics
        }

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            activityCallback = context as CallsOnActivityInteraction
        } catch (e: ClassCastException) {
            Timber.e(e)
            throw ClassCastException("$context must implement interface MeeraCallFragment.OnActivityInteraction")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ringtoneAudioFocusManager = RingtoneAudioFocusManager(requireContext().applicationContext)
        ringtoneAudioFocusManager?.requestAudioFocus()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityCallback?.onCallFragmentAction(CallFragmentActions.DismissAllDialogs)
        initComponents()
        getArgumentParameters()
        setSpeakerDisabled()
        initViews()
        initClicks()
        observeViewEvents()
        setIncomingCallVibrate()
        createCallNotification()
    }

    override fun onStart() {
        super.onStart()
        connectionWrapper?.isCallActive = true
        setPermissions()
        enableWakeLock()
        audioSwitch.start { audioDevices, selectedAudioDevice ->
            onAudioManagerDevicesChanged(selectedAudioDevice)
        }
        playCallRingtone()
        playProgressConnectSoundWhenPushTransit()
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        disableWakeLock()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onStop() {
        super.onStop()
        connectionWrapper?.isCallActive = true
        compositeDisposable.dispose()
        audioSwitch.stop()
        tooltipSnackbar?.dismiss()
        errorSnackbar?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vibrator?.cancel()
        requireContext().unregisterReceiver(callReceiver)
        callTimerHandler.removeCallbacksAndMessages(null)
        notificationManager.cancel(CALL_NOTIFICATION_ID)
        Thread.setDefaultUncaughtExceptionHandler(null)
        disconnect()
        activityRunning = false
    }

    override fun callIsStopped() {
        Timber.e("MEERA_CALL_LOG CallIsStopped")
        vibrator?.cancel()
        uiPost { playSoundStopCallAndDisconnect() }
    }

    override fun callIsRejected() {
        Timber.e("MEERA_CALL_LOG CallIsRejected")
        vibrator?.cancel()
        uiPost { playSoundReject() }
    }

    override fun callIsAccepted(roomIdInProc: Long, uuid: String?) {
        Timber.e("MEERA_CALL_LOG START_CALL")
        vibrator?.cancel()
        this.uuId = uuid
        this.roomId = roomIdInProc
        showConnectionStringStatus()
        initWebrtcCall()
    }

    override fun lineIsBusy() {
        Timber.e("MEERA_CALL_LOG LineIsBusy")
        vibrator?.cancel()
        if (!isIncomingCall) {
            uiPost { playSoundReject() }
        }
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    override fun onLocalDescription(sdp: SessionDescription) {
        Timber.e("MEERA_CALL_LOG onLocalDescription")
        val delta = System.currentTimeMillis() - callStartedTimeMs
        uiPost {
            if (appRtcClient != null) {
                Timber.d("MEERA_CALL_LOG Sending ${sdp.type}, delay=$delta ms")
                if (signalingParameters?.initiator == true) {
                    appRtcClient?.sendOfferSdp(sdp)
                } else {
                    appRtcClient?.sendAnswerSdp(sdp)
                }
            }
            peerConnectionParameters?.videoMaxBitrate?.let { videoMaxBitrate ->
                if (videoMaxBitrate > 0) {
                    peerConnectionClient?.setVideoMaxBitrate(videoMaxBitrate)
                }
            }
        }
    }

    // обрабатывать каждый раз, когда обнаруживается кандидат ICE
    // Затем вам нужно отправить кандидата своему контакту через свой сигнальный механизм
    override fun onIceCandidate(candidate: IceCandidate) {
        Timber.e("MEERA_CALL_LOG onIceCandidate() AppRtc client:$appRtcClient CANDIDATE:${candidate.serverUrl}")
        uiPost {
            if (appRtcClient != null) {
                appRtcClient?.sendLocalIceCandidate(candidate)
            }
        }
    }

    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
        Timber.e("MEERA_CALL_LOG onIceCandidatesRemoved")
        uiPost {
            if (appRtcClient != null) {
                appRtcClient?.sendLocalIceCandidateRemovals(candidates)
            }
        }
    }

    override fun onConnectionEstablished() {
        Timber.d("MEERA_CALL_LOG => CONGRATS!!! onIceConnected()")
        uiPost {
            Timber.d("ICE connected, delay=${System.currentTimeMillis() - callStartedTimeMs} ms")
            callAccepted = true
            iceConnected = true
            setDefaultRowCallButtons()
            callConnected()
        }
    }

    override fun onDisconnect() {
        callWasInterrupted = true
        playSoundReconnecting()
    }

    override fun onConnectionClosed() {
        Timber.e("MEERA_CALL_LOG onConnectionClosed()")
        iceConnected = false
        uiPost { playSoundStopCallAndDisconnect() }
    }

    override fun onPeerConnectionStatsReady(reports: Array<out StatsReport>?) = Unit

    override fun onPeerConnectionError(description: String?) {
        Timber.e("MEERA_CALL_LOG onPeerConnectionError $description")
        uiPost { playSoundReject() }
    }

    override fun onDataChannelMessage(message: String?) {
        Timber.d("MEERA_CALL_LOG DATA_CHANNEL: $message")
        when (message) {
            WEBRTC_SET_CAMERA_DISABLED -> setRemoteVideoDisable()
            WEBRTC_SET_CAMERA_ENABLED -> setRemoteVideoEnable()
            WEBRTC_SET_MIC_DISABLED -> Timber.d("REMOTE_MIC_IS_DISABLED")
            WEBRTC_SET_MIC_ENABLED -> Timber.d("REMOTE_MIC_IS_ENABLED")
        }
    }

    override fun onConnectedToRoom(params: AppRTCClient.SignalingParameters) {
        Timber.e("MEERA_CALL_LOG onConnectedToRoom")
        uiPost { onConnectedToRoomInternal(params) }
    }

    override fun onRemoteDescription(sdp: SessionDescription?) {
        Timber.e("MEERA_CALL_LOG onRemoteDescription")
        uiPost {
            if (peerConnectionClient == null) {
                Timber.e("MEERA_CALL_LOG Received remote SDP for non-initilized peer connection.")
                return@uiPost
            }

            peerConnectionClient?.setRemoteDescription(sdp)
            if (signalingParameters?.initiator?.not() == true) {
                peerConnectionClient?.createAnswer()
            }
        }
    }

    override fun onRemoteIceCandidate(candidate: IceCandidate?) {
        Timber.e("MEERA_CALL_LOG onRemoteIceCandidate() CANDIDATE:${candidate?.serverUrl}")
        uiPost {
            if (peerConnectionClient == null) {
                Timber.e("MEERA_CALL_LOG Received ICE candidate for a non-initialized peer connection.")
                return@uiPost
            }
            peerConnectionClient?.addRemoteIceCandidate(candidate)
        }
    }

    override fun onRemoteIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
        Timber.e("MEERA_CALL_LOG onRemoteIceCandidatesRemoved")
        uiPost {
            if (peerConnectionClient == null) {
                Timber.e("MEERA_CALL_LOG Received ICE candidate removals for a non-initialized peer connection.")
                return@uiPost
            }
            peerConnectionClient!!.removeRemoteIceCandidates(candidates)
        }
    }

    override fun onChannelClose() {
        Timber.e("MEERA_CALL_LOG onChannelClose (Remote end hung up; dropping PeerConnection)")
    }

    override fun onChannelError(description: String?) {
        Timber.e("MEERA_CALL_LOG onChannelError -> REJECT_CALL")
        playSoundReject()
    }

    override fun onDeviceSelected(device: AudioDevice) {
        Timber.d("MEERA_CAL_LOG OnSelected --------device:$device")
        audioSwitch.selectDevice(device)
        audioSwitch.activate()
    }

    private fun initComponents() {
        notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        audioSwitch = AudioSwitch(requireContext())
        connectionWrapper = callViewModel.getSignalingServiceConnectionWrapper()
        signalingService = connectionWrapper?.signalingService
        signalingService?.registerCallProgressClient(this)

        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(ringtoneAudioFocusManager?.getCallRingtoneAudioAttributes())
        }
        androidAudioManager = (requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager)
        powerManager = requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager?.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
            this.javaClass.name
        )
    }

    private fun getArgumentParameters() {
        arguments?.let {
            isIncomingCall = it.getInt(TYPE_CALL_KEYS) == INCOMING_CALL_KEY
            callUser = it.getParcelable(IArgContainer.ARG_USER_MODEL)
            callAccepted = it.getBoolean(IArgContainer.ARG_CALL_ACCEPTED)
        }
    }

    private fun setSpeakerDisabled() {
        runCatching {
            if (wakeLock?.isHeld == true) {
                wakeLock?.acquire()
            }
        }

        if (mediaPlayer != null && mediaPlayer?.isPlaying == true && !callConnected) {
            playCallRingtone()
        }
    }

    private fun initViews() {
        binding.apply {
            ivUserAvatar.let {
                Glide.with(this@MeeraCallFragment)
                    .load(callUser?.avatarSmall)
                    .placeholder(AppCompatResources.getDrawable(it.context, R.drawable.fill_8_round))
                    .apply(RequestOptions.circleCropTransform())
                    .into(it)
            }
            tvUserName.text = callUser?.name
            tvTypeCallHeader.text = callUser?.name
            ivCallBackground.visible()
            ivChangeCamera.gone()
        }

        if (isIncomingCall) {
            setIncomingRowCallButtons()
        } else {
            setDefaultRowCallButtons()
        }
    }

    private fun setIncomingRowCallButtons() = with(binding) {
        vgCallButtonsContainer.setMargins(
            start = INCOMING_CALL_BUTTONS_MARGIN.dp,
            end = INCOMING_CALL_BUTTONS_MARGIN.dp
        )
        vBtnSpacer.visible()
        ivBtnEnableCall.visible()
        ivBtnDisableCall.visible()
        ivBtnCallSpeaker.gone()
        ivBtnCallCamera.gone()
        ivBtnCallMic.gone()
    }

    private fun setDefaultRowCallButtons() = with(binding) {
        vgCallButtonsContainer.setMargins(
            start = INCOMING_CALL_DEFAULT_BUTTONS_MARGIN.dp,
            end = INCOMING_CALL_DEFAULT_BUTTONS_MARGIN.dp
        )
        vBtnSpacer.gone()
        ivBtnEnableCall.gone()
        ivBtnDisableCall.visible()
        ivBtnCallSpeaker.visible()
        ivBtnCallCamera.visible()
        ivBtnCallMic.visible()
    }

    private fun initClicks() {
        handleButtonDisableClick()
        handleButtonMicClick()
        handleButtonSpeakerClick()
        handleButtonAcceptCallClick()
        handleButtonSwitchVideoCallClick()
        handleButtonChangeCameraClick()
        handleTouchPipVideo()
    }

    /**
     *  отмена звонка когда мы звоним
     *  Срабытывает, если мы сами звоним а потом нажимаем кнопку отмены звонка
     */
    private fun handleButtonDisableClick() {
        binding.ivBtnDisableCall.setThrottledClickListener {
            if (isIncomingCall) {
                context?.vibrate()
                playSoundReject()
                callViewModel.logCallCancel(AmplitudePropertyCallCanceller.CALLED)
            } else {
                context?.vibrate()
                if (connectionUpTimeMs == 0L) playSoundReject() else playSoundStopCallAndDisconnect()
                logAnalyticsEndedCall()
            }
        }
    }

    /**
     *  Кнопка "Перечеркнутый микрофон"
     */
    private fun handleButtonMicClick() {
        binding.ivBtnCallMic.setThrottledClickListener {
            if (micEnabled) {
                setMicDisable()
                micDisabledTooltip()
            } else {
                setMicEnable()
                micEnabledTooltip()
            }
            context?.vibrate()
        }
    }

    /**
     * Переключение источников звонков
     * Кнопка "Динамик" - переключение источников звонков
     */
    private fun handleButtonSpeakerClick() {
        binding.ivBtnCallSpeaker.setThrottledClickListener {
            toggleSpeakerTooltip()
            selectDeviceOutput()
            context?.vibrate()
        }
    }

    /**
     * Кнопка приёма звонка
     * Кнопка "Зелёная трубка" - приём входящего звонка
     */
    private fun handleButtonAcceptCallClick() {
        binding.ivBtnEnableCall.setThrottledClickListener {
            Timber.d("MEERA_CALL_LOG Click enableCallButton srv:${signalingService.toString()}")
            if (isDisconnected || canDisconnect.not()) return@setThrottledClickListener
            if (callViewModel.isInternetConnected()) {
                mediaPlayer?.stop()
                callAccepted = true
                signalingService?.callProgressAccept()
                context?.vibrate()
                audioSwitch.activate()
                showConnectionStringStatus()
                selectEarPhone()
                playSoundConnect(isLooping = true)
            } else {
                Timber.e("MEERA_CALL_LOG DISCONNECT")
                disconnect()
                showErrorSnackbar(R.string.no_internet_connection)
            }
        }
    }

    /**
     * Переключение на видео звонок с обычного звонка
     */
    private fun handleButtonSwitchVideoCallClick() {
        binding.ivBtnCallCamera.setThrottledClickListener {
            uiPost {
                if (isVideoCallOn.not()) {
                    isVideoCallOn = true
                    binding.ivBtnCallCamera.setImageResource(R.drawable.meera_btn_call_camera_on)
                    peerConnectionClient?.startVideoSource()
                    outgoingVideoEnabled = true
                    peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_ENABLED)
                    binding.pipVideoViewContainer.visible()
                    runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
                    if (callConnected) binding.ivChangeCamera.visible()
                } else {
                    isVideoCallOn = false
                    binding.ivBtnCallCamera.setImageResource(R.drawable.meera_btn_call_camera_off)
                    outgoingVideoEnabled = false
                    peerConnectionClient?.stopVideoSource()
                    peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_DISABLED)
                    binding.pipVideoViewContainer.gone()
                    binding.ivChangeCamera.gone()
                    runCatching { wakeLock?.acquire() }
                }
                switchCameraOutput(isEnabled = isVideoCallOn)
            }
        }
    }

    /**
     * Переключение передней/задней камеры
     */
    private fun handleButtonChangeCameraClick() {
        binding.ivChangeCamera.setThrottledClickListener {
            frontCamera = !frontCamera
            pipRenderer?.setMirror(frontCamera)
            binding.ivChangeCamera.isEnabled = false
            peerConnectionClient?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(p0: Boolean) {
                    uiPost {
                        binding.ivChangeCamera.isEnabled = true
                    }
                }

                override fun onCameraSwitchError(p0: String?) {
                    uiPost {
                        binding.ivChangeCamera.isEnabled = true
                    }
                }
            })
        }
    }

    /**
     * Контейнер видео окошка PIP при видео звонке
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun handleTouchPipVideo() {
        var dXView = 0f
        var dYView = 0f
        var touchXView = 0f
        var touchYView = 0f
        var movePipWindow = false
        binding.pipVideoViewContainer.setOnTouchListener { v, event ->
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
                        || abs(touchXView.toInt() - positionXView.toInt()) > HALF_SCREEN_PERCENT
                        || abs(touchYView.toInt() - positionYView.toInt()) > HALF_SCREEN_PERCENT
                    ) {
                        movePipWindow = true
                        v.x = positionXView
                        v.y = positionYView
                    }
                }
            }
            true
        }
    }

    private fun setMicDisable() {
        uiPost {
            if (peerConnectionClient != null) {
                binding.ivBtnCallMic.setImageResource(R.drawable.meera_btn_call_mic_off)
                peerConnectionClient?.setAudioEnabled(true)
                micEnabled = false
            }
        }
    }

    private fun setMicEnable() {
        uiPost {
            if (peerConnectionClient != null) {
                binding.ivBtnCallMic.setImageResource(R.drawable.meera_btn_call_mic_on)
                peerConnectionClient?.setAudioEnabled(false)
                micEnabled = true
            }
        }
    }

    private fun selectDeviceOutput() {
        if (deviceHasExtraOutput()) {
            showAvailableDevices()
        } else {
            toggleSpeaker()
        }
    }

    private fun observeViewEvents() {
        callViewModel.viewEventFlow.onEach { event ->
            when (event) {
                is CallViewEvent.StopCall -> {
                    doDelayed(DELAY_BEFORE_STOP_CALL) {
                        playSoundStopCallAndDisconnect()
                        logAnalyticsCallEnd()
                    }
                }

                is CallViewEvent.RejectCall -> playSoundReject()
                else -> event.toString()
            }
        }
            .flowWithLifecycle(lifecycle)
            .launchIn(lifecycleScope)
    }

    private fun setIncomingCallVibrate() {
        if (isIncomingCall) {
            val pattern = longArrayOf(0, 200, 100, 200, 1000)
            val audioAttributes = ringtoneAudioFocusManager?.getCallRingtoneAudioAttributes()
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0), audioAttributes)
        }
    }

    private fun showConnectionStringStatus() {
        binding.tvCallTime.visible()
        binding.tvCallTime.text = getString(R.string.call_connection)
    }

    private fun initWebrtcCall() {
        signalingService?.registerWebRtcClient(this)
        appRtcClient = signalingService
        startCall()
    }

    private fun startCall() {
        if (appRtcClient == null || roomConnectionParameters == null) return
        callStartedTimeMs = System.currentTimeMillis()
        appRtcClient?.connectToRoom(roomConnectionParameters)
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    @SuppressLint("BinaryOperationInTimber")
    private fun onConnectedToRoomInternal(params: AppRTCClient.SignalingParameters) {
        Timber.d("MEERA_CALL_LOG Creating peer connection, delay=${System.currentTimeMillis() - callStartedTimeMs} ms")
        signalingParameters = params
        videoCapturer = createVideoCapturer()
        peerConnectionClient?.createPeerConnection(localProxyVideoSink, remoteSinks, videoCapturer, signalingParameters)

        if (signalingParameters?.initiator == true) {
            Timber.d("MEERA_CALL_LOG Creating OFFER...: ${signalingParameters?.initiator}")
            peerConnectionClient?.createOffer()
        } else {
            Timber.d(
                "MEERA_CALL_LOG Creating ANSWER...:${signalingParameters?.initiator} " +
                    "params.offerSdp:${params.offerSdp} ice candidates:${params.iceCandidates}"
            )
            handleOfferSdp(params)
            addRemoteIceCandidatesFromRoom(params)
        }
    }

    private fun handleOfferSdp(params: AppRTCClient.SignalingParameters) {
        if (params.offerSdp != null) {
            peerConnectionClient?.setRemoteDescription(params.offerSdp)
            peerConnectionClient?.createAnswer()
        }
    }

    private fun addRemoteIceCandidatesFromRoom(params: AppRTCClient.SignalingParameters) {
        if (params.iceCandidates != null) {
            for (iceCandidate in params.iceCandidates) {
                peerConnectionClient?.addRemoteIceCandidate(iceCandidate)
            }
        }
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    @Synchronized
    fun disconnect() {
        ringtoneAudioFocusManager?.releaseAudioFocus()
        if (isDisconnected) return
        isDisconnected = true

        videoCapturer?.stopCapture()
        videoCapturer?.dispose()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        mediaPlayer = null
        notificationManager.cancel(CALL_NOTIFICATION_ID)

        if (callConnected) {
            signalingService?.callProgressStop()
        } else {
            signalingService?.callProgressReject()
        }

        activityRunning = false
        remoteProxyRenderer.setTarget(null)
        localProxyVideoSink.setTarget(null)
        appRtcClient?.disconnectFromRoom()
        pipRenderer?.release()
        videoFileRenderer?.release()
        fullscreenRenderer?.release()
        peerConnectionClient?.close()
        uuId?.let { uuid ->
            roomId?.let { id ->
                callViewModel.onCallFinished(uuid, id)
            }
        }
        Timber.e("MEERA_CALL_LOG call finished -> disconnect()")
        activityCallback?.onCallFragmentAction(CallFragmentActions.OnCallFinished)
    }

    private fun setRemoteVideoDisable() {
        setRemoteVideoState(isEnabled = false)
    }

    private fun setRemoteVideoEnable() {
        setRemoteVideoState(isEnabled = true)
    }

    private fun setRemoteVideoState(isEnabled: Boolean) {
        uiPost {
            if (peerConnectionClient != null) {
                incomingVideoEnabled = isEnabled
                configIncomingVideo()
            }
        }
    }

    private fun configIncomingVideo() {
        if (incomingVideoEnabled) {
            binding.apply {
                ivUserAvatar.gone()
                tvUserName.gone()
                tvCallTime.gone()
                tvTypeCallHeader.visible()
                tvCallTimeHeader.visible()
                ivCallBackground.gone()
            }

        } else {
            binding.apply {
                ivUserAvatar.visible()
                tvUserName.visible()
                tvCallTime.visible()
                tvTypeCallHeader.gone()
                tvCallTimeHeader.gone()
                ivCallBackground.visible()
            }
        }
        switchCameraOutput(incomingVideoEnabled)
    }

    private fun switchCameraOutput(isEnabled: Boolean) {
        when {
            isEnabled && deviceHasExtraOutput() -> showAvailableDevices()
            isEnabled -> selectSpeakerPhone()
            else -> Unit
        }
    }

    private fun deviceHasExtraOutput(): Boolean {
        return audioSwitch.availableAudioDevices.size > 1 &&
            audioSwitch.availableAudioDevices.any { it is AudioDevice.BluetoothHeadset } ||
            audioSwitch.availableAudioDevices.any { it is AudioDevice.WiredHeadset }
    }

    /**
     * Check audio connected devices and show popup with options
     */
    private fun showAvailableDevices() {
        if (MeeraAudioSwitchBottomSheet.isShowed(childFragmentManager)) return
        if (deviceHasExtraOutput()) {
            MeeraAudioSwitchBottomSheet().show(
                fm = childFragmentManager,
                devices = audioSwitch.availableAudioDevices.toList(),
                selected = audioSwitch.selectedAudioDevice,
                onDeviceSelected = { device ->
                    onDeviceSelected(device)
                }
            )
        }
    }

    private fun toggleSpeaker() {
        uiPost {
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

    private fun createCallNotification() {
        val intentFilter = IntentFilter(CallBroadcastReceiver.BROADCAST_INTENT)
        callReceiver.register(
            context = requireContext(),
            filter = intentFilter
        )
        val broadcastIntent = Intent(CallBroadcastReceiver.BROADCAST_INTENT)
        val broadcastPendingIntent = PendingIntent.getBroadcast(context, 0, broadcastIntent, getPendingIntentFlag())
        val i = Intent(context, Act::class.java)
        val callActivityIntent = PendingIntent.getActivity(context, 0, i, getPendingIntentFlag())
        val builder = androidx.core.app.NotificationCompat.Builder(requireContext(), CALL_NOTIFICATION_CHANNEL_ID)
        builder.setContentTitle(callUser?.name)
            .setSmallIcon(R.drawable.phone_icon)
            .setColor(ContextCompat.getColor(requireContext(), R.color.uiKitColorAccentPrimary))
            .setContentIntent(callActivityIntent)
            .setContentText(getString(R.string.current_call))
            .setOngoing(true)
            .addAction(R.drawable.close_call, getString(R.string.end_call), broadcastPendingIntent)
        val notificationChannel = NotificationChannel(
            CALL_NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(notificationChannel)
        builder.priority = androidx.core.app.NotificationCompat.PRIORITY_LOW
        notificationManager.notify(CALL_NOTIFICATION_ID, builder.build())
    }

    // Should be called from UI thread
    private fun callConnected() {
        Timber.e("MEERA_CALL_LOG CALL_CONNECTED: delay=${System.currentTimeMillis() - callStartedTimeMs} ms")
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
            Timber.d("MEERA_CALL_LOG Call is connected in closed or error state")
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
        doDelayed(DELAY_SEND_DATA_CHANNEL_MESSAGE) {
            Timber.e("MEERA_CALL_LOG SEND Message: WEBRTC_SET_CAMERA_ENABLED")
            peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_ENABLED)
            peerConnectionClient?.stopVideoSource()
            peerConnectionClient?.startVideoSource()
            binding.ivChangeCamera.visible()
        }
    }

    private fun sendMessageCameraDisabled() {
        doDelayed(DELAY_SEND_DATA_CHANNEL_MESSAGE) {
            Timber.e("MEERA_CALL_LOG SEND Message: WEBRTC_SET_CAMERA_DISABLED")
            peerConnectionClient?.sendMessage(WEBRTC_SET_CAMERA_DISABLED)
            peerConnectionClient?.stopVideoSource()
            binding.ivChangeCamera.gone()
        }
    }

    private fun startCallTimer() {
        callTimerHandler.postDelayed(object : Runnable {
            @SuppressLint("SimpleDateFormat")
            override fun run() {
                val sdf = SimpleDateFormat(CALL_TIMER_TEXT_FORMAT)
                val resultDate = Date(System.currentTimeMillis() - connectionUpTimeMs)
                val time = sdf.format(resultDate)
                uiPost {
                    binding.tvCallTimeHeader.text = time
                    binding.tvCallTime.text = time
                }
                callTimerHandler.postDelayed(this, CALL_TIMER_DELAY_MS)
            }
        }, CALL_TIMER_DELAY_MS)
    }


    private fun getPendingIntentFlag(): Int {
        val sdkVersion = Build.VERSION.SDK_INT
        return if (sdkVersion >= Build.VERSION_CODES.S && sdkVersion < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.FLAG_MUTABLE
        } else if (sdkVersion >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    private fun setPermissions() {
        val rxPermissions = RxPermissions(requireActivity())
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.RECORD_AUDIO)
        }.toTypedArray()

        rxPermissions.request(*permissions)
            ?.subscribe(
                { granted ->
                    if (granted) {
                        initCallParameters()
                    }
                },
                { error -> Timber.e(error) }
            )
            ?.also { compositeDisposable.add(it) }
    }

    private fun initCallParameters() {
        Thread.setDefaultUncaughtExceptionHandler(UnhandledExceptionHandler(requireActivity()))
        iceConnected = false
        signalingParameters = null
        fullscreenRenderer = binding.fullscreenVideoView
        pipRenderer = binding.pipVideoView
        remoteSinks.add(remoteProxyRenderer)
        eglBase = EglBase.create()
        pipRenderer?.init(eglBase.eglBaseContext, null)
        pipRenderer?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
        val saveRemoteVideoToFile = requireActivity().intent.getStringExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE)
        if (saveRemoteVideoToFile != null) {
            val videoOutWidth = requireActivity().intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_WIDTH, 0)
            val videoOutHeight = requireActivity().intent.getIntExtra(EXTRA_SAVE_REMOTE_VIDEO_TO_FILE_HEIGHT, 0)
            try {
                videoFileRenderer = VideoFileRenderer(
                    saveRemoteVideoToFile, videoOutWidth, videoOutHeight, eglBase.eglBaseContext
                )
                videoFileRenderer?.let { remoteSinks.add(it) }

            } catch (e: IOException) {
                throw RuntimeException(
                    "Failed to open video file for output: $saveRemoteVideoToFile", e
                )
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
            Timber.e(getString(org.appspot.apprtc.R.string.missing_url))
            Timber.e("MEERA_CALL_LOG Didn't get any URL in intent!")
            return
        }

        screencaptureEnabled = USE_SCREEN_CAPTURE
        if (screencaptureEnabled && videoWidth == 0 && videoHeight == 0) {
            val displayMetrics = displayMetrics
            videoWidth = displayMetrics.widthPixels
            videoHeight = displayMetrics.heightPixels
        }
        var dataChannelParameters: PeerConnectionClient.DataChannelParameters? = null
        if (DATA_CHANNEL_ENABLED) {
            dataChannelParameters = PeerConnectionClient.DataChannelParameters(
                DATA_CHANNEL_ORDERED,
                DATA_CHANNEL_MAX_RETRY_MS,
                DATA_CHANNEL_MAX_RETRY,
                DATA_CHANNEL_PROTOCOL,
                DATA_CHANNEL_NEGOTIATED,
                DATA_CHANNEL_TEMP_ID
            )
        }
        peerConnectionParameters = PeerConnectionClient.PeerConnectionParameters(
            true,
            PEER_CONNECTION_LOOPBACK,
            PEER_CONNECTION_TRACING,
            videoWidth,
            videoHeight,
            PEER_CONNECTION_CAMERA_FPS,
            PEER_CONNECTION_VIDEO_START_BITRATE,
            PEER_CONNECTION_VIDEO_CODEC,
            PEER_CONNECTION_HW_CODEC,
            PEER_CONNECTION_FLEX_FEC_ENABLED,
            PEER_CONNECTION_AUDIO_START_BITRATE,
            PEER_CONNECTION_AUDIO_CODEC,
            PEER_CONNECTION_NO_AUDIO_PROCESSING,
            PEER_CONNECTION_AEC_DUMP,
            PEER_CONNECTION_SAVE_INPUT_AUDIO_TO_FILE,
            PEER_CONNECTION_USE_OPEN_SL_ES,
            PEER_CONNECTION_DISABLE_BUILT_IN_AEC,
            PEER_CONNECTION_DISABLE_BUILT_IN_AGC,
            PEER_CONNECTION_DISABLE_BUILT_IN_NS,
            PEER_CONNECTION_DISABLE_WEB_RTC_AGC_AND_HPF,
            PEER_CONNECTION_RTC_EVENT_LOG_ENABLED,
            PEER_CONNECTION_USE_LEGACY_AUDIO_DEVICE,
            dataChannelParameters
        )

        createConnectionParameters()
        createPeerConnectionClient()
        activityRunning = true

        val parameters = AppRTCClient.SignalingParameters(
            ArrayList(), true, null, null, null, null, null
        )
        videoCapturer = createVideoCapturer()
        peerConnectionClient?.createPeerConnection(
            localProxyVideoSink, remoteSinks, videoCapturer, parameters
        )
        peerConnectionClient?.setVideoEnabled(true)
        peerConnectionClient?.stopVideoSource()
        localProxyVideoSink.setTarget(pipRenderer)
        remoteProxyRenderer.setTarget(fullscreenRenderer)
        if (callAccepted) {
            signalingService?.callProgressAccept()
            context?.vibrate()
        }
    }

    private fun createConnectionParameters() {
        val urlParameters = requireActivity().intent.getStringExtra(EXTRA_URLPARAMETERS)
        roomConnectionParameters = AppRTCClient.RoomConnectionParameters(
            uri.toString(),
            String.empty(),
            PEER_CONNECTION_LOOPBACK,
            urlParameters
        )
    }

    private fun createPeerConnectionClient() {
        peerConnectionClient = PeerConnectionClient(requireContext(), eglBase, peerConnectionParameters, this)
        val options = PeerConnectionFactory.Options()
        if (PEER_CONNECTION_LOOPBACK) {
            options.networkIgnoreMask = 0
        }
        peerConnectionClient?.createPeerConnectionFactory(options)
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val videoCapturer: VideoCapturer?
        val videoFileAsCamera = requireActivity().intent.getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA)
        when {
            videoFileAsCamera != null -> {
                try {
                    videoCapturer = FileVideoCapturer(videoFileAsCamera)
                } catch (e: IOException) {
                    Timber.e("Failed to open video file for emulated camera")
                    return null
                }
            }

            useCamera2() -> {
                if (CAPTURE_TO_TEXTURE.not()) {
                    Timber.e(getString(org.appspot.apprtc.R.string.camera2_texture_only_error))
                    return null
                }

                Timber.d("Creating capturer using camera2 API.")
                videoCapturer = createCameraCapturer(Camera2Enumerator(context))
            }

            else -> {
                Timber.d("Creating capturer using camera1 API.")
                videoCapturer = createCameraCapturer(Camera1Enumerator(CAPTURE_TO_TEXTURE))
            }
        }
        if (videoCapturer == null) {
            Timber.e("Failed to open camera")
            return null
        }
        return videoCapturer
    }

    private fun useCamera2(): Boolean = Camera2Enumerator.isSupported(context) && USE_CAMERA_2

    private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
        val deviceNames = enumerator.deviceNames
        Timber.d("Looking for front facing cameras.")
        for (deviceName in deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Timber.d("Creating front facing camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)
                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }

        Timber.d("Looking for other cameras.")
        for (deviceName in deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Timber.d("Creating other camera capturer.")
                val videoCapturer = enumerator.createCapturer(deviceName, null)

                if (videoCapturer != null) {
                    return videoCapturer
                }
            }
        }
        return null
    }

    private fun playSoundStopCallAndDisconnect() {
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

    private fun logAnalyticsCallEnd() {
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

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private fun onAudioManagerDevicesChanged(selectedDevice: AudioDevice?) {
        val imageRes = when (selectedDevice) {
            is AudioDevice.Speakerphone -> R.drawable.meera_btn_call_speaker_on
            is AudioDevice.Earpiece -> R.drawable.meera_btn_call_speaker_off
            is AudioDevice.BluetoothHeadset -> R.drawable.meera_btn_call_bluetooth
            is AudioDevice.WiredHeadset -> R.drawable.meera_btn_call_earphone
            else -> -1
        }
        binding?.ivBtnCallSpeaker?.setImageResource(imageRes)
    }

    //TODO https://nomera.atlassian.net/browse/BR-19415
    private fun playSoundReconnecting() {
        runCatching {
            prepareMediaPlayerBeforePlay(R.raw.sound_call_reconnected, isLooping = true)
            mediaPlayer?.start()
        }.onFailure {
            Timber.e(it.message)
        }
    }

    private fun playSoundReject() {
        if (canDisconnect) {
            canDisconnect = false
            prepareMediaPlayerBeforePlay(R.raw.sound_call_rejected, isLooping = false)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                disconnect()
                canDisconnect = true
            }
        }
        ringtoneAudioFocusManager?.releaseAudioFocus()
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

    private fun playProgressConnectSoundWhenPushTransit() {
        if (isIncomingCall && callAccepted) {
            setAudioAttributes()
            prepareMediaPlayerBeforePlay(R.raw.sound_call_connected, isLooping = true)
            mediaPlayer?.start()
        }
    }

    private fun playIncomeCallRingtone() {
        Timber.d("MEERA_CALL_LOG Play INCOME RingTone")
        selectSpeakerPhone()
        playFirstRingtone(R.raw.sound_incoming_call, isCheckSilentMode = true)
    }

    private fun playOutgoingCallRingtone() {
        Timber.d("MEERA_CALL_LOG play outgoing sound")
        playFirstRingtone(R.raw.sound_outgoing_call, isCheckSilentMode = false)
    }

    private fun playFirstRingtone(@RawRes ringtoneRes: Int, isCheckSilentMode: Boolean) {
        setAudioAttributes()
        prepareMediaPlayerBeforePlay(ringtoneRes, isLooping = true, isCheckSilentMode)
        mediaPlayer?.start()
    }

    private fun setAudioAttributes() {
        mediaPlayer?.setAudioAttributes(AudioAttributes.Builder().setLegacyStreamType(STREAM_AUDIO_TYPE).build())
        androidAudioManager?.getStreamMaxVolume(STREAM_AUDIO_TYPE)?.let { maxVolume ->
            androidAudioManager?.setStreamVolume(STREAM_AUDIO_TYPE, maxVolume, 0)
        }
    }

    private fun playSoundConnect(isLooping: Boolean) {
        prepareMediaPlayerBeforePlay(R.raw.sound_call_connected, isLooping = isLooping)
        mediaPlayer?.start()
        ringtoneAudioFocusManager?.releaseAudioFocus()
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

    private fun selectEarPhone() {
        audioSwitch.availableAudioDevices.find { it is AudioDevice.Earpiece }?.let { speakerPhone ->
            onDeviceSelected(speakerPhone)
        }
    }

    private fun selectSpeakerPhone() {
        audioSwitch.availableAudioDevices.find { it is AudioDevice.Speakerphone }?.let { speakerPhone ->
            onDeviceSelected(speakerPhone)
        }
    }

    private fun enableWakeLock() {
        if (wakeLock?.isHeld?.not() == true) {
            runCatching {
                wakeLock?.acquire()
            }
        }
    }

    private fun disableWakeLock() {
        runCatching {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        }
    }

    private fun logAnalyticsEndedCall() {
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

    private fun uiPost(block: () -> Unit) {
        view?.post {
            block.invoke()
        }
    }

    private fun toggleSpeakerTooltip() {
        isSpeakerEnabled = if (isSpeakerEnabled) {
            speakerDisableTooltip()
            false
        } else {
            speakerEnableTooltip()
            true
        }
    }

    private fun speakerEnableTooltip() {
        showTooltipSnackbar(R.string.tooltip_speaker_turned_on)
    }

    private fun speakerDisableTooltip() {
        showTooltipSnackbar(R.string.tooltip_speakers_turned_off)
    }

    private fun micEnabledTooltip() {
        showTooltipSnackbar(R.string.tooltip_mic_turned_off)
    }

    private fun micDisabledTooltip() {
        showTooltipSnackbar(R.string.tooltip_mic_turned_on)
    }

    private fun showTooltipSnackbar(@StringRes message: Int) {
        tooltipSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(message)
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                paddingState = PaddingState(bottom = TOOLTIP_MARGIN_BOTTOM.dp)
            )
        )
        tooltipSnackbar?.show()
    }

    private fun showErrorSnackbar(@StringRes message: Int) {
        errorSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(message)
                )
            )
        )
        errorSnackbar?.show()
    }

    private class ProxyVideoSink : VideoSink {
        private var target: VideoSink? = null

        @Synchronized
        override fun onFrame(frame: VideoFrame) {
            if (target == null) {
                Timber.d("Dropping frame in proxy because target is null.")
                return
            }
            target?.onFrame(frame)
        }

        @Synchronized
        fun setTarget(target: VideoSink?) {
            this.target = target
        }
    }

}
