/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.appspot.apprtc;

import static com.meera.core.common.ConstantsKt.PREF_NAME;
import static com.meera.core.common.PreferencesKeysKt.PREF_KEY_RTCP_MUX_POLICY;
import static com.meera.core.common.PreferencesKeysKt.PREF_KEY_TCP_CANDIDATE_POLICY;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;

import org.appspot.apprtc.AppRTCClient.SignalingParameters;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.IceCandidateErrorEvent;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpParameters;
import org.webrtc.RtpReceiver;
import org.webrtc.RtpSender;
import org.webrtc.RtpTransceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SoftwareVideoDecoderFactory;
import org.webrtc.SoftwareVideoEncoderFactory;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule.AudioRecordErrorCallback;
import org.webrtc.audio.JavaAudioDeviceModule.AudioTrackErrorCallback;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Peer connection client implementation.
 *
 * <p>All public methods are routed to local looper thread.
 * All PeerConnectionEvents callbacks are invoked from the same looper thread. This class is a singleton.
 */
public class PeerConnectionClient {

    public static final String VIDEO_TRACK_ID = "HOMEPAVS01";
    public static final String AUDIO_TRACK_ID = "HOMEPAAS01";
    public static final String VIDEO_TRACK_TYPE = "video";
    private static final String TAG = "PCRTCClient";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String VIDEO_CODEC_H264_BASELINE = "H264 Baseline";
    private static final String VIDEO_CODEC_H264_HIGH = "H264 High";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String VIDEO_FLEXFEC_FIELDTRIAL =
        "WebRTC-FlexFEC-03-Advertised/Enabled/WebRTC-FlexFEC-03/Enabled/";
    private static final String VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL = "WebRTC-IntelVP8/Enabled/";
    private static final String DISABLE_WEBRTC_AGC_FIELDTRIAL =
        "WebRTC-Audio-MinimizeResamplingOnMobile/Enabled/";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int BPS_IN_KBPS = 1000;
    private static final String RTCEVENTLOG_OUTPUT_DIR_NAME = "rtc_event_log";
    private static final String SDP_WRONG_PARAMETER = "a=extmap-allow-mixed\r\n";

    // Executor thread is started once in private ctor and is used for all
    // peer connection API calls to ensure new peer connection factory is
    // created on the same thread as previously destroyed factory.
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();
    private final Timer statsTimer = new Timer();
    private final EglBase rootEglBase;
    private final Context appContext;
    private final PeerConnectionParameters peerConnectionParameters;
    private final PeerConnectionEvents events;
    private final boolean dataChannelEnabled;
    private final SharedPreferences sharedPref;
    @Nullable
    private PeerConnectionFactory factory;
    @Nullable
    private PeerConnection peerConnection;
    @Nullable
    private AudioSource audioSource;
    @Nullable
    private SurfaceTextureHelper surfaceTextureHelper;
    @Nullable
    private VideoSource videoSource;
    private boolean preferIsac;
    private boolean videoCapturerStopped;
    private boolean isError;
    @Nullable
    private VideoSink localRender;
    @Nullable
    private List<VideoSink> remoteSinks;
    private SignalingParameters signalingParameters;
    private int videoWidth;
    private int videoHeight;
    private int videoFps;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
    @Nullable
    private List<IceCandidate> queuedRemoteCandidates;
    private boolean isInitiator;
    @Nullable
    private SessionDescription localSdp; // either offer or answer SDP
    @Nullable
    private VideoCapturer videoCapturer;
    // enableVideo is set to true if video should be rendered and sent.
    private boolean renderVideo = true;
    @Nullable
    private VideoTrack localVideoTrack;
    @Nullable
    private VideoTrack remoteVideoTrack;
    @Nullable
    private RtpSender localVideoSender;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio = true;
    @Nullable
    private AudioTrack localAudioTrack;
    @Nullable
    private DataChannel dataChannel;
    // Enable RtcEventLog.
    @Nullable
    private RtcEventLog rtcEventLog;
    // Implements the WebRtcAudioRecordSamplesReadyCallback interface and writes
    // recorded audio samples to an output file.
    @Nullable
    private RecordedAudioToFileController saveRecordedAudioToFile = null;

