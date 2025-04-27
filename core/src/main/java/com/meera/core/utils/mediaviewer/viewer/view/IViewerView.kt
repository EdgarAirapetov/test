package com.meera.core.utils.mediaviewer.viewer.view

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import com.meera.core.utils.mediaviewer.ImageViewerData
import com.meera.core.utils.mediaviewer.MediaViewerPhotoEditorCallback
import com.meera.core.utils.mediaviewer.listeners.OnImageChangeListener


abstract class IViewerView@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
): RelativeLayout(context, attrs, defStyleAttr) {
    abstract var onSaveImage: ((imageUrl: String) -> Unit)?
    abstract var onImageReady: ((imageUrl: String) -> Unit)
    var onVideoReady: ((videoUrl: String) -> Unit) = {}
    abstract var onImageEdit: ((imageUrl: String) -> Unit)

    var mediaViewerPhotoEditorCallback: MediaViewerPhotoEditorCallback? = null
    open var selectedMediaCount: Int = 0
    open var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _->

    }

    var imageChangeListener: OnImageChangeListener? = null

    internal abstract var onDismiss: (() -> Unit)?
    internal abstract var onPageChange: ((position: Int) -> Unit)?


    internal abstract var currentPosition: Int
    internal abstract val isScaled: Boolean
    internal abstract var imagesMargin: Int

    internal abstract fun close()
    internal abstract fun open(transitionView: ImageView?, animate: Boolean)
    internal abstract fun updateImages(images: MutableList<ImageViewerData>)
    internal abstract fun updateTransitionImage(imageView: ImageView?)
    internal abstract fun resetScale()
    internal abstract fun setImages(images: MutableList<ImageViewerData>, startPosition: Int)

    fun getSilentState(): Boolean {
        val am = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        return when (am?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> true
            AudioManager.RINGER_MODE_VIBRATE -> true
            AudioManager.RINGER_MODE_NORMAL -> false
            else -> false
        }
    }
}
