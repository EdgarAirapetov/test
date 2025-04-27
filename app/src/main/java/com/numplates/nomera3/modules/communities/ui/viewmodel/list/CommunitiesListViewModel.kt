package com.numplates.nomera3.modules.communities.ui.viewmodel.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.domain.usecase.CommunityListEventsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesTopUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityModelMapper
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunitiesListViewModel : ViewModel() {

    @Inject
    lateinit var getCommunities: GetCommunitiesTopUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var communityChangesUseCase: CommunityListEventsUseCase

    val isLoading: Boolean
        get() = communitiesListLoader?.isLoading ?: false

    val isListEndReached: Boolean
        get() = communitiesListLoader?.isListEndReached ?: false

    val event = SingleLiveEvent<CommunityListEvent>()

    private var communitiesListLoader: CommunitiesListLoader? = null

    private var communityModelMapper: CommunityModelMapper = CommunityModelMapper()

    init {
        App.component.inject(this)
        initLoader()
        observeCommunityEvents()
    }

    private fun observeCommunityEvents() {
        viewModelScope.launch {
            communityChangesUseCase.invoke().collect {
                event.postValue(CommunityListEvent.CommunityChanges(it))
            }
        }
    }

    fun onRefresh() {
        event.value = CommunityListEvent.CommunityListLoadingProgress(true)
        communitiesListLoader?.reset()
        loadNext()
    }

    fun loadNext() {
        event.value = CommunityListEvent.CommunityListLoadingProgress(true)
        viewModelScope.launch {
            communitiesListLoader?.loadNext()
        }
    }

    private fun initLoader() {
        communitiesListLoader = CommunitiesListLoader(getCommunities)
        communitiesListLoader?.loadingStateListener = { result: Result<Communities?> ->
            event.value = CommunityListEvent.CommunityListLoadingProgress(false)
            result.fold(
                { communities: Communities? ->
                    if (communities != null) {
                        communityModelMapper
                            .map(communities)
                            .also { list: List<CommunityListItemUIModel> ->
                                event.value = CommunityListEvent.CommunityListLoaded(
                                    isNewList = communities.isNewList,
                                    totalCount = null,
                                    uiModelList = list
                                )
                            }
                    } else {
                        event.value = CommunityListEvent.CommunityListLoaded(
                            isNewList = null,
                            totalCount = null,
                            uiModelList = listOf()
                        )
                    }
                },
                { throwable: Throwable ->
                    Timber.e(throwable)
                    event.value = CommunityListEvent.CommunityListLoadingFailed
                }
            )
        }
    }
}
