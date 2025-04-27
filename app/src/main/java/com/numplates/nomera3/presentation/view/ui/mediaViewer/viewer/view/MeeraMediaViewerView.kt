package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedListBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.nav.UiKitNavView
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
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
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.adapter.ImagesPagerAdapter
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber

private const val TRANSLATION_LIMIT_Y = 4f
private const val TRANSLATION_LIMIT = 1f
private const val ANIMATE_ALPHA_DURATION = 300L
private const val MIN_POINTER_COUNT = 1

private const val NO_POSITION = -1
private const val DELAY_FOR_PAUSE_SIDE_POSITION_VIDEOS = 50L
private const val START_VIDEO_DELAY = 50L

internal class MeeraMediaViewerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : IViewerView(context, attrs, defStyleAttr) {

    var orientation: OrientationScreenListener? = null
    private var lifecycle: Lifecycle? = null
    var onImageDelete: ((imageId: Long) -> Unit)? = null
    var onImageCopy: ((imageUrl: String) -> Unit)? = null
    var onImageShare: ((imageUrl: String) -> Unit)? = null
    var onVideoShare: ((videoUrl: String) -> Unit)? = null
    var shareMenuAvailable: Boolean = true
    var copyMenuAvailable: Boolean = true
    var onAddToFavorite: ((position: Int, isInFavorites: Boolean) -> Unit)? = null
    var onGetFavorites: (() -> List<MediakeyboardFavoriteRecentUiModel>)? = null
    var isOpenPost = false
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
    private var vNavView: UiKitNavView? = null
    private var ivDotsMenu: ImageView? = null
    private var editBtn: UiKitButton? = null
    private var ivSelectPhotoBtn: ImageView? = null
    private var statusBar: View? = null
    private var ivSelectedMedia: ImageView? = null
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
    private var isVideoView = false

    private var images: MutableList<ImageViewerData> = mutableListOf()
    private lateinit var transitionImageAnimator: TransitionImageAnimator
    private var lastResetPosition = -1

