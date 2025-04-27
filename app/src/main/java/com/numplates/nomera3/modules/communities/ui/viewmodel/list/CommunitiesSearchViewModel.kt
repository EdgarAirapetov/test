package com.numplates.nomera3.modules.communities.ui.viewmodel.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.communities.domain.usecase.SearchGroupsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.SearchGroupsUseCaseParams
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityModelMapper
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunitiesSearchViewModel : BaseViewModel() {

    @Inject
    lateinit var searchGroupUseCase: SearchGroupsUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    var liveSearchGroup = MutableLiveData<List<CommunityListItemUIModel>>()
    var liveSearchGroupMore = MutableLiveData<List<CommunityListItemUIModel>>()
    val liveSearchProgress = MutableLiveData<Boolean>()

    private val mapper = CommunityModelMapper()
    private var isLast = false
    private var isLoading = false
    private var searchQuery = ""

    init {
        App.component.inject(this)
    }

    fun setQuery(query: String) {
        search(query)
    }

    private fun search(query: String) {
        liveSearchProgress.value = true
        searchQuery = query
        isLoading = true
        isLast = false
        viewModelScope.launch {
            searchGroupUseCase.execute(
                params = SearchGroupsUseCaseParams(query, 0),
                success = {
                    liveSearchProgress.value = false
                    val searchData = mapper.map(it)
                    isLoading = false
                    if (searchData.isEmpty()) isLast = true
                    analyticLogInputSearch(searchData)
                    liveSearchGroup.value = searchData
                },
                fail = {
                    liveSearchProgress.value = false
                    isLoading = false
                    Timber.e(it)
                }
            )
        }
    }

    private fun analyticLogInputSearch(items: List<CommunityListItemUIModel>) {
        if (items.isNotEmpty()) {
            amplitudeHelper.logSearchInput(
                type = AmplitudePropertySearchType.NONE,
                haveResult = AmplitudePropertyHaveResult.YES,
                whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.COMMUNITY
            )
        } else {
            amplitudeHelper.logSearchInput(
                type = AmplitudePropertySearchType.NONE,
                haveResult = AmplitudePropertyHaveResult.NO,
                whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.COMMUNITY
            )
        }
    }

    //paginator methods
    fun isLoading() = isLoading

    fun onLast() = isLast

    fun loadMoreSearch(itemCount: Int) {
        liveSearchProgress.value = true
        isLoading = true
        viewModelScope.launch {
            searchGroupUseCase.execute(
                params = SearchGroupsUseCaseParams(searchQuery, itemCount + 20),
                success = {
                    liveSearchProgress.value = false
                    val searchData = mapper.map(it)
                    liveSearchGroupMore.value = searchData
                    isLoading = false
                    if (searchData.isEmpty()) isLast = true
                },
                fail = {
                    liveSearchProgress.value = false
                    isLoading = false
                    Timber.e(it)
                }
            )
        }
    }
}
