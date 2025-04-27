package com.meera.core.utils.mediaviewer.viewer.view

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.TransitionDrawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.R
import com.meera.core.bottomsheets.SuggestionsMenuContract
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.string
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
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_IMAGE
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO_NOT_PLAYING
import com.meera.core.utils.mediaviewer.viewer.adapter.ImagesPagerAdapter
import com.meera.core.utils.tedbottompicker.models.MediaUriModel
import com.meera.core.views.EditTextExtended
import com.meera.core.views.EditTextExtended.DefaultTextColor.MediaViewerPreviewChat
import com.meera.core.views.MenuPopup
import com.meera.media_controller_common.MediaControllerOpenPlace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MediaViewerPreviewChat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IViewerView(context, attrs, defStyleAttr) {

    var fragmentManager: FragmentManager? = null
    var textWatcher: (message: String) -> Unit = {}
    var startText: String = ""
    var shouldOpenEditor: Boolean = false

    private var filesUtil = FileUtilsImpl(context)
    private var isZoomingAllowed = true
    private var isSwipeToDismissAllowed = true
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
    private var isShowingHint = false
    private var isGoFurther = false

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var transitionView: ImageView
    private var externalTransitionImageView: ImageView? = null
    private var overlayView: ConstraintLayout
    private var backBtn: ImageButton

    private var viewOverlayStatusBar: View? = null
    private var svDescription: ScrollView? = null
    private var tvEdit: TextView? = null
    private var tvOverlayDescription: TextView? = null
    private var tvOverlayToolbar: TextView? = null
    private var ivOverlayMenu: ImageView? = null

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
    private var cvAddPhotoBtn: ImageView? = null
    private var comment: EditTextExtended? = null
    private var ivSelectPhotoBtn: ImageView? = null
    private var tvSelectedMediaCount: TextView? = null
    private var llHint: LinearLayout? = null
    private var tvHintText: TextView? = null
    private var uniqueNameSuggestionMenuLayout: View? = null
    private var images: MutableList<ImageViewerData> = mutableListOf()

    private lateinit var transitionImageAnimator: TransitionImageAnimator
    private var lifecycle: Lifecycle? = null
    private var startPosition: Int = 0
        set(value) {
            field = value
            currentPosition = value
        }

    override var selectedMediaCount: Int = 0
        set(value) {
            field = value
            if (selectedMediaCount > 0) {
                tvSelectedMediaCount?.text = selectedMediaCount.toString()
                tvSelectedMediaCount?.visible()
            } else tvSelectedMediaCount?.gone()
        }

    private val shouldDismissToBottom: Boolean
        get() = externalTransitionImageView == null
            || !externalTransitionImageView.isRectVisible
            || !isAtStartPosition

    private val isAtStartPosition: Boolean
        get() = currentPosition == startPosition

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    init {
        View.inflate(context, R.layout.image_viewer_preview_chat, this)

        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        overlayView = findViewById(R.id.cl_overlay_container)
        cvAddPhotoBtn = findViewById(R.id.iv_btn_continue)
        backBtn = findViewById(R.id.ib_cancel_preview)
        comment = findViewById(R.id.editText)
        ivSelectPhotoBtn = findViewById(R.id.iv_selected_media_btn)
        tvSelectedMediaCount = findViewById(R.id.tv_selected_count)
        llHint = findViewById(R.id.ll_hint_preview)
        tvHintText = findViewById(R.id.toast_text)
        viewOverlayStatusBar = findViewById(R.id.view_overlay_statusbar)
        svDescription = findViewById(R.id.sv_description)
        tvEdit = findViewById(R.id.tv_edit)
        tvOverlayDescription = findViewById(R.id.tv_overlay_description)
        tvOverlayToolbar = findViewById(R.id.tv_overlay_toolbar)
        ivOverlayMenu = findViewById(R.id.iv_overlay_menu)
        uniqueNameSuggestionMenuLayout = findViewById(R.id.uniqueNameSuggestionMenuL)

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

        comment?.setOnClickListener {
            isGoFurther = true
        }
        comment?.doAfterTextChanged { editable ->
            val text = editable?.toString() ?: return@doAfterTextChanged
            textWatcher.invoke(text)
        }
        comment?.setOnFocusChangeListener { _, _ ->
            isGoFurther = true
        }

        backBtn.setOnClickListener {
            isGoFurther = true
            close()
        }

        cvAddPhotoBtn?.setOnClickListener {
            isGoFurther = true
            val images: MutableList<Uri>? = imagesAdapter?.getSelectedImages()
            checkIsValidData(imagesAdapter?.getSelectedData()) { isValidData ->
                if (isValidData) {
                    images?.let {
                        if (it.isEmpty()) it.add(Uri.parse(imagesAdapter?.getImgUrlByPosition(imagesPager.currentItem)))
                        onImageReadyWithText(images, comment?.text?.toString() ?: "")
                        close()
                    } ?: kotlin.run {
                        onImageReadyWithText(mutableListOf(), comment?.text?.toString() ?: "")
                        close()
                    }
                } else {
                    images?.let {
                        if (it.isEmpty()) it.add(Uri.parse(imagesAdapter?.getImgUrlByPosition(imagesPager.currentItem)))
                        if (it.size > 0) showDialogToLongVideo()
                    }
                }
            }
        }

        ivSelectPhotoBtn?.setOnClickListener {
            isGoFurther = true
            handleClickCheckAction()
        }

        tvEdit?.setThrottledClickListener {
            openEditor()
        }

        val addBtnParams = (cvAddPhotoBtn?.layoutParams as? ConstraintLayout.LayoutParams)
        val navBarHeight = getNavBarHeight()
        addBtnParams?.bottomMargin = (addBtnParams?.bottomMargin ?: 0) + navBarHeight

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()

        setDotsMenu()
        initKeyboardHeightProvider();
    }


    /**
     * Надо переделать верстку чтобы инпут и кнопка были завернуты в конте
     * */
    private fun initKeyboardHeightProvider() {
        rootContainer.post {
            val inputContainerViewGroup = findViewById<View>(R.id.inputContainer)

            keyboardHeightProvider = KeyboardHeightProvider(rootContainer)
            keyboardHeightProvider?.observer = fun(keyboardHeight: Int) {
                if (keyboardHeight > 0) {
                    inputContainerViewGroup?.animate()
                        ?.translationY(-(keyboardHeight.toFloat()))
                        ?.setDuration(150)
                        ?.start()

                    setSuggestionsMenuExtraPeekHeight(keyboardHeight + inputContainerViewGroup.height)
                } else {
                    inputContainerViewGroup?.animate()
                        ?.translationY(0.0f)
                        ?.setDuration(150)
                        ?.start()

                    setSuggestionsMenuExtraPeekHeight(inputContainerViewGroup.height)
                }
            }
        }
    }


    private var uniqueNameSuggestionMenu: SuggestionsMenuContract? = null

    fun initUniqueNameSearch(uniqueNameSuggestionMenu: SuggestionsMenuContract?) {
        this.uniqueNameSuggestionMenu = uniqueNameSuggestionMenu

        comment?.setOnNewUniqueNameAfterTextChangedListener(object : EditTextExtended.OnNewUniqueNameListener {
            override fun onNewUniqueName(uniqueName: String) {
                searchUsersByUniqueName(uniqueName)
            }
        })

        comment?.setOnUniqueNameNotFoundListener(object : EditTextExtended.OnUniqueNameNotFoundListener {
            override fun onNotFound() {
                if (uniqueNameSuggestionMenu?.isHidden == false) {
                    uniqueNameSuggestionMenu.forceCloseMenu()
                }
            }
        })

        comment?.setDefaultTextColor(MediaViewerPreviewChat)

        uniqueNameSuggestionMenuLayout?.let { tagsList ->
            val bottomSheetBehavior = BottomSheetBehavior.from(tagsList)
            val recyclerTagsView = tagsList.findViewById<RecyclerView>(R.id.recycler_tags)
            recyclerTagsView?.let { recyclerTags ->
                comment?.let { etWriteComment ->
                    uniqueNameSuggestionMenu?.init(
                        recyclerTags,
                        etWriteComment,
                        bottomSheetBehavior
                    )

                    uniqueNameSuggestionMenu?.suggestedUniqueNameClicked =
                        fun(userData: SuggestionsMenuContract.UITagEntity) {
                            replaceUniqueNameBySuggestion(userData)
                            uniqueNameSuggestionMenu?.forceCloseMenu()
                        }

                    uniqueNameSuggestionMenu?.setDarkColored()
                }
            }
        }
    }

    private fun setSuggestionsMenuExtraPeekHeight(extraPeekHeight: Int) {
        uniqueNameSuggestionMenu?.setExtraPeekHeight(extraPeekHeight, true)
    }

    private var lastSearchUniqueName: String? = null

    private fun replaceUniqueNameBySuggestion(userData: SuggestionsMenuContract.UITagEntity?) {
        val uniqueName = userData?.uniqueName?.let { "@$it " }
        val commentText = comment?.text
        if (!commentText.isNullOrEmpty() && uniqueName != null) {
            lastSearchUniqueName?.let { nonNullLastSearchUniqueName ->
                commentText
                    .toString()
                    .replace(nonNullLastSearchUniqueName, uniqueName)
                    .also { comment?.setText(it) }

                commentText
                    .indexOf(uniqueName)
                    .let { it + uniqueName.length }
                    .also { comment?.setSelection(it + 1) }
            }
        }
    }

    private var searchUniqueNameJob: Job? = null

    private fun searchUsersByUniqueName(uniqueName: String) {
        searchUniqueNameJob?.cancel()
        searchUniqueNameJob = CoroutineScope(Dispatchers.IO).launch {
            delay(300)
            lastSearchUniqueName = uniqueName
            val uniqueNameWithoutPrefix = uniqueName.replace("@", "", true)
            uniqueNameSuggestionMenu?.searchUsersByUniqueName(uniqueNameWithoutPrefix)
        }
    }

    private fun checkIsValidData(images: MutableList<ImageViewerData>?, isValidCallback: (Boolean) -> Unit) {

        if (images?.isEmpty() == true) {
            imagesAdapter?.getMediaByPosition(imagesPager.currentItem)?.let { images.add(it) }
        }
        doAsync({
            images?.forEach {
                it.getInitialStringUri().let { nonNullImageUrl ->
                    if (it.viewType == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                        val time = filesUtil.getVideoDurationMils(Uri.parse(nonNullImageUrl))
                        if (time > 300000) return@doAsync false
                    }
                }
            }
            return@doAsync true
        }, {
            isValidCallback(it)
        })

    }


    private fun openEditor() {
        isGoFurther = true
        imagesAdapter?.getImgUrlByPosition(currentPosition)?.let { imageUrl ->
            mediaViewerPhotoEditorCallback?.onOpenPhotoEditor(
                imageUrl = Uri.parse(imageUrl),
                type = MediaControllerOpenPlace.Chat,
                supportGifEditing = true,
                resultCallback = object : MediaViewerPhotoEditorCallback.MediaViewerPhotoEditorResultCallback {
                    override fun onPhotoReady(resultUri: Uri) {
                        val prevSelectedState = unselectOldPhotoIfNeeded()
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(MediaUriModel.initial(resultUri), "")
                        res.isSelected = prevSelectedState
                        data.add(res)
                        data.addAll(images)
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        if (prevSelectedState) imageChangeListener?.onImageChecked(res, true)
                        val isSelectedDrawable =
                            if (prevSelectedState) R.drawable.ic_selected_photo else R.drawable.white_ring_bg
                        ivSelectPhotoBtn?.setImageResource(isSelectedDrawable)
                    }

                    override fun onVideoReady(resultUri: Uri) {
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(
                            MediaUriModel.initial(resultUri),
                            ""
                        )
                        res.viewType = VIEW_TYPE_VIDEO_NOT_PLAYING
                        res.isSelected = true
                        data.add(res)
                        data.addAll(images)
                        unselectAllMedia()
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        ivSelectPhotoBtn?.setImageResource(R.drawable.ic_selected_photo)
                        selectedMediaCount = 1
                        resultUri.path?.let { str -> mediaViewerPhotoEditorCallback?.onAddHashSetVideoToDelete(str) }
                    }

                    override fun onError() {
                        close()
                    }

                    override fun onCanceled() {
                        val currentData = imagesAdapter?.getCurrentData(currentPosition)
                        ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)
                        currentData?.isSelected = false
                        selectedMediaCount--
                        imageChangeListener?.onImageChecked(currentData, false)
                    }
                }
            )
        }
    }

    private fun unselectOldPhotoIfNeeded(): Boolean {
        val currentData = imagesAdapter?.getCurrentData(currentPosition)
        val prevState = currentData?.isSelected ?: false
        currentData?.isSelected = false
        imageChangeListener?.onImageChecked(currentData, false)
        return prevState
    }

    private fun unselectAllMedia() {
        images.forEach {
            it.isSelected = false
        }
    }

    private fun showDialogToLongVideo() {
        fragmentManager?.let {
            val currentData = imagesAdapter?.getCurrentData(currentPosition)
            ConfirmDialogBuilder()
                .setHeader(context.string(R.string.invalid_duration))
                .setDescription(context.string(R.string.you_cant_send_video_more))
                .setLeftBtnText(context.string(R.string.unsubscribe_dialog_cancel_subs))
                .setRightBtnText(context.string(R.string.to_editor))
                .setCancelable(false)
                .setLeftClickListener {
                    ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)
                    currentData?.isSelected = false
                    selectedMediaCount--
                    imageChangeListener?.onImageChecked(currentData, false)
                }
                .setRightClickListener { openEditor() }
                .show(it)
        }
    }

    private fun handleClickCheckAction() {
        val currentData = imagesAdapter?.getCurrentData(currentPosition)
        if (imagesAdapter?.getCurrentData(currentPosition)?.isSelected == false) {
            if (selectedMediaCount >= 5) {
                showHint(context.getString(R.string.you_can_add_5_objects))
                return
            }
            val selected = imagesAdapter?.getSelectedData()
            if (selected?.size ?: 0 > 0) {
                if (selected?.get(0)?.viewType == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                    if (currentData?.viewType == VIEW_TYPE_IMAGE) {
                        showHint(context.getString(R.string.photo_adding_not_allowed))
                    } else {
                        showHint(context.getString(R.string.video_adding_not_allowed))
                    }
                    return
                }
                if (selected?.get(0)?.viewType == VIEW_TYPE_IMAGE
                    && currentData?.viewType == VIEW_TYPE_VIDEO_NOT_PLAYING
                ) {
                    showHint(context.getString(R.string.video_adding_not_allowed))
                    return
                }
            }
            ivSelectPhotoBtn?.setImageResource(R.drawable.ic_selected_photo)
            currentData?.isSelected = true
            selectedMediaCount++
            //imageChangeListener?.onImageChecked(currentData, true)
            val strUriToCheck = currentData?.getInitialStringUri()
            doAsync({
                strUriToCheck?.let { str ->
                    val uriToCheck = Uri.parse(str)
                    val time = filesUtil.getVideoDurationMils(uriToCheck)
                    if (time > 300000) {
                        return@doAsync uriToCheck
                    } else return@doAsync null
                } ?: kotlin.run { return@doAsync null }
            }, {
                it?.let {
                    showDialogToLongVideo()
                } ?: kotlin.run {
                    imageChangeListener?.onImageChecked(currentData, true)
                }
            })

        } else {
            ivSelectPhotoBtn?.setImageResource(R.drawable.white_ring_bg)
            currentData?.isSelected = false
            selectedMediaCount--
            imageChangeListener?.onImageChecked(currentData, false)
        }
    }

    //hint max selected count
    private fun showHint(textToShow: String) {
        tvHintText?.text = textToShow
        if (isShowingHint) return
        isShowingHint = true
        hintAnimate(1.0f)
        Handler(Looper.getMainLooper()).postDelayed({
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

        return if (isScaled || uniqueNameSuggestionMenu?.isHidden != true) super.dispatchTouchEvent(event) else handleTouchIfNotScaled(
            event
        )
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

        imagesAdapter?.onVideoClicked = { media ->
            isGoFurther = true
            openVideo(media)
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
                        .setOrientationChangedListener(orientation)
                        .setLifeCycle(life) // need when video shown
                        .show()
                }
            }
        }
    }

    override fun open(transitionView: ImageView?, animate: Boolean) {
        comment?.setText(startText)
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
            .load(images[startPosition].getInitialStringUri())
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(transitionView)
    }

    @SuppressLint("SetTextI18n")
    fun updateOverlayViews(position: Int) {
        val model = images[position]
        if (model.description.isNullOrEmpty()) {
            tvOverlayDescription?.gone()
        } else {
            tvOverlayDescription?.text = model.description
            tvOverlayDescription?.visible()
        }

        if (images.size > 1) {
            tvOverlayToolbar?.text = "${position + 1} ${context.getString(R.string.of)} ${images.size}"
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
                        images[currentPosition].getInitialStringUri().let { it1 ->
                            onSaveImage?.invoke(it1)
                        }
                    }
                    show(
                        ivOverlayMenu, Gravity.TOP or Gravity.END, 0, -(ivOverlayMenu?.height
                            ?: 0)
                    )
                }
            } else if (!menu!!.isShowing) {
                menu?.show(
                    ivOverlayMenu, Gravity.TOP or Gravity.END, 0, -(ivOverlayMenu?.height
                        ?: 0)
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
        dismissContainer.animate().translationY(0f).setDuration(300).setInterpolator(DecelerateInterpolator()).start()
        isOpenCompleted = true
        imagesPager.visible()
        if (isImageReady) prepareViewsForViewer()
    }

    private fun animateClose() {
        keyboardHeightProvider?.release()

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
        overlayView.let { it.isVisible && it.dispatchTouchEvent(event) }

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

    fun setLifeCycle(lifecycle: Lifecycle?) {
        this.lifecycle = lifecycle
    }
}
