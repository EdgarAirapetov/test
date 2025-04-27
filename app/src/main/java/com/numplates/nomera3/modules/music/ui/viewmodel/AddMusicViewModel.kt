package com.numplates.nomera3.modules.music.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import com.numplates.nomera3.modules.music.domain.usecase.GetTopMusicUseCase
import com.numplates.nomera3.modules.music.domain.usecase.SearchMusicUseCase
import com.numplates.nomera3.modules.music.ui.adapter.MusicAdapterType
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.music.ui.entity.PagingParams
import com.numplates.nomera3.modules.music.ui.entity.state.MusicSearchScreenState
import com.numplates.nomera3.modules.music.ui.entity.state.Status
import com.numplates.nomera3.modules.music.ui.mapper.EntityMapper
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.launch

class AddMusicViewModel @Inject constructor(
    private val searchMusicUseCaseTest: SearchMusicUseCase,
    private val topMusicTest: GetTopMusicUseCase
) : BaseViewModel() {

    private var recommendations = mutableListOf<MusicCellUIEntity>()
    private var searchResults = mutableListOf<MusicCellUIEntity>()
    private var lastRequestStatus: Status = Status.STATUS_OK
    private var isAddingMode = true

    private val pagingParams = PagingParams()
    private val mapper = EntityMapper()

    private val _liveState = MutableLiveData<MusicSearchScreenState>()
    val liveState = _liveState as LiveData<MusicSearchScreenState>

    init {
        requestRecommendations()
    }

    // paging
    fun loadMore() {
        if (pagingParams.searchText.isEmpty()){
            requestRecommendations()
        } else {
            requestSearchTest()
        }
    }

    fun isLastPage() = pagingParams.isLastPage

    fun isLoading() = _liveState.value is MusicSearchScreenState.MusicSearchLoading

    // логика отображения ячеек немного отличается если мы заменяем музыку, а не добовляем
    fun setIsAdding(addingMode: Boolean) {
        isAddingMode = addingMode
    }

    fun searchMusic(query: String) {
        pagingParams.clear()
        searchResults.clear()
        if (query.trim().isEmpty()) {
            pagingParams.searchText = String.empty()
            pagingParams.isLastPage = true
            submitRecommendations(true)
            return
        }
        pagingParams.searchText = query.trim()
        requestSearchTest()
    }

    private fun requestSearchTest() {
        viewModelScope.launch {
            _liveState.value = MusicSearchScreenState.MusicSearchLoading
            pagingParams.offset = searchResults.size
            runCatching {searchMusicUseCaseTest.invoke(
                pagingParams.limit,
                pagingParams.offset,
                pagingParams.searchText
            )  }
                .onSuccess { result -> handleSuccess(result, isSearchResult = true) }
                .onFailure { throwable -> handleError(throwable, isSearchResult = true) }
        }
    }

    private fun requestRecommendations() {
        viewModelScope.launch {
            _liveState.value = MusicSearchScreenState.MusicSearchLoading
            pagingParams.offset = recommendations.size
            pagingParams.isLastPage = true
            runCatching { topMusicTest.invoke(pagingParams.limit, pagingParams.offset) }
                .onSuccess { result -> handleSuccess(result, isSearchResult = false) }
                .onFailure { throwable -> handleError(throwable, isSearchResult = false)}
        }
    }

    private fun submitRecommendations(needToScrollUp: Boolean = false) {
        val newList = mutableListOf<MusicCellUIEntity>()
        recommendations.updateLastStatus()
        newList.addHeaderRecommendation()
        newList.addAll(recommendations)
        newList.handleSeparator()
        if (!pagingParams.isLastPage && recCountMultipleOfLimit()) newList.addProgress()
        _liveState.postValue(
            MusicSearchScreenState.RecommendationState(
                newList,
                lastRequestStatus,
                needToScrollUp
            )
        )
    }

    private fun recCountMultipleOfLimit(): Boolean = recommendations.size % pagingParams.limit == 0

    private fun submitSearch(needToScrollUp: Boolean = false) {
        val newList = mutableListOf<MusicCellUIEntity>()
        searchResults.updateLastStatus()
        newList.addHeaderSearch()
        newList.addAll(searchResults)
        newList.handleSeparator()
        if (!pagingParams.isLastPage) newList.addProgress()
        _liveState.postValue(
            MusicSearchScreenState.SearchResultState(
                newList,
                lastRequestStatus,
                needToScrollUp
            )
        )
    }

    private fun handleError(error: Throwable, isSearchResult: Boolean) {
        pagingParams.isLastPage = true

        if (error is UnknownHostException) lastRequestStatus = Status.STATUS_NETWORK_ERROR

        if (isSearchResult) submitSearch()
        else submitRecommendations()
    }

    private fun handleSuccess(result: List<MusicSearchEntity>, isSearchResult: Boolean) {
        lastRequestStatus = Status.STATUS_OK

        val mappedData = mapper.map(result, isAddingMode)
        if (mappedData.isEmpty()) pagingParams.isLastPage = true

        if (isSearchResult) {
            searchResults.addAll(mappedData)
            submitSearch(isFirstRequest())
        } else {
            recommendations.addAll(mappedData)
            submitRecommendations(isFirstRequest())
        }
    }

    //проверяем статус последней операции и синхронизируем ее с состоянием списка
    private fun MutableList<MusicCellUIEntity>.updateLastStatus() {
        if (lastRequestStatus == Status.STATUS_OK && this.isEmpty()) {
            lastRequestStatus = Status.STATUS_EMPTY_LIST
        } else if (lastRequestStatus == Status.STATUS_NETWORK_ERROR && this.isNotEmpty()) {
            lastRequestStatus = Status.STATUS_OK
        } else if (lastRequestStatus == Status.STATUS_EMPTY_LIST && this.isNotEmpty()) {
            lastRequestStatus = Status.STATUS_OK
        }
    }

    // добавляем заголовок для рекомендаций
    private fun MutableList<MusicCellUIEntity>.addHeaderRecommendation() {
        add(MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_HEADER_RECOMMENDATION))
    }

    private fun MutableList<MusicCellUIEntity>.handleSeparator() {
        //скрыть сепаратор у первого элемента
        this.forEach {
            if (it.type == MusicAdapterType.ITEM_TYPE_MUSIC) {
                it.needToShowSeparator = false
                return
            }
        }
    }

    // добавляем заголовок для поиска
    private fun MutableList<MusicCellUIEntity>.addHeaderSearch() {
        add(MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_HEADER_SEARCH))
    }

    // добавляем прогресс холдер
    private fun MutableList<MusicCellUIEntity>.addProgress() {
        add(MusicCellUIEntity(type = MusicAdapterType.ITEM_TYPE_PROGRESS))
    }

    private fun isFirstRequest() = pagingParams.offset == 0
}
