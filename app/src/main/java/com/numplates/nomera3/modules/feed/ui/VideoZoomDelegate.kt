package com.numplates.nomera3.modules.feed.ui

import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ZoomyDuplicateVideoPlayerBinding
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy

class VideoZoomDelegate(
    post: PostUIEntity? = null,
    media: MediaAssetEntity? = null,
    itemView: View?,
    zoomyProvider: Zoomy.ZoomyProvider?,
    canZoom: CanPerformZoom,
    externalZoomListener: ZoomListener? = null
) {

    private val errorThrowable: IllegalStateException by lazy {
        IllegalStateException("Невозможно инициализировать видео-зум (zoomy) для ячейки ${post?.postId ?: media?.id}")
    }

    private val context = itemView?.context ?: error(errorThrowable)
    private val videoView: PlayerView = itemView?.findViewById(R.id.video_post_view)
        ?: itemView?.findViewById(R.id.pv_post_multimedia_item_video_view) ?: error(errorThrowable)
    private val mediaContainer: FrameLayout = itemView?.findViewById(R.id.media_container)
        ?: itemView?.findViewById(R.id.fl_post_multimedia_item_media_container) ?: error(errorThrowable)

    private val duplicatePlayerBinding = ZoomyDuplicateVideoPlayerBinding.inflate(LayoutInflater.from(context))
    private val duplicatePlayerContainer = duplicatePlayerBinding.root
    private val duplicatePlayerViewForZooming = duplicatePlayerBinding.pvZoomVideoDuplicate
    private val duplicatePlayerPreCacheScreenShot = duplicatePlayerBinding.ivZoomVideoPrecacheScreenshot

    private val builder = zoomyProvider?.provideBuilder() ?: error(errorThrowable)
    private val previewImage: ImageView = itemView?.findViewById(R.id.ivPicture)
        ?: itemView?.findViewById(R.id.iv_post_multimedia_item_image_view) ?: error(errorThrowable)

    private var startLayoutParams: LayoutParams? = null

    private val zoomListener = object : ZoomListener {
        override fun onViewStartedZooming(view: View?) {
            restoreLayoutParams()
            externalZoomListener?.onViewStartedZooming(view)
            val player = videoView.player
            videoView.player = null
            duplicatePlayerViewForZooming.player = player
            mediaContainer.invisible()
            previewImage.invisible()
            duplicatePlayerContainer.visible()
        }

        override fun onViewEndedZooming(view: View?) {
            externalZoomListener?.onViewEndedZooming(view)
            val player = duplicatePlayerViewForZooming.player ?: (view as? PlayerView?)?.player
            duplicatePlayerViewForZooming.player = null
            videoView.player = player
            fixBlinkingAfterZoomOut()
            previewImage.visible()
            mediaContainer.visible()
            duplicatePlayerContainer.gone()
        }

        private fun fixBlinkingAfterZoomOut() = Thread.sleep(50)
    }

    init {
        val aspect = post?.getSingleAspect() ?: media?.aspect?.toDouble() ?: 1.0
        builder.target(videoView)
            .setTargetDuplicate(duplicatePlayerContainer)
            .interpolator(OvershootInterpolator())
            .animateZooming(true)
            .zoomListener(zoomListener)
            .tapListener { mediaContainer.performClick() }
            .canPerformZoom(object : CanPerformZoom {
                override fun canZoom(): Boolean {
                    val canPerform = videoView.player?.isPlaying ?: false

                    if (canPerform) {
                        onZoomPrepare()
                    }

                    return canPerform && canZoom.canZoom()
                }
            })
            .aspectRatio(aspect)
        builder.register()
    }

    fun endZoom() {
        builder.endZoom()
    }

    private fun restoreLayoutParams() {
        if (startLayoutParams == null) {
            startLayoutParams = duplicatePlayerContainer.layoutParams
        }

        duplicatePlayerContainer.layoutParams = startLayoutParams
    }

    private fun onZoomPrepare() {
        val textureView = videoView.videoSurfaceView as TextureView
        duplicatePlayerPreCacheScreenShot.setImageBitmap(textureView.bitmap)
        duplicatePlayerPreCacheScreenShot.visible()
    }
}
