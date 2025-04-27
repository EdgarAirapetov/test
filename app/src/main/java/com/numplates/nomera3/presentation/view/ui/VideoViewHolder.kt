package com.numplates.nomera3.presentation.view.ui

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.exoplayer2.ui.PlayerView

// Для отоброжения видео в ленте достаточно имплементировать данный холдер
interface VideoViewHolder {

    var onShowPostClicked: (() -> Unit)?

    fun getPicture(): ImageView?

    fun getMediaContainer(): FrameLayout?
    fun getMediaContainerForVolume(): FrameLayout?

    fun getVideoDurationViewContainer(): View?

    fun getVideoUrlString(): String?

    fun getItemView(): View

    fun getVideoDuration(): Int

    fun needToPlay(): Boolean

    fun getVideoPlayerView(): PlayerView?

    fun startPlayingVideo(position: Long? = null)

    fun stopPlayingVideo()

    fun initPlayer()

    fun detachPlayer()

    fun getSelectedMediaPosition(): Int? { return null }

    fun holderIsNotAttachedToWindow(): Boolean
}
