package com.numplates.nomera3.modules.baseCore.helper

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.meera.core.extensions.empty
import com.meera.core.extensions.isOnTheScreen
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.di.AudioModule.Companion.AUDIO_EXO_PLAYER
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionPlayStopMusic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMusicPlay
import com.numplates.nomera3.modules.music.ui.entity.ID_POST_DURING_CREATING
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.view.widgets.CustomControlView
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

const val AUDIO_FEED_HELPER_VIEW_TAG = "AUDIO_FEED_HELPER_VIEW_TAG"
private const val MAX_MUSIC_LENGTH_MILLISECOND = 30_000.0
private const val PERIOD_LENGTH_MILLISECOND = 20L
private const val HUNDRED_PERCENT = 100
private const val TWO_DIGITS_AFTER_ZERO = 100
private const val DELAY_LOADING_HANDING_MLS = 1_000L

interface ViewHolderAudio {
    fun unSubscribe()
    fun subscribe()
}

interface AudioFeedHelper {
    fun startPlaying(
        idPost: Long? = null,
        url: String = String.empty(),
        audioEventListener: AudioEventListener? = null,
        holderPosition: Int = 0,
        musicView: View?,
    )

    fun stopPlaying(
        isLifecycleStop: Boolean = false,
        isReset: Boolean = false,
        needToLog: Boolean = false,
        isStoppedFromPost: Boolean = true
    )

    fun addAudioPriorityListener(listener: () -> Unit)
    fun removeAudioPriorityListener(listener: () -> Unit)
    fun addAudioEventListener(event: AudioEventListener)
    fun removeAudioEventListener(event: AudioEventListener)
    fun getCurrentAudio(): Long? = null
    fun getCurrentAudioURL(): String? = null
    fun getHolderPosition(): Int
    fun isPlaying(): Boolean
    fun onScrolled(manager: LinearLayoutManager? = null)
    fun releasePlayer()
    fun init()
}

interface AudioEventListener {
    fun onPlay(withListener: Boolean = false)
    fun onPause(isReset: Boolean = false)
    fun onLoad(isDownload: Boolean)
    fun onProgress(percent: Int)
}

