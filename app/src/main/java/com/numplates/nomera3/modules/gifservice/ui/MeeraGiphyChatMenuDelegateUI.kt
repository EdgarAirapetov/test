package com.numplates.nomera3.modules.gifservice.ui

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.meera.core.extensions.displayHeight
import com.meera.core.extensions.dp
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.bottomsheet.UiKitViewPagerBottomSheetBehavior
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.MeeraChatFragmentBinding
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.domain.interactornew.SetKeyboardHeightUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardDefaultBlockProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardHowProperty
import com.numplates.nomera3.modules.chat.ChatAnalyticDelegate
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.gifservice.ui.entity.GifMenuCallbackEvents
import com.numplates.nomera3.modules.gifservice.ui.entity.MediaKeyboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

private const val MENU_SHADOW_VISIBLE_SLIDE_OFFSET = 0.16
private const val KEYBOARD_VISIBLE_SLIDE_OFFSET = 0.8
private const val KEYBOARD_DRAGGABLE_HEIGHT = 20
private const val CONTAINER_TOP_SPACE = 52

class MeeraGiphyChatMenuDelegateUI(
    private val fragment: Fragment,
    private val binding: MeeraChatFragmentBinding,
    private val keyboardHeightProvider: KeyboardHeightProvider,
    private val menuEvents: (GifMenuDelegateEvents) -> Unit = { },
    private val imagesCallback: OnImagesReady
) : DefaultLifecycleObserver {

    @Inject
    lateinit var amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic

    @Inject
    lateinit var chatAnalyticDelegate: ChatAnalyticDelegate

    @Inject
    lateinit var getKeyboardHeightUseCase: GetKeyboardHeightUseCase

    @Inject
    lateinit var setKeyboardHeightUseCase: SetKeyboardHeightUseCase

    val mediaKeyboardFavoritesTabPosition: Point?
        get() = mediaKeyboard?.favoritesTabPosition

    var mediaKeyboard: MeeraMediaKeyboard? = null
        private set

    private var peekSoftwareKeyboardHeight: Int = 0
    private var cachedSoftInputMode: Int = -1
    private var currentState = BottomSheetBehavior.STATE_HIDDEN

    private val mediaKeyboardStateFlow = MutableStateFlow(MediaKeyboardState())
    private val bottomSheetBehavior: BottomSheetBehavior<View> = BottomSheetBehavior.from(binding.mediakeyboard.root)

    private val mediaSheetPrimaryCallback by lazy { primaryBottomSheetCallback() }
    private val mediaSheetKeyboardCallback by lazy { keyboardBottomSheetCallback() }

    private val window
        get() = fragment.requireActivity().window

    private val lifecycleOwner
        get() = fragment.viewLifecycleOwner

    init {
        App.component.inject(this)
        lifecycleOwner.lifecycle.addObserver(this)
        initBottomSheetMenu()
        initScreenAdjustmentFlow()
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        keyboardHeightProvider.start()
        cachedSoftInputMode = window.attributes.softInputMode
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        bottomSheetBehavior.addBottomSheetCallback(mediaSheetPrimaryCallback)
        bottomSheetBehavior.addBottomSheetCallback(mediaSheetKeyboardCallback)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        keyboardHeightProvider.release()
        window.setSoftInputMode(cachedSoftInputMode)
        bottomSheetBehavior.removeBottomSheetCallback(mediaSheetPrimaryCallback)
        bottomSheetBehavior.removeBottomSheetCallback(mediaSheetKeyboardCallback)
    }

    fun enableViewPagerScrolling(enabled: Boolean) {
        mediaKeyboard?.enableViewPagerScrolling(enabled)
    }

    fun changeMenuState(state: GifMenuState) {
        mediaKeyboard?.switchState(state)
    }

    fun onStartAnimationTransitionFragment() {
        mediaKeyboard?.onStartAnimationTransitionFragment()
    }

    fun onOpenTransitionFragment() {
        mediaKeyboard?.onOpenTransitionFragment()
    }

    fun scrolledToNewStickerPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        mediaKeyboard?.setCurrentChosenStickerPack(stickerPack)
    }

    fun scrolledToRecentStickers() {
        mediaKeyboard?.setRecentStickersChosen()
    }

    fun setStickerPacks(
        stickerPacks: List<MediaKeyboardStickerPackUiModel>,
        recentStickers: List<MediakeyboardFavoriteRecentUiModel>
    ) {
        mediaKeyboard?.setStickerPacks(stickerPacks, recentStickers)
    }

    fun showAddToFavoritesAnimation() {
        mediaKeyboard?.showAddToFavoritesAnimation()
    }

    fun collapseMenu() {
        if (mediaKeyboard?.isHidden == false) {
            mediaKeyboard?.collapseMenuWhenHeaderClick()
        }
    }

    fun openMediaKeyboardFullScreen() {
        mediaKeyboard?.expandMenuFullScreen()
    }

    private fun initBottomSheetMenu() {
        bottomSheetBehavior.maxHeight = fragment.requireContext().displayHeight - CONTAINER_TOP_SPACE.dp

        setPeekKeyboardHeight(getKeyboardHeightUseCase.invoke())
        initKeyboardObserver()

        mediaKeyboard = MeeraMediaKeyboard(
            fragment = fragment,
            bottomSheetBehavior = bottomSheetBehavior,
            rootView = binding.mediakeyboard.root,
            openFrom = MediaControllerOpenPlace.Chat,
            interactionCallback = ::handleGifMenuEvents,
            imagesCallback = imagesCallback,
            amplitudeMediaKeyboardAnalytic = amplitudeMediaKeyboardAnalytic
        )

        initClickListeners()
    }

    private fun initKeyboardObserver() {
        if (keyboardHeightProvider.observer == null) {
            keyboardHeightProvider.observer = { height ->
                saveKeyboardHeight(height)
                pushKeyboardHeight(height)
                handleGifMenuIcon(height)
                pushKeyboardHeightToState(height)
                setPeekKeyboardHeight(height)
            }
        }
    }

    private fun initScreenAdjustmentFlow() {
        mediaKeyboardStateFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { state ->
                val targetHeight = when {
                    state.isHiddenKeyboards() -> 0
                    else -> peekSoftwareKeyboardHeight
                }
                setContentSpaceBottom(targetHeight)
            }
            .launchIn(lifecycleOwner.lifecycleScope)
    }

    fun getBehavior() = currentState

    private fun saveKeyboardHeight(height: Int) {
        if (height > 0) {
            val oldKeyboardHeight = getKeyboardHeightUseCase.invoke()
            if (height <= oldKeyboardHeight) return
            setKeyboardHeightUseCase.invoke(height)
        }
    }

    private fun setPeekKeyboardHeight(height: Int) {
        if (height > 0 && height != peekSoftwareKeyboardHeight) {
            peekSoftwareKeyboardHeight = height
            bottomSheetBehavior.peekHeight = height + KEYBOARD_DRAGGABLE_HEIGHT.dp
        }
    }

    private fun pushKeyboardHeight(height: Int) {
        menuEvents(GifMenuDelegateEvents.OnKeyboardHeightChanged(height))
    }

    private fun handleGifMenuIcon(keyboardHeight: Int) {
        if (keyboardHeight > 0) {
            val anyNewStickerPack = mediaKeyboard?.stickerPacks?.any { !it.viewed } == true
            menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(anyNewStickerPack))
        }
    }

    private fun pushKeyboardHeightToState(height: Int) {
        mediaKeyboardStateFlow.value = mediaKeyboardStateFlow.value.copy(
            isSoftwareKeyboardOpened = height > 0,
            isChangingKeyboard = false
        )
    }

    private fun pushKeyboardChanging(isChanging: Boolean) {
        mediaKeyboardStateFlow.value = mediaKeyboardStateFlow.value.copy(
            isChangingKeyboard = isChanging
        )
    }

    private fun handleGifMenuEvents(event: GifMenuCallbackEvents) {
        when (event) {
            is GifMenuCallbackEvents.OnHideKeyboard -> {
                menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
            }

            is GifMenuCallbackEvents.OnStickerPackViewed -> {
                menuEvents.invoke(GifMenuDelegateEvents.OnStickerPackViewed(event.stickerPack))
            }
        }
    }

    // Check if we can remove it
    private fun openMediaKeyboard(newBehavior: Int? = null, startPosition: Int) {
        switchToMediaKeyboard(startPosition)
        if (newBehavior != null) {
            binding.mediakeyboard.also { menuBinding ->
                val bottomSheetBehavior: BottomSheetBehavior<View> = menuBinding.let {
                    BottomSheetBehavior.from(it.root)
                }
                bottomSheetBehavior.state = newBehavior
            }
        }
    }

    fun openMediaKeyboardWithBehavior(behavior: Int, startPosition: Int) {
        if (behavior == BottomSheetBehavior.STATE_HIDDEN) return
        openMediaKeyboard(behavior, startPosition)
    }

    private fun switchToMediaKeyboard(position: Int? = null) {
        mediaKeyboard?.showMenuIfNotExpanded(position)
        menuEvents(GifMenuDelegateEvents.OnDisplayKeyboardIcon)
        menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
        pushKeyboardChanging(isChanging = true)
        amplitudeMediaKeyboardAnalytic.logMediaPanelOpen(
            how = AmplitudeMediaKeyboardHowProperty.BUTTON_MEDIA_KEYBOARD,
            defaultBlock = AmplitudeMediaKeyboardDefaultBlockProperty.GIF
        )
    }

    private fun switchToSoftwareKeyboard() {
        binding.sendMessageContainer.etWrite.requestFocus()
        menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(mediaKeyboard?.stickerPacks?.any { !it.viewed } == true))
        menuEvents(GifMenuDelegateEvents.OnShowSoftwareKeyboard)
        pushKeyboardChanging(isChanging = true)
        amplitudeMediaKeyboardAnalytic.logMediaPanelClose(
            how = AmplitudeMediaKeyboardHowProperty.ICON
        )
    }

    private fun initClickListeners() {
        binding.menuShadow.setThrottledClickListener {
            mediaKeyboard?.collapseMenuWhenHeaderClick()
        }
        binding.sendMessageContainer.btnMediaFiles.setThrottledClickListener {
            switchToMediaKeyboard(position = POSITION_PICKER)
        }
        binding.sendMessageContainer.btnGifUpload.setThrottledClickListener {
            if (mediaKeyboard?.isVisible == false ||
                (mediaKeyboard?.isVisible == true && keyboardHeightProvider.isOpened())
            ) {
                switchToMediaKeyboard(position = POSITION_STICKERS)
                chatAnalyticDelegate.logChatGifButtonPress()
            } else if (!keyboardHeightProvider.isOpened()) {
                switchToSoftwareKeyboard()
            }
        }
    }

    private fun primaryBottomSheetCallback(): BottomSheetCallback {
        return object : BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        currentState = BottomSheetBehavior.STATE_HIDDEN
                        mediaKeyboard?.onBottomSheetStateChange(UiKitViewPagerBottomSheetBehavior.STATE_HIDDEN)
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        currentState = BottomSheetBehavior.STATE_COLLAPSED
                        menuEvents(GifMenuDelegateEvents.OnDialogStateCollapsed)
                        mediaKeyboard?.onBottomSheetStateChange(UiKitViewPagerBottomSheetBehavior.STATE_COLLAPSED)
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        currentState = BottomSheetBehavior.STATE_EXPANDED
                        menuEvents(GifMenuDelegateEvents.OnDialogExpanded)
                        mediaKeyboard?.onBottomSheetStateChange(UiKitViewPagerBottomSheetBehavior.STATE_EXPANDED)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val isSlidingUp = slideOffset > MENU_SHADOW_VISIBLE_SLIDE_OFFSET
                binding.menuShadow.isVisible = isSlidingUp
                mediaKeyboard?.switchVisibilityDialogHeader(isSlidingUp)

                val isExpanded = currentState == BottomSheetBehavior.STATE_EXPANDED
                if (slideOffset > 0
                    && isExpanded
                    && slideOffset < KEYBOARD_VISIBLE_SLIDE_OFFSET
                    && keyboardHeightProvider.isOpened()
                ) {
                    menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
                }
            }
        }
    }

    private fun keyboardBottomSheetCallback(): BottomSheetCallback {
        return object : BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                mediaKeyboardStateFlow.value = mediaKeyboardStateFlow.value.copy(
                    isInAppMediaSheetOpened = newState != BottomSheetBehavior.STATE_HIDDEN,
                    isChangingKeyboard = newState == BottomSheetBehavior.STATE_DRAGGING
                        || newState == BottomSheetBehavior.STATE_SETTLING
                )
            }
        }
    }

    fun hideAllOpenedKeyboards(): Boolean {
        var hasChanges = false
        if (mediaKeyboard?.isVisible == true) {
            mediaKeyboard?.dismissMenu()
            hasChanges = true
        }
        if (keyboardHeightProvider.isOpened()) {
            menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
            hasChanges = true
        }
        if (hasChanges) {
            menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(mediaKeyboard?.stickerPacks?.any { !it.viewed } == true))
        }
        return hasChanges
    }

    private fun setContentSpaceBottom(bottomSpace: Int) {
        binding.vgContent.setPaddingBottom(bottomSpace)
        binding.vgContent.requestLayout()
    }
}
