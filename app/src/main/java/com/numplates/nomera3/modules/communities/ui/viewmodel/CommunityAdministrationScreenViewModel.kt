package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.domain.mapper.CommunityInfoResponseMapper
import com.numplates.nomera3.modules.communities.domain.usecase.DeleteCommunityUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.DeleteCommunityUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCaseParams
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_ID
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityDeletionSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingStart
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityDashboardScreenEvent.CommunityInfoLoadingSuccess
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunityAdministrationScreenViewModel : BaseViewModel() {

    @Inject
    lateinit var getCommunityInformationUseCase: GetCommunityInformationUseCase

    @Inject
    lateinit var deleteCommunityUseCase: DeleteCommunityUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    var isPrivateCommunity = 1

    var communityId: Int = UNKNOWN_COMMUNITY_ID

    val eventLiveData = SingleLiveEvent<CommunityDashboardScreenEvent>()

    private val communityInfoResponseMapper = CommunityInfoResponseMapper()

    fun injectDependencies() {
        App.component.inject(this)
    }

    fun getCommunityInformation() {
        if (communityId != UNKNOWN_COMMUNITY_ID) {
            viewModelScope.launch {
                eventLiveData.postValue(CommunityInfoLoadingStart())
                GetCommunityInformationUseCaseParams(
                    communityId
                ).also { parameters: GetCommunityInformationUseCaseParams ->
                    getCommunityInformationUseCase.execute(
                        params = parameters,
                        success = { response: Community? ->
                            val uiModel = communityInfoResponseMapper.map(response)
                            if (uiModel != null) {
                                eventLiveData.postValue(CommunityInfoLoadingSuccess(uiModel))
                            } else {
                                eventLiveData.postValue(CommunityInfoLoadingFailed())
                            }
                        },
                        fail = { error: Exception ->
                            Timber.e(error)
                            eventLiveData.postValue(CommunityInfoLoadingFailed())
                        }
                    )
                }
            }
        } else {
            eventLiveData.postValue(CommunityInfoLoadingFailed())
        }
    }

    fun deleteCommunity() {
        if (communityId != UNKNOWN_COMMUNITY_ID) {
            viewModelScope.launch {
                eventLiveData.postValue(CommunityDeletionStart())
                DeleteCommunityUseCaseParams(communityId).also { parameters: DeleteCommunityUseCaseParams ->
                    deleteCommunityUseCase.execute(
                        params = parameters,
                        success = { success: Boolean ->
                            val event = if (success) CommunityDeletionSuccess() else CommunityDeletionFailed()
                            eventLiveData.postValue(event)
                        },
                        fail = { error: Exception ->
                            Timber.e(error)
                            eventLiveData.postValue(CommunityDeletionFailed())
                        }
                    )
                }
            }
        }
    }

    fun deletionCommunityStart() {
        viewModelScope.launch {
            deleteCommunityUseCase.deletionCommunityStart(communityId.toLong())
        }
    }
}
