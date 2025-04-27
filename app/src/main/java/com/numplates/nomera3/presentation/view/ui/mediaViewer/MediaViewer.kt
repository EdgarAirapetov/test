package com.numplates.nomera3.presentation.view.ui.mediaViewer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.meera.core.extensions.dp
import com.meera.core.extensions.getColorCompat
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnDismissListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnImageChangeListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.IViewerView
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.MediaViewerPreview
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.MediaViewerPreviewChat
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.MediaViewerView
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.MeeraMediaViewerPreviewChat
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view.MeeraMediaViewerView

@Deprecated("это старый MediaViewer. Новый перенесён в Core. Не использовать в текущих итерациях")
class MediaViewer(
    val context: Context,
    val images: MutableList<ImageViewerData>,
    var openEditor: Boolean = false,
    var startPosition: Int = 0,
    var imageChangeListener: OnImageChangeListener? = null,
    var onDismissListener: OnDismissListener? = null,
    var transitionView: ImageView?,
    var onSaveImage: ((imageUrl: String) -> Unit)? = null,
    var previewMode: Boolean = false,
    var onImageReady: ((imageUrl: String) -> Unit) = {},
    var onImageEdit: ((imageUrl: String) -> Unit) = {},
    var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _ -> },
    var act: Act? = null,
    var meeraAct: MeeraAct? = null,
    var type: MediaControllerOpenPlace,
    var selectedMediaCount: Int = 0,
    var onImageDelete: ((imageId: Long) -> Unit)? = null,
    var onImageCopy: ((imageUrl: String) -> Unit)? = null,
    var onImageShare: ((imageUrl: String) -> Unit)? = null,
    var onVideoShare: ((videoUrl: String) -> Unit)? = null,
    var shareMenuAvailable:Boolean = true,
    var copyMenuAvailable:Boolean = true,
    var message: String = "",
    var userName: String = "",
    var watcher: ((message: String) -> Unit) = {},
    var lifecycle: Lifecycle? = null,
    var orientation: OrientationScreenListener? = null,
    var uniqueNameSuggestionMenu: SuggestionsMenu? = null,
    var onSaveVideo: ((videoUrl: String) -> Unit),
    var onAddToFavorite: ((position: Int, isInFavorites: Boolean) -> Unit)? = null,
    var onGetFavorites: (() -> List<MediakeyboardFavoriteRecentUiModel>)? = null,
    var onGetPreselectedMedia: (() -> Set<Uri>?)? = null,
    val setOnlyOneImage: Boolean = false,
) {
    companion object {
        fun with(context: Context?): Builder {
            return Builder(context)
        }

        @JvmStatic
        fun buildForChatSingleImage(
            imagePath: String,
            fragment: Fragment
        ): Builder {
            return Builder(fragment.requireContext())
                .setImageList(mutableListOf(ImageViewerData(imagePath)))
                .setType(MediaControllerOpenPlace.Chat)
                .setLifeCycle(fragment.lifecycle)
                .setOnlyOneImage()
                .setSelectedCount(0)
                .setUniqueNameSuggestionsMenu(
                    SuggestionsMenu(
                        fragment,
                        SuggestionsMenuType.ROAD
                    )
                )
        }
    }

    private val dialog: AlertDialog
    private val viewerView: IViewerView
    private var animateOpen = true
    private val dialogStyle: Int = R.style.MediaViewerDialog_Default

    init {
        viewerView = when (type) {
            MediaControllerOpenPlace.Gallery -> MediaViewerPreview(context)

            MediaControllerOpenPlace.Common,
            MediaControllerOpenPlace.Post -> getMediaViewerView()

            MediaControllerOpenPlace.Chat -> initMediaViewerPreviewChat()

            else -> MediaViewerView(context)
        }

        if (viewerView is MediaViewerView) {
            viewerView.onImageDelete = onImageDelete
            viewerView.onImageCopy = onImageCopy
            viewerView.onImageShare = onImageShare
            viewerView.onVideoShare = onVideoShare
            viewerView.shareMenuAvailable = shareMenuAvailable
            viewerView.copyMenuAvailable = copyMenuAvailable
            viewerView.onAddToFavorite = onAddToFavorite
            viewerView.onGetFavorites = onGetFavorites
            viewerView.orientation = orientation
            viewerView.initLifecycleHandler(lifecycle)
            viewerView.onVideoReady = onSaveVideo
        }

        if (viewerView is MeeraMediaViewerView) {
            viewerView.onImageDelete = onImageDelete
            viewerView.onImageCopy = onImageCopy
            viewerView.onImageShare = onImageShare
            viewerView.onVideoShare = onVideoShare
            viewerView.shareMenuAvailable = shareMenuAvailable
            viewerView.copyMenuAvailable = copyMenuAvailable
            viewerView.onAddToFavorite = onAddToFavorite
            viewerView.onGetFavorites = onGetFavorites
            viewerView.orientation = orientation
            viewerView.initLifecycleHandler(lifecycle)
            viewerView.onVideoReady = onSaveVideo
            viewerView.isOpenPost = type == MediaControllerOpenPlace.Post
        }

        if (viewerView is MediaViewerPreviewChat) {
            viewerView.startText = message
            viewerView.textWatcher = watcher
            viewerView.setLifeCycle(lifecycle)
            viewerView.screenListener = orientation
            viewerView.shouldOpenEditor = openEditor
            viewerView.onlyOneImage = setOnlyOneImage
            viewerView.initUniqueNameSearch(uniqueNameSuggestionMenu)
            viewerView.onGetPreselectedMedia = this.onGetPreselectedMedia
        }
        if (viewerView is MeeraMediaViewerPreviewChat) {
            viewerView.startText = message
            viewerView.userName = userName
            viewerView.textWatcher = watcher
            viewerView.setLifeCycle(lifecycle)
            viewerView.screenListener = orientation
            viewerView.shouldOpenEditor = openEditor
            viewerView.onlyOneImage = setOnlyOneImage
            viewerView.initUniqueNameSearch(uniqueNameSuggestionMenu)
            viewerView.onGetPreselectedMedia = this.onGetPreselectedMedia
            viewerView.fragmentManager = (meeraAct as AppCompatActivity).supportFragmentManager
        }

        viewerView.selectedMediaCount = selectedMediaCount
        viewerView.act = act
        viewerView.meeraAct = meeraAct
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

    private fun initMediaViewerPreviewChat(): IViewerView{
        return if (IS_APP_REDESIGNED){
            MeeraMediaViewerPreviewChat(context)
        } else {
            MediaViewerPreviewChat(context)
        }
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

    private fun getMediaViewerView(): IViewerView {
       return MeeraMediaViewerView(context)
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
        private var onImageShare: ((imageUrl: String) -> Unit)? = null
        private var onVideoShare: ((videoUrl: String) -> Unit)? = null
        private var shareMenuAvailable:Boolean = true
        private var copyMenuAvailable:Boolean = true
        private var onImageCopy: ((imageUrl: String) -> Unit)? = null
        private var onAddToFavorite: ((position: Int, isInFavorites: Boolean) -> Unit)? = null
        private var onGetFavorites: (() -> List<MediakeyboardFavoriteRecentUiModel>)? = null
        private var textWatcher: ((message: String) -> Unit) = {}
        private var act: Act? = null
        private var meeraAct: MeeraAct? = null
        private var message: String = ""
        private var userName: String = ""
        private var lifecycle: Lifecycle? = null
        private var setOnlyOneImage = false
        private var orientation: OrientationScreenListener? = null
        private var uniqueNameSuggestionMenu: SuggestionsMenu? = null
        private var onSaveVideo: ((videoUrl: String) -> Unit) = {}
        private var onGetPreselectedMedia: () -> Set<Uri>? = { null }

        private var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _ ->

        }
        private var type: MediaControllerOpenPlace = MediaControllerOpenPlace.Common

        fun onGetPreselectedMedia(onGetPreselectedMedia: () -> Set<Uri>?): Builder {
            this.onGetPreselectedMedia = onGetPreselectedMedia
            return this
        }

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

        fun setAct(act: Act?): Builder {
            this.act = act
            return this
        }

        fun setMeeraAct(meeraAct: MeeraAct?): Builder {
            this.meeraAct = meeraAct
            return this
        }

        fun setType(type: MediaControllerOpenPlace): Builder {
            this.type = type
            return this
        }

        fun setImageList(list: MutableList<ImageViewerData>): Builder {
            images.addAll(list)
            return this
        }

        fun setUserName(userName: String): Builder {
            this.userName = userName
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

        fun onImageShare(onShare: ((imageUrl: String) -> Unit)): Builder {
            onImageShare = onShare
            return this
        }

        fun onVideoShare(onShare: ((imageUrl: String) -> Unit)): Builder {
            onVideoShare = onShare
            return this
        }

        fun shareAvailable(shareMenuAvailable: Boolean): Builder {
            this.shareMenuAvailable = shareMenuAvailable
            return this
        }

        fun copyAvailable(copyMenuAvailable: Boolean): Builder {
            this.copyMenuAvailable = copyMenuAvailable
            return this
        }

        fun onImageCopy(onCopy: ((imageUrl: String) -> Unit)): Builder {
            onImageCopy = onCopy
            return this
        }

        fun onVideoReady() {
            this.onVideoReady()
        }

        fun onAddToFavorite(onAddToFavorite: (position: Int, isInFavorites: Boolean) -> Unit): Builder {
            this.onAddToFavorite = onAddToFavorite
            return this
        }

        fun onGetFavorites(onGetFavorites: (() -> List<MediakeyboardFavoriteRecentUiModel>)): Builder {
            this.onGetFavorites = onGetFavorites
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

        fun setOnlyOneImage(): Builder {
            this.setOnlyOneImage = true
            return this
        }

        fun setOrientationChangedListener(orientation: OrientationScreenListener): Builder {
            this.orientation = orientation
            return this
        }

        fun setUniqueNameSuggestionsMenu(uniqueNameSuggestionsMenu: SuggestionsMenu?): Builder {
            this.uniqueNameSuggestionMenu = uniqueNameSuggestionsMenu
            return this
        }

        fun show(): MediaViewer? {
            return context?.let {
                MediaViewer(
                    context = context,
                    openEditor = openEditor,
                    images = images,
                    startPosition = startPosition,
                    imageChangeListener = imageChangeListener,
                    onDismissListener = onDismissListener,
                    transitionView = transitionView,
                    onSaveImage = onSaveImage,
                    onImageReady = onImageReady,
                    onImageEdit = onImageEdit,
                    onImageReadyWithText = onImageReadyWithText,
                    act = act,
                    meeraAct = meeraAct,
                    type = type,
                    selectedMediaCount = selectedCount,
                    onImageDelete = onImageDelete,
                    onImageCopy = onImageCopy,
                    onImageShare = onImageShare,
                    onVideoShare = onVideoShare,
                    shareMenuAvailable = shareMenuAvailable,
                    copyMenuAvailable = copyMenuAvailable,
                    message = message,
                    userName = userName,
                    watcher = textWatcher,
                    lifecycle = lifecycle,
                    orientation = orientation,
                    uniqueNameSuggestionMenu = uniqueNameSuggestionMenu,
                    onSaveVideo = onSaveVideo,
                    onAddToFavorite = onAddToFavorite,
                    onGetFavorites = onGetFavorites,
                    onGetPreselectedMedia = onGetPreselectedMedia,
                    setOnlyOneImage = setOnlyOneImage,
                )
            }
        }
    }
}
