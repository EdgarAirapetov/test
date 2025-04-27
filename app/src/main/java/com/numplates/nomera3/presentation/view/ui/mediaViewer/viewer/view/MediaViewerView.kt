package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.graphics.NGraphics.getNavigationBarSize
import com.meera.core.utils.listeners.OrientationScreenListener
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.isGifUrl
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isRectVisible
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SimpleOnGestureListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeDirection
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeDirectionDetector
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeToDismissHandler
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.MultiTouchViewPager
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_IMAGE
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO
import com.numplates.nomera3.presentation.view.ui.mediaViewer.listeners.OnHideToolbar
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.adapter.ImagesPagerAdapter
import com.numplates.nomera3.presentation.view.ui.menuPopup.MenuPopup
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber
import java.util.Locale

private const val NO_POSITION = -1
private const val DELAY_FOR_PAUSE_SIDE_POSITION_VIDEOS = 50L
private const val START_VIDEO_DELAY = 50L

internal class MediaViewerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IViewerView(context, attrs, defStyleAttr) {

    var orientation: OrientationScreenListener? = null
    private var lifecycle: Lifecycle? = null
    var onImageDelete: ((imageId: Long) -> Unit)? = null
    var onImageCopy: ((imageUrl: String) -> Unit)? = null
    var onImageShare: ((imageUrl: String) -> Unit)? = null
    var onVideoShare: ((videoUrl: String) -> Unit)? = null
    var shareMenuAvailable:Boolean = true
    var copyMenuAvailable:Boolean = true
    var onAddToFavorite: ((position: Int, isInFavorites: Boolean) -> Unit)? = null
    var onGetFavorites: (() -> List<MediakeyboardFavoriteRecentUiModel>)? = null
    internal var isZoomingAllowed = true
    internal var isSwipeToDismissAllowed = true

    override var currentPosition: Int
        get() = imagesPager.currentItem
        set(value) {
            imagesPager.currentItem = value
        }

    override var onDismiss: (() -> Unit)? = null
    override var onPageChange: ((position: Int) -> Unit)? = null
    override var onImageReady: (imageUrl: String) -> Unit = {}
    override var onImageEdit: (imageUrl: String) -> Unit = {}

    override val isScaled
        get() = imagesAdapter?.isScaled(currentPosition) ?: false

    override var imagesMargin
        get() = imagesPager.pageMargin
        set(value) {
            imagesPager.pageMargin = value
        }

    override var onSaveImage: ((imageUrl: String) -> Unit)? = null
    private var isOpenCompleted = false
    private var isImageReady = false

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var transitionView: ImageView
    private var externalTransitionImageView: ImageView? = null
    private var overlayView: ConstraintLayout

    private var viewOverlayStatusBar: View? = null
    private var svDescription: ScrollView? = null
    private var ibCancelPreview: ImageButton? = null
    private var ivOverlayMenu: ImageView? = null
    private var tvOverlayDescription: TextView? = null
    private var tvOverlayToolbar: TextView? = null

    private var imagesPager: MultiTouchViewPager
    private var imagesAdapter: ImagesPagerAdapter? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null
    private var toolbar: ConstraintLayout? = null
    private var isVideoView = false

    private var images: MutableList<ImageViewerData> = mutableListOf()
    private lateinit var transitionImageAnimator: TransitionImageAnimator
    private var lastResetPosition = -1

    private var startPosition: Int = NO_POSITION
        set(value) {
            field = value
            currentPosition = value
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
            || !externalTransitionImageView.isRectVisible
            || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    init {
        View.inflate(context, R.layout.view_image_viewer, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        overlayView = findViewById(R.id.cl_overlay_container)
        toolbar = findViewById(R.id.cl_toolbar)
        viewOverlayStatusBar = findViewById(R.id.view_overlay_statusbar)
        svDescription = findViewById(R.id.sv_description)
        ibCancelPreview = findViewById(R.id.ib_cancel_preview)
        ivOverlayMenu = findViewById(R.id.iv_overlay_menu)
        tvOverlayDescription = findViewById(R.id.tv_overlay_description)
        tvOverlayToolbar = findViewById(R.id.tv_overlay_toolbar)
        viewOverlayStatusBar?.layoutParams?.height = context.getStatusBarHeight()
        svDescription?.setMargins(bottom = context.getNavigationBarHeight())

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = { position ->
                    currentPosition = position
                    externalTransitionImageView?.apply {
                        if (isAtStartPosition) invisible() else visible()
                    }
                    onPageChange?.invoke(position)
                    updateOverlayViews(position)
            },
            onPageScrollStateChanged = { scrollState ->
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (currentPosition == lastResetPosition) {
                        resumeVideoPlayback(currentPosition)
                    } else {
                        resetVideoPlayback(currentPosition)
                    }
                } else {
                    pauseAllVideoPlayback()
                }
            }
        )

        ibCancelPreview?.setOnClickListener {
            close()
        }

        pauseSidePages()

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
        setDotsMenu()
    }

