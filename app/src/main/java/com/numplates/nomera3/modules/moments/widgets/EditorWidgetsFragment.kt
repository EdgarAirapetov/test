package com.numplates.nomera3.modules.moments.widgets

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.utils.tedbottompicker.compat.OnImagesReady
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.presentation.BROADCAST_GIF_EXTRA
import com.meera.media_controller_implementation.presentation.BROADCAST_IMAGE_EXTRA
import com.meera.media_controller_implementation.presentation.BROADCAST_MEDIA_ACTION
import com.meera.media_controller_implementation.presentation.BROADCAST_MEDIA_DATA
import com.meera.media_controller_implementation.presentation.BROADCAST_STICKER_EXTRA
import com.meera.media_controller_implementation.presentation.BROADCAST_WIDGET_EXTRA
import com.noomeera.nmrmediatools.NMRStoryGifObject
import com.noomeera.nmrmediatools.NMRStoryStickerObject
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.EditorWidgetsFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.chat.MediaKeyboardCallback
import com.numplates.nomera3.modules.chat.helpers.ChatMenuDelegate
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardWidget
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.gifservice.ui.MediaKeyboard
import com.numplates.nomera3.modules.gifservice.ui.MediaKeyboardPagesPosition
import com.numplates.nomera3.modules.gifservice.ui.entity.GifMenuCallbackEvents
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior.STATE_EXPANDED
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior.STATE_HIDDEN
import com.numplates.nomera3.modules.maps.ui.snippet.view.ViewPagerBottomSheetBehavior.STATE_SETTLING
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMediaKeyboardEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class EditorWidgetsFragment :
    BaseFragmentNew<EditorWidgetsFragmentBinding>(),
    MediaKeyboardCallback,
    MeeraMenuBottomSheet.Listener {

    @Inject
    lateinit var amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> EditorWidgetsFragmentBinding
        get() = EditorWidgetsFragmentBinding::inflate

    private val viewModel by viewModels<EditorWidgetsViewModel> { App.component.getViewModelFactory() }

    private var mediaKeyboard: MediaKeyboard? = null
    private var chatMenuDelegate: ChatMenuDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMediaKeyboard()
        initChatMenuDelegate()
        observeEvent()
        observeStickers()

        viewModel.loadStickers()
    }

    private fun initMediaKeyboard() {
        val binding = binding ?: return
        val bottomSheetBehavior = ViewPagerBottomSheetBehavior
            .from(binding.bottomSheetRecycler)
        mediaKeyboard = MediaKeyboard(
            fragment = this,
            bottomSheetBehavior = bottomSheetBehavior,
            rootView = binding.root,
            openFrom = MediaControllerOpenPlace.Moments,
            interactionCallback = {
                if (it is GifMenuCallbackEvents.OnStickerPackViewed) {
                    viewModel.onStickerPackViewed(it.stickerPack)
                }
            },
            imagesCallback = object : OnImagesReady {
                override fun onReady(image: Uri) = sendBroadcast(
                    BROADCAST_IMAGE_EXTRA,
                    bundleOf(BROADCAST_MEDIA_DATA to image)
                )
            },
            amplitudeMediaKeyboardAnalytic = amplitudeMediaKeyboardAnalytic
        )
        mediaKeyboard?.onFirstStart(MediaKeyboardPagesPosition.WIDGETS.stickers)

        bottomSheetBehavior.state = STATE_EXPANDED
        bottomSheetBehavior.addBottomSheetCallback(
            object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_HIDDEN) {
                        val params = act.window.attributes.apply { alpha = 0f }
                        act.window.setAttributes(params)
                        act.finish()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (bottomSheetBehavior.state == STATE_SETTLING) {
                        bottomSheetBehavior.state = if (slideOffset > 0) STATE_EXPANDED else STATE_HIDDEN
                    }
                }
            }
        )
    }

    private fun initChatMenuDelegate() {
        chatMenuDelegate = ChatMenuDelegate(
            fragment = this,
            binding = null,
            featureToggles = activity?.application as FeatureTogglesContainer,
            isFromMoments = true,
            onAction = { viewModel.handleAction(it) }
        )
    }

    private fun observeEvent() {
        viewModel.event
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeStickers() {
        viewModel.stickersFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { state ->
                mediaKeyboard?.setStickerPacks(
                    state.stickerPacks,
                    state.recentStickers
                )
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleEvent(event: ChatMediaKeyboardEvent) {
        when (event) {
            is ChatMediaKeyboardEvent.ShowMediaPreview -> {
                event.mediaPreview?.let {
                    mediaKeyboard?.enableViewPagerScrolling(false)
                    chatMenuDelegate?.showMediaPreview(
                        event.mediaPreview,
                        event.deleteRecentClickListener
                    )
                }
            }

            is ChatMediaKeyboardEvent.OnSendFavoriteRecent -> {
                onChooseSticker(
                    event.favoriteRecentUiModel.id,
                    event.favoriteRecentUiModel.url
                )
            }

            else -> Unit
        }
    }

    override fun onGifClicked(
        gifUri: Uri,
        aspect: Double,
        giphyEntity: GiphyEntity?,
        gifSentWhereProp: AmplitudeMediaKeyboardWhereProperty
    ) = giphyEntity?.let {
        onChooseGif(
            it.id,
            it.smallUrl,
            it.originalAspectRatio,
            it.originalUrl
        )
    } ?: Unit

    override fun onGifLongClicked(
        id: String,
        preview: String,
        url: String,
        ratio: Double
    ) = Unit

    override fun onFavoriteRecentClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteRecentListener: (Int) -> Unit
    ) = when (favoriteRecent.type) {
        MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.GIF -> {
            onChooseGif(
                favoriteRecent.gifId.orEmpty(),
                favoriteRecent.url,
                favoriteRecent.ratio?.toDouble() ?: 0.0,
                favoriteRecent.url
            )
        }

        MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER -> {
            onChooseSticker(favoriteRecent.id, favoriteRecent.url)
        }

        else -> Unit
    }

    override fun onFavoriteRecentLongClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteClickListener: (Int) -> Unit
    ) = viewModel.handleAction(
        ChatActions.OnFavoriteRecentLongClick(
            favoriteRecent,
            type,
            deleteClickListener
        )
    )

    override fun onScrollToNewStickerPack(stickerPack: MediaKeyboardStickerPackUiModel) {
        mediaKeyboard?.setCurrentChosenStickerPack(stickerPack)
    }

    override fun onScrollToRecentStickers() {
        mediaKeyboard?.setRecentStickersChosen()
    }

    override fun onScrollToWidgets() {
        mediaKeyboard?.setWidgetsChosen()
    }

    override fun onStickerClicked(
        sticker: MediaKeyboardStickerUiModel,
        emoji: String?
    ) = onChooseSticker(sticker.id, sticker.url)

    override fun onStickerLongClicked(sticker: MediaKeyboardStickerUiModel) {
        viewModel.mediaKeyboardStickerLongClicked(sticker)
    }

    override fun onWidgetClicked(widget: MediaKeyboardWidget) {
        when (widget) {
            MediaKeyboardWidget.MUSIC_WIDGET -> (act as MomentsWrapperActivity).openAudioPlayer()
            MediaKeyboardWidget.TIME_WIDGET -> sendBroadcast(BROADCAST_WIDGET_EXTRA)
        }
    }

    override fun onDismiss() {
        mediaKeyboard?.enableViewPagerScrolling(true)
    }

    private fun onChooseSticker(id: Int, url: String) {
        val stickerObjectStory = NMRStoryStickerObject(id, url)
        sendBroadcast(
            BROADCAST_STICKER_EXTRA,
            bundleOf(BROADCAST_MEDIA_DATA to stickerObjectStory)
        )
    }

    private fun onChooseGif(
        id: String,
        smallUrl: String,
        ratio: Double,
        url: String
    ) {
        val gifObjectStory = NMRStoryGifObject(
            gifId = id,
            preview = smallUrl,
            ratio = ratio,
            url = url
        )
        sendBroadcast(
            BROADCAST_GIF_EXTRA,
            bundleOf(BROADCAST_MEDIA_DATA to gifObjectStory)
        )
    }

    private fun sendBroadcast(extraName: String, data: Bundle? = null) {
        val intent = Intent(BROADCAST_MEDIA_ACTION)
        intent.setPackage(requireContext().packageName)
        intent.putExtra(extraName, data)
        activity?.sendBroadcast(intent)
        activity?.finish()
    }

}
