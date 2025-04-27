package com.numplates.nomera3.modules.gifservice.ui

import android.graphics.Point
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.pxToDp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.FragmentChatBinding
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.domain.interactornew.SetKeyboardHeightUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardDefaultBlockProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardHowProperty
import com.numplates.nomera3.modules.chat.ChatAnalyticDelegate
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.gifservice.ui.entity.GifMenuCallbackEvents
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import javax.inject.Inject

private const val GIF_MENU_VISIBILITY_DELAY = 200L
const val GIF_MENU_UI_REACTION_DELAY = 300L
private const val MENU_SHADOW_VISIBLE_SLIDE_OFFSET = 0.16
private const val KEYBOARD_VISIBLE_SLIDE_OFFSET = 0.8
private const val MESSAGE_CONTAINER_HEIGHT = 60


class GiphyChatMenuDelegateUI(
        private val fragment: Fragment,
        private val binding: FragmentChatBinding?,
        private val keyboardHeightProvider: KeyboardHeightProvider,
        private val defaultKeyboardHeight: Int,
        private val menuEvents: (GifMenuDelegateEvents) -> Unit = { },
        private val imagesCallback: OnImagesReady
) {

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

    private var mediaKeyboard: MediaKeyboard? = null
    private var isGifDisplayed = false
    private var isShowKeyboardWhenGifMenuClosed = true
    private var isKeyboardVisible = false

    private var currentState = BottomSheetBehavior.STATE_HIDDEN

    private val lifecycle
            get() = fragment.viewLifecycleOwner

    init {
        App.component.inject(this)
        binding?.mediakeyboard?.also { menuBinding ->
            val bottomSheetBehavior: ViewPagerBottomSheetBehavior<View> = menuBinding.let {
                lifecycle.doDelayed(GIF_MENU_VISIBILITY_DELAY) {
                    it.root.visible()
                }
                ViewPagerBottomSheetBehavior.from(it.root)
            }

            bottomSheetBehavior.peekHeight = pxToDp(defaultKeyboardHeight).dp
            mediaKeyboard?.setKeyboardHeight(defaultKeyboardHeight)

            bottomSheetBehavior.addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_HIDDEN -> {
                            currentState = BottomSheetBehavior.STATE_HIDDEN
                            lowerDownBottomBorder()
                            if (isShowKeyboardWhenGifMenuClosed) {
                                menuEvents(GifMenuDelegateEvents.OnShowSoftwareKeyboard)
                            }
                            isShowKeyboardWhenGifMenuClosed = true
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            currentState = BottomSheetBehavior.STATE_COLLAPSED
                            if (isKeyboardVisible) {
                                menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
                            }
                            menuEvents(GifMenuDelegateEvents.OnDialogStateCollapsed)
                        }
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            currentState = BottomSheetBehavior.STATE_EXPANDED
                            menuEvents(GifMenuDelegateEvents.OnDialogExpanded)
                        }
                    }
                    handleMenuShadowMargin(newState)
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (slideOffset > MENU_SHADOW_VISIBLE_SLIDE_OFFSET) {
                        binding.menuShadow.visible()
                    } else {
                        binding.menuShadow.gone()
                    }

                    val isExpanded = currentState == BottomSheetBehavior.STATE_EXPANDED
                    if (slideOffset > 0
                            && isExpanded
                            && slideOffset < KEYBOARD_VISIBLE_SLIDE_OFFSET
                            && isKeyboardVisible) {
                        menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
                        isKeyboardVisible = false
                    }
                }
            })

            initKeyboardObserver()

            mediaKeyboard = MediaKeyboard(
                fragment = fragment,
                bottomSheetBehavior = bottomSheetBehavior,
                rootView = binding.mediakeyboard.root,
                interactionCallback = ::handleGifMenuEvents,
                imagesCallback = imagesCallback,
                openFrom = MediaControllerOpenPlace.Chat,
                amplitudeMediaKeyboardAnalytic = amplitudeMediaKeyboardAnalytic
            )

            initMenuShadow()

            handleClickGifMenu()
        }
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

    fun initKeyboardObserver() {
        if (keyboardHeightProvider.observer == null) {
            keyboardHeightProvider.observer = { height ->
                saveKeyboardHeight(height)
                pushKeyboardHeight(height)
                handleGifMenuIcon(height)
                handleGifMenuAppearance(height)
            }
        }
    }

    fun getBehavior() = currentState

    private fun saveKeyboardHeight(height: Int) {
        if (height > 0) {
            val oldKeyboardHeight = getKeyboardHeightUseCase.invoke()
            if (height <= oldKeyboardHeight) return
            setKeyboardHeightUseCase.invoke(height)
        }
    }

    private fun pushKeyboardHeight(height: Int) {
        menuEvents(GifMenuDelegateEvents.OnKeyboardHeightChanged(height))
    }

    private fun handleGifMenuIcon(keyboardHeight: Int) {
        if (keyboardHeight > 0 && !isGifDisplayed) {
            menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(mediaKeyboard?.stickerPacks?.any { !it.viewed } == true))
        }
    }

    private fun handleGifMenuAppearance(keyboardHeight: Int) {
        if (keyboardHeight == 0) {
            isKeyboardVisible = false
            if (isGifDisplayed) {
                lifecycle.doDelayed(GIF_MENU_UI_REACTION_DELAY) {
                    raiseBottomBorder()
                }
            }
        } else {
            lowerDownBottomBorder()
            isKeyboardVisible = true
        }
    }

    private fun handleGifMenuEvents(event: GifMenuCallbackEvents) {
        when (event) {
            is GifMenuCallbackEvents.OnHideKeyboard ->
                menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
            is GifMenuCallbackEvents.OnStickerPackViewed -> {
                menuEvents.invoke(GifMenuDelegateEvents.OnStickerPackViewed(event.stickerPack))
            }
        }
    }

    private fun initMenuShadow() {
        setMenuShadowMarginDefault()
        binding?.menuShadow?.click {
            mediaKeyboard?.collapseMenuWhenHeaderClick()
        }
    }

    private fun handleMenuShadowMargin(state: Int) {
        if (state == BottomSheetBehavior.STATE_EXPANDED) {
            setMenuShadowMarginWithKeyboard()
        } else {
            setMenuShadowMarginDefault()
        }
    }

    private fun setMenuShadowMarginDefault() {
        val shadowBottomOffset = pxToDp(defaultKeyboardHeight).dp + MESSAGE_CONTAINER_HEIGHT.dp
        binding?.menuShadow?.setMargins(bottom = shadowBottomOffset)
    }

    private fun setMenuShadowMarginWithKeyboard() {
        val mediaKeyboardHeight = binding?.mediakeyboard?.root?.height ?: return
        val keyboardHeight = getKeyboardHeightUseCase.invoke()
        val menuShadowMargin = mediaKeyboardHeight - keyboardHeight
        binding.menuShadow.setMargins(bottom = menuShadowMargin)
    }

    private fun handleClickGifMenu() {
        binding?.sendMessageContainer?.lavAddToFavorites?.click {
            isGifDisplayed = true
            menuEvents(GifMenuDelegateEvents.OnDisplayKeyboardIcon)
            menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)

            lifecycle.doDelayed(GIF_MENU_VISIBILITY_DELAY) {
                lowerDownBottomBorder()
                raiseBottomBorder()
                mediaKeyboard?.showMenuIfNotExpanded(POSITION_PICKER)
                amplitudeMediaKeyboardAnalytic.logMediaPanelOpen(
                    how = AmplitudeMediaKeyboardHowProperty.BUTTON_ADD_MEDIA,
                    defaultBlock = AmplitudeMediaKeyboardDefaultBlockProperty.MEDIA_BLOCK
                )
            }
        }
        binding?.sendMessageContainer?.btnGifUpload?.click {
            if (isGifDisplayed) {
                isGifDisplayed = false
                menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(mediaKeyboard?.stickerPacks?.any { !it.viewed } == true))
                lowerDownBottomBorder()
                mediaKeyboard?.dismissMenu()
                binding.sendMessageContainer.etWrite.requestFocus()
                amplitudeMediaKeyboardAnalytic.logMediaPanelClose(
                    how = AmplitudeMediaKeyboardHowProperty.ICON
                )
            } else {
                isGifDisplayed = true
                menuEvents(GifMenuDelegateEvents.OnDisplayKeyboardIcon)
                menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)

                lifecycle.doDelayed(GIF_MENU_VISIBILITY_DELAY) {
                    lowerDownBottomBorder()
                    raiseBottomBorder()
                    mediaKeyboard?.showMenuIfNotExpanded(POSITION_STICKERS)
                }
                amplitudeMediaKeyboardAnalytic.logMediaPanelOpen(
                    how = AmplitudeMediaKeyboardHowProperty.BUTTON_MEDIA_KEYBOARD,
                    defaultBlock = AmplitudeMediaKeyboardDefaultBlockProperty.GIF
                )
                chatAnalyticDelegate.logChatGifButtonPress()
            }
        }
    }

    fun hideGifMenuWhenBackPressed() = if (isGifDisplayed) {
        lowerDownBottomBorder()
        mediaKeyboard?.dismissMenu()
        isGifDisplayed = false
        isShowKeyboardWhenGifMenuClosed = false
        menuEvents(GifMenuDelegateEvents.OnDisplayGifMenuIcon(mediaKeyboard?.stickerPacks?.any { !it.viewed } == true))
        true
    } else {
        menuEvents(GifMenuDelegateEvents.OnHideSoftwareKeyboard)
        false
    }

    fun dismissGifMenuWhenKeyboardAppear() {
        if (isGifDisplayed) {
            lowerDownBottomBorder()
            mediaKeyboard?.dismissMenu()
            isShowKeyboardWhenGifMenuClosed = false
            isGifDisplayed = false
        }
    }

    private fun raiseBottomBorder() {
        setContentMarginBottom(defaultKeyboardHeight)
    }

    private fun lowerDownBottomBorder() {
        setContentMarginBottom(0)
    }

    private fun setContentMarginBottom(marginBottomPx: Int) {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0 ,0,0, marginBottomPx)
        binding?.vgContent?.layoutParams = params
    }

}
