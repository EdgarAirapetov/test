package com.meera.core.utils.mediaviewer.viewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.meera.core.R
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.clearText
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.dp
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.mediaviewer.ImageViewerData
import com.meera.core.utils.mediaviewer.MediaViewer
import com.meera.core.utils.mediaviewer.MediaViewerPhotoEditorCallback
import com.meera.core.utils.mediaviewer.common.extensions.addOnPageChangeListener
import com.meera.core.utils.mediaviewer.common.extensions.animateAlpha
import com.meera.core.utils.mediaviewer.common.extensions.isRectVisible
import com.meera.core.utils.mediaviewer.common.extensions.isVisible
import com.meera.core.utils.mediaviewer.common.gestures.SimpleOnGestureListener
import com.meera.core.utils.mediaviewer.common.gestures.SwipeDirection
import com.meera.core.utils.mediaviewer.common.gestures.SwipeDirectionDetector
import com.meera.core.utils.mediaviewer.common.gestures.SwipeToDismissHandler
import com.meera.core.utils.mediaviewer.common.pager.MultiTouchViewPager
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO_NOT_PLAYING
import com.meera.core.utils.mediaviewer.viewer.adapter.ImagesPagerAdapter
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.tooltip.createTooltip
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.tooltip.TooltipMessage
import com.meera.uikit.widgets.tooltip.UiKitTooltipBubbleMode
import com.meera.uikit.widgets.tooltip.UiKitTooltipViewState

private const val DEFAULT_MAX_SELECTED_MEDIA_COUNT = 10
private const val DEFAULT_MAX_VIDEO_LENGTH = 90
private const val TRANSITION_OPEN_ANIMATION_DURATION = 300L

class MediaViewerPreviewCreatePost @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IViewerView(context, attrs, defStyleAttr) {

    var fragmentManager: FragmentManager? = null
    var shouldOpenEditor: Boolean = false
    var maxMediaCount: Int = DEFAULT_MAX_SELECTED_MEDIA_COUNT
    var maxVideoLengthInSeconds: Int = DEFAULT_MAX_VIDEO_LENGTH
    private var editIsEnabled = true

    private var filesUtil = FileUtilsImpl(context)
    private var isZoomingAllowed = true
    private var isSwipeToDismissAllowed = false
    var screenListener: OrientationScreenListener? = null
    override var currentPosition: Int
        get() = imagesPager.currentItem
        set(value) {
            imagesPager.currentItem = value
        }

    override var onDismiss: (() -> Unit)? = null
    override var onPageChange: ((position: Int) -> Unit)? = null

    override val isScaled
        get() = imagesAdapter?.isScaled(currentPosition) ?: false

    override var imagesMargin
        get() = imagesPager.pageMargin
        set(value) {
            imagesPager.pageMargin = value
        }

    override var onSaveImage: ((imageUrl: String) -> Unit)? = null
    override var onImageReady: ((imageUrl: String) -> Unit) = {}
    override var onImageReadyWithText: ((imageUrl: List<Uri>, text: String) -> Unit) = { _, _ ->

    }
    override var onImageEdit: ((imageUrl: String) -> Unit) = {}

    private var isOpenCompleted = false
    private var isImageReady = false
    private var isGoFurther = false

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var transitionView: ImageView
    private var externalTransitionImageView: ImageView? = null
    private var overlayView: ConstraintLayout

    private var viewOverlayStatusBar: View? = null
    private var ivEditPreviewMedia: UiKitButton? = null
    private var tvOverlayToolbar: TextView? = null
    private var ivOverlayMenu: ImageView? = null

    private var imagesPager: MultiTouchViewPager
    private var imagesAdapter: ImagesPagerAdapter? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private var swipeDismissHandler: SwipeToDismissHandler? = null

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null
    private var btnContinue: UiKitButton? = null
    private var tvSelectedMediaCount: TextView? = null
    private var ivBtnClose: ImageView? = null
    private var tvSelectedPhoto: TextView? = null
    private var images: MutableList<ImageViewerData> = mutableListOf()

