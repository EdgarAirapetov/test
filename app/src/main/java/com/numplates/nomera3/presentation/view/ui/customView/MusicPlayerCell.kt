package com.numplates.nomera3.presentation.view.ui.customView

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.widgets.CircularProgressBar

private const val DURATION_150 = 150L
private const val DURATION_200 = 200L
private const val RADIUS_8 = 8
private const val BUTTON_ANIM_STATE_70 = 0.7f
private const val BUTTON_ANIM_STATE_100 = 1.0f
private const val BUTTON_ANIM_STATE_125 = 1.25f

interface MediaPlayerListener {
    fun onPlay(withListener: Boolean)
    fun onStop(withListener: Boolean, isReset: Boolean = false)
    fun clickShare()
    fun onDoubleClick() = Unit
}

class MusicPlayerCell @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var ivAlbum: ImageView? = null
    private var pbStatus: CircularProgressBar? = null
    private var pbInfinite: ProgressBar? = null
    private var ivShareIcon: ImageView? = null
    private var vAction: View? = null
    private var ivAppleLabel: AppCompatImageView? = null
    private var tvMusicArtist: TextView? = null
    private var tvMusicTitle: TextView? = null
    private var vContent: View? = null
    private var vPlayerContainer: View? = null
    private var ivBtn: ImageView? = null
    private var albumDrawable: RoundedBitmapDrawable? = null
    private var isPlaying = false
    private var mListener: MediaPlayerListener? = null
    private var isChangedImage = false
    private var playButtonAnimatorSet: AnimatorSet? = null
    private var albumAnimatorSet: AnimatorSet? = null
    private val albumRadiusDefault = RADIUS_8.dp.toFloat()
    private var customViewTarget: CustomViewTarget<ImageView, Bitmap>? = null
    private var appendDoubleClickListenerForContainer : DoubleOrOneClickListener? = null
    private var appendDoubleClickListenerForContent : DoubleOrOneClickListener? = null
    private var appendDoubleClickListenerForAction : DoubleOrOneClickListener? = null
    private var updateListener: ValueAnimator.AnimatorUpdateListener? = null
    private var firstHalf: ValueAnimator? = null
    private var secondHalf: ValueAnimator? = null
    private var actionClick: (() -> Unit)? = null

    fun clearResources() {
        customViewTarget?.let {
            ivAlbum?.let { ivAlbum ->
                Glide.with(ivAlbum).clear(customViewTarget)
            }
            customViewTarget = null
        }

        ivAlbum?.glideClear()
        ivAlbum?.setImageDrawable(null)
        albumDrawable = null

        playButtonAnimatorSet?.removeAllListeners()
        playButtonAnimatorSet?.cancel()
        playButtonAnimatorSet = null

        albumAnimatorSet?.removeAllListeners()
        albumAnimatorSet?.cancel()
        albumAnimatorSet = null

        firstHalf?.removeAllUpdateListeners()
        secondHalf?.removeAllUpdateListeners()
        firstHalf?.cancel()
        firstHalf?.interpolator = null
        secondHalf?.interpolator = null
        secondHalf?.cancel()
        firstHalf = null
        secondHalf = null

        vPlayerContainer?.setOnClickListener(null)
        vContent?.setOnClickListener(null)
        vAction?.setOnClickListener(null)
        ivShareIcon?.setOnClickListener(null)

        appendDoubleClickListenerForContainer = null
        appendDoubleClickListenerForContent = null
        appendDoubleClickListenerForAction = null
        actionClick = null

        pbStatus?.progress = 0f
        pbStatus?.gone()
        pbInfinite?.gone()
        tvMusicArtist?.text = ""
        tvMusicTitle?.text = ""

        updateListener = null
        mListener = null
    }

    init {
        View.inflate(context, R.layout.music_player_cell, this)
        val arr = context.obtainStyledAttributes(attrs, R.styleable.MusicPlayerCell)
        val showActionButton: Boolean = arr.getBoolean(R.styleable.MusicPlayerCell_show_action_button, true)
        val actionImgResource = arr.getResourceId(
            R.styleable.MusicPlayerCell_action_btn_src,
            R.drawable.ic_url_apple_music
        )
        val needToShowAppleMusicLabel =
            arr.getBoolean(R.styleable.MusicPlayerCell_need_to_show_apple_music_label, true)
        ivAlbum = findViewById(R.id.iv_album)
        pbStatus = findViewById(R.id.pb_music_player)
        pbInfinite = findViewById(R.id.pb_infinite)
        ivShareIcon = findViewById(R.id.iv_share)
        vAction = findViewById(R.id.v_action)
        ivAppleLabel = findViewById(R.id.iv_apple_label)
        tvMusicArtist = findViewById(R.id.tv_music_artist)
        tvMusicTitle = findViewById(R.id.tv_music_title)
        vContent = findViewById(R.id.v_player_content)
        vPlayerContainer = findViewById(R.id.v_player)
        ivBtn = findViewById(R.id.iv_btn)
        isChangedImage = actionImgResource != R.drawable.ic_url_apple_music
        ivShareIcon?.setImageResource(actionImgResource)
        if (needToShowAppleMusicLabel) ivAppleLabel?.visible() else ivAppleLabel?.gone()
        if (showActionButton) {
            ivShareIcon?.visible()
            vAction?.visible()
        } else {
            ivShareIcon?.gone()
            vAction?.gone()
        }

        arr.recycle()

        pbInfinite?.gone()
        pbStatus?.gone()

        initListeners()
    }

    fun stopPlaying(withListener: Boolean = true, isReset: Boolean = false) {
        mListener?.onStop(withListener, isReset)
        if (isPlaying && !isReset) {
            isPlaying = false
            animatePlayButton()
        }
        isPlaying = false
        pbStatus?.gone()
        pbStatus?.progress = 0F
        pbInfinite?.gone()
        if (isReset) {
            ivBtn?.setImageResource(R.drawable.ic_filled_play_s)
            albumDrawable?.cornerRadius = albumRadiusDefault
            ivAlbum?.setImageDrawable(albumDrawable)
            albumAnimatorSet?.cancel()
        }
    }

    fun startDownloading() {
        pbInfinite?.gone()
    }

    fun stopDownloading() {
        pbInfinite?.gone()
    }

    fun startPlaying(withListener: Boolean = true) {
        mListener?.onPlay(withListener)
        if (!isPlaying) {
            isPlaying = true
            animatePlayButton()
        }
    }

    fun setProgress(progress: Int) {
        pbStatus?.visible()
        pbStatus?.progress = progress.toFloat()
        if (progress == 10000) {
            stopPlaying()
        }
    }

    private fun animatePlayButton() {
        playButtonAnimatorSet?.cancel()
        playButtonAnimatorSet = AnimatorSet()
        updateListener = ValueAnimator.AnimatorUpdateListener {
            val animatedValue = it.animatedValue as Float
            ivBtn?.scaleX = animatedValue
            ivBtn?.scaleY = animatedValue
        }
        firstHalf = ValueAnimator.ofFloat(BUTTON_ANIM_STATE_100, BUTTON_ANIM_STATE_70).apply {
            addUpdateListener(updateListener)
            interpolator = DecelerateInterpolator()
            duration = DURATION_200
            doOnEnd {
                if (isPlaying) {
                    ivBtn?.setImageResource(R.drawable.ic_filled_stop_l)
                } else {
                    ivBtn?.setImageResource(R.drawable.ic_filled_play_s)
                }
            }
        }
        secondHalf = ValueAnimator.ofFloat(
            BUTTON_ANIM_STATE_70,
            BUTTON_ANIM_STATE_125,
            BUTTON_ANIM_STATE_100
        ).apply {
            addUpdateListener(updateListener)
            interpolator = DecelerateInterpolator()
            duration = DURATION_150
        }
        playButtonAnimatorSet?.apply {
            playSequentially(firstHalf, secondHalf)
            start()
        }
    }

    fun showActionButton() =
        ivShareIcon?.visible()

    fun setMediaInformation(albumUrl: String?, artistName: String?, musicTitle: String?, isDarkMode: Boolean = false) {
        albumAnimatorSet?.cancel()
        tvMusicArtist?.text = artistName
        tvMusicTitle?.text = musicTitle
        ivAlbum?.setImageDrawable(null)
        ivAlbum?.let { ivAlbum->
        customViewTarget = object : CustomViewTarget<ImageView, Bitmap>(ivAlbum) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                ivAlbum.setImageDrawable(null)
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                albumDrawable = RoundedBitmapDrawableFactory.create(resources, resource)
                albumDrawable?.cornerRadius = RADIUS_8.dp.toFloat()
                ivAlbum.setImageDrawable(albumDrawable)
            }

            override fun onResourceCleared(placeholder: Drawable?) {
                ivAlbum.setImageDrawable(null)
            }

        }.also { target ->
            Glide.with(ivAlbum)
                .asBitmap()
                .load(albumUrl)
                .into(target)
        }
    }

        setupStyle(isDarkMode)
    }

    private fun setupStyle(isDarkMode: Boolean) {
        if (isDarkMode) {
            tvMusicTitle?.setTextColor(ContextCompat.getColor(context, R.color.uiKitColorForegroundInvers))
        } else {
            tvMusicTitle?.setTextColor(ContextCompat.getColor(context, R.color.uiKitColorForegroundPrimary))
        }
    }

    fun initMediaController(listener: MediaPlayerListener) {
        this.mListener = listener
    }

    fun isPlaying(): Boolean = isPlaying

    fun setOnActionBtnClickListener(click: () -> Unit) {
        actionClick = click
        vAction?.click { actionClick?.invoke() }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initListeners()
    }

    private fun initListeners() {
        appendDoubleClickListenerForContainer = object : DoubleOrOneClickListener() {
            override fun onDoubleClick() {
                mListener?.onDoubleClick()
            }

            override fun onClick() {
                if (isPlaying) {
                    stopPlaying(true)
                } else {
                    startPlaying(true)
                }
            }
        }

        appendDoubleClickListenerForContent = object : DoubleOrOneClickListener() {
            override fun onDoubleClick() {
                mListener?.onDoubleClick()
            }

            override fun onClick() {
                mListener?.clickShare()
            }
        }

        appendDoubleClickListenerForAction = object : DoubleOrOneClickListener() {
            override fun onDoubleClick() {
                mListener?.onDoubleClick()
            }

            override fun onClick() {
                mListener?.clickShare()
            }
        }

        vPlayerContainer?.setOnClickListener(appendDoubleClickListenerForContainer)
        vContent?.setOnClickListener(appendDoubleClickListenerForContent)
        vAction?.setOnClickListener(appendDoubleClickListenerForAction)
    }

}
