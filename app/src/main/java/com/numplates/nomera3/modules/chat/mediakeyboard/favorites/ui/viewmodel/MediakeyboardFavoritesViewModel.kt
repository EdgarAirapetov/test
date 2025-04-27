package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.GetMediakeyboardFavoritesUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteEvent
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.mapper.MediakeyboardFavoritesUiMapper
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val FAVORITES_PAGE_SIZE = 50

class MediakeyboardFavoritesViewModel @Inject constructor(
    private val getMediakeyboardFavoritesUseCase: GetMediakeyboardFavoritesUseCase,
    private val mediakeyboardFavoritesMapper: MediakeyboardFavoritesUiMapper,
    private val networkStatusProvider: NetworkStatusProvider
) : BaseViewModel() {

    var favoritesLiveData: LiveData<PagedList<MediakeyboardFavoriteRecentUiModel>> = MutableLiveData()

    private val _mediakeyboardFavoritesEventFlow = MutableSharedFlow<MediakeyboardFavoriteEvent>()
    val mediakeyboardFavoritesEventFlow: Flow<MediakeyboardFavoriteEvent> = _mediakeyboardFavoritesEventFlow

    fun checkNetworkStatus() {
        event(MediakeyboardFavoriteEvent.OnNetworkStatusReceived(
            networkStatusProvider.isInternetConnected()
        ))
    }

    fun loadFavorites(isForMoments: Boolean) {
        val dataSourceFactory = getMediakeyboardFavoritesUseCase.invoke(isForMoments)

        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(FAVORITES_PAGE_SIZE)
            .build()

        favoritesLiveData = dataSourceFactory
            .map(mediakeyboardFavoritesMapper::mapDomainToUiModel)
            .toLiveData(config)

        event(MediakeyboardFavoriteEvent.OnPagingInitialized)
    }

    private fun event(event: MediakeyboardFavoriteEvent) {
        viewModelScope.launch {
            _mediakeyboardFavoritesEventFlow.emit(event)
        }
    }

}