    private fun configureToolbar() { //Временная заглушка для видео
        if (isVideoView) {
            dismissContainer.setMargins(top = 0)
            toolbar?.setBackgroundColor(ContextCompat.getColor(context, R.color.black_overlay))
//            ivOverlayMenu?.gone()
        }
    }


    private val toolbarAnimator = object : OnHideToolbar {
        override fun showToolbar() {
            toolbar?.measure(ViewGroup.LayoutParams.MATCH_PARENT, 50.dp)
            toolbar?.animateHeight(50.dp, 200) {
                tvOverlayToolbar?.requestLayout()
                toolbar?.visible()
            }
        }

        override fun hideToolbar() {
            toolbar?.animateHeight(0, 200) {
                toolbar?.gone()
            }
        }

    }

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun ownerStartListener() {
            Timber.d("OnLifecycleEvent Lifecycle.Event.ON_START")
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun ownerStopListener() {
            imagesAdapter?.pauseVideo()
        }
    }


    fun initLifecycleHandler(lifecycle: Lifecycle? = null) { // handle lifecycle to start or stop video
        this.lifecycle = lifecycle
        this.lifecycle?.addObserver(lifecycleObserver)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (overlayView.isVisible && overlayView.dispatchTouchEvent(event)) {
            return true
        }

        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
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
            return imagesPager.dispatchTouchEvent(event)
        }
        val res = super.dispatchTouchEvent(event)
        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event, res)