    private var onlyOneImage = true
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
        View.inflate(context, R.layout.meera_view_image_viewer, this)
        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        overlayView = findViewById(R.id.cl_overlay_container)
        viewOverlayStatusBar = findViewById(R.id.view_overlay_statusbar)
        svDescription = findViewById(R.id.sv_description)
        vNavView = findViewById(R.id.v_nav_view)
        ivDotsMenu = findViewById(R.id.iv_dots_menu)
        ivSelectedMedia = findViewById(R.id.iv_selected_media_btn)
        editBtn = findViewById(R.id.v_edit_btn)
        statusBar = findViewById(R.id.fake_status_bar)
        tvOverlayDescription = findViewById(R.id.tv_overlay_description)
        tvOverlayToolbar = findViewById(R.id.tv_overlay_toolbar)
        viewOverlayStatusBar?.layoutParams?.height = context.getStatusBarHeight()
        svDescription?.setMargins(bottom = context.getNavigationBarHeight())

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = {
                externalTransitionImageView?.apply {
                    if (isAtStartPosition) invisible() else visible()
                }
                onPageChange?.invoke(it)
                updateOverlayViews(it)
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
            })
        ivDotsMenu?.setThrottledClickListener {
            setDotsMenu()
        }

        vNavView?.apply {
            closeButtonClickListener = { close() }
            setTextColor(R.color.ui_white)
        }

        pauseSidePages()

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
    }

    private fun configureToolbar() { //Временная заглушка для видео
        if (isVideoView) {
            dismissContainer.setMargins(top = 0)
        }
    }

    private fun initToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer) { _, windowInsets ->
            val fakeStatusBar = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            statusBar?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = fakeStatusBar
            }
            windowInsets
        }
    }

    private fun openEditor() {
        imagesAdapter?.getImgUrlByPosition(currentPosition)?.let {
            act?.getMediaControllerFeature()?.open(
                uri = Uri.parse(it),
                openPlace = MediaControllerOpenPlace.Chat,
                callback = object : MediaControllerCallback {
                    override fun onPhotoReady(resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?) {
                        Timber.e("On image Ready URI:$resultUri")
                        val prevSelectedState = unselectOldPhotoIfNeeded()
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(resultUri.toString(), "")
                        res.isSelected = prevSelectedState
                        data.add(res)
                        if (!onlyOneImage) {
                            data.addAll(images)
                        }
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        if (prevSelectedState) imageChangeListener?.onImageChecked(res, true)
                        val isSelectedDrawable =
                            if (prevSelectedState) R.drawable.ic_meera_selected_photo else R.drawable.meera_green_ring_bg
                        ivSelectPhotoBtn?.setImageResource(isSelectedDrawable)
                    }

                    override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(
                            resultUri.toString(),
                            ""
                        )
                        res.viewType = RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING
                        res.isSelected = true
                        data.add(res)
                        data.addAll(images)
                        unselectAllMedia()
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        ivSelectPhotoBtn?.setImageResource(R.drawable.ic_meera_selected_photo)
                        selectedMediaCount = MIN_POINTER_COUNT
                    }

                    override fun onError() {
                        close()
                    }

                    override fun onCanceled() {
                        val currentData = imagesAdapter?.getCurrentData(currentPosition)
                        ivSelectPhotoBtn?.setImageResource(R.drawable.meera_green_ring_bg)
                        currentData?.isSelected = false
                        selectedMediaCount--
                        imageChangeListener?.onImageChecked(currentData, false)
                    }
                })
        }
    }

    private fun unselectAllMedia() {
        images.forEach {
            it.isSelected = false
        }
    }

    private fun unselectOldPhotoIfNeeded(): Boolean {
        val currentData = imagesAdapter?.getCurrentData(currentPosition)
        val prevState = currentData?.isSelected ?: false
        currentData?.isSelected = false
        imageChangeListener?.onImageChecked(currentData, false)
        return prevState
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


    fun initLifecycleHandler(lifecycle: Lifecycle? = null) {
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

        if (wasDoubleTapped &&
            event.action == MotionEvent.ACTION_MOVE &&
            event.pointerCount == MIN_POINTER_COUNT
        ) {
            return true
        }

        handleUpDownEvent(event)

        if (swipeDirection == null && (scaleDetector.isInProgress || event.pointerCount > MIN_POINTER_COUNT || wasScaled)) {
            wasScaled = true
            return imagesPager.dispatchTouchEvent(event)
        }
        val res = super.dispatchTouchEvent(event)
        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event, res)
    }

    override fun setBackgroundColor(color: Int) {
        backgroundView.setBackgroundResource(R.color.ui_black)
    }

    override fun setImages(images: MutableList<ImageViewerData>, startPosition: Int) {
        this.images.clear()
        this.images.addAll(images)
        this.startPosition = startPosition
        imagesAdapter = ImagesPagerAdapter()
        imagesAdapter?.disableStartVideoOnCreate()
        imagesAdapter?.isSilentMode = false
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
        images.forEach { img ->
            if (img.viewType == RecyclingPagerAdapter.VIEW_TYPE_VIDEO) {
                isVideoView = true
                return@forEach
            }
        }
        configureToolbar()
        postDelayed({
            resetVideoPlayback(currentPosition)
        }, START_VIDEO_DELAY)
        initToolbar()
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
        meeraAct?.let {
            vNavView?.showCloseButton = true
            ivDotsMenu?.visible()
            editBtn?.setThrottledClickListener {
                openEditor()
            }
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

    private fun setDotsMenu(){
        val fm = (context as? AppCompatActivity)?.supportFragmentManager
        fm?.let {
            MeeraConfirmDialogUnlimitedListBuilder()
                .setHeader(R.string.actions)
                .setListItems(initListItemMediaViewerMenu())
                .setItemListener { action ->
                    initMediaViewerMenuListener(action as MeeraMediaViewerMenuAction)
                }.show(fm)
        }
    }

    private fun initMediaViewerMenuListener(
        action: MeeraMediaViewerMenuAction
    ){
        when(action){
            MeeraMediaViewerMenuAction.AddFavorite -> {
                onAddToFavorite?.invoke(currentPosition, false)
            }
            MeeraMediaViewerMenuAction.RemoveFavorite -> {
                onAddToFavorite?.invoke(currentPosition, true)
            }
            MeeraMediaViewerMenuAction.Copy -> {
                images[currentPosition].imageUrl?.let { imageUrl ->
                    onImageCopy?.invoke(imageUrl)
                }
            }
            MeeraMediaViewerMenuAction.Save -> {
                images[currentPosition].imageUrl?.let { imageUrl ->
                    if (isVideoView) {
                        onVideoReady(imageUrl)
                    } else {
                        showCommonSuccessMessage(context.getText(R.string.image_saved), rootView)
                        onSaveImage?.invoke(imageUrl)
                    }
                }
            }
            MeeraMediaViewerMenuAction.Share -> {
                images[currentPosition].imageUrl?.let { imageUrl ->
                    if (isVideoView) {
                        onVideoShare?.invoke(imageUrl)
                    } else {
                        onImageShare?.invoke(imageUrl)
                    }
                }
            }
        }
    }

    private fun initListItemMediaViewerMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        when{
            isOpenPost -> return listOf(
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.map_events_time_picker_action,
                    icon = R.drawable.ic_outlined_download_m,
                    action = MeeraMediaViewerMenuAction.Save,
                )
            )

            isVideoView -> return listOf(
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.map_events_time_picker_action,
                    icon = R.drawable.ic_outlined_download_m,
                    action = MeeraMediaViewerMenuAction.Save,
                ),
                checkForAddToFavoriteItem(),
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.image_share,
                    icon = R.drawable.ic_outlined_share_m,
                    action = MeeraMediaViewerMenuAction.Share,
                )
            )

            else -> return listOf(
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.map_events_time_picker_action,
                    icon = R.drawable.ic_outlined_download_m,
                    action = MeeraMediaViewerMenuAction.Save,
                ),
                checkForAddToFavoriteItem(),
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.text_copy_txt,
                    icon = R.drawable.ic_outlined_copy_m,
                    action = MeeraMediaViewerMenuAction.Copy,
                ),
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.image_share,
                    icon = R.drawable.ic_outlined_share_m,
                    action = MeeraMediaViewerMenuAction.Share,
                )
            )
        }
    }

    private fun checkForAddToFavoriteItem(): MeeraConfirmDialogUnlimitedNumberItemsData {
        val favorites = onGetFavorites?.invoke() ?: emptyList()
        val currentItem = images[currentPosition]
        val isCurrentItemInFavorites = favorites.any { it.url == currentItem.imageUrl }
        val shouldShowAddToFavoritesItem = onAddToFavorite != null && onGetFavorites != null
        when {
            shouldShowAddToFavoritesItem && !isCurrentItemInFavorites -> {
               return MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.meera_add_to_favorites,
                    icon = R.drawable.ic_outlined_star2_m,
                    contentColor = R.color.uiKitColorForegroundPrimary,
                    action = MeeraMediaViewerMenuAction.AddFavorite,
                )
            }
            else -> {
                return MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.meera_remove_from_favorites,
                    icon = R.drawable.ic_outlined_star2_m,
                    contentColor = R.color.uiKitColorForegroundPrimary,
                    action = MeeraMediaViewerMenuAction.RemoveFavorite,
                )
            }
        }
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

            if (imagesAdapter?.count ?: 0 > MIN_POINTER_COUNT) {
                vNavView?.title =
                    "${position + MIN_POINTER_COUNT}/${imagesAdapter?.count ?: images.size}"
            }
        } else {
            tvOverlayDescription?.gone()
        }
    }

    override fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, TRANSLATION_LIMIT, duration)
                overlayView.animateAlpha(0f, TRANSLATION_LIMIT, duration)
            },
            onTransitionEnd = {
                isOpenCompleted = true
                imagesPager.visible()
                if (isImageReady) prepareViewsForViewer()
            })
    }

    private fun animateOpenWithoutTransition() {
        backgroundView.animateAlpha(0f, TRANSLATION_LIMIT, ANIMATE_ALPHA_DURATION)
        overlayView.animateAlpha(0f, TRANSLATION_LIMIT, ANIMATE_ALPHA_DURATION)
        isOpenCompleted = true
        imagesPager.visible()
        if (isImageReady) prepareViewsForViewer()
    }

    private fun animateClose() {
        prepareViewsForTransition()

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
        transitionImageContainer.alpha = TRANSLATION_LIMIT
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
        TRANSLATION_LIMIT - TRANSLATION_LIMIT / translationLimit.toFloat() / TRANSLATION_LIMIT_Y * Math.abs(translationY)

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
