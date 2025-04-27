package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.R
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
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.adapter.ImagesPagerAdapter
import com.numplates.nomera3.presentation.view.ui.menuPopup.MenuPopup
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber

internal class MediaViewerPreview @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IViewerView(context, attrs, defStyleAttr) {


    private var isZoomingAllowed = true
    private var isSwipeToDismissAllowed = true

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
    override var onImageEdit: ((imageUrl: String) -> Unit) = {}

    private var isOpenCompleted = false
    private var isImageReady = false
    private var isGoFurther = false
    private var isShowingHint = false

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var transitionView: ImageView
    private var externalTransitionImageView: ImageView? = null
    private var overlayView: ConstraintLayout
    private var backBtn: ImageButton
    private var editBtn: TextView
    private var tvContinue: TextView? = null
    private var viewOverlayStatusBar: View? = null
    private var svDescription: ScrollView? = null

    private var imagesPager: MultiTouchViewPager
    private var imagesAdapter: ImagesPagerAdapter? = null
    private var tvEdit: TextView? = null
    private var tvOverlayDescription: TextView? = null
    private var tvOverlayToolbar: TextView? = null
    private var ivOverlayMenu: ImageView? = null

    private var directionDetector: SwipeDirectionDetector
    private var gestureDetector: GestureDetectorCompat
    private var scaleDetector: ScaleGestureDetector
    private lateinit var swipeDismissHandler: SwipeToDismissHandler

    private var wasScaled: Boolean = false
    private var wasDoubleTapped = false
    private var isOverlayWasClicked: Boolean = false
    private var swipeDirection: SwipeDirection? = null
    private var cvAddPhotoBtn: CardView? = null
    private var ivSelectPhotoBtn: ImageView? = null
    private var tvSelectedMediaCount: TextView? = null
    private var llHint: LinearLayout? = null

    private var images: MutableList<ImageViewerData> = mutableListOf()
    private lateinit var transitionImageAnimator: TransitionImageAnimator

    private var isPressAddPhoto = false

    override var selectedMediaCount: Int = 0
        set(value) {
            field = value
            if (selectedMediaCount > 0) {
                tvSelectedMediaCount?.text = selectedMediaCount.toString()
                tvSelectedMediaCount?.visible()
                when (selectedMediaCount) {
                    1 -> {
                        tvContinue?.setText(R.string.add_one_photo)
                    }
                    in 2..4 -> {
                        tvContinue?.text =
                            resources.getString(R.string.add_few_photo, selectedMediaCount)
                    }
                    5 -> {
                        tvContinue?.text =
                            resources.getString(R.string.add_many_photo, selectedMediaCount)
                    }
                }
            } else {
                tvContinue?.text = context?.getString(R.string.add_photo_txt)
                tvSelectedMediaCount?.gone()
            }

        }

    private var startPosition: Int = 0
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
        View.inflate(context, R.layout.view_image_viewer_preview, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        overlayView = findViewById(R.id.cl_overlay_container)
        cvAddPhotoBtn = findViewById(R.id.cv_btn_continue)
        backBtn = findViewById(R.id.ib_cancel_preview)
        editBtn = findViewById(R.id.tv_edit)
        ivSelectPhotoBtn = findViewById(R.id.iv_selected_media_btn)
        tvSelectedMediaCount = findViewById(R.id.tv_selected_count)
        llHint = findViewById(R.id.ll_hint_preview)
        tvContinue = findViewById(R.id.tvContinue)
        viewOverlayStatusBar = findViewById(R.id.view_overlay_statusbar)
        svDescription = findViewById(R.id.sv_description)
        tvEdit = findViewById(R.id.tv_edit)
        tvOverlayDescription = findViewById(R.id.tv_overlay_description)
        tvOverlayToolbar = findViewById(R.id.tv_overlay_toolbar)
        ivOverlayMenu = findViewById(R.id.iv_overlay_menu)
        viewOverlayStatusBar?.layoutParams?.height = context.getStatusBarHeight()
        svDescription?.setMargins(bottom = context.getNavigationBarHeight())

        transitionImageContainer = findViewById(R.id.transitionImageContainer)
        transitionView = findViewById(R.id.transitionImageView)

        imagesPager = findViewById(R.id.imagesPager)
        imagesPager.addOnPageChangeListener(
            onPageSelected = {
                if (imagesAdapter?.getCurrentData(it)?.isSelected == true)
                    ivSelectPhotoBtn?.setImageResource(R.drawable.ic_selected_photo)
                else ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)

                externalTransitionImageView?.apply {
                    if (isAtStartPosition) invisible() else visible()
                }
                onPageChange?.invoke(it)
                updateOverlayViews(it)
            })

        backBtn.setOnClickListener {
            isGoFurther = true
            close()
        }

        cvAddPhotoBtn?.setOnClickListener {
            if (!isPressAddPhoto) {
                isGoFurther = true
                val images: MutableList<Uri>? = imagesAdapter?.getSelectedImages()

                images?.let {
                    if (it.isEmpty()) it.add(
                        Uri.parse(
                            imagesAdapter?.getImgUrlByPosition(
                                currentPosition
                            )
                        )
                    )
                    onImageReadyWithText(images, "")
                    close()
                } ?: kotlin.run {
                    onImageReadyWithText(mutableListOf(), "")
                    close()
                }
                isPressAddPhoto = true
            }
        }

