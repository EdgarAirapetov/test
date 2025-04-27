package com.meera.core.utils.mediaviewer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.meera.core.R
import com.meera.core.bottomsheets.SuggestionsMenuContract
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.mediaviewer.listeners.OnDismissListener
import com.meera.core.utils.mediaviewer.listeners.OnImageChangeListener
import com.meera.core.utils.mediaviewer.viewer.view.IViewerView
import com.meera.core.utils.mediaviewer.viewer.view.MediaViewerPreview
import com.meera.core.utils.mediaviewer.viewer.view.MediaViewerPreviewChat
import com.meera.core.utils.mediaviewer.viewer.view.MediaViewerPreviewCreatePost
import com.meera.core.utils.mediaviewer.viewer.view.MediaViewerView
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment.DEFAULT_VIDEO_MAX_DURATION
import com.meera.media_controller_common.MediaControllerOpenPlace

@Suppress("detekt:UnusedPrivateMember")
class MediaViewer(
    context: Context,
    val images: MutableList<ImageViewerData>,
    var startPosition: Int = 0,
    var openEditor: Boolean = false,
    var imageChangeListener: OnImageChangeListener? = null,
    var onDismissListener: OnDismissListener? = null,
    var transitionView: ImageView?,
    var onSaveImage: ((imageUrl: String) -> Unit)? = null,
    var previewMode: Boolean = false,
    var onImageReady: ((imageUrl: String) -> Unit) = {},
    var onImageEdit: ((imageUrl: String) -> Unit) = {},
    var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _ -> },
    var fragmentManager: FragmentManager? = null,
    var type: MediaControllerOpenPlace,
    var selectedMediaCount: Int = 0,
    var onImageDelete: ((imageId: Long) -> Unit)? = null,
    var message: String = "",
    var watcher: ((message: String) -> Unit) = {},
    var lifecycle: Lifecycle? = null,
    var orientation: OrientationScreenListener? = null,
    var uniqueNameSuggestionMenu: SuggestionsMenuContract? = null,
    var mediaViewerViewCallback: MediaViewerViewCallback? = null,
    var mediaViewerPhotoEditorCallback: MediaViewerPhotoEditorCallback? = null,
    var onSaveVideo: ((videoUrl: String) -> Unit),
    var maxMediaCount: Int,
    var maxVideoLengthInSeconds: Int,
    var preSelectedCount: Int = 0
) {

    companion object {
        fun with(context: Context?): Builder {
            return Builder(context)
        }
    }

    private val dialog: AlertDialog
    private val viewerView: IViewerView
    private var animateOpen = true
    private val dialogStyle: Int = R.style.MediaViewerDialog_Default

    init {
        viewerView = when (type) {
            MediaControllerOpenPlace.Gallery -> MediaViewerPreview(context)
            MediaControllerOpenPlace.Common -> MediaViewerView(context)
            MediaControllerOpenPlace.Chat -> MediaViewerPreviewChat(context)
            MediaControllerOpenPlace.CreatePost -> MediaViewerPreviewCreatePost(context)
            else -> MediaViewerView(context)
        }

        if (viewerView is MediaViewerView) {
            viewerView.mediaViewerViewCallback = mediaViewerViewCallback
            viewerView.onImageDelete = onImageDelete
            viewerView.orientation = orientation
            viewerView.initLifecycleHandler(lifecycle)
            viewerView.onVideoReady = onSaveVideo
            if (type is MediaControllerOpenPlace.CreatePostVideoPreview) {
                viewerView.setSingleVideoPreview()
            }
        }
        if (viewerView is MediaViewerPreviewChat) {
            viewerView.startText = message
            viewerView.textWatcher = watcher
            viewerView.setLifeCycle(lifecycle)
            viewerView.screenListener = orientation
            viewerView.shouldOpenEditor = openEditor
            viewerView.initUniqueNameSearch(uniqueNameSuggestionMenu)
        }

        if (viewerView is MediaViewerPreviewCreatePost) {
            viewerView.setLifeCycle(lifecycle)
            viewerView.screenListener = orientation
            viewerView.shouldOpenEditor = openEditor
            viewerView.maxMediaCount = maxMediaCount
            viewerView.maxVideoLengthInSeconds = maxVideoLengthInSeconds
            viewerView.fragmentManager = fragmentManager
        }

        viewerView.selectedMediaCount = selectedMediaCount + preSelectedCount
        viewerView.mediaViewerPhotoEditorCallback = mediaViewerPhotoEditorCallback
        setupViewerView()
        viewerView.onImageReady = onImageReady
        viewerView.onImageEdit = onImageEdit
        viewerView.onImageReadyWithText = onImageReadyWithText
        viewerView.imageChangeListener = imageChangeListener
        dialog = AlertDialog
            .Builder(context, dialogStyle)
            .setView(viewerView)
            .setOnKeyListener { _, keyCode, event -> onDialogKeyEvent(keyCode, event) }
            .create()
            .apply {
                setOnShowListener { viewerView.open(transitionView, animateOpen) }
                setOnDismissListener { onDismissListener?.onDismiss() }
            }
        dialog.show()
        setColorStatusBarNavigationBlack()
        onSaveImage?.let {
            viewerView.onSaveImage = it
        }
    }

    fun show(animate: Boolean) {
        animateOpen = animate
        dialog.show()
    }

    fun close() {
        viewerView.close()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    fun updateImages(images: MutableList<ImageViewerData>) {
        viewerView.updateImages(images)
    }

    fun getCurrentPosition(): Int =
        viewerView.currentPosition

    fun setCurrentPosition(position: Int): Int {
        viewerView.currentPosition = position
        return viewerView.currentPosition
    }

    fun updateTransitionImage(imageView: ImageView?) {
        viewerView.updateTransitionImage(imageView)
    }

    private fun onDialogKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP &&
            !event.isCanceled
        ) {
            if (viewerView.isScaled) {
                viewerView.resetScale()
            } else {
                viewerView.close()
            }
            return true
        }
        return false
    }

    private fun setupViewerView() {
        viewerView.apply {
            imagesMargin = 16.dp

            setBackgroundColor(Color.BLACK)
            setImages(images, startPosition)

            onPageChange = { position -> imageChangeListener?.onImageChange(position) }
            onDismiss = { dialog.dismiss() }
        }
    }

    private fun setColorStatusBarNavigationBlack() {
        dialog.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        dialog.window?.statusBarColor = dialog.context.getColorCompat(R.color.colorTransparent)
        dialog.window?.navigationBarColor = Color.TRANSPARENT
    }


    class Builder(private val context: Context?) {

        private var selectedCount: Int = 0
        private var images: MutableList<ImageViewerData> = mutableListOf()
        private var startPosition: Int = 0
        private var openEditor: Boolean = false
        private var imageChangeListener: OnImageChangeListener? = null
        private var onDismissListener: OnDismissListener? = null
        private var transitionView: ImageView? = null
        private var onSaveImage: ((imageUrl: String) -> Unit)? = null
        private var onImageReady: ((imageUrl: String) -> Unit) = {}
        private var onImageEdit: ((imageUrl: String) -> Unit) = {}
        private var onImageDelete: ((imageId: Long) -> Unit)? = null
        private var textWatcher: ((message: String) -> Unit) = {}
        private var fragmentManager: FragmentManager? = null
        private var needToShowDelete: Long? = null
        private var message: String = ""
        private var lifecycle: Lifecycle? = null
        private var maxMediaCount: Int = 10
        private var maxVideoLengthInSeconds : Int = DEFAULT_VIDEO_MAX_DURATION
        private var orientation: OrientationScreenListener? = null
        private var uniqueNameSuggestionMenu: SuggestionsMenuContract? = null
        private var mediaViewerViewCallback: MediaViewerViewCallback? = null
        private var mediaViewerPhotoEditorCallback: MediaViewerPhotoEditorCallback? = null
        private var onSaveVideo: ((videoUrl: String) -> Unit) = {}

        private var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _ ->

        }
        private var onImageEditWithText: ((imageUrl: String) -> Unit) = {}
        private var type: MediaControllerOpenPlace = MediaControllerOpenPlace.Common
        private var preSelectedCount = 0

        fun onSaveVideo(videoListener: ((videoUrl: String) -> Unit)): Builder {
            onSaveVideo = videoListener
            return this
        }

        fun setSelectedCount(count: Int): Builder {
            this.selectedCount = count
            return this
        }

        fun addTextWatcher(watcher: ((message: String) -> Unit)): Builder {
            this.textWatcher = watcher
            return this
        }

        fun setMessage(message: String): Builder {
            this.message = message
            return this
        }

        fun setSupportFragmentManager(fragmentManager: FragmentManager?): Builder {
            this.fragmentManager = fragmentManager
            return this
        }

        fun addMediaViewerViewCallback(mediaViewerViewCallback: MediaViewerViewCallback): Builder {
            this.mediaViewerViewCallback = mediaViewerViewCallback
            return this
        }

        fun addMediaViewerPhotoEditorCallback(mediaViewerPhotoEditorCallback: MediaViewerPhotoEditorCallback): Builder {
            this.mediaViewerPhotoEditorCallback = mediaViewerPhotoEditorCallback
            return this
        }

        fun setType(type: MediaControllerOpenPlace): Builder {
            this.type = type
            return this
        }

        fun setPreSelectedCount(count: Int): Builder {
            this.preSelectedCount = count
            return this
        }

        fun setImageList(list: MutableList<ImageViewerData>): Builder {
            images.addAll(list)
            return this
        }

        fun setSingleImage(image: ImageViewerData): Builder {
            images.add(image)
            return this
        }

        fun startPosition(position: Int): Builder {
            startPosition = position
            return this
        }

        fun openEditor(openEditor: Boolean): Builder {
            this.openEditor = openEditor
            return this
        }

        fun onChangeListener(imageChangeListener: OnImageChangeListener): Builder {
            this.imageChangeListener = imageChangeListener
            return this
        }

        fun onDismissListener(onDismissListener: OnDismissListener): Builder {
            this.onDismissListener = onDismissListener
            return this
        }

        fun onSaveImage(onSaveImage: (imageUrl: String) -> Unit): Builder {
            this.onSaveImage = onSaveImage
            return this
        }

        fun onImageReady(imageReady: (imageUrl: String) -> Unit): Builder {
            this.onImageReady = imageReady
            return this
        }

        fun onImageEdit(imageEdit: (imageUrl: String) -> Unit): Builder {
            this.onImageEdit = imageEdit
            return this
        }

        fun onImageReadyWithText(onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit)): Builder {
            this.onImageReadyWithText = onImageReadyWithText
            return this
        }

        fun transitionFromView(imageView: ImageView): Builder {
            transitionView = imageView
            return this
        }

        fun onImageDelete(onDelete: ((imageId: Long) -> Unit)): Builder {
            onImageDelete = onDelete
            return this
        }

        fun hideDeleteMenuItem(): Builder {
            onImageDelete = null
            return this
        }

        fun setLifeCycle(lifecycle: Lifecycle): Builder {
            this.lifecycle = lifecycle
            return this
        }

        fun setMaxCount(maxMediaCount: Int): Builder {
            this.maxMediaCount = maxMediaCount
            return this
        }

        fun setVideoMaxLength(maxVideoLength: Int): Builder {
            this.maxVideoLengthInSeconds = maxVideoLength
            return this
        }

        fun setOrientationChangedListener(orientation: OrientationScreenListener): Builder {
            this.orientation = orientation
            return this
        }

        fun setUniqueNameSuggestionsMenu(uniqueNameSuggestionsMenu: SuggestionsMenuContract?): Builder {
            this.uniqueNameSuggestionMenu = uniqueNameSuggestionsMenu
            return this
        }

        fun show(): MediaViewer? {
            return context?.let {
                MediaViewer(
                    context = context,
                    images = images,
                    startPosition = startPosition,
                    openEditor = openEditor,
                    imageChangeListener = imageChangeListener,
                    onDismissListener = onDismissListener,
                    transitionView = transitionView,
                    onSaveImage = onSaveImage,
                    onImageReady = onImageReady,
                    onImageEdit = onImageEdit,
                    onImageReadyWithText = onImageReadyWithText,
                    type = type,
                    selectedMediaCount = selectedCount,
                    onImageDelete = onImageDelete,
                    message = message,
                    watcher = textWatcher,
                    lifecycle = lifecycle,
                    orientation = orientation,
                    uniqueNameSuggestionMenu = uniqueNameSuggestionMenu,
                    mediaViewerViewCallback = mediaViewerViewCallback,
                    mediaViewerPhotoEditorCallback = mediaViewerPhotoEditorCallback,
                    onSaveVideo = onSaveVideo,
                    maxMediaCount = maxMediaCount,
                    maxVideoLengthInSeconds = maxVideoLengthInSeconds,
                    fragmentManager = fragmentManager,
                    preSelectedCount = preSelectedCount
                )
            }
        }


    }
}

