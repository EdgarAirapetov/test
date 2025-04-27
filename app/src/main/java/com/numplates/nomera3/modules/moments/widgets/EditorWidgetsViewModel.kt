package com.numplates.nomera3.modules.moments.widgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.FAVORITES_PAGE_SIZE
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.AddToMediaKeyboardFavoritesUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.DeleteMediaKeyboardFavoriteUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.GetMediaKeyboardFavoritesFlowUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.LoadFavoritesUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.SaveMediakeyboardFavoritesInDbUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper.MediakeyboardFavoritesUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.ReloadAllStickersInFlow
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.SetStickerPackViewedUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.SubscribeStickersFlow
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickersAndRecentStickersUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.mapper.MediaKeyboardStickerUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaUiModel
import com.numplates.nomera3.modules.chat.ui.action.ChatActions
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatMediaKeyboardEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class EditorWidgetsViewModel @Inject constructor(
    private val getMediaKeyboardFavoritesUseCase: GetMediaKeyboardFavoritesFlowUseCase,
    private val stickersMapper: MediaKeyboardStickerUiMapper,
    private val mediakeyboardFavoritesMapper: MediakeyboardFavoritesUiMapper,
    private val loadFavoritesUseCase: LoadFavoritesUseCase,
    private val saveFavoritesToDbUseCase: SaveMediakeyboardFavoritesInDbUseCase,
    private val addToMediaKeyboardFavoritesUseCase: AddToMediaKeyboardFavoritesUseCase,
    private val deleteMediaKeyboardFavoriteUseCase: DeleteMediaKeyboardFavoriteUseCase,
    private val setStickerPackViewedUseCase: SetStickerPackViewedUseCase,
    subscribeStickersFlow: SubscribeStickersFlow,
    private val reloadAllStickersInFlow: ReloadAllStickersInFlow,
) : ViewModel() {

    private val currentMediaFavorites = mutableListOf<MediakeyboardFavoriteRecentUiModel>()

    private val _event = MutableSharedFlow<ChatMediaKeyboardEvent>()
    val event: SharedFlow<ChatMediaKeyboardEvent> = _event

    val stickersFlow: Flow<MediaKeyboardStickersAndRecentStickersUiModel> = subscribeStickersFlow.invoke()
        .map { stickers ->
            MediaKeyboardStickersAndRecentStickersUiModel(
                stickerPacks = stickers.stickerPacks.map(stickersMapper::mapStickerPackDomainToUiModel),
                recentStickers = stickers.recentStickers.map(mediakeyboardFavoritesMapper::mapDomainToUiModel),
            )
        }

    init {
        observeFavoritesChanges()
        loadMoreFavorites()
    }

    fun handleAction(action: ChatActions) = when (action) {
        is ChatActions.SendFavoriteRecent -> {
            sendEffect(
                ChatMediaKeyboardEvent.OnSendFavoriteRecent(
                    action.favoriteRecentUiModel,
                    action.type
                )
            )
        }

        is ChatActions.OnFavoriteRecentLongClick -> {
            mediaKeyboardFavoriteRecentLongClicked(
                action.model,
                action.type,
                action.deleteRecentClickListener
            )
        }

        is ChatActions.AddToFavorites -> addToFavorites(action.mediaPreview)

        is ChatActions.RemoveFromFavorites -> removeFromFavorites(action.mediaPreview)

        else -> Unit
    }

    fun loadStickers() = viewModelScope.launch {
        runCatching {
            reloadAllStickersInFlow.invoke()
        }.onFailure { Timber.d(it) }
    }

    fun mediaKeyboardStickerLongClicked(sticker: MediaKeyboardStickerUiModel) {
        val media = MediaUiModel.StickerMediaUiModel(
            favoriteId = currentMediaFavorites.firstOrNull { it.url == sticker.url }?.id,
            stickerId = sticker.id,
            stickerUrl = sticker.url,
            lottieUrl = sticker.lottieUrl,
            stickerPackTitle = sticker.stickerPackTitle
        )
        val mediaPreview = MediaPreviewUiModel(
            media = media,
            type = MediaPreviewType.STICKER,
            isAdded = currentMediaFavorites.any { it.url == sticker.url }
        )
        sendEffect(ChatMediaKeyboardEvent.ShowMediaPreview(mediaPreview))
    }

    fun onStickerPackViewed(stickerPack: MediaKeyboardStickerPackUiModel) {
        viewModelScope.launch {
            runCatching {
                setStickerPackViewedUseCase.invoke(stickerPack.id)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun observeFavoritesChanges() {
        getMediaKeyboardFavoritesUseCase.invoke(isForMoments = true)
            .onEach { newFavorites ->
                currentMediaFavorites.clear()
                currentMediaFavorites += newFavorites.map(
                    mediakeyboardFavoritesMapper::mapDomainToUiModel
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadMoreFavorites(startId: Int? = null) {
        viewModelScope.launch {
            val favorites = runCatching { loadFavoritesUseCase.invoke(startId, true) }
                .onFailure(Timber::e)
                .getOrDefault(emptyList())
            saveFavoritesToDbUseCase.invoke(favorites)
            if (favorites.isEmpty() || favorites.size < FAVORITES_PAGE_SIZE) {
                return@launch
            } else {
                loadMoreFavorites(favorites.last().id)
            }
        }
    }

    private fun mediaKeyboardFavoriteRecentLongClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteRecentListener: (Int) -> Unit
    ) {
        val mediaUiModel = MediaUiModel.fromMediakeyboardFavoriteRecentUiModel(favoriteRecent, null)
        val mediaPreviewUiModel = MediaPreviewUiModel(
            media = mediaUiModel,
            type = type,
            isAdded = currentMediaFavorites.any { it.url == favoriteRecent.url },
            favoriteRecentModel = favoriteRecent
        )
        sendEffect(
            ChatMediaKeyboardEvent.ShowMediaPreview(
                mediaPreview = mediaPreviewUiModel,
                deleteRecentClickListener = deleteRecentListener
            )
        )
    }

    private fun addToFavorites(mediaPreview: MediaPreviewUiModel) {
        mediaPreview.media.toAddFavoriteBody()?.let { body ->
            viewModelScope.launch {
                runCatching {
                    addToMediaKeyboardFavoritesUseCase.invoke(body, true)
                }.onFailure {
                    Timber.e(it)
                }.onSuccess {
                    loadMoreFavorites()
                }
            }
        }
    }

    private fun removeFromFavorites(mediaPreview: MediaPreviewUiModel) {
        val mediaUrl = when (val media = mediaPreview.media) {
            is MediaUiModel.GifMediaUiModel -> media.url
            is MediaUiModel.ImageMediaUiModel -> media.url
            is MediaUiModel.StickerMediaUiModel -> media.stickerUrl
            is MediaUiModel.VideoMediaUiModel -> media.url
        }
        val mediaId = currentMediaFavorites
            .firstOrNull { it.url == mediaUrl }?.id
            ?: mediaPreview.media.id
            ?: return
        viewModelScope.launch {
            runCatching { deleteMediaKeyboardFavoriteUseCase.invoke(mediaId, true) }
                .onFailure(Timber::e)
                .onSuccess { loadMoreFavorites() }
        }
    }

    private fun sendEffect(typeEvent: ChatMediaKeyboardEvent) {
        viewModelScope.launch {
            _event.emit(typeEvent)
        }
    }
}
