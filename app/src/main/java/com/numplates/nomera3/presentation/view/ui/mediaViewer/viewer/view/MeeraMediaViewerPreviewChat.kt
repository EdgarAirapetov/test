package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.clearText
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.invisible
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.listeners.OrientationScreenListener
import com.meera.core.utils.mediaviewer.common.pager.RecyclingPagerAdapter
import com.meera.core.views.MeeraEditTextExtended
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_api.model.MediaControllerNeedEditResponse
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.tooltip.createTooltip
import com.meera.uikit.widgets.buttons.UiKitButton
import com.meera.uikit.widgets.nav.UiKitNavView
import com.meera.uikit.widgets.tooltip.TooltipMessage
import com.meera.uikit.widgets.tooltip.UiKitTooltipBubbleMode
import com.meera.uikit.widgets.tooltip.UiKitTooltipViewState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.presentation.view.ui.mediaViewer.ImageViewerData
import com.numplates.nomera3.presentation.view.ui.mediaViewer.MediaViewer
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.animateAlpha
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isRectVisible
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SimpleOnGestureListener
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeDirection
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeDirectionDetector
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.gestures.SwipeToDismissHandler
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.MultiTouchViewPager
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.pager.RecyclingPagerAdapter.Companion.VIEW_TYPE_VIDEO_NOT_PLAYING
import com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.adapter.ImagesPagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber

private const val DEFAULT_MAX_SELECTED_MEDIA_COUNT = 10
private const val DEFAULT_MAX_VIDEO_LENGTH = 90
private const val TOOLTIP_DURATION = 4000L
private const val TOOLTIP_ABOVE_OFFSET_Y = -4
private const val TOOLTIP_OFFSET_X = 5
private const val TOOLTIP_OFFSET_Y = 4

@Deprecated("Необходимо перенести в Core MediaViewer")
class MeeraMediaViewerPreviewChat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : IViewerView(context, attrs, defStyleAttr) {

    var fragmentManager: FragmentManager? = null
    var maxMediaCount: Int = DEFAULT_MAX_SELECTED_MEDIA_COUNT
    var maxVideoLengthInSeconds: Int = DEFAULT_MAX_VIDEO_LENGTH

    var textWatcher: (message: String) -> Unit = {}
    var startText: String = ""
    var userName: String = ""
    var onlyOneImage = false
        set(value) {
            field = value
            if (value) {
                tvSelectedPhoto?.gone()
                tvSelectedMediaCount?.gone()
            }
        }
    var shouldOpenEditor: Boolean = false
    var onGetPreselectedMedia: (() -> Set<Uri>?)? = null
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
    private var isGoFurther = false

    private var rootContainer: ViewGroup
    private var backgroundView: View
    private var dismissContainer: ViewGroup

    private val transitionImageContainer: FrameLayout
    private var transitionView: ImageView
    private var externalTransitionImageView: ImageView? = null
    private var viewOverlayStatusBar: View? = null
    private var svDescription: ScrollView? = null
    private var vEditBtn: UiKitButton? = null
    private var tvOverlayDescription: TextView? = null
    private var navBar: UiKitNavView? = null
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
    private var cvAddPhotoBtn: UiKitButton? = null
    private var comment: MeeraEditTextExtended? = null
    // каунтер вверху
    private var tvSelectedPhoto: TextView? = null
    //  каунтер на кнопке
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
            val totalSelectedMediaCount = getTotalSelectedMediaCount()
            if (totalSelectedMediaCount > 0) {
                tvSelectedMediaCount?.text = totalSelectedMediaCount.toString()
            }
            if (selectedMediaCount > 0 && !onlyOneImage) {
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

    private var isKeyboardOpen = false

    init {
        View.inflate(context, R.layout.meera_view_image_viewer_preview_chat, this)
        rootContainer = findViewById(R.id.rootContainer)
        backgroundView = findViewById(R.id.backgroundView)
        dismissContainer = findViewById(R.id.dismissContainer)
        cvAddPhotoBtn = findViewById(R.id.iv_btn_continue)
        comment = findViewById(R.id.editText)
        tvSelectedPhoto = findViewById(R.id.tv_selected_media)
        tvSelectedMediaCount = findViewById(R.id.tv_selected_count)
        llHint = findViewById(R.id.ll_hint_preview)
        tvHintText = findViewById(R.id.toast_text)
        viewOverlayStatusBar = findViewById(R.id.about_fragment_fake_status_bar)
        svDescription = findViewById(R.id.sv_description)
        vEditBtn = findViewById(R.id.v_edit_btn)
        tvOverlayDescription = findViewById(R.id.tv_overlay_description)
        navBar = findViewById(R.id.v_viewer_nav_bar)
        uniqueNameSuggestionMenuLayout = findViewById(R.id.uniqueNameSuggestionMenuL)
        initToolbar()
        svDescription?.setMargins(bottom = context.getNavigationBarHeight())

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
            })
        if (onlyOneImage) {
            tvSelectedPhoto?.gone()
            tvSelectedMediaCount?.gone()
        }
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

