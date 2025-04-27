package com.numplates.nomera3.modules.search.ui.viewmodel.hashtag

import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.search.domain.mapper.HashTagQueryMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.SearchHashTagResultMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchHashTagParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchHashTagUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.HashTagSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.base.DEFAULT_SEARCH_RESULT_PAGE_SIZE
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SearchHashTagResultViewModel : SearchResultScreenBaseViewModel() {

    @Inject
    lateinit var searchHashTagUseCase: SearchHashTagUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    private var hashTagRemoveMapper = HashTagQueryMapper()

    init {
        App.component.inject(this)
    }

    override fun loadMore() {
        loadResult(
            query = query,
            offset = getPagingProperties().offset
        )
    }

    fun selectHashTagItem(item: SearchItem.HashTag) {
        publishEvent(HashTagSearchViewEvent.OpenHashTag(item))
    }

    override fun search(query: String) {
        super.search(hashTagRemoveMapper.map(query))
        loadResult(this.query)
    }

    fun showEmpty() {
        publishList(SearchResultViewState.DefaultResult(emptyList()))
    }

    fun logHashTagPressed() = amplitudeHelper.logHashTagPress(AmplitudePropertyWhere.SEARCH)

    private fun loadResult(
        query: String = String.empty(),
        offset: Int = 0,
        limit: Int = DEFAULT_SEARCH_RESULT_PAGE_SIZE,
    ) {
        showShimmerLoading()

        val isPagingLoad = offset != 0
        getPagingProperties().isLoading = true

        if(!isPagingLoad) {
            publishList(SearchResultViewState.SearchStart)
        }

        viewModelScope.launch(Dispatchers.IO) {
            searchHashTagUseCase.execute(
                params = SearchHashTagParams(query, limit, offset),
                success = { response ->
                    val mappedNewItems = SearchHashTagResultMapper().map(response.tagList)

                    val resultList = if (isPagingLoad.not()) {
                        newSearchItems(mappedNewItems)
                    } else {
                        addSearchItems(mappedNewItems)
                    }

                    if (isPagingLoad.not()) {
                        publishList(SearchResultViewState.SearchResult(resultList))
                    } else {
                        publishList(SearchResultViewState.Data(resultList))
                    }

                    getPagingProperties().isLastPage = response.tagList.isEmpty()
                    getPagingProperties().isLoading = false
                    getPagingProperties().offset = offset + mappedNewItems.size
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    Timber.d(exception)
                }
            )
        }
    }

    private fun newSearchItems(newItems: List<SearchItem>): List<SearchItem> {
        return if (newItems.isNotEmpty()) {
            val title = SearchItem.Title(R.string.search_result_list_title)
            mutableListOf<SearchItem>(title).apply {
                addAll(newItems)
            }
        } else {
            newItems
        }
    }

    private fun addSearchItems(newItems: List<SearchItem>): List<SearchItem> {
        val currentItems = getAllSearchItems()
        return mutableListOf<SearchItem>().apply {
            addAll(currentItems)
            addAll(newItems)
        }
    }
}