        tvEdit?.setOnClickListener {
            isGoFurther = true
            imagesAdapter?.getImgUrlByPosition(currentPosition)?.let {
                imagesAdapter?.getImgUrlByPosition(currentPosition)?.let {
                    act?.getMediaControllerFeature()?.open(
                        uri = Uri.parse(it),
                        openPlace = MediaControllerOpenPlace.Chat,
                        callback = object : MediaControllerCallback {
                            override fun onPhotoReady(
                                resultUri: Uri,
                                nmrAmplitude: NMRPhotoAmplitude?
                            ) {
                                val data = mutableListOf<ImageViewerData>()
                                val res = ImageViewerData(resultUri.toString(), "")
                                res.isSelected = false
                                data.add(res)
                                data.addAll(images)
                                setImages(data, 0)
                                imageChangeListener?.onImageAdded(res)
                                ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)
                            }

                            override fun onError() {
                                close()
                            }
                        }
                    )
                }
            }
        }

        ivSelectPhotoBtn?.setOnClickListener {
            isGoFurther = true
            handleClickCheckAction()
        }

        val addBtnParams = (cvAddPhotoBtn?.layoutParams as? ConstraintLayout.LayoutParams)
        val navBarHeight = getNavBarHeight()
        addBtnParams?.bottomMargin = (addBtnParams?.bottomMargin ?: 0) + navBarHeight

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()

        setDotsMenu()
    }

    //hint max selected count
    private fun showHint() {
        if (isShowingHint) return
        isShowingHint = true
        hintAnimate(1.0f)
        Handler().postDelayed({
            hintAnimate(0f)
            isShowingHint = false
        }, 1500)
    }

    private fun hintAnimate(alpha: Float) {
        llHint?.animate()
            ?.alpha(alpha)
            ?.setDuration(300)
            ?.setListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    llHint?.alpha = alpha
                }
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationStart(animation: Animator) = Unit
            })?.start()
    }


    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val overVi = overlayView.dispatchTouchEvent(event)
        if (!overVi) {
            return true
        }

        Timber.d("Bazaleev: onDispatchTouchEvent")
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
            if (!overVi)
                return imagesPager.dispatchTouchEvent(event)
        }

        return if (isScaled) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(event)
    }

    override fun setBackgroundColor(color: Int) {
        findViewById<View>(R.id.backgroundView).setBackgroundColor(color)
    }

    private fun getNavBarHeight(): Int {
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    override fun setImages(images: MutableList<ImageViewerData>, startPosition: Int) {
        this.images.clear()
        this.images.addAll(images)
        this.startPosition = startPosition
        imagesAdapter = ImagesPagerAdapter()
        imagesAdapter?.isZoomable = isZoomingAllowed
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
        if (images[startPosition].isSelected)
            ivSelectPhotoBtn?.setImageResource(R.drawable.ic_selected_photo)
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
        val model = images.getOrNull(position)
        if (model?.description.isNullOrEmpty()) {
            tvOverlayDescription?.gone()
        } else {
            tvOverlayDescription?.text = model?.description
            tvOverlayDescription?.visible()
        }

        if (images.size > 1) {
            tvOverlayToolbar?.text =
                "${position + 1}/${images.size}"
            tvOverlayToolbar?.visible()
        } else {
            tvOverlayToolbar?.gone()
        }
    }

    private var menu: MenuPopup? = null

    private fun setDotsMenu() {
        ivOverlayMenu?.setOnClickListener {
            if (menu == null) {
                menu = MenuPopup(context).apply {
                    addItem(R.drawable.image_download_menu_item, R.string.save_image) {
                        images[currentPosition].imageUrl?.let { it1 ->
                            onSaveImage?.invoke(it1)
                        }
                    }
                    show(
                        ivOverlayMenu,
                        Gravity.TOP or Gravity.END,
                        0,
                        -(ivOverlayMenu?.height ?: 0)
                    )
                }
            } else if (!menu!!.isShowing) {
                menu?.show(
                    ivOverlayMenu,
                    Gravity.TOP or Gravity.END,
                    0,
                    -(ivOverlayMenu?.height ?: 0)
                )
            }
        }
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

    private fun animateOpenWithoutTransition() {
        backgroundView.animateAlpha(0f, 1f, 300)
        overlayView.animateAlpha(0f, 1f, 300)
        dismissContainer.translationY = context.displayHeight.toFloat()
        dismissContainer.animate().translationY(0f).setDuration(300)
            .setInterpolator(DecelerateInterpolator()).start()
        isOpenCompleted = true
        imagesPager.visible()
        if (isImageReady) prepareViewsForViewer()
    }

    private fun handleClickCheckAction() {
        val currentData = imagesAdapter?.getCurrentData(currentPosition)
        if (imagesAdapter?.getCurrentData(currentPosition)?.isSelected == false) {
            if (selectedMediaCount >= 5) {
                showHint()
                return
            }
            ivSelectPhotoBtn?.setImageResource(R.drawable.ic_selected_photo)
            currentData?.isSelected = true
            selectedMediaCount++
            imageChangeListener?.onImageChecked(currentData, true)
        } else {
            ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)
            currentData?.isSelected = false
            selectedMediaCount--
            imageChangeListener?.onImageChecked(currentData, false)
        }
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
                    swipeDismissHandler.onTouch(rootContainer, event)
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

        swipeDismissHandler.onTouch(rootContainer, event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleEventActionUp(event: MotionEvent) {
        wasDoubleTapped = false
        swipeDismissHandler.onTouch(rootContainer, event)
        imagesPager.dispatchTouchEvent(event)
        isOverlayWasClicked = dispatchOverlayTouch(event)
    }

    private fun handleSingleTap(isOverlayWasClicked: Boolean) {
        if (!isOverlayWasClicked) {
            if (!isGoFurther) {
                handleClickCheckAction()
            } else {
                isGoFurther = false
            }
        }
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
            onSingleTap = {
                if (imagesPager.isIdle) {
                    handleSingleTap(isOverlayWasClicked)
                }
                false
            },
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
    fun release() = Unit
}
