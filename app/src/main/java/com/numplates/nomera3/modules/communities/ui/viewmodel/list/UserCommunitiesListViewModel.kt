package com.numplates.nomera3.modules.communities.ui.viewmodel.list

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.domain.usecase.CommunityListEventsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.DeleteCommunityUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityModelMapper
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingProgress
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoaded
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityListLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.list.CommunityListEvent.CommunityChanges
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserCommunitiesListViewModel : BaseViewModel() {

    @Inject
    lateinit var getCommunities: GetCommunitiesUseCase

    @Inject
    lateinit var deleteCommunityUseCase: DeleteCommunityUseCase

    @Inject
    lateinit var communityChangesUseCase: CommunityListEventsUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    val isLoading: Boolean
        get() = communitiesListLoader?.isLoading ?: false

    val isListEndReached: Boolean
        get() = communitiesListLoader?.isListEndReached ?: false

    val event = SingleLiveEvent<CommunityListEvent>()

    // костыль😭 чтоб пользователь не мог открыть удаленное сообщество,
    // может быть кейс когда в списке "Мои сообщества" сообщество еще
    // есть, а на сервере его уже нет, но ответ от сервера на удаление
    // еще не пришел, но пользователь успел кликнуть по удаленному
    // сообществу
    var communityIdDeletionInProgress: Int? = null

    private var communitiesListLoader: CommunitiesListLoader? = null

    private var communityModelMapper: CommunityModelMapper = CommunityModelMapper()
    private var refreshJob: Job? = null

    init {
        App.component.inject(this)
        observeCommunityEvents()
    }

    //  todo need optimize mapping and move into coroutine
    fun initUserCommunityListLoader() {
        communitiesListLoader = CommunitiesListLoader(getCommunities)
        communitiesListLoader?.loadingStateListener = { result: Result<Communities?> ->
            result.fold(
                { communities: Communities? ->
                    event.value = CommunityListLoadingProgress(false)
                    if (communities != null && communities.communityEntities?.isNotEmpty() == true) {
                        communityModelMapper
                            .map(communities)
                            .filter { it.id != communityIdDeletionInProgress }
                            .also { list: List<CommunityListItemUIModel> ->
                                event.value = CommunityListLoaded(
                                    isNewList = communities.isNewList,
                                    totalCount = communities.totalCount,
                                    uiModelList = list
                                )

                            }
                    } else if (communities == null) {
                        clearCommunities()
                    }
                },
                { throwable: Throwable ->
                    Timber.e(throwable)
                    event.value = CommunityListLoadingFailed
                    event.value = CommunityListLoadingProgress(false)
                }
            )
        }
    }

    fun resetUserCommunityListLoader() {
        communitiesListLoader?.reset()
        event.value = CommunityListLoadingProgress(true)
        if (refreshJob?.isCompleted != false) {
            refreshJob = viewModelScope.launch {
                communitiesListLoader?.loadNext()
            }
        }
    }

    fun loadUserCommunityListNext() {
        event.value = CommunityListLoadingProgress(true)
        viewModelScope.launch {
            communitiesListLoader?.loadNext()
        }
    }

    fun resetCommunityIdDeletionInProgress() {
        communityIdDeletionInProgress = null
    }

    private fun observeCommunityEvents() {
        viewModelScope.launch {
            communityChangesUseCase.invoke().collect {
                when (it) {
                   is CommunityListEvents.CreateSuccess -> resetCommunities()
                    else -> event.postValue(CommunityChanges(it))
                }
            }
        }
    }

    private fun resetCommunities() {
        clearCommunities()
        resetUserCommunityListLoader()
    }

    private fun clearCommunities(){
        event.value = CommunityListLoaded(
            isNewList = null,
            totalCount = null,
            uiModelList = listOf()
        )
    }


    override fun onCleared() {
        super.onCleared()
        refreshJob?.cancel()
    }
}
