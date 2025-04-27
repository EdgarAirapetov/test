package com.numplates.nomera3.modules.gifservice.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.gifservice.domain.mapper.GiphyDbMapper
import com.numplates.nomera3.modules.gifservice.domain.mapper.GiphyNetworkMapper
import com.numplates.nomera3.modules.gifservice.domain.usecase.GetGiphyTrendingParams
import com.numplates.nomera3.modules.gifservice.domain.usecase.GetGiphyTrendingUseCase
import com.numplates.nomera3.modules.gifservice.domain.usecase.GetRecentGifsParams
import com.numplates.nomera3.modules.gifservice.domain.usecase.GetRecentGifsUseCase
import com.numplates.nomera3.modules.gifservice.domain.usecase.GiphySearchParams
import com.numplates.nomera3.modules.gifservice.domain.usecase.GiphySearchUseCase
import com.numplates.nomera3.modules.gifservice.domain.usecase.SetGifToRecentParams
import com.numplates.nomera3.modules.gifservice.domain.usecase.SetGifToRecentUseCase
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity
import com.numplates.nomera3.modules.gifservice.ui.entity.state.GifMenuViewState
import com.numplates.nomera3.modules.gifservice.ui.entity.state.Status
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.meera.db.models.DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// https://developers.giphy.com/docs/optional-settings/#language-support
private const val DEFAULT_REQUEST_LANG = "en"
const val GIF_PAGE_SIZE = 20
const val GIF_SEARCH_DEBOUNCE_TIMEOUT_MS = 500L

class GiphyViewModel : ViewModel() {

    @Inject
    lateinit var getGiphyTrendingUseCase: GetGiphyTrendingUseCase

    @Inject
    lateinit var giphySearchUseCase: GiphySearchUseCase

    @Inject
    lateinit var setGifToRecentUseCase: SetGifToRecentUseCase

    @Inject
    lateinit var getRecentGifsUseCase: GetRecentGifsUseCase

    @Inject
    lateinit var networkStatusProvider: NetworkStatusProvider

    private val networkMapper = GiphyNetworkMapper()
    private val dbMapper = GiphyDbMapper()

    val liveViewState = MutableLiveData<GifMenuViewState>()

    var isLoading = false
    var isLastPage = false

    private val cacheGifResults = mutableMapOf<String, List<GiphyEntity>>()

    init {
        App.component.inject(this)
    }

    fun getTrending(query: String, limit: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLastPage = false
            isLoading = true

            getGiphyTrendingUseCase.execute(
                    params = GetGiphyTrendingParams(limit, offset),
                    success = { response ->
                        val gifs = networkMapper.map(response)
                        cacheGifResults[query] = gifs

                        if (gifs.isEmpty()) {
                            isLastPage = true
                        } else {
                            gifs.submitGifs(
                                    isFirstPage = offset == 0,
                                    status = Status.STATUS_OK
                            )
                        }
                        isLoading = false
                    },
                    fail = { exception ->
                        Timber.e("Get Giphy trending ERROR:${exception.message}")
                        handleRequestError(query)
                    }
            )
        }
    }

    fun search(query: String, limit: Int, offset: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            isLastPage = false
            isLoading = true

            giphySearchUseCase.execute(
                    params = GiphySearchParams(query, limit, offset, DEFAULT_REQUEST_LANG),
                    success = { response ->
                        val responseData = response.data ?: emptyList()
                        val gifs = networkMapper.map(responseData)
                        cacheGifResults[query] = gifs

                        val pagination = response.pagination
                        if (pagination?.totalCount == 0 && pagination.count == 0) {
                            isLastPage = true
                            gifs.submitGifs(
                                    isFirstPage = offset == 0,
                                    status = Status.STATUS_EMPTY_SEARCH_GIFS
                            )
                        } else {
                            gifs.submitGifs(
                                    isFirstPage = offset == 0,
                                    status = Status.STATUS_OK
                            )
                        }

                        isLoading = false
                    },
                    fail = { exception ->
                        Timber.e("GIPHY Search ERROR:${exception.message}")
                        handleRequestError(query)
                    }
            )
        }
    }

    private fun handleRequestError(query: String) {
        val cachedGifs = cacheGifResults[query]

        if (cachedGifs.isNullOrEmpty()) {
            mutableListOf<GiphyEntity>().submitGifs(
                    status = Status.STATUS_NETWORK_ERROR
            )
        } else {
            cachedGifs.submitGifs(
                    status = Status.STATUS_NETWORK_ERROR_WITH_CACHE
            )
        }
    }

    fun setGifToRecent(
            id: String,
            smallUrl: String,
            originalUrl: String,
            originalAspectRatio: Double?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            setGifToRecentUseCase.execute(
                    params = SetGifToRecentParams(
                            id,
                            smallUrl,
                            originalUrl,
                            originalAspectRatio ?: DEFAULT_ORIGINAL_ASPECT_GIPHY_IMAGE
                    ),
                    success = { Timber.d("Gif successed added to recent gifs Db") },
                    fail = { Timber.e("Fail add to recent gifs. Internal Db error") }
            )
        }
    }

    fun getRecentGifs() {
        viewModelScope.launch(Dispatchers.IO) {
            getRecentGifsUseCase.execute(
                    params = GetRecentGifsParams(),
                    success = { gifUrls ->
                        val recentGifs = dbMapper.map(gifUrls)
                        if (recentGifs.isNotEmpty()) {
                            recentGifs.submitGifs(status = Status.STATUS_OK)
                        } else {
                            recentGifs.submitGifs(status = Status.STATUS_EMPTY_RECENT_GIFS)
                        }
                    },
                    fail = { Timber.e("Fail get recent gifs. Internal Db error") }
            )
        }
    }

    private fun List<GiphyEntity>.submitGifs(
            isFirstPage: Boolean = true,
            status: Status
    ) {
        liveViewState.postValue(GifMenuViewState.SearchResultState(
                isFirstPage = isFirstPage,
                resultList = this,
                status = status
        ))
    }
}