class AudioFeedHelperImpl @Inject constructor(
    private val context: Context,
    @Named(AUDIO_EXO_PLAYER)
    private val audioPlayer: SimpleExoPlayer,
    private val analytics: AnalyticsInteractor
) : AudioFeedHelper {

    private var currentAudio: PostMusicContainer? = null

    private var currentAudioEventListener: AudioEventListener? = null

    private var musicView: View? = null

    private var isPlaying = false

    private val control = CustomControlView(context, R.layout.feed_exo_player_control_view_extended)

    private val commonDisposable = CompositeDisposable()

    private val loadDisposable = CompositeDisposable()

    private val audioPriorityListener = mutableSetOf<() -> Unit>()

    private var audioEventListeners = linkedSetOf<AudioEventListener>()

    private var isNotReset: Boolean = true

    private val playerEventListener = object : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    currentAudioEventListener?.onLoad(true)
                }

                Player.STATE_ENDED -> {
                    currentAudioEventListener?.onPause()
                }

                Player.STATE_IDLE ->
                    Single.timer(DELAY_LOADING_HANDING_MLS, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                            onError = {
                                Timber.d("AudioEventListener Player.STATE_BUFFERING ERROR")
                            },
                            onSuccess = {
                                Timber.d("AudioEventListener Player.STATE_BUFFERING SUCCESS")
                                if (isNotReset) currentAudioEventListener?.onPause()
                            }
                        )
                        .addTo(loadDisposable)

                else -> {
                    if (isNotReset) currentAudioEventListener?.onLoad(false)
                    loadDisposable.clear()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if (this@AudioFeedHelperImpl.isPlaying && isPlaying.not()) currentAudioEventListener?.onPause()
            if (isPlaying.not()) commonDisposable.clear()
            else {
                currentAudioEventListener?.onPlay()

                Observable.interval(PERIOD_LENGTH_MILLISECOND, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map { control.player.currentPosition }
                    .map { HUNDRED_PERCENT * it / MAX_MUSIC_LENGTH_MILLISECOND }
                    .map { it * TWO_DIGITS_AFTER_ZERO }
                    .map { it.toInt() }
                    .distinct()
                    .subscribe { currentAudioEventListener?.onProgress(it) }
                    .addTo(commonDisposable)
            }
        }
    }

    init { init() }

    override fun isPlaying() = isPlaying

    override fun addAudioPriorityListener(listener: () -> Unit) {
        audioPriorityListener.add(listener)
    }

    override fun removeAudioPriorityListener(listener: () -> Unit) {
        audioPriorityListener.remove(listener)
    }

    override fun addAudioEventListener(event: AudioEventListener) {
        audioEventListeners.add(event)
    }

    override fun removeAudioEventListener(event: AudioEventListener) {
        audioEventListeners.remove(event)
    }

    private fun clearCacheForCurrentAudio() {
        val url = currentAudio?.urlMusic ?: return
        Act.simpleCache?.removeResource(url)
    }

    override fun startPlaying(
        idPost: Long?,
        url: String,
        audioEventListener: AudioEventListener?,
        holderPosition: Int,
        musicView: View?
    ) {
        logPlay(idPost)
        this.musicView = musicView
        if (currentAudioEventListener != null) stopPlaying()

        currentAudioEventListener = audioEventListener
        isNotReset = true
        idPost ?: return
        currentAudio = PostMusicContainer(idPost, url, holderPosition)

        val dataSource =
            DefaultDataSource.Factory(context)

        val cacheDataSource =
            CacheDataSource.Factory()
                .setCache(MeeraAct.simpleCache!!)
                .setUpstreamDataSourceFactory(dataSource)

        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(cacheDataSource)
            .createMediaSource(MediaItem.fromUri(url))
        audioPlayer.setMediaSource(mediaSource)
        audioPlayer.prepare()

        isPlaying = true
        audioPlayer.playWhenReady = true
        audioPriorityListener.forEach { it() }
    }

    fun logPlay(postID: Long?) {
        val where =
            if (postID == ID_POST_DURING_CREATING) AmplitudePropertyWhereMusicPlay.MUSIC_SEARCH
            else AmplitudePropertyWhereMusicPlay.POST
        analytics.logPlayStopMusic(
            where = where,
            actionType = AmplitudePropertyActionPlayStopMusic.PLAY
        )
    }

    fun logStop(isStoppedFromPost: Boolean) {
        val where =
            if (isStoppedFromPost) AmplitudePropertyWhereMusicPlay.POST
            else AmplitudePropertyWhereMusicPlay.MUSIC_SEARCH
        analytics.logPlayStopMusic(
            where = where,
            actionType = AmplitudePropertyActionPlayStopMusic.STOP
        )
    }

    override fun stopPlaying(
        isLifecycleStop: Boolean,
        isReset: Boolean,
        needToLog: Boolean,
        isStoppedFromPost: Boolean,
    ) {
        if (needToLog) logStop(isStoppedFromPost)
        musicView = null
        isPlaying = false

        currentAudioEventListener?.onPause(isReset)

        control.doPause()

        audioPlayer.playWhenReady = false
        currentAudioEventListener = null

        commonDisposable.clear()
        loadDisposable.clear()
    }

    override fun getCurrentAudio() = currentAudio?.idPost

    override fun getCurrentAudioURL() = currentAudio?.urlMusic

    override fun getHolderPosition() = currentAudio?.holderPosition ?: 0

    override fun onScrolled(manager: LinearLayoutManager?) {
        if (!isPlaying) return
        val isViewVisible = musicView?.isOnTheScreen()
        if (isViewVisible == false) stopPlaying()
    }

    override fun releasePlayer() {
        clearCacheForCurrentAudio()
        currentAudioEventListener = null
        audioPlayer.removeListener(playerEventListener)
        control.player = null
    }

    override fun init() {
        control.player = audioPlayer
        audioPlayer.addListener(playerEventListener)
    }
}

private data class PostMusicContainer(
    val idPost: Long,
    val urlMusic: String,
    val holderPosition: Int
)