//        return super.dispatchTouchEvent(event)
    }

    private fun getNavBarHeigh(): Int {
        val heightWithoutNavbar = getNavigationBarSize(context)?.y ?: 0
        return if (heightWithoutNavbar == 0)
            0
        else context.getNavigationBarHeight()
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    override fun setImages(images: MutableList<ImageViewerData>, startPosition: Int) {
        this.images.clear()
        this.images.addAll(images)
        this.startPosition = startPosition
        imagesAdapter = ImagesPagerAdapter()
        imagesAdapter?.disableStartVideoOnCreate()
        imagesAdapter?.isSilentMode = false//getSilentState()
        imagesAdapter?.toolbarAnimator = toolbarAnimator
        imagesAdapter!!.setData(images)
        imagesPager.adapter = imagesAdapter
        imagesAdapter?.initOnPageChangeListener(imagesPager)
        imagesPager.setCurrentItem(startPosition, false)
        imagesAdapter!!.startPosition = startPosition
        imagesAdapter!!.resourceReady = {
            isImageReady = true
            if (isOpenCompleted) {
                prepareViewsForViewer()
            }
        }
        updateOverlayViews(startPosition)
        orientation?.orientationChangedListener = {
            Timber.d("NavbarHeigh = ${getNavBarHeigh()}")
            if (it == Configuration.ORIENTATION_PORTRAIT) {
                val params = (dismissContainer.layoutParams as? FrameLayout.LayoutParams)
                params?.marginEnd = 0
                val toolbarParams = (toolbar?.layoutParams as? ConstraintLayout.LayoutParams)
                toolbarParams?.marginEnd = 0
                params?.bottomMargin = getNavBarHeigh()
                dismissContainer.layoutParams = params
                toolbar?.layoutParams = toolbarParams
            } else if (it == Configuration.ORIENTATION_LANDSCAPE) {
                val params = (dismissContainer.layoutParams as? FrameLayout.LayoutParams)
                val toolbarParams = (toolbar?.layoutParams as? ConstraintLayout.LayoutParams)
                params?.marginEnd = getNavBarHeigh()
                params?.bottomMargin = 0
                toolbarParams?.marginEnd = getNavBarHeigh()
                dismissContainer.layoutParams = params
                toolbar?.layoutParams = toolbarParams
            }
        }

        (dismissContainer.layoutParams as? FrameLayout.LayoutParams)?.bottomMargin = getNavBarHeigh()

        images.forEach { img ->
            if (img.viewType == RecyclingPagerAdapter.VIEW_TYPE_VIDEO) {
                isVideoView = true
                return@forEach
            }
        }

        postDelayed({
            resetVideoPlayback(currentPosition)
        }, START_VIDEO_DELAY)

        configureToolbar()

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
                        .load(images[currentPosition].imageUrl)
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
    }

    override fun close() {
        if (shouldDismissToBottom) {
            swipeDismissHandler.initiateDismissToBottom()
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
            .load(images[startPosition].imageUrl)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(transitionView)
    }

    @SuppressLint("SetTextI18n")
    fun updateOverlayViews(position: Int) {
        if (images.isNotEmpty() && position >= 0) {
            val model = images[position]
            if (model.description.isNullOrEmpty()) {
                tvOverlayDescription?.gone()
            } else {
                tvOverlayDescription?.text = model.description
                tvOverlayDescription?.visible()
            }

            if (imagesAdapter?.count ?: 0 > 1) {
                tvOverlayToolbar?.text =
                    "${position + 1}/${imagesAdapter?.count ?: images.size}"
                tvOverlayToolbar?.visible()
            } else {
                tvOverlayToolbar?.gone()
            }
        } else {
            tvOverlayDescription?.gone()
            tvOverlayToolbar?.gone()
        }
    }

    private var menu: MenuPopup? = null

    private fun setDotsMenu() {
        ivOverlayMenu?.setOnClickListener {
            val menu = MeeraMenuBottomSheet(context)
            checkForAddToFavoriteItem(menu)
            val currentItem = images[currentPosition]

            if(currentItem.viewType == VIEW_TYPE_IMAGE && currentItem.imageUrl?.lowercase(Locale.ROOT)?.isGifUrl() == false) {
                if (copyMenuAvailable) {
                    menu.addItem(R.string.text_copy_txt, R.drawable.ic_chat_copy_message) {
                        images[currentPosition].imageUrl?.let { it1 ->
                            onImageCopy?.invoke(it1)
                        }
                    }
                }
            }
            menu.addItem(R.string.save_image, R.drawable.image_download_menu_item) {
                images[currentPosition].let { media ->
                    val mediaUrl = media.imageUrl?:return@let
                    if (media.viewType == VIEW_TYPE_VIDEO) {
                        onVideoReady(mediaUrl)
                        showSaveImageInfoToast(context.getString(R.string.video_saved))
                    } else {
                        onSaveImage?.invoke(mediaUrl)
                        showSaveImageInfoToast(context.getString(R.string.image_saved))
                    }
                }
            }

            if (shareMenuAvailable) {
                menu.addItem(R.string.image_share, R.drawable.ic_repost_more) {
                    images[currentPosition].let { media ->
                        val mediaUrl = media.imageUrl?:return@let
                        if (media.viewType == VIEW_TYPE_VIDEO) {
                            onVideoShare?.invoke(mediaUrl)
                        } else {
                            onImageShare?.invoke(mediaUrl)
                        }
                    }
                }
            }

            if (onImageDelete != null) {
                menu.addItem(R.string.delete_photo_txt, R.drawable.delete_red_menu_item) {
                    imagesAdapter?.dataList?.get(currentPosition)?.photoID?.let { it1 ->
                        onImageDelete?.invoke(it1)
                        imagesAdapter?.removePhoto(currentPosition)
                        if (imagesAdapter?.count == 0) close()
                        else updateOverlayViews(currentPosition)
                    }
                }
            }

            val fm = (context as? AppCompatActivity)?.supportFragmentManager
            menu.show(fm)
        }
    }

    private fun showSaveImageInfoToast(messageString: String) {
        NSnackbar.with(this)
            .inView(rootView)
            .marginBottom(0)
            .typeSuccess()
            .text(messageString)
            .durationLong()
            .show()
    }

    override fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
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

    private fun checkForAddToFavoriteItem(menu: MeeraMenuBottomSheet) {
        val favorites = onGetFavorites?.invoke() ?: emptyList()
        val currentItem = images[currentPosition]
        val isCurrentItemInFavorites = favorites.any { it.url == currentItem.imageUrl }
        val shouldShowAddToFavoritesItem = onAddToFavorite != null && onGetFavorites != null
        when {
            shouldShowAddToFavoritesItem && isCurrentItemInFavorites -> {
                menu.addItem(R.string.remove_from_favorites, R.drawable.ic_delete_media_from_favorites) {
                    onAddToFavorite?.invoke(currentPosition, true)
                }
            }

            shouldShowAddToFavoritesItem && !isCurrentItemInFavorites -> {
                menu.addItem(R.string.add_to_favorites, R.drawable.ic_add_to_favorite) {
                    onAddToFavorite?.invoke(currentPosition, false)
                }
            }
        }
    }

    private fun animateOpenWithoutTransition() {
        backgroundView.animateAlpha(0f, 1f, 300)
        overlayView.animateAlpha(0f, 1f, 300)
        dismissContainer.translationY = context.displayHeight.toFloat()
        dismissContainer.animate().translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        isOpenCompleted = true
        imagesPager.visible()
        if (isImageReady) prepareViewsForViewer()
    }

    private fun animateClose() {
        prepareViewsForTransition()
        dismissContainer.setMargins(0, 0, 0, 0)

        transitionImageAnimator.animateClose(
            shouldDismissToBottom = shouldDismissToBottom,
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(backgroundView.alpha, 0f, duration)
                overlayView.animateAlpha(overlayView.alpha, 0f, duration)
            },
            onTransitionEnd = {
                imagesAdapter?.release()
                lifecycle?.removeObserver(lifecycleObserver)
                onDismiss?.invoke()
            })
    }

    private fun prepareViewsForTransition() {
        transitionImageContainer.alpha = 1f
        transitionImageContainer.visible()
        imagesPager.invisible()
    }

    private fun prepareViewsForViewer() {
        transitionImageContainer.invisibleAnimation()
    }

    private fun handleTouchIfNotScaled(event: MotionEvent, res: Boolean): Boolean {
        directionDetector.handleTouchEvent(event)

        return when (swipeDirection) {
            SwipeDirection.UP, SwipeDirection.DOWN -> {
                if (isSwipeToDismissAllowed && !wasScaled && imagesPager.isIdle) {
                    swipeDismissHandler.onTouch(rootContainer, event)
                } else true
            }

            SwipeDirection.LEFT, SwipeDirection.RIGHT -> {
                //imagesPager.dispatchTouchEvent(event)
                res
            }

            else -> res
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

        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSwipeViewMove(translationY: Float, translationLimit: Int) {
        val alpha = calculateTranslationAlpha(translationY, translationLimit)
        backgroundView.alpha = alpha
        overlayView.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        overlayView
            .let { it.isVisible && it.dispatchTouchEvent(event) }

    private fun calculateTranslationAlpha(translationY: Float, translationLimit: Int): Float =
        1.0f - 1.0f / translationLimit.toFloat() / 4f * Math.abs(translationY)

    private fun createSwipeDirectionDetector() =
        SwipeDirectionDetector(context) { swipeDirection = it }

    private fun createGestureDetector() =
        GestureDetectorCompat(context, SimpleOnGestureListener(
            onSingleTap = { false },
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

    private fun resetVideoPlayback(position: Int) {
        getVideoViewHolder(position)?.resetVideoPlayback()
        lastResetPosition = position
    }

    private fun pauseAllVideoPlayback(){
        imagesAdapter?.pauseVideo()
    }

    private fun resumeVideoPlayback(position: Int) {
        getVideoViewHolder(position)?.resumePlayback()
    }

    private fun getVideoViewHolder(position: Int): ImagesPagerAdapter.VideoViewHolder?
        = imagesAdapter?.getVideoViewHolderForPosition(position)


    private fun pauseSidePages() {
        doDelayed(DELAY_FOR_PAUSE_SIDE_POSITION_VIDEOS) {
            imagesAdapter?.pauseSidePositionVideo(currentPosition)
        }
    }

    fun release() = Unit
}