        navBar?.backButtonClickListener = {
            isGoFurther = true
            close()
        }

        cvAddPhotoBtn?.setOnClickListener {
            if (isKeyboardOpen) {
                context.hideKeyboard(this)
                onCloseKeyboard { sendImageWithText() }
            } else {
                sendImageWithText()
            }
        }

        tvSelectedPhoto?.setThrottledClickListener {
            isGoFurther = true
            handleClickCheckAction()
        }

        vEditBtn?.setThrottledClickListener {
            openEditor()
        }
        navBar?.setTextColor(R.color.uiKitColorBackgroundPrimary)
        val addBtnParams = (cvAddPhotoBtn?.layoutParams as? ConstraintLayout.LayoutParams)
        val navBarHeight = getNavBarHeight()
        addBtnParams?.bottomMargin = (addBtnParams?.bottomMargin ?: 0) + navBarHeight

        directionDetector = createSwipeDirectionDetector()
        gestureDetector = createGestureDetector()
        scaleDetector = createScaleGestureDetector()
        initKeyboardHeightProvider()
    }

    private fun showCountNumber() {
        val count = images[currentPosition].cnt
        if (count == 0) {
            tvSelectedPhoto?.setBackgroundResource(com.meera.core.R.drawable.white_ring_bg)
            tvSelectedPhoto?.clearText()
        } else {
            tvSelectedPhoto?.setBackgroundResource(com.meera.core.R.drawable.circle_tab_white_stroke_bg)
            tvSelectedPhoto?.text = count.toString()
        }
    }

    private fun initToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(rootContainer) { _, windowInsets ->
            val statusBar = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            viewOverlayStatusBar?.updateLayoutParams<ConstraintLayout.LayoutParams> {
                height = statusBar
            }
            windowInsets
        }
    }

    private fun sendImageWithText() {
        isGoFurther = true

        val images: MutableList<Uri>? = imagesAdapter?.getSelectedImages()

        when (val needEditResponse = checkIsValidData(imagesAdapter?.getSelectedData())) {
            is MediaControllerNeedEditResponse.NoNeedToEdit -> {
                images?.let { imageList ->
                    if (imageList.isEmpty()) {
                        imageList.add(Uri.parse(imagesAdapter?.getImgUrlByPosition(imagesPager.currentItem)))
                    }
                    onImageReadyWithText(images, comment?.text?.toString() ?: String.empty())
                    close()
                } ?: kotlin.run {
                    onImageReadyWithText(mutableListOf(), comment?.text?.toString() ?: String.empty())
                    close()
                }
            }

            is MediaControllerNeedEditResponse.VideoTooLong -> {
                images?.let { imageList ->
                    if (imageList.isEmpty()) imageList.add(Uri.parse(imagesAdapter?.getImgUrlByPosition(imagesPager.currentItem)))
                    if (imageList.isNotEmpty()) {
                        act?.getMediaControllerFeature()?.showVideoTooLongDialog(
                            openPlace = MediaControllerOpenPlace.Chat,
                            needEditResponse = needEditResponse,
                            showInMinutes = true
                        ) {
                            openEditor()
                        }
                    }
                }
            }

            else -> error("Неподходящий тип $needEditResponse в данном месте ожидается только MediaEditorNewPostNeedEditUtil.Response.VideoTooLong")
        }
    }

    /**
     * Надо переделать верстку чтобы инпут и кнопка были завернуты в конте
     * */
    private fun initKeyboardHeightProvider() {
        rootContainer?.post {
            val inputContainerViewGroup = findViewById<View>(R.id.inputContainer)

            keyboardHeightProvider = rootContainer?.let { KeyboardHeightProvider(it) }
            keyboardHeightProvider?.observer = fun(keyboardHeight: Int) {
                if (keyboardHeight > 0) {
                    inputContainerViewGroup?.animate()
                        ?.translationY(-(keyboardHeight.toFloat()))
                        ?.setDuration(150)
                        ?.start()
                    setSuggestionsMenuExtraPeekHeight(keyboardHeight + inputContainerViewGroup.height)
                    this.isKeyboardOpen = true
                } else {
                    inputContainerViewGroup?.animate()
                        ?.translationY(0.0f)
                        ?.setDuration(150)
                        ?.start()
                    setSuggestionsMenuExtraPeekHeight(inputContainerViewGroup.height)
                    this.isKeyboardOpen = false
                }
            }
        }
    }

    private fun onCloseKeyboard(onClose: () -> Unit) {
        keyboardHeightProvider?.observer = { height ->
            if (height == 0) onClose.invoke()
        }
    }

    private fun setSuggestionsMenuExtraPeekHeight(extraPeekHeight: Int) {
        uniqueNameSuggestionMenu?.setExtraPeekHeight(extraPeekHeight, true)
    }

    private var uniqueNameSuggestionMenu: SuggestionsMenu? = null

    fun initUniqueNameSearch(uniqueNameSuggestionMenu: SuggestionsMenu?) {
        this.uniqueNameSuggestionMenu = uniqueNameSuggestionMenu

        comment?.setOnNewUniqueNameAfterTextChangedListener(object : MeeraEditTextExtended.OnNewUniqueNameListener {
            override fun onNewUniqueName(uniqueName: String) {
                searchUsersByUniqueName(uniqueName)
            }
        })

        comment?.setOnUniqueNameNotFoundListener(object : MeeraEditTextExtended.OnUniqueNameNotFoundListener {
            override fun onNotFound() {
                if (uniqueNameSuggestionMenu?.isHidden == false) {
                    uniqueNameSuggestionMenu.forceCloseMenu()
                }
            }
        })

        uniqueNameSuggestionMenuLayout?.setOnFocusChangeListener { v, hasFocus ->
        }

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

                    uniqueNameSuggestionMenu?.onSuggestedUniqueNameClicked = fun(userData: UITagEntity) {
                        replaceUniqueNameBySuggestion(userData)
                        uniqueNameSuggestionMenu?.forceCloseMenu()
                    }
                }
            }
        }
    }

    private var lastSearchUniqueName: String? = null

    private fun replaceUniqueNameBySuggestion(userData: UITagEntity?) {
        val uniqueName = userData?.uniqueName?.let { "@$it " }
        val commentText = comment?.text
        if (!commentText.isNullOrEmpty() && uniqueName != null) {
            lastSearchUniqueName?.let { nonNullLastSearchUniqueName ->
                commentText
                    .toString()
                    .replace(nonNullLastSearchUniqueName, uniqueName)
                    .also {
                        comment?.setText(it)
                    }

                commentText
                    .indexOf(uniqueName)
                    .let { it + uniqueName.length }
                    .also {
                        comment?.setSelection(it + 1)
                    }
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

    private fun checkIsValidData(images: MutableList<ImageViewerData>?): MediaControllerNeedEditResponse {
        if (images?.isEmpty() == true) {
            imagesAdapter?.getMediaByPosition(imagesPager.currentItem)?.let { images.add(it) }
        }

        images?.forEach {
            it.imageUrl?.let { nonNullImageUrl ->
                if (it.viewType == VIEW_TYPE_VIDEO_NOT_PLAYING) {
                    val needToEditResponse = act?.getMediaControllerFeature()
                        ?.needEditMedia(uri = Uri.parse(nonNullImageUrl), openPlace = MediaControllerOpenPlace.Chat)
                        ?: MediaControllerNeedEditResponse.NoNeedToEdit

                    if (needToEditResponse != MediaControllerNeedEditResponse.NoNeedToEdit) {
                        return needToEditResponse
                    }
                }
            }
        }
        return MediaControllerNeedEditResponse.NoNeedToEdit
    }

    private fun openEditor() {
        isGoFurther = true
        imagesAdapter?.getImgUrlByPosition(currentPosition)?.let {
            meeraAct?.getMediaControllerFeature()?.open(
                uri = Uri.parse(it),
                openPlace = MediaControllerOpenPlace.Chat,
                callback = object : MediaControllerCallback {
                    override fun onPhotoReady(
                        resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?
                    ) {
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
                    }

                    override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(
                            resultUri.toString(),
                            ""
                        )
                        res.viewType = VIEW_TYPE_VIDEO_NOT_PLAYING
                        res.isSelected = true
                        data.add(res)
                        data.addAll(images)
                        unselectAllMedia()
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        selectedMediaCount = 1
                    }

                    override fun onCanceled() {
                        val currentData = imagesAdapter?.getCurrentData(currentPosition)
                        currentData?.isSelected = false
                        selectedMediaCount--
                        imageChangeListener?.onImageChecked(currentData, false)
                    }

                    override fun onError() {
                        close()
                        Timber.e("MEDIA_PICKER_LOG ERROR picker in chat")
                    }
                })



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
                    }

                    override fun onVideoReady(resultUri: Uri, nmrAmplitude: NMRVideoAmplitude?) {
                        val data = mutableListOf<ImageViewerData>()
                        val res = ImageViewerData(
                            resultUri.toString(),
                            ""
                        )
                        res.viewType = VIEW_TYPE_VIDEO_NOT_PLAYING
                        res.isSelected = true
                        data.add(res)
                        data.addAll(images)
                        unselectAllMedia()
                        setImages(data, 0)
                        imageChangeListener?.onImageAdded(res)
                        selectedMediaCount = 1
                    }

                    override fun onError() {
                        close()
                    }

                    override fun onCanceled() {
                        val currentData = imagesAdapter?.getCurrentData(currentPosition)
                        currentData?.isSelected = false
                        selectedMediaCount--
                        imageChangeListener?.onImageChecked(currentData, false)
                    }
                })
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

    private fun handleClickCheckAction() {
        val currentData = imagesAdapter?.getCurrentData(currentPosition) ?: return

        if (!currentData.isSelected) {
            if (selectedMediaCount == maxMediaCount) {
                createAndShowTooltip(
                    messageString = context.getString(com.meera.core.R.string.may_only_pick_n_media, maxMediaCount),
                    showView = tvSelectedPhoto,
                    bubbleGravity = UiKitTooltipBubbleMode.RIGHT_TOP,
                    showAbove = false
                )
                return
            }

            if (currentData.viewType == RecyclingPagerAdapter.VIEW_TYPE_VIDEO_NOT_PLAYING) {
                val strUriToCheck = currentData.imageUrl?.let { Uri.parse(it) }
                this.lifecycle?.doAsync({
                    strUriToCheck.let { uri ->
                        val time = filesUtil.getVideoDurationMils(uri)
                        if (time > maxVideoLengthInSeconds * 1000) {
                            return@doAsync uri
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
            currentData.isSelected = false
            selectedMediaCount--
            currentData.cnt = selectedMediaCount
            showCountNumber()
            imageChangeListener?.onImageChecked(currentData, false)
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
                        duration = TOOLTIP_DURATION,
                        offsetY = TOOLTIP_ABOVE_OFFSET_Y.dp
                    )
                } else {
                    tooltip.showUniversal(
                        view = it,
                        duration = TOOLTIP_DURATION,
                        offsetX = TOOLTIP_OFFSET_X.dp,
                        offsetY = TOOLTIP_OFFSET_Y.dp
                    )
                }
            }
        }
    }

    private fun getTotalSelectedMediaCount() = getPreselectedMediaCount().plus(selectedMediaCount)

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val overVi = rootContainer.dispatchTouchEvent(event)//overlayView.dispatchTouchEvent(event)
        if (!overVi) {
            return true
        }

        Timber.d("MEERA onDispatchTouchEvent mediaViewerPreviewChat")
        if (!this::transitionImageAnimator.isInitialized || transitionImageAnimator.isAnimating) {
            return true
        }

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

        imagesAdapter?.onVideoClicked = { media ->
            isGoFurther = true
            openVideo(media)
        }
        showCountNumber()
    }

    private fun openVideo(video: ImageViewerData) {
        val imageList = mutableListOf<ImageViewerData>()
        imageList.add(ImageViewerData(video.imageUrl, viewType = VIEW_TYPE_VIDEO))
        lifecycle?.let { life ->
            screenListener?.let { orientation ->
                if (imageList.isNotEmpty()) {
                    MediaViewer.with(context)
                        .setImageList(imageList)
                        .startPosition(0)
                        .setOrientationChangedListener(orientation)
                        .setAct(act)
                        .setLifeCycle(life)
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
            .load(images[startPosition].imageUrl)
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
        if (userName == String.empty()){
            if (images.size > 1) {
                navBar?.title =
                    "${position + 1} ${context.getString(R.string.of)} ${images.size}"
            } else {
                navBar?.title = String.empty()
            }
        } else {
            navBar?.title = userName
        }
    }

    override fun resetScale() {
        imagesAdapter?.resetScale(currentPosition)
    }

    private fun animateOpen() {
        transitionImageAnimator.animateOpen(
            onTransitionStart = { duration ->
                backgroundView.animateAlpha(0f, 1f, duration)
                rootContainer.animateAlpha(0f, 1f, duration)
            },
            onTransitionEnd = {
                isOpenCompleted = true
                imagesPager.visible()
                if (isImageReady) prepareViewsForViewer()
            })
    }

    private fun animateOpenWithoutTransition() {
        backgroundView.animateAlpha(0f, 1f, 300)
        rootContainer.animateAlpha(0f, 1f, 300)
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
                rootContainer.animateAlpha(rootContainer.alpha, 0f, duration)
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

    private fun getPreselectedMediaCount() = onGetPreselectedMedia?.invoke()?.count() ?: 0

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
        rootContainer.alpha = alpha
    }

    private fun dispatchOverlayTouch(event: MotionEvent): Boolean =
        rootContainer
            .let { it.visibility == View.VISIBLE && it.dispatchTouchEvent(event) }

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

    fun setLifeCycle(lifecycle: Lifecycle?) {
        this.lifecycle = lifecycle
    }
}
