package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityFollow
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.domain.usecase.CommunitiesUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.SubscribeCommunityUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.UnsubscribeCommunityUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityViewEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunitySubscriptionViewModel @Inject constructor(
    private val subscribeUseCase: SubscribeCommunityUseCase,
    private val unsubscribeUseCase: UnsubscribeCommunityUseCase,
    private val analyticsHelper: AnalyticsInteractor,
    private val getUserUidUseCase: GetUserUidUseCase
): ViewModel() {

    private val _viewEvent = MutableSharedFlow<CommunityViewEvent>()
    val viewEvent = _viewEvent.asSharedFlow()

    fun subscribeCommunity(
        community: CommunityListItemUIModel?,
        where: AmplitudePropertyWhereCommunityFollow,
        position: Int? = null
    ) {
        Timber.d("Subscribe group id: ${community?.id}")
        if (community == null) return
        val communityId = community.id ?: return

        viewModelScope.launch {
            _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(true))
            runCatching {
                subscribeUseCase.invoke(CommunitiesUseCaseParams(communityId))
            }.onSuccess {
                Timber.d("Successfully group subscribe")
                community.isMember = true
                community.userStatus = CommunityEntity.USER_STATUS_APPROVED
                val event = if (community.isPrivate) {
                    CommunityViewEvent.SuccessSubscribePrivateCommunity(community.id)
                } else {
                    CommunityViewEvent.SuccessSubscribeCommunity(community.id)
                }
                event.position = position
                _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(false))
                _viewEvent.emit(event)
                analyticsHelper.logCommunityFollow(
                    userId = getUserUidUseCase.invoke(),
                    where = where,
                    communityId = communityId
                )
            }.onFailure {
                Timber.e(it)
                _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(false))
                _viewEvent.emit(CommunityViewEvent.FailureSubscribeCommunity)
            }
        }
    }

    fun unsubscribeCommunity(
        community: CommunityEntity?,
        position: Int? = null
    ) {
        Timber.d("Unsubscribe group id: ${community?.groupId}")
        if (community == null) return
        val communityId = community.groupId

        viewModelScope.launch {
            _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(true))
            runCatching {
                unsubscribeUseCase.invoke(CommunitiesUseCaseParams(communityId))
            }.onSuccess {
                Timber.d("Successfully group unsubscribe")
                community.isSubscribed = UNSUBSCRIBED
                community.userStatus = CommunityEntity.USER_STATUS_UNSUBSCRIBED
                val event = if (community.private == 1) {
                    CommunityViewEvent.SuccessUnsubscribePrivateCommunity(community.groupId)
                } else {
                    CommunityViewEvent.SuccessUnsubscribeCommunity(community.groupId)
                }
                event.position = position
                _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(false))
                _viewEvent.emit(event)
                analyticsHelper.logCommunityUnFollow(
                    userId = getUserUidUseCase.invoke(),
                    communityId = communityId
                )
            }.onFailure {
                Timber.e(it)
                _viewEvent.emit(CommunityViewEvent.SubscribeCommunityProgress(false))
                _viewEvent.emit(CommunityViewEvent.FailureUnsubscribeCommunity)
            }
        }
    }

    companion object {
        const val UNSUBSCRIBED = 0
        const val SUBSCRIBED = 1
    }
}
