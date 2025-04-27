package com.numplates.nomera3.modules.search.ui.viewmodel.group

import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.communities.data.states.CommunityState
import com.numplates.nomera3.modules.communities.domain.usecase.SubscribeCommunitySubjectUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.SubscribeCommunitySubjectUseCaseParams
import com.numplates.nomera3.modules.search.domain.mapper.action.ReplaceGroupMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.GroupSubscribeStatusButtonMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.SearchGroupResultMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchGroupsParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchGroupsUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchSubscribeGroupParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchSubscribeGroupUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.GroupSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.base.DEFAULT_SEARCH_RESULT_PAGE_SIZE
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SearchGroupResultViewModel : SearchResultScreenBaseViewModel() {

    @Inject
    lateinit var searchGroupsUseCase: SearchGroupsUseCase

    @Inject
    lateinit var searchSubscribeUseCase: SearchSubscribeGroupUseCase

    @Inject
    lateinit var communityStatesObserverUseCase: SubscribeCommunitySubjectUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    init {
        App.component.inject(this)
        addCommunityStatesObserver()
    }

    fun showEmpty() {
        publishList(SearchResultViewState.DefaultResult(emptyList()))
    }

    override fun loadMore() {
        loadResult(
            query = query,
            offset = getPagingProperties().offset
        )
    }

    fun selectGroupItem(item: SearchItem.Group) {
        publishEvent(GroupSearchViewEvent.SelectGroup(item.groupId))
    }

    fun subscribeGroup(item: SearchItem.Group) {
        showLoading()

        viewModelScope.launch(Dispatchers.IO) {
            searchSubscribeUseCase.execute(
                params = SearchSubscribeGroupParams(item.groupId),
                success = {
                    val newGroup = item.copy(
                        buttonState = SearchItem.Group.ButtonState.Hide
                    )

                    showListWithNewGroup(newGroup)

                    val message = if (item.isClosedGroup) {
                        SearchMessageViewEvent.GroupSendRequest
                    } else {
                        SearchMessageViewEvent.GroupSubscribed
                    }

                    publishMessage(message)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    Timber.e(exception)
                }
            )
        }
    }

    override fun search(query: String) {
        super.search(query)

        loadResult(this.query)
    }

    private fun loadResult(
        query: String = String.empty(),
        offset: Int = 0,
        limit: Int = DEFAULT_SEARCH_RESULT_PAGE_SIZE,
    ) {
        showShimmerLoading()

        val isPagingLoad = offset != 0
        getPagingProperties().isLoading = true

        if (!isPagingLoad) {
            publishList(SearchResultViewState.SearchStart)
        }

        viewModelScope.launch(Dispatchers.IO) {
            searchGroupsUseCase.execute(
                params = SearchGroupsParams(query, limit, offset),
                success = { response ->
                    val mappedNewItems = SearchGroupResultMapper().map(response.groups)

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

                    getPagingProperties().isLastPage = response.groups.isEmpty()
                    getPagingProperties().isLoading = false
                    getPagingProperties().offset = offset + mappedNewItems.size
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    getPagingProperties().isLoading = false

                    Timber.d(exception)
                }
            )
        }
    }

    private fun showListWithNewGroup(newGroup: SearchItem.Group) {
        ReplaceGroupMapper(getCurrentRenderData()).map(newGroup).let { newData ->
            resultState.postValue(newData)
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

    private fun getCurrentSearchItems(): List<SearchItem> {
        return getAllSearchItems().filterIsInstance<SearchItem.Group>()
    }

    private fun addCommunityStatesObserver() {
        disposables.add(
            communityStatesObserverUseCase
                .execute(SubscribeCommunitySubjectUseCaseParams())
                .subscribeOn(Schedulers.io())
                .subscribe(::updateCommunityItemStatus)
                { Timber.e(it) }
        )
    }

    private fun updateCommunityItemStatus(state: CommunityState) {
        when (state) {
            is CommunityState.OnSubscribe ->
                findCommunityItemAndUpdate(state.groupId, isSubscribe = true)
            is CommunityState.OnUnsubscribe ->
                findCommunityItemAndUpdate(state.groupId, isSubscribe = false)
        }
    }

    private fun findCommunityItemAndUpdate(groupId: Int, isSubscribe: Boolean) {
        val foundItem = getCurrentSearchItems().find { searchItem ->
            if (searchItem is SearchItem.Group) {
                return@find searchItem.groupId == groupId
            } else {
                return@find false
            }
        }

        if (foundItem is SearchItem.Group) {
            showListWithNewGroup(
                foundItem.copy(buttonState = GroupSubscribeStatusButtonMapper().map(isSubscribe))
            )
        }
    }

}