    /**
     * Create a PeerConnectionClient with the specified parameters. PeerConnectionClient takes ownership of |eglBase|.
     */
    public PeerConnectionClient(
        Context appContext,
        EglBase eglBase,
        PeerConnectionParameters peerConnectionParameters,
        PeerConnectionEvents events
    ) {
        this.rootEglBase = eglBase;
        this.appContext = appContext;
        this.events = events;
        this.peerConnectionParameters = peerConnectionParameters;
        this.dataChannelEnabled = peerConnectionParameters.dataChannelParameters != null;
        this.sharedPref = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        Log.d(TAG, "Preferred video codec: " + getSdpVideoCodecName(peerConnectionParameters));

        final String fieldTrials = getFieldTrials(peerConnectionParameters);
        executor.execute(() -> {
            Log.d(TAG, "Initialize WebRTC. Field trials: " + fieldTrials);
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .setFieldTrials(fieldTrials)
                    .setEnableInternalTracer(true)
                    .createInitializationOptions());
        });
    }

    private static String getSdpVideoCodecName(PeerConnectionParameters parameters) {
        switch (parameters.videoCodec) {
            case VIDEO_CODEC_VP8:
                return VIDEO_CODEC_VP8;
            case VIDEO_CODEC_VP9:
                return VIDEO_CODEC_VP9;
            case VIDEO_CODEC_H264_HIGH:
            case VIDEO_CODEC_H264_BASELINE:
                return VIDEO_CODEC_H264;
            default:
                return VIDEO_CODEC_VP8;
        }
    }

    private static String getFieldTrials(PeerConnectionParameters peerConnectionParameters) {
        String fieldTrials = "";
        if (peerConnectionParameters.videoFlexfecEnabled) {
            fieldTrials += VIDEO_FLEXFEC_FIELDTRIAL;
            Log.d(TAG, "Enable FlexFEC field trial.");
        }
        fieldTrials += VIDEO_VP8_INTEL_HW_ENCODER_FIELDTRIAL;
        if (peerConnectionParameters.disableWebRtcAGCAndHPF) {
            fieldTrials += DISABLE_WEBRTC_AGC_FIELDTRIAL;
            Log.d(TAG, "Disable WebRTC AGC field trial.");
        }
        return fieldTrials;
    }

    @SuppressWarnings("StringSplitter")
    private static String setStartBitrate(
        String codec, boolean isVideoCodec, String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet =
                        "a=fmtp:" + codecRtpMap + " " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "="
                        + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }
        return newSdpDescription.toString();
    }

    /**
     * Returns the line number containing "m=audio|video", or -1 if no such line exists.
     */
    private static int findMediaDescriptionLine(boolean isAudio, String[] sdpLines) {
        final String mediaDescription = isAudio ? "m=audio " : "m=video ";
        for (int i = 0; i < sdpLines.length; ++i) {
            if (sdpLines[i].startsWith(mediaDescription)) {
                return i;
            }
        }
        return -1;
    }

    private static String joinString(
        Iterable<? extends CharSequence> s, String delimiter, boolean delimiterAtEnd) {
        Iterator<? extends CharSequence> iter = s.iterator();
        if (!iter.hasNext()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(iter.next());
        while (iter.hasNext()) {
            buffer.append(delimiter).append(iter.next());
        }
        if (delimiterAtEnd) {
            buffer.append(delimiter);
        }
        return buffer.toString();
    }

    private static @Nullable
    String movePayloadTypesToFront(
        List<String> preferredPayloadTypes, String mLine) {
        // The format of the media description line should be: m=<media> <port> <proto> <fmt> ...
        final List<String> origLineParts = Arrays.asList(mLine.split(" "));
        if (origLineParts.size() <= 3) {
            Log.e(TAG, "Wrong SDP media description format: " + mLine);
            return null;
        }
        final List<String> header = origLineParts.subList(0, 3);
        final List<String> unpreferredPayloadTypes =
            new ArrayList<>(origLineParts.subList(3, origLineParts.size()));
        unpreferredPayloadTypes.removeAll(preferredPayloadTypes);
        // Reconstruct the line with |preferredPayloadTypes| moved to the beginning of the payload
        // types.
        final List<String> newLineParts = new ArrayList<>();
        newLineParts.addAll(header);
        newLineParts.addAll(preferredPayloadTypes);
        newLineParts.addAll(unpreferredPayloadTypes);
        return joinString(newLineParts, " ", false /* delimiterAtEnd */);
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        final String[] lines = sdpDescription.split("\r\n");
        final int mLineIndex = findMediaDescriptionLine(isAudio, lines);
        if (mLineIndex == -1) {
            Log.w(TAG, "No mediaDescription line, so can't prefer " + codec);
            return sdpDescription;
        }
        // A list with all the payload types with name |codec|. The payload types are integers in the
        // range 96-127, but they are stored as strings here.
        final List<String> codecPayloadTypes = new ArrayList<>();
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        final Pattern codecPattern = Pattern.compile("^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$");
        for (String line : lines) {
            Matcher codecMatcher = codecPattern.matcher(line);
            if (codecMatcher.matches()) {
                codecPayloadTypes.add(codecMatcher.group(1));
            }
        }
        if (codecPayloadTypes.isEmpty()) {
            Log.w(TAG, "No payload types with name " + codec);
            return sdpDescription;
        }

        final String newMLine = movePayloadTypesToFront(codecPayloadTypes, lines[mLineIndex]);
        if (newMLine == null) {
            return sdpDescription;
        }
        Log.d(TAG, "Change media description from: " + lines[mLineIndex] + " to " + newMLine);
        lines[mLineIndex] = newMLine;
        return joinString(Arrays.asList(lines), "\r\n", true /* delimiterAtEnd */);
    }

    private void createPeerConnectionInternal() {
        if (factory == null || isError) {
            Log.e(TAG, "Peerconnection factory is not created");
            return;
        }
        CallExtraSettings extraSettings = getCallExtraSettings();
        Log.d(TAG, "Create peer connection. EXTRA_SETTINGS:" + extraSettings);

        queuedRemoteCandidates = new ArrayList<>();

        PeerConnection.RTCConfiguration rtcConfig =
            new PeerConnection.RTCConfiguration(signalingParameters.iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = extraSettings.getTcpCandidatePolicy();
        rtcConfig.rtcpMuxPolicy = extraSettings.getRtcpMuxPolicy();
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        /**
         * Возможно не нужно
         * */
        // Enable DTLS for normal calls and disable for loopback calls.
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        peerConnection = factory.createPeerConnection(rtcConfig, pcObserver);

        if (dataChannelEnabled) {
            DataChannel.Init init = new DataChannel.Init();
            init.ordered = peerConnectionParameters.dataChannelParameters.ordered;
            init.negotiated = peerConnectionParameters.dataChannelParameters.negotiated;
            init.maxRetransmits = peerConnectionParameters.dataChannelParameters.maxRetransmits;
            init.maxRetransmitTimeMs = peerConnectionParameters.dataChannelParameters.maxRetransmitTimeMs;
            init.id = peerConnectionParameters.dataChannelParameters.id;
            init.protocol = peerConnectionParameters.dataChannelParameters.protocol;
            dataChannel = peerConnection.createDataChannel("HOMEPADataChannel", init);
        }
        isInitiator = false;

        // Set INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

        List<String> mediaStreamLabels = Collections.singletonList("HOMEPAMS");
        if (isVideoCallEnabled()) {
            peerConnection.addTrack(createVideoTrack(videoCapturer), mediaStreamLabels);
            // We can add the renderers right away because we don't need to wait for an
            // answer to get the remote track.
            remoteVideoTrack = getRemoteVideoTrack();
            remoteVideoTrack.setEnabled(renderVideo);
            for (VideoSink remoteSink : remoteSinks) {
                remoteVideoTrack.addSink(remoteSink);
            }
        }
        peerConnection.addTrack(createAudioTrack(), mediaStreamLabels);

        findVideoSender();

        if (peerConnectionParameters.aecDump) {
            try {
                ParcelFileDescriptor aecDumpFileDescriptor =
                    ParcelFileDescriptor.open(new File(Environment.getExternalStorageDirectory().getPath()
                            + File.separator + "Download/audio.aecdump"),
                        ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE
                            | ParcelFileDescriptor.MODE_TRUNCATE);
                factory.startAecDump(aecDumpFileDescriptor.detachFd(), -1);
            } catch (IOException e) {
                Log.e(TAG, "Can not open aecdump file", e);
            }
        }

        if (saveRecordedAudioToFile != null) {
            if (saveRecordedAudioToFile.start()) {
                Log.d(TAG, "Recording input audio to file is activated");
            }
        }
        Log.d(TAG, "Peer connection created.");
    }

    private CallExtraSettings getCallExtraSettings() {
        PeerConnection.TcpCandidatePolicy tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        PeerConnection.RtcpMuxPolicy rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;

        String prefTcpCandidatePolicy = sharedPref.getString(PREF_KEY_TCP_CANDIDATE_POLICY, null);
        String prefRtcpMuxPolicy = sharedPref.getString(PREF_KEY_RTCP_MUX_POLICY, null);

        if (prefTcpCandidatePolicy != null
            && prefTcpCandidatePolicy.equals(CallExtraSettings.TCP_CANDIDATE_POLICY_ENABLED)) {
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED;
        }

        if (prefRtcpMuxPolicy != null
            && prefRtcpMuxPolicy.equals(CallExtraSettings.RTCP_MUX_POLICY_NEGOTIATE)) {
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        }

        return new CallExtraSettings(tcpCandidatePolicy, rtcpMuxPolicy);
    }

    /**
     * This function should only be called once.
     */
    public void createPeerConnectionFactory(PeerConnectionFactory.Options options) {
        if (factory != null) {
            throw new IllegalStateException("PeerConnectionFactory has already been constructed");
        }
        executor.execute(() -> createPeerConnectionFactoryInternal(options));
    }

    public void createPeerConnection(final VideoSink localRender, final VideoSink remoteSink,
                                     final VideoCapturer videoCapturer, final SignalingParameters signalingParameters) {
        if (peerConnectionParameters.videoCallEnabled && videoCapturer == null) {
            Log.w(TAG, "Video call enabled but no video capturer provided.");
        }
        createPeerConnection(
            localRender, Collections.singletonList(remoteSink), videoCapturer, signalingParameters);
    }

    public void createPeerConnection(final VideoSink localRender, final List<VideoSink> remoteSinks,
                                     final VideoCapturer videoCapturer, final SignalingParameters signalingParameters) {
        if (peerConnectionParameters == null) {
            Log.e(TAG, "Creating peer connection without initializing factory.");
            return;
        }
        this.localRender = localRender;
        this.remoteSinks = remoteSinks;
        this.videoCapturer = videoCapturer;
        this.signalingParameters = signalingParameters;
        executor.execute(() -> {
            try {
                createMediaConstraintsInternal();
                createPeerConnectionInternal();
                maybeCreateAndStartRtcEventLog();
            } catch (Exception e) {
                reportError("Failed to create peer connection: " + e.getMessage());
                throw e;
            }
        });
    }

    public void close() {
        executor.execute(this::closeInternal);
    }

    private boolean isVideoCallEnabled() {
        return peerConnectionParameters.videoCallEnabled && videoCapturer != null;
    }

    private void createPeerConnectionFactoryInternal(PeerConnectionFactory.Options options) {
        isError = false;

        if (peerConnectionParameters.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                    + "webrtc-trace.txt");
        }

        // Check if ISAC is used by default.
        preferIsac = peerConnectionParameters.audioCodec != null
            && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

        // It is possible to save a copy in raw PCM format on a file by checking
        // the "Save input audio to file" checkbox in the Settings UI. A callback
        // interface is set when this flag is enabled. As a result, a copy of recorded
        // audio samples are provided to this client directly from the native audio
        // layer in Java.
        if (peerConnectionParameters.saveInputAudioToFile) {
            if (!peerConnectionParameters.useOpenSLES) {
                Log.d(TAG, "Enable recording of microphone input audio to file");
                saveRecordedAudioToFile = new RecordedAudioToFileController(executor);
            } else {
                // TODO(henrika): ensure that the UI reflects that if OpenSL ES is selected,
                // then the "Save inut audio to file" option shall be grayed out.
                Log.e(TAG, "Recording of input audio is not supported for OpenSL ES");
            }
        }

        final AudioDeviceModule adm = createJavaAudioDevice();

        // Create peer connection factory.
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: " + options.networkIgnoreMask);
        }
        final boolean enableH264HighProfile =
            VIDEO_CODEC_H264_HIGH.equals(peerConnectionParameters.videoCodec);
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        if (peerConnectionParameters.videoCodecHwAcceleration) {
            encoderFactory = new DefaultVideoEncoderFactory(
                rootEglBase.getEglBaseContext(), true /* enableIntelVp8Encoder */, enableH264HighProfile);
            decoderFactory = new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext());
        } else {
            encoderFactory = new SoftwareVideoEncoderFactory();
            decoderFactory = new SoftwareVideoDecoderFactory();
        }

        factory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setAudioDeviceModule(adm)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory();
        Log.d(TAG, "Peer connection factory created.");
        adm.release();
    }

    AudioDeviceModule createJavaAudioDevice() {
        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters.useOpenSLES) {
            Log.w(TAG, "External OpenSLES ADM not implemented yet.");
            // TODO(magjed): Add support for external OpenSLES ADM.
        }

        // Set audio record error callbacks.
        AudioRecordErrorCallback audioRecordErrorCallback = new AudioRecordErrorCallback() {
            @Override
            public void onWebRtcAudioRecordInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordInitError: " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordStartError(
                JavaAudioDeviceModule.AudioRecordStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioRecordError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioRecordError: " + errorMessage);
                reportError(errorMessage);
            }
        };

        AudioTrackErrorCallback audioTrackErrorCallback = new AudioTrackErrorCallback() {
            @Override
            public void onWebRtcAudioTrackInitError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackInitError: " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackStartError(
                JavaAudioDeviceModule.AudioTrackStartErrorCode errorCode, String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackStartError: " + errorCode + ". " + errorMessage);
                reportError(errorMessage);
            }

            @Override
            public void onWebRtcAudioTrackError(String errorMessage) {
                Log.e(TAG, "onWebRtcAudioTrackError: " + errorMessage);
                reportError(errorMessage);
            }
        };

        return JavaAudioDeviceModule.builder(appContext)
            .setSamplesReadyCallback(saveRecordedAudioToFile)
            .setUseHardwareAcousticEchoCanceler(!peerConnectionParameters.disableBuiltInAEC)
            .setUseHardwareNoiseSuppressor(!peerConnectionParameters.disableBuiltInNS)
            .setAudioRecordErrorCallback(audioRecordErrorCallback)
            .setAudioTrackErrorCallback(audioTrackErrorCallback)
            .createAudioDeviceModule();
    }

    private void createMediaConstraintsInternal() {
        // Create video constraints if video call is enabled.
        if (isVideoCallEnabled()) {
            videoWidth = peerConnectionParameters.videoWidth;
            videoHeight = peerConnectionParameters.videoHeight;
            videoFps = peerConnectionParameters.videoFps;

            // If video resolution is not specified, default to HD.
            if (videoWidth == 0 || videoHeight == 0) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }

            // If fps is not specified, default to 30.
            if (videoFps == 0) {
                videoFps = 30;
            }
            Logging.d(TAG, "Capturing format: " + videoWidth + "x" + videoHeight + "@" + videoFps);
        }

        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            Log.d(TAG, "Disabling audio processing");
            audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(
            new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
            "OfferToReceiveVideo", Boolean.toString(isVideoCallEnabled())));
    }

    public void sendMessage(String message) {
        ByteBuffer data = ByteBuffer.wrap(message.getBytes(Charset.defaultCharset()));
        if (dataChannel != null) {
            Timber.i("DATA_CHANNEL sendMessage:" + message);
            dataChannel.send(new DataChannel.Buffer(data, false));
        }
    }

    private ByteBuffer stringToByteBuffer(String msg, Charset charset) {
        return ByteBuffer.wrap(msg.getBytes(charset));
    }

    private void closeInternal() {
        if (factory != null && peerConnectionParameters.aecDump) {
            factory.stopAecDump();
        }
        Log.d(TAG, "Closing peer connection.");
        statsTimer.cancel();
        if (dataChannel != null) {
            dataChannel.dispose();
            dataChannel = null;

        }
        if (rtcEventLog != null) {
            // RtcEventLog should stop before the peer connection is disposed.
            rtcEventLog.stop();
            rtcEventLog = null;
        }
        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
        }
        Log.d(TAG, "Closing audio source.");
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }
        Log.d(TAG, "Stopping capture.");
        if (videoCapturer != null) {
            try {
                videoCapturer.stopCapture();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            videoCapturerStopped = true;
            videoCapturer.dispose();
            videoCapturer = null;
        }
        Log.d(TAG, "Closing video source.");
        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        if (surfaceTextureHelper != null) {
            surfaceTextureHelper.dispose();
            surfaceTextureHelper = null;
        }
        if (saveRecordedAudioToFile != null) {
            Log.d(TAG, "Closing audio file for recorded input audio.");
            saveRecordedAudioToFile.stop();
            saveRecordedAudioToFile = null;
        }
        localRender = null;
        remoteSinks = null;
        Log.d(TAG, "Closing peer connection factory.");
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        rootEglBase.release();
        Log.d(TAG, "Closing peer connection done.");
        events.onConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }

    private File createRtcEventLogOutputFile() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmm_ss", Locale.getDefault());
        Date date = new Date();
        final String outputFileName = "event_log_" + dateFormat.format(date) + ".log";
        return new File(
            appContext.getDir(RTCEVENTLOG_OUTPUT_DIR_NAME, Context.MODE_PRIVATE), outputFileName);
    }

    private void maybeCreateAndStartRtcEventLog() {
        if (appContext == null || peerConnection == null) {
            return;
        }
        if (!peerConnectionParameters.enableRtcEventLog) {
            Log.d(TAG, "RtcEventLog is disabled.");
            return;
        }
        rtcEventLog = new RtcEventLog(peerConnection);
        rtcEventLog.start(createRtcEventLogOutputFile());
    }

    public void startVideoSource() {
        Timber.d("startVideoSource: " + videoCapturer + " " + videoCapturerStopped);
        executor.execute(() -> {
            if (videoCapturer != null && videoCapturerStopped) {
                Log.d(TAG, "Restart video source.");
                videoCapturer.startCapture(videoWidth, videoHeight, videoFps);
                videoCapturerStopped = false;
            }
        });
    }

    public boolean isHDVideo() {
        return isVideoCallEnabled() && videoWidth * videoHeight >= 1280 * 720;
    }

    @SuppressWarnings("deprecation") // TODO(sakal): getStats is deprecated.
    private void getStats() {
        if (peerConnection == null || isError) {
            return;
        }
        boolean success = peerConnection.getStats(new StatsObserver() {
            @Override
            public void onComplete(final StatsReport[] reports) {
                events.onPeerConnectionStatsReady(reports);
            }
        }, null);
        if (!success) {
            Log.e(TAG, "getStats() returns false!");
        }
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        if (enable) {
            try {
                statsTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executor.execute(() -> getStats());
                    }
                }, 0, periodMs);
            } catch (Exception e) {
                Log.e(TAG, "Can not schedule statistics timer", e);
            }
        } else {
            statsTimer.cancel();
        }
    }

    public void setAudioEnabled(final boolean enable) {
        executor.execute(() -> {
            enableAudio = enable;
            if (localAudioTrack != null) {
                localAudioTrack.setEnabled(enableAudio);
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(() -> {
            renderVideo = enable;
            if (localVideoTrack != null) {
                localVideoTrack.setEnabled(renderVideo);
            }
            if (remoteVideoTrack != null) {
                remoteVideoTrack.setEnabled(renderVideo);
            }
        });
    }

    public void createOffer() {
        executor.execute(() -> {
            Timber.d("call createOffer() method:" + peerConnection + " isError:" + isError);
            if (peerConnection != null && !isError) {
                Timber.d("PC Create OFFER");
                isInitiator = true;
                peerConnection.createOffer(sdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void createAnswer() {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                Timber.d("PC create ANSWER");
                isInitiator = false;
                peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        executor.execute(() -> {
            if (peerConnection != null && !isError) {
                if (queuedRemoteCandidates != null) {
                    queuedRemoteCandidates.add(candidate);
                } else {
                    peerConnection.addIceCandidate(candidate);
                }
            }
        });
    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        executor.execute(() -> {
            if (peerConnection == null || isError) {
                return;
            }
            // Drain the queued remote candidates if there is any so that
            // they are processed in the proper order.
            drainCandidates();
            peerConnection.removeIceCandidates(candidates);
        });
    }

    public void setRemoteDescription(final SessionDescription sdp) {
        executor.execute(() -> {
            if (peerConnection == null || isError) {
                return;
            }
            String sdpDescription = sdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
            }
            if (isVideoCallEnabled()) {
                sdpDescription =
                    preferCodec(sdpDescription, getSdpVideoCodecName(peerConnectionParameters), false);
            }
            if (peerConnectionParameters.audioStartBitrate > 0) {
                sdpDescription = setStartBitrate(
                    AUDIO_CODEC_OPUS, false, sdpDescription, peerConnectionParameters.audioStartBitrate);
            }
            Timber.d("Set remote SDP.");
            SessionDescription sdpRemote = new SessionDescription(sdp.type, sdpDescription);
            peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
        });
    }

    public void stopVideoSource() {
        executor.execute(() -> {
            if (videoCapturer != null && !videoCapturerStopped) {
                Log.d(TAG, "Stop video source.");
                try {
                    videoCapturer.stopCapture();
                } catch (InterruptedException e) {
                }
                videoCapturerStopped = true;
            }
        });
    }

    public void setVideoMaxBitrate(@Nullable final Integer maxBitrateKbps) {
        executor.execute(() -> {
            if (peerConnection == null || localVideoSender == null || isError) {
                return;
            }
            Log.d(TAG, "Requested max video bitrate: " + maxBitrateKbps);
            if (localVideoSender == null) {
                Log.w(TAG, "Sender is not ready.");
                return;
            }

            RtpParameters parameters = localVideoSender.getParameters();
            if (parameters.encodings.size() == 0) {
                Log.w(TAG, "RtpParameters are not ready.");
                return;
            }

            for (RtpParameters.Encoding encoding : parameters.encodings) {
                // Null value means no limit.
                encoding.maxBitrateBps = maxBitrateKbps == null ? null : maxBitrateKbps * BPS_IN_KBPS;
            }
            if (!localVideoSender.setParameters(parameters)) {
                Log.e(TAG, "RtpSender.setParameters failed.");
            }
            Log.d(TAG, "Configured max video bitrate to: " + maxBitrateKbps);
        });
    }

    private void reportError(final String errorMessage) {
        Timber.e("Peerconnection error: " + errorMessage);
        executor.execute(() -> {
            if (!isError) {
                events.onPeerConnectionError(errorMessage);
                isError = true;
            }
        });
    }

    @Nullable
    private AudioTrack createAudioTrack() {
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    @Nullable
    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        surfaceTextureHelper =
            SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());
        videoSource = factory.createVideoSource(capturer.isScreencast());
        capturer.initialize(surfaceTextureHelper, appContext, videoSource.getCapturerObserver());
        //    capturer.startCapture(videoWidth, videoHeight, videoFps);

        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addSink(localRender);
        return localVideoTrack;
    }

    private void findVideoSender() {
        for (RtpSender sender : peerConnection.getSenders()) {
            if (sender.track() != null) {
                String trackType = sender.track().kind();
                if (trackType.equals(VIDEO_TRACK_TYPE)) {
                    Log.d(TAG, "Found video sender.");
                    localVideoSender = sender;
                }
            }
        }
    }

    // Returns the remote VideoTrack, assuming there is only one.
    private @Nullable
    VideoTrack getRemoteVideoTrack() {
        for (RtpTransceiver transceiver : peerConnection.getTransceivers()) {
            MediaStreamTrack track = transceiver.getReceiver().track();
            if (track instanceof VideoTrack) {
                return (VideoTrack) track;
            }
        }
        return null;
    }

    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal(CameraVideoCapturer.CameraSwitchHandler handler) {
        if (videoCapturer instanceof CameraVideoCapturer) {
            if (!isVideoCallEnabled() || isError) {
                Log.e(TAG,
                    "Failed to switch camera. Video: " + isVideoCallEnabled() + ". Error : " + isError);
                return; // No video is sent or only one camera is available or error happened.
            }
            Log.d(TAG, "Switch camera");
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) videoCapturer;
            cameraVideoCapturer.switchCamera(handler);
        } else {
            Log.d(TAG, "Will not switch camera, video caputurer is not a camera");
        }
    }

    public void switchCamera() {
        executor.execute(() -> switchCameraInternal(null));
    }

    public void switchCamera(CameraVideoCapturer.CameraSwitchHandler handler) {
        executor.execute(() -> switchCameraInternal(handler));
    }

    public void changeCaptureFormat(final int width, final int height, final int framerate) {
        executor.execute(() -> changeCaptureFormatInternal(width, height, framerate));
    }

    private void changeCaptureFormatInternal(int width, int height, int framerate) {
        if (!isVideoCallEnabled() || isError || videoCapturer == null) {
            Log.e(TAG,
                "Failed to change capture format. Video: " + isVideoCallEnabled()
                    + ". Error : " + isError);
            return;
        }
        Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        videoSource.adaptOutputFormat(width, height, framerate);
    }

    /**
     * Peer connection events.
     */
    public interface PeerConnectionEvents {

        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once connection is established (IceConnectionState is CONNECTED).
         */
        void onConnectionEstablished();

        /**
         * Callback fired once connection is broken (IceConnectionState is DISCONNECTED). Reconnect possible!
         */
        void onDisconnect();

        /**
         * Callback fired once peer connection is closed.
         */
        void onConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);


        void onDataChannelMessage(final String message);
    }

    /**
     * Peer connection parameters.
     */
    public static class DataChannelParameters {

        public final boolean ordered;
        public final int maxRetransmitTimeMs;
        public final int maxRetransmits;
        public final String protocol;
        public final boolean negotiated;
        public final int id;

        public DataChannelParameters(boolean ordered, int maxRetransmitTimeMs, int maxRetransmits,
                                     String protocol, boolean negotiated, int id) {
            this.ordered = ordered;
            this.maxRetransmitTimeMs = maxRetransmitTimeMs;
            this.maxRetransmits = maxRetransmits;
            this.protocol = protocol;
            this.negotiated = negotiated;
            this.id = id;
        }
    }

    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {

        public final boolean videoCallEnabled;
        public final boolean loopback;
        public final boolean tracing;
        public final int videoWidth;
        public final int videoHeight;
        public final int videoFps;
        public final int videoMaxBitrate;
        public final String videoCodec;
        public final boolean videoCodecHwAcceleration;
        public final boolean videoFlexfecEnabled;
        public final int audioStartBitrate;
        public final String audioCodec;
        public final boolean noAudioProcessing;
        public final boolean aecDump;
        public final boolean saveInputAudioToFile;
        public final boolean useOpenSLES;
        public final boolean disableBuiltInAEC;
        public final boolean disableBuiltInAGC;
        public final boolean disableBuiltInNS;
        public final boolean disableWebRtcAGCAndHPF;
        public final boolean enableRtcEventLog;
        public final boolean useLegacyAudioDevice;
        private final DataChannelParameters dataChannelParameters;

        public PeerConnectionParameters(boolean videoCallEnabled, boolean loopback, boolean tracing,
                                        int videoWidth, int videoHeight, int videoFps, int videoMaxBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean videoFlexfecEnabled, int audioStartBitrate,
                                        String audioCodec, boolean noAudioProcessing, boolean aecDump, boolean saveInputAudioToFile,
                                        boolean useOpenSLES, boolean disableBuiltInAEC, boolean disableBuiltInAGC,
                                        boolean disableBuiltInNS, boolean disableWebRtcAGCAndHPF, boolean enableRtcEventLog,
                                        boolean useLegacyAudioDevice, DataChannelParameters dataChannelParameters) {
            this.videoCallEnabled = videoCallEnabled;
            this.loopback = loopback;
            this.tracing = tracing;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.videoMaxBitrate = videoMaxBitrate;
            this.videoCodec = videoCodec;
            this.videoFlexfecEnabled = videoFlexfecEnabled;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration;
            this.audioStartBitrate = audioStartBitrate;
            this.audioCodec = audioCodec;
            this.noAudioProcessing = noAudioProcessing;
            this.aecDump = aecDump;
            this.saveInputAudioToFile = saveInputAudioToFile;
            this.useOpenSLES = useOpenSLES;
            this.disableBuiltInAEC = disableBuiltInAEC;
            this.disableBuiltInAGC = disableBuiltInAGC;
            this.disableBuiltInNS = disableBuiltInNS;
            this.disableWebRtcAGCAndHPF = disableWebRtcAGCAndHPF;
            this.enableRtcEventLog = enableRtcEventLog;
            this.useLegacyAudioDevice = useLegacyAudioDevice;
            this.dataChannelParameters = dataChannelParameters;
        }
    }

    static class CallExtraSettings {

        public static final String TCP_CANDIDATE_POLICY_ENABLED = "enabled";
        public static final String RTCP_MUX_POLICY_NEGOTIATE = "negotiate";
        PeerConnection.TcpCandidatePolicy tcpCandidatePolicy;
        PeerConnection.RtcpMuxPolicy rtcpMuxPolicy;

        public CallExtraSettings(
            PeerConnection.TcpCandidatePolicy tcpCandidatePolicy,
            PeerConnection.RtcpMuxPolicy rtcpMuxPolicy
        ) {
            this.tcpCandidatePolicy = tcpCandidatePolicy;
            this.rtcpMuxPolicy = rtcpMuxPolicy;
        }

        public PeerConnection.TcpCandidatePolicy getTcpCandidatePolicy() {
            return tcpCandidatePolicy;
        }

        public PeerConnection.RtcpMuxPolicy getRtcpMuxPolicy() {
            return rtcpMuxPolicy;
        }

        @Override
        public String toString() {
            return "tcpCandidatePolicy=" + tcpCandidatePolicy + ", rtcpMuxPolicy=" + rtcpMuxPolicy;
        }
    }

    // Implementation detail: observe ICE & stream changes and react accordingly.
    private class PCObserver implements PeerConnection.Observer {

        private final int MAX_RECONNECT_ATTEMPTS = 2;
        private AtomicInteger disconnectAttemptsCounter = new AtomicInteger(0);

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            Timber.d("ON_ICE_CANDIDATE:" + candidate.serverUrl);
            executor.execute(() -> events.onIceCandidate(candidate));
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(() -> events.onIceCandidatesRemoved(candidates));
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Timber.d("SignalingState: " + newState);
        }

        @Override
        public void onIceCandidateError(IceCandidateErrorEvent event) {
            PeerConnection.Observer.super.onIceCandidateError(event);
            String errText = event.errorText;
            int errCode = event.errorCode;
            Timber.e("ON_ICE_CANDIDATE_ERROR:" + errText + " ERR Code:" + errCode + " " + event.url);
        }

        @Override
        public void onIceConnectionChange(final IceConnectionState newState) {
            executor.execute(() -> {
                Timber.d("IceConnectionState: " + newState);
                switch (newState) {
                    case CONNECTED: {
                        disconnectAttemptsCounter.set(0);
                        events.onConnectionEstablished();
                        return;
                    }
                    case DISCONNECTED: {
                        Timber.e("ICE connection change state DISCONNECTED");
                        events.onDisconnect();
                        if (disconnectAttemptsCounter.incrementAndGet() > MAX_RECONNECT_ATTEMPTS) {
                            events.onConnectionClosed();
                        }
                        return;
                    }
                    case CLOSED:
                    case FAILED: {
                        Timber.e("ICE connection change state FAILED or CLOSED");
                        events.onConnectionClosed();
                        return;
                    }
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Timber.d("IceGatheringState: " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Timber.d("IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onAddStream(final MediaStream stream) {
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            Timber.d("New DATA_CHANNEL " + dc.label() + " INF:" + dc);
            if (!dataChannelEnabled) {
                return;
            }

            dc.registerObserver(new DataChannel.Observer() {
                @Override
                public void onBufferedAmountChange(long previousAmount) {
                    Timber.d("DATA_CHANNEL buffered amount changed: " + dc.label() + ": " + dc.state());
                }

                @Override
                public void onStateChange() {
                    Timber.d("DATA_CHANNEL state changed: " + dc.label() + ": " + dc.state() + " INF:" + dc);
                    if (dc.state() == DataChannel.State.CLOSED) {
                        events.onConnectionClosed();
                    }
                }

                @Override
                public void onMessage(final DataChannel.Buffer buffer) {
                    if (buffer.binary) {
                        Timber.d("DATA_CHANNEL Received binary msg over " + dc);
                        return;
                    }
                    ByteBuffer data = buffer.data;
                    final byte[] bytes = new byte[data.capacity()];
                    data.get(bytes);
                    String strData = new String(bytes, StandardCharsets.UTF_8);
                    Timber.d("DATA_CHANNEL Got msg: " + strData + " over " + dc);

                    events.onDataChannelMessage(strData);
                }
            });
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }

        @Override
        public void onAddTrack(final RtpReceiver receiver, final MediaStream[] mediaStreams) {
        }
    }

    // Implementation detail: handle offer creation/signaling and answer setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {

        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            if (localSdp != null) {
                reportError("Multiple SDP create.");
                return;
            }
            String sdpDescription = origSdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
            }
            if (isVideoCallEnabled()) {
                sdpDescription =
                    preferCodec(sdpDescription, getSdpVideoCodecName(peerConnectionParameters), false);
            }

            String filteredSdpDescription = sdpDescription.replace(SDP_WRONG_PARAMETER, "");
            final SessionDescription sdp = new SessionDescription(origSdp.type, filteredSdpDescription);

            localSdp = sdp;
            executor.execute(() -> {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "Set local SDP from " + sdp.type + " SDP: " + sdp.description);
                    peerConnection.setLocalDescription(sdpObserver, sdp);
                }
            });
        }

        @Override
        public void onSetSuccess() {
            executor.execute(() -> {
                Timber.e("SDPObserver SDP onSetSuccess ---peerConnection:" + peerConnection + " isError:" + isError);
                if (peerConnection == null || isError) {
                    return;
                }
                if (isInitiator) {
                    // For offering peer connection we first create offer and set
                    // local SDP, then after receiving answer set remote SDP.
                    if (peerConnection.getRemoteDescription() == null) {
                        // We've just set our local SDP so time to send it.
                        Log.d(TAG, "Local SDP set succesfully");
                        events.onLocalDescription(localSdp);
                    } else {
                        // We've just set remote description, so drain remote
                        // and send local ICE candidates.
                        Log.d(TAG, "Remote SDP set succesfully");
                        drainCandidates();
                    }
                } else {
                    // For answering peer connection we set remote SDP and then
                    // create answer and set local SDP.
                    if (peerConnection.getLocalDescription() != null) {
                        // We've just set our local SDP so time to send it, drain
                        // remote and send local ICE candidates.
                        Log.d(TAG, "Local SDP set succesfully");
                        events.onLocalDescription(localSdp);
                        drainCandidates();
                    } else {
                        // We've just set remote SDP - do nothing for now -
                        // answer will be created soon.
                        Log.d(TAG, "Remote SDP set succesfully");
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(final String error) {
            Timber.e("SDPObserver SDP onCreateFailure: %s", error);
            reportError("createSDP error: " + error);
        }

        @Override
        public void onSetFailure(final String error) {
            Timber.e("SDPObserver SDP onSetFailure: %s", error);
            reportError("SDPObserver setSDP error: " + error);
        }
    }


}