    private var transitionImageAnimator: TransitionImageAnimator? = null
    private var lifecycle: Lifecycle? = null
    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    override var selectedMediaCount: Int = 0
        set(value) {
            field = value
            initSelectedMediaBtn(value)
            btnContinue?.visible()
        }

    private fun initSelectedMediaBtn(value: Int) {
        val zeroValue = value == 0
        btnContinue?.isEnabled = !zeroValue
        tvSelectedMediaCount?.setVisible(!zeroValue)
        tvSelectedMediaCount?.text = value.toString()
    }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
            || !externalTransitionImageView.isRectVisible
            || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    init {
        View.inflate(context, R.layout.image_viewer_preview_create_post, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        overlayView = findViewById(R.id.cl_overlay_container)
        btnContinue = findViewById(R.id.btn_continue)
        tvSelectedMediaCount = findViewById(R.id.tv_selected_media_count)
        ivBtnClose = findViewById(R.id.iv_btn_close)
        ivEditPreviewMedia = findViewById(R.id.iv_edit_preview_media)
        tvSelectedPhoto = findViewById(R.id.tv_selected_media)
        viewOverlayStatusBar = findViewById(R.id.view_overlay_statusbar)
        tvOverlayToolbar = findViewById(R.id.tv_overlay_toolbar)
        ivOverlayMenu = findViewById(R.id.iv_overlay_menu)

        viewOverlayStatusBar?.layoutParams?.height = context.getStatusBarHeight()

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = {
                showCountNumber()
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) invisible() else visible()
                }
                onPageChange?.invoke(it)
                updateOverlayViews(it)
                checkButtonsAvailable()
            })

        btnContinue?.setThrottledClickListener {
            isGoFurther = true
            close()
        }

        ivBtnClose?.setThrottledClickListener {
            isGoFurther = true
            close()
        }

        tvSelectedPhoto?.setThrottledClickListener {
            isGoFurther = true
            handleClickCheckAction()
        }

        ivEditPreviewMedia?.setThrottledClickListener {
            if(editIsEnabled) {
                openEditor()
            } else {
                createAndShowTooltip(
                    messageString = context.getString(R.string.may_only_edit_n_media, maxMediaCount),
                    showView = ivEditPreviewMedia,
                    bubbleGravity = UiKitTooltipBubbleMode.LEFT_BOTTOM,
                    showAbove = true
                )

            }
        }

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
    }

    private fun checkButtonsAvailable() {
        imagesAdapter?.getMediaByPosition(currentPosition)?.let {
            val buttonTint = if (!it.isSelected && selectedMediaCount == maxMediaCount) {
                editIsEnabled = false
                R.color.uiKitColorBackgroundFadeWhite20
            } else {
                editIsEnabled = true
                R.color.uiKitColorForegroundInvers
            }
            ivEditPreviewMedia?.updateContentColor(
                context.getColorStateList(buttonTint)
            )
        }
    }

    private fun openEditor() {
        isGoFurther = true
        imagesAdapter?.getMediaByPosition(currentPosition)?.let { media ->
            mediaViewerPhotoEditorCallback?.onOpenPhotoEditor(
                imageUrl = Uri.parse(media.getActualStringUri()),
                type = MediaControllerOpenPlace.CreatePost,
                supportGifEditing = true,
                resultCallback = object : MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback {
                    override fun onPhotoReady(resultUri: Uri) {
                        val res = images.find { it.getInitialStringUri() == media.getInitialStringUri() } ?: return
                        val index = images.indexOf(res)
                        res.mediaUriModel.editedUri = resultUri
                        if (!res.isSelected) {
                            selectedMediaCount++
                            res.cnt = selectedMediaCount
                            res.isSelected = true
                            imageChangeListener?.onImageChecked(res, true)
                        } else {
                            imageChangeListener?.onImageEdited(res)
                        }
                        setImages(images.toMutableList(), index)
                    }

                    override fun onVideoReady(resultUri: Uri) {
                        val res = images.find { it.getInitialStringUri() == media.getInitialStringUri() } ?: return
                        val index = images.indexOf(res)
                        res.mediaUriModel.editedUri = resultUri
                        if (!res.isSelected) {
                            selectedMediaCount++
                            res.cnt = selectedMediaCount
                            res.isSelected = true
                            imageChangeListener?.onImageChecked(res, true)
                        } else {
                            imageChangeListener?.onImageEdited(res)
                        }
                        setImages(images.toMutableList(), index)
                        resultUri.path?.let { str -> mediaViewerPhotoEditorCallback?.onAddHashSetVideoToDelete(str) }
                    }

                    override fun onError() {
                        close()
                    }
                }
            )
        }
    }

    private fun handleClickCheckAction() {
        val currentData = imagesAdapter?.getCurrentData(currentPosition) ?: return

        if (!currentData.isSelected) {
            if (selectedMediaCount == maxMediaCount) {
                createAndShowTooltip(
                    messageString = context.getString(R.string.may_only_pick_n_media, maxMediaCount),
                    showView = tvSelectedPhoto,
                    bubbleGravity = UiKitTooltipBubbleMode.RIGHT_TOP,
                    showAbove = false
                )
                return
            }

            if (currentData.viewType == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                val strUriToCheck = currentData.getActualStringUri()
                doAsync({
                    strUriToCheck.let { str ->
                        val uriToCheck = Uri.parse(str)
                        val time = filesUtil.getVideoDurationMils(uriToCheck)
                        if (time > maxVideoLengthInSeconds * 1000) {
                            return@doAsync uriToCheck
                        } else return@doAsync null
                    }
                }, {
                    it?.let {
                        openEditor()
                    } ?: kotlin.run {
                        currentData.isSelected = true
                        selectedMediaCount++
                        currentData.cnt = selectedMediaCount
                        showCountNumber()
                        imageChangeListener?.onImageChecked(currentData, true)
                    }
                })
            } else {
                currentData.isSelected = true
                selectedMediaCount++
                currentData.cnt = selectedMediaCount
                showCountNumber()
                imageChangeListener?.onImageChecked(currentData, true)
            }
        } else {
            if(currentData.mediaUriModel.isEdited()) {
                showConfirmRemoveDialog(currentData)
            } else {
                removeMedia(currentData)
            }

        }
    }

    private fun showConfirmRemoveDialog(currentData: ImageViewerData) {
        fragmentManager?.let { fm ->

            MeeraConfirmDialogBuilder()
                .setHeader(context.getString(R.string.post_reset_media_dialog_title))
                .setDescription(context.getString(R.string.post_reset_media_dialog_description))
                .setTopBtnText(context.getString(R.string.post_reset_media_dialog_action))
                .setTopBtnType(ButtonType.FILLED_ERROR)
                .setTopClickListener {
                    removeMedia(currentData)
                }
                .setBottomBtnText(context.getString(R.string.cancel))
                .setCancelable(false)
                .show(fm)
        }
    }

    private fun createAndShowTooltip(
        messageString: String,
        showView: View?,
        bubbleGravity: UiKitTooltipBubbleMode,
        showAbove: Boolean
    ) {
        showView ?: return
        val fragment: Fragment = fragmentManager?.fragments?.firstOrNull() ?: return
        val tooltip = fragment.createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = bubbleGravity,
                tooltipMessage = TooltipMessage.TooltipMessageString(messageString),
                showCloseButton = false
            )
        )
        post {
            showView.let {
                if (showAbove) {
                    tooltip.showAboveView(
                        view = it,
                        duration = 4000L,
                        offsetY = (-4).dp
                    )
                } else {
                    tooltip.showUniversal(
                        view = it,
                        duration = 4000L,
                        offsetX = (5).dp,
                        offsetY = (4).dp
                    )
                }
            }
        }
    }

    private fun removeMedia(mediaForRemove: ImageViewerData){
        unselectMedia(mediaForRemove)
        showCountNumber()
        imageChangeListener?.onImageChecked(mediaForRemove, false)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val overVi = overlayView.dispatchTouchEvent(event)
        if (!overVi) {
            return true
        }

        if (transitionImageAnimator?.isAnimating == true) {
            return true
        }

        //one more tiny kludge to prevent single tap a one-finger zoom which is broken by the SDK
        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == 1
        ) {
            return true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > 1 || wasScaled)) {
            wasScaled = true
            if (!overVi)
                return imagesPager.dispatchTouchEvent(event)
        }

        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(
            event
        )
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    override fun setImages(images: MutableList<ImageViewerData>, startPosition: Int) {
        this.images.clear()
        this.images.addAll(images)
        this.startPosition = startPosition
        imagesAdapter = ImagesPagerAdapter()
        imagesAdapter?.isZoomable = isZoomingAllowed
        imagesAdapter?.setData(images)
        imagesPager.adapter = imagesAdapter
        imagesAdapter?.initOnPageChangeListener(imagesPager)
        imagesPager.setCurrentItem(startPosition, false)
        imagesAdapter?.startPosition = startPosition
        imagesAdapter?.resourceReady = {
            isImageReady = true
            if (isOpenCompleted) {
                prepareViewsForViewer()
            }
        }
        updateOverlayViews(startPosition)
        currentPosition = startPosition
        showCountNumber()
        imagesAdapter?.onVideoClicked = { media ->
            isGoFurther = true
            openVideo(media)
        }
    }

    private fun unselectMedia(currentData: ImageViewerData?) {
        val selectedImages = images.filter { it.isSelected }.sortedBy { it.cnt }
        val currentDataCnt = currentData?.cnt ?: return
        for (item in selectedImages) {
            if (item.cnt > currentDataCnt) {
                item.cnt--
            }
        }
        currentData.isSelected = false
        currentData.cnt = 0
        clearEditedIfNeed(currentData)
        selectedMediaCount--
    }

    private fun clearEditedIfNeed(data: ImageViewerData) {
        if (data.mediaUriModel.editedUri != null) {
            data.mediaUriModel.clearEditedUri()
            imagesAdapter?.notifyDataSetChanged()
        }
    }

    private fun showCountNumber() {
        val count = images[currentPosition].cnt
        if (count == 0) {
            tvSelectedPhoto?.setBackgroundResource(R.drawable.white_ring_bg)
            tvSelectedPhoto?.clearText()
        } else {
            tvSelectedPhoto?.setBackgroundResource(R.drawable.circle_tab_white_stroke_bg)
            tvSelectedPhoto?.text = count.toString()
        }
    }

    private fun openVideo(video: ImageViewerData) {
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(ImageViewerData(video.mediaUriModel, viewType = VIEW_TYPE_VIDEO))
        lifecycle?.let { life ->
            screenListener?.let { orientation ->
                if (imageList.isNotEmpty()) {
                    //Далее собираем билдер
                    MediaViewer.with(context)
                        .setImageList(imageList)
                        .startPosition(0)
                        .setType(MediaControllerOpenPlace.CreatePostVideoPreview)
                        .setOrientationChangedListener(orientation)
                        .setLifeCycle(life) // need when video shown
                        .show()
                }
            }
        }
    }

    override fun open(transitionView: ImageView?, animate: Boolean) {
        prepareViewsForTransition()
        externalTransitionImageView = transitionView
        transitionView?.drawable?.let {
            when (it) {
                is BitmapDrawable -> {
                    this.transitionView.setImageBitmap(it.bitmap)
                }

                is TransitionDrawable -> {
                    this.transitionView.setImageDrawable(it)
                }

                is GifDrawable -> {
                    this.transitionView.setImageDrawable(it)
                    it.start()
                }

                else -> {
                    Glide.with(this.transitionView)
                        .load(images[currentPosition].getInitialStringUri())
                        .fitCenter()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(this.transitionView)
                }
            }
        }
        transitionImageAnimator = createTransitionImageAnimator(transitionView)
        swipeDismissHandler = createSwipeToDismissHandler()
        rootContainer.setOnTouchListener(swipeDismissHandler)
        if (animate) {
            if (transitionView != null)
                animateOpen()
            else
                animateOpenWithoutTransition()
        } else {
            prepareViewsForViewer()
        }
        if (shouldOpenEditor) {
            openEditor()
        }
    }

    override fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler?.initiateDismissToBottom()
        } else {
            animateClose()
        }
    }

    override fun updateImages(images: MutableList<ImageViewerData>) {
        this.images.clear()
        this.images.addAll(images)
        imagesAdapter?.setData(images)
    }

    override fun updateTransitionImage(imageView: ImageView?) {
        externalTransitionImageView?.visible()
        imageView?.invisible()

        externalTransitionImageView = imageView
        startPosition = currentPosition
        transitionImageAnimator = createTransitionImageAnimator(imageView)
        Glide.with(transitionView)
            .load(images[startPosition].getInitialStringUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(transitionView)
    }

    @SuppressLint("SetTextI18n")
    fun updateOverlayViews(position: Int) {
        if (images.size > 1) {
            tvOverlayToolbar?.text = "${position + 1}/${images.size}"
            tvOverlayToolbar?.visible()
        } else {
            tvOverlayToolbar?.gone()
        }
    }

    override fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator?.animateOpen(
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                overlayView.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = {
                isOpenCompleted = true
                imagesPager.visible()
                if (isImageReady) prepareViewsForViewer()
            })
    }

    private fun animateOpenWithoutTransition() {
        backgroundView.animateAlpha(0f, 1f, TRANSITION_OPEN_ANIMATION_DURATION)
        overlayView.animateAlpha(0f, 1f, TRANSITION_OPEN_ANIMATION_DURATION)
        dismissContainer.translationY = context.displayHeight.toFloat()
        dismissContainer.animate().translationY(0f).setDuration(TRANSITION_OPEN_ANIMATION_DURATION)
            .setInterpolator(DecelerateInterpolator()).start()
        isOpenCompleted = true
        imagesPager.visible()
        if (isImageReady) prepareViewsForViewer()
    }

    private fun animateClose() {
        keyboardHeightProvider?.release()

        prepareViewsForTransition()
        dismissContainer.setMargins(0, 0, 0, 0)

        transitionImageAnimator?.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView.animateAlpha(overlayView.alpha, 0f, duration)
            },
            onTransitionEnd = { onDismiss?.invoke() })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.alpha = 1f
        transitionImageContainer.visible()
        imagesPager.invisible()
    }

    private fun prepareViewsForViewer() {
        transitionImageContainer.invisibleAnimation()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent): Boolean {

        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            SwipeDirection.UP, SwipeDirection.DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler?.onTouch(rootContainer, event) ?: true
                } else true
            }

            SwipeDirection.LEFT, SwipeDirection.RIGHT -> {
                imagesPager.dispatchTouchEvent(event)
            }

            else -> true
        }
    }

    private fun handleUpDownEvent(event: MotionEvent) {

        if (event.action == MotionEvent.ACTION_UP) {
            handleEventActionUp(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            handleEventActionDown(event)
        }

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
    }

    private fun handleEventActionDown(event: MotionEvent) {
        swipeDirection = null
        wasScaled = false
        imagesPager.dispatchTouchEvent(event)

        swipeDismissHandler?.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler?.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView.let { it.isVisible && it.dispatchTouchEvent(event) }

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onDoubleTap = {
                wasDoubleTapped = !isScaled
                false
            }
        ))

    private fun createScaleGestureDetector() =
        ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())

    private fun createSwipeToDismissHandler()
        : SwipeToDismissHandler = SwipeToDismissHandler(
        swipeView = dismissContainer,
        shouldAnimateDismiss = { shouldDismissToBottom },
        onDismiss = {
            animateClose()
        },
        onSwipeViewMove = ::handleSwipeViewMove
    )

    private fun createTransitionImageAnimator(transitionView: ImageView?) =
        TransitionImageAnimator(
            externalImage = transitionView,
            internalImage = this.transitionView,
            internalImageContainer = this.transitionImageContainer
        )

    fun setLifeCycle(lifecycle: Lifecycle?) {
        this.lifecycle = lifecycle
    }
}

