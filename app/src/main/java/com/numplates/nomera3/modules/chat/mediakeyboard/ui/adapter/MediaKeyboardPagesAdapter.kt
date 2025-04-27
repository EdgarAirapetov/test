package com.numplates.nomera3.modules.chat.mediakeyboard.ui.adapter

import android.hardware.camera2.CameraCharacteristics
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.fragment.MediakeyboardFavoritesFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.gif.ui.fragment.SearchGifFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.picker.ui.fragment.PickerFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.fragment.MediaKeyboardRecentsFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.fragment.MediaKeyboardStickersFragment
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.BottomSheetSlideOffsetListener
import com.numplates.nomera3.modules.gifservice.ui.GifMenuState
import com.numplates.nomera3.presentation.view.utils.tedbottompicker.TedBottomPicker

const val NEED_SHOW_WIDGETS_ARG = "NEED_SHOW_WIDGETS_ARG"

class MediaKeyboardPagesAdapter(
    state: GifMenuState,
    private val fragment: Fragment,
    private val pickerCallback: OnImagesReady,
    private val openFrom: MediaControllerOpenPlace
) : FragmentStateAdapter(fragment) {

    private val targetState: Lifecycle.State = Lifecycle.State.STARTED
    private val needShowWidgets = openFrom == MediaControllerOpenPlace.Moments

    private val PICKER_FRAGMENT: PickerFragment
        get() {
            val fragment = PickerFragment()
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
                    .setOnImageSelectedListener(pickerCallback::onReady)
                    .setOnMultiImageSelectedListener(pickerCallback::onReady)
            }

            return fragment
        }

    private val items = when (state) {
        GifMenuState.DEFAULT -> listOf(
            MediaKeyboardRecentsFragment(),
            MediakeyboardFavoritesFragment(),
            PICKER_FRAGMENT,
            SearchGifFragment(),
            MediaKeyboardStickersFragment()
        )

        GifMenuState.CREATE_MOMENT -> listOf(
            MediakeyboardFavoritesFragment().apply {
                arguments = bundleOf(NEED_SHOW_WIDGETS_ARG to needShowWidgets)
            },
            PICKER_FRAGMENT,
            MediaKeyboardStickersFragment().apply {
                arguments = bundleOf(NEED_SHOW_WIDGETS_ARG to needShowWidgets)
            }
        )

        GifMenuState.MESSAGE_EDITING -> listOf(PICKER_FRAGMENT)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return items[position]
    }

    fun refreshRecents() {
        items.filterIsInstance<MediaKeyboardRecentsFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.refreshRecents()
            }
        }
    }

    fun onStartAnimationTransitionFragment() {
        items.forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.onStartAnimationTransitionFragment()
            }
        }
    }

    fun onOpenTransitionFragment() {
        items.forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.onOpenTransitionFragment()
            }
        }
    }

    @Suppress("LocalVariableName")
    fun onBottomSheetSlide(slideOffset: Float) {
        items.filterIsInstance<BottomSheetSlideOffsetListener>().forEach { _fragment ->
            val fragment = _fragment as? Fragment
            if (fragment?.lifecycle?.currentState == targetState) {
                _fragment.onBottomSheetSlide(slideOffset)
            }
        }
    }

    fun scrollStickersToWidgets() {
        items.filterIsInstance<MediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.tryToScrollToWidgets()
            }
        }
    }

    fun scrollStickersToRecent() {
        items.filterIsInstance<MediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.tryToScrollToRecentStickers()
            }
        }
    }

    fun scrollStickersToPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        items.filterIsInstance<MediaKeyboardStickersFragment>().forEach { fragment ->
            if (fragment.lifecycle.currentState == targetState) {
                fragment.tryToScrollToStickerPackById(stickerPack.id)
            }
        }
    }
}
