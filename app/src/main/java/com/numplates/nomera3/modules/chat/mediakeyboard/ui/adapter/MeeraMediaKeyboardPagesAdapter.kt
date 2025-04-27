package com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter

import android.hardware.camera2.CameraCharacteristics
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.meera.core.extensions.isTrue
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.fragment.MeeraMediakeyboardFavoritesFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.gif.ui.fragment.MeeraSearchGifFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.MeeraPickerFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.fragment.MeeraMediaKeyboardRecentsFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.fragment.MeeraMediaKeyboardStickersFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.BottomSheetSlideOffsetListener
import com.numplates.nomera3.modules.gifservice.ui.GifMenuState
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomPicker

class MeeraMediaKeyboardPagesAdapter(
    private val state: GifMenuState,
    private val fragment: Fragment,
    private val pickerCallback: OnImagesReady,
    private val openFrom: MediaControllerOpenPlace
) : FragmentStateAdapter(fragment.childFragmentManager, fragment.lifecycle) {

    private val needShowWidgets = openFrom == MediaControllerOpenPlace.Moments
    private val fragments
        get() = fragment.childFragmentManager.fragments

    override fun getItemCount(): Int {
        return when (state) {
            GifMenuState.DEFAULT -> 5
            GifMenuState.CREATE_MOMENT -> 3
            else -> 1
        }
    }

    override fun createFragment(position: Int): Fragment {
        return when (state) {
            GifMenuState.DEFAULT -> when (position) {
                0 -> MeeraMediaKeyboardRecentsFragment()
                1 -> MeeraMediakeyboardFavoritesFragment()
                2 -> getPickerFragment()
                3 -> MeeraSearchGifFragment()
                4 -> MeeraMediaKeyboardStickersFragment()
                else -> Fragment()
            }

            GifMenuState.CREATE_MOMENT -> when (position) {
                0 -> MeeraMediakeyboardFavoritesFragment().apply {
                    arguments = bundleOf(NEED_SHOW_WIDGETS_ARG to needShowWidgets)
                }

                1 -> getPickerFragment()
                2 -> MeeraMediaKeyboardStickersFragment().apply {
                    arguments = bundleOf(NEED_SHOW_WIDGETS_ARG to needShowWidgets)
                }

                else -> Fragment()
            }

            else -> getPickerFragment()
        }
    }

    private fun getPickerFragment(): Fragment {
        val fragment = MeeraPickerFragment()
        fragment.builder = if (needShowWidgets) {
            TedBottomPicker.with(this.fragment.activity)
                .setWithPreview(openFrom)
                .showGalleryTile(false)
                .showTitle(false)
                .setPreviewMaxCount(Integer.MAX_VALUE)
                .setCameraLensFacing(CameraCharacteristics.LENS_FACING_BACK)
                .setOnImageSelectedListener(pickerCallback::onReady)
        } else {
            TedBottomPicker.with(this.fragment.activity)
                .setWithPreview(openFrom)
                .showGalleryTile(false)
                .showTitle(false)
                .setPreviewMaxCount(Integer.MAX_VALUE)
                .setDialogDismissListener(pickerCallback::onDismiss)
                .setOnImageReadyWithText(pickerCallback::onReadyWithText)
                .setOnRequestChangeBottomSheetState(pickerCallback::onRequestChangeState)
                .showImageAndVideoMedia()
                .setCameraLensFacing(CameraCharacteristics.LENS_FACING_BACK)
                .setOnMultiImageSelectedListener(pickerCallback::onReady)
        }
        return fragment
    }

    fun refreshRecents() {
        fragments.filterIsInstance<MeeraMediaKeyboardRecentsFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.refreshRecents()
            }
        }
    }

    fun onBottomSheetStateChange(state: Int) {
        fragments.filterIsInstance<MeeraPickerFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.onBottomSheetStateChange(state)
            }
        }
    }

    fun onStartAnimationTransitionFragment() {
        fragments.filterIsInstance<MeeraPickerFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.onStartAnimationTransitionFragment()
            }
        }
    }

    fun onOpenTransitionFragment() {
        fragments.filterIsInstance<MeeraPickerFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.onOpenTransitionFragment()
            }
        }
    }

    @Suppress("LocalVariableName")
    fun onBottomSheetSlide(slideOffset: Float) {
        fragments.filterIsInstance<BottomSheetSlideOffsetListener>().forEach { _fragment ->
            val fragment = _fragment as? Fragment
            if (fragment?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED).isTrue()) {
                _fragment.onBottomSheetSlide(slideOffset)
            }
        }
    }

    fun scrollStickersToWidgets() {
        fragments.filterIsInstance<MeeraMediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.tryToScrollToWidgets()
            }
        }
    }

    fun scrollStickersToRecent() {
        fragments.filterIsInstance<MeeraMediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.tryToScrollToRecentStickers()
            }
        }
    }

    fun scrollStickersToPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        fragments.filterIsInstance<MeeraMediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                fragment.tryToScrollToStickerPackById(stickerPack.id)
            }
        }
    }
}
