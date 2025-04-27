package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardAnalytic
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper.MediakeyboardFavoritesUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain.ClearMediaKeyboardRecentsUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain.DeleteMediaKeyboardRecentUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.SubscribeStickersFlow
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiAction
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickersEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickersState
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.mapper.MediaKeyboardStickerUiMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
const val NEED_SHOW_WIDGETS_ARG = "NEED_SHOW_WIDGETS_ARG"


class MediaKeyboardStickersViewModel @Inject constructor(
    subscribeStickersFlow: SubscribeStickersFlow,
    private val stickersMapper: MediaKeyboardStickerUiMapper,
    private val recentsMapper: MediakeyboardFavoritesUiMapper,
    private val clearMediaKeyboardRecentsUseCase: ClearMediaKeyboardRecentsUseCase,
    private val deleteMediaKeyboardRecentUseCase: DeleteMediaKeyboardRecentUseCase,
    private val amplitudeMediaKeyboardAnalytic: AmplitudeMediaKeyboardAnalytic
) : ViewModel() {

    val mediaKeyboardStickersState: StateFlow<MediaKeyboardStickersState> = subscribeStickersFlow.invoke()
        .mapNotNull { model ->
            val stickerPacks = model.stickerPacks.map(stickersMapper::mapStickerPackDomainToUiModel)
            if (stickerPacks.isEmpty()) {
                MediaKeyboardStickersState()
            } else {
                MediaKeyboardStickersState(
                    stickerPacks = stickerPacks,
                    recentStickers = model.recentStickers.map(recentsMapper::mapDomainToUiModel)
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, MediaKeyboardStickersState())

    private val _mediaKeyboardStickersEvent = MutableSharedFlow<MediaKeyboardStickersEvent>()
    val mediaKeyboardStickersEvent: SharedFlow<MediaKeyboardStickersEvent> = _mediaKeyboardStickersEvent

    fun handleUiAction(action: MediaKeyboardStickerUiAction) {
        when (action) {
            is MediaKeyboardStickerUiAction.ClearRecentStickers -> clearRecentStickers(action.isForMoment)
            is MediaKeyboardStickerUiAction.DeleteRecentSticker -> {
                deleteRecentSticker(
                    action.recentId,
                    action.stickerId,
                    action.isForMoment
                )
            }
        }
    }

    private fun clearRecentStickers(isForMoment: Boolean) {
        viewModelScope.launch {
            runCatching {
                clearMediaKeyboardRecentsUseCase.invoke(
                    type = MediaKeyboardRecentType.STICKERS,
                    isForMoment = isForMoment
                )
                amplitudeMediaKeyboardAnalytic.logClearRecentStickers()
            }.onFailure {
                _mediaKeyboardStickersEvent.emit(MediaKeyboardStickersEvent.OnLoadingStickersError)
            }
        }
    }

    private fun deleteRecentSticker(
        recentId: Int,
        stickerId: Int?,
        isForMoment: Boolean
    ) {
        viewModelScope.launch {
            runCatching {
                deleteMediaKeyboardRecentUseCase.invoke(
                    recentId = recentId,
                    type = MediaKeyboardRecentType.STICKERS,
                    isForMoment = isForMoment
                )
                stickerId?.let { amplitudeMediaKeyboardAnalytic.logDeleteRecentSticker(it) }
            }.onFailure {
                _mediaKeyboardStickersEvent.emit(MediaKeyboardStickersEvent.OnLoadingStickersError)
            }
        }
    }

}
