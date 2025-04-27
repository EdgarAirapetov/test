package com.numplates.nomera3.modules.gifservice.ui.entity.state

import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity

sealed class GifMenuViewState {

    data class SearchResultState(
            val isFirstPage: Boolean,
            val resultList: List<GiphyEntity>,
            val status: Status
    ): GifMenuViewState()

}

enum class Status {
    STATUS_OK,                          // Успешный запрос
    STATUS_NETWORK_ERROR,               // Неуспешный запрос, отсутствуют закешированные данные
    STATUS_NETWORK_ERROR_WITH_CACHE,    // Неуспешный запрос, присутствую закешированные данные
    STATUS_EMPTY_SEARCH_GIFS,           // Не найдены результаты по запросу
    STATUS_EMPTY_RECENT_GIFS            // В "Недавние" ещё нет ни одной Gif
}
