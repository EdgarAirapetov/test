package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.numplates.nomera3.modules.chat.mediakeyboard.data.entity.MediaKeyboardRecentType
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper.MediakeyboardFavoritesUiMapper
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain.ClearMediaKeyboardRecentsUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain.DeleteMediaKeyboardRecentUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.domain.GetMediaKeyboardRecentMediaUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentAdapterItem
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity.MediaKeyboardRecentsState
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SHIMMER_ITEM_COUNT = 36

class MediaKeyboardRecentsViewModel @Inject constructor(
    private val getMediaKeyboardRecentMediaUseCase: GetMediaKeyboardRecentMediaUseCase,
    private val deleteMediaKeyboardRecentUseCase: DeleteMediaKeyboardRecentUseCase,
    private val clearMediaKeyboardRecentsUseCase: ClearMediaKeyboardRecentsUseCase,
    private val mediakeyboardFavoritesUiMapper: MediakeyboardFavoritesUiMapper
) : BaseViewModel() {

    private var isInitialListLoaded = false

    private val _mediaKeyboardRecentsState = MutableLiveData<MediaKeyboardRecentsState>()
    val mediaKeyboardRecentsState: LiveData<MediaKeyboardRecentsState> = _mediaKeyboardRecentsState

    private val _mediaKeyboardRecentsEvent = MutableSharedFlow<MediaKeyboardRecentsEvent>()
    val mediaKeyboardRecentsEvent: SharedFlow<MediaKeyboardRecentsEvent> = _mediaKeyboardRecentsEvent

    fun loadRecents() {
        if (!isInitialListLoaded || _mediaKeyboardRecentsState.value?.recentList?.isEmpty() == true) {
            val shimmerItems = mutableListOf<MediaKeyboardRecentAdapterItem>()
            repeat(SHIMMER_ITEM_COUNT) {
                shimmerItems.add(MediaKeyboardRecentAdapterItem(type = MediaKeyboardRecentAdapterItem.ItemType.SHIMMER))
            }
            _mediaKeyboardRecentsState.postValue(MediaKeyboardRecentsState(recentList = shimmerItems))
            isInitialListLoaded = true
        }
        launchLoadRecentsJob()
    }

    fun deleteRecent(recentId: Int) {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            runCatching {
                deleteMediaKeyboardRecentUseCase.invoke(
                    recentId = recentId,
                    type = MediaKeyboardRecentType.MEDIA,
                    isForMoment = false
                )
            }.onFailure {
                _mediaKeyboardRecentsEvent.emit(MediaKeyboardRecentsEvent.OnLoadingRecentError)
            }.onSuccess {
                proceedDeleteRecentSuccess(recentId)
            }
        }
    }

    fun clearRecents() {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            runCatching {
                clearMediaKeyboardRecentsUseCase.invoke(
                    type = MediaKeyboardRecentType.MEDIA,
                    isForMoment = false
                )
            }.onFailure {
                _mediaKeyboardRecentsEvent.emit(MediaKeyboardRecentsEvent.OnLoadingRecentError)
            }.onSuccess {
                proceedClearRecentsSuccess()
            }
        }
    }

    private fun launchLoadRecentsJob() {
        CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
            val recents = runCatching {
                getMediaKeyboardRecentMediaUseCase.invoke().map(mediakeyboardFavoritesUiMapper::mapDomainToUiModel)
            }.onFailure {
                _mediaKeyboardRecentsEvent.emit(MediaKeyboardRecentsEvent.OnLoadingRecentError)
            }.getOrNull()

            recents?.let { _mediaKeyboardRecentsState.postValue(
                MediaKeyboardRecentsState(recentList = it.map { uiModel ->
                    MediaKeyboardRecentAdapterItem(
                        type = MediaKeyboardRecentAdapterItem.ItemType.RECENT,
                        model = uiModel
                    ) })
            ) }
        }
    }

    private fun proceedDeleteRecentSuccess(recentId: Int) {
        val currentList = mediaKeyboardRecentsState.value?.recentList ?: return
        val newList = currentList.filter { it.model?.id != recentId }
        _mediaKeyboardRecentsState.postValue(MediaKeyboardRecentsState(recentList = newList))
    }

    private fun proceedClearRecentsSuccess() {
        _mediaKeyboardRecentsState.postValue(MediaKeyboardRecentsState(recentList = emptyList()))
    }

}
