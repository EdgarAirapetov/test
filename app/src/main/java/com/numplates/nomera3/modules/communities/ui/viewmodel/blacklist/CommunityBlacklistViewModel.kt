package com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.domain.usecase.BlockCommunityMemberUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.CommunitiesUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityUsersUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.UnblockAllUsersUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.UnblockCommunityMemberUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_BLACKLIST_SIZE
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_ID
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel.BlacklistedMemberUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserStarted
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserSuccess
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommunityBlacklistViewModel : BaseViewModel() {

    @Inject
    lateinit var communityMemberUseCase: GetCommunityUsersUseCase

    @Inject
    lateinit var unblockAllUsersUseCase: UnblockAllUsersUseCase

    @Inject
    lateinit var unblockCommunityMemberUseCase: UnblockCommunityMemberUseCase

    val isLoading: Boolean
        get() = communityBlacklistLoader?.isLoading ?: false

    val isListEndReached: Boolean
        get() = communityBlacklistLoader?.isListEndReached ?: false

    val eventLiveData = SingleLiveEvent<CommunityBlacklistScreenEvent>()

    var communityId: Int = UNKNOWN_COMMUNITY_ID

    var communityBlacklistTotalSize: Int = UNKNOWN_COMMUNITY_BLACKLIST_SIZE

    private var communityBlacklistLoader: CommunityBlacklistLoader? = null

    private var communityBlacklistMemberModelMapper: CommunityBlacklistMemberModelMapper =
        CommunityBlacklistMemberModelMapper()

    init {
        App.component.inject(this)
    }

    fun initCommunityBlacklistLoader() {
        if (communityId != UNKNOWN_COMMUNITY_ID) {
            communityBlacklistLoader = CommunityBlacklistLoader(communityId, communityMemberUseCase)
            communityBlacklistLoader?.loadingStateListener = { result: Result<CommunityMembersEntity> ->
                result.fold(
                    { blacklistedMembersEntity: CommunityMembersEntity? ->
                        if (communityBlacklistTotalSize == UNKNOWN_COMMUNITY_BLACKLIST_SIZE) {
                            blacklistedMembersEntity?.totalCount
                                ?.takeIf { it >= 0 }
                                ?.also { communityBlacklistTotalSize = it }
                        }

                        communityBlacklistMemberModelMapper
                            .map(blacklistedMembersEntity)
                            .also { uiModel ->
                                eventLiveData.postValue(BlacklistLoadingSuccess(uiModel))
                            }
                    },
                    { throwable: Throwable ->
                        Timber.e(throwable)
                        eventLiveData.postValue(BlacklistLoadingFailed)
                    }
                )
            }
        }
    }

    fun resetUserCommunityListLoader() {
        communityBlacklistLoader?.reset()
    }

    fun loadNextData() {
        viewModelScope.launch {
            communityBlacklistLoader?.loadNext()
        }
    }

    fun removeMemberFromBlacklist(userId: Long) {
        if (communityId != UNKNOWN_COMMUNITY_ID) {
            val params = BlockCommunityMemberUseCaseParams(communityId, userId)
            viewModelScope.launch {
                eventLiveData.postValue(UnblockingUserStarted)
                unblockCommunityMemberUseCase.execute(
                    params = params,
                    success = { result: Boolean? ->
                        if (result == true) {
                            eventLiveData.postValue(UnblockingUserSuccess(userId))
                        } else {
                            eventLiveData.postValue(UnblockingUserFailed)
                        }
                    },
                    fail = {
                        Timber.e(it)
                        eventLiveData.postValue(UnblockingUserFailed)
                    }
                )
            }
        }
    }

    fun clearBlacklist() {
        if (communityId != UNKNOWN_COMMUNITY_ID) {
            val params = CommunitiesUseCaseParams(communityId)
            viewModelScope.launch {
                unblockAllUsersUseCase.execute(
                    params = params,
                    success = { result: Boolean? ->
                        if (result == true) {
                            eventLiveData.postValue(UnblockingAllUserSuccess)
                        } else {
                            eventLiveData.postValue(UnblockingAllUserFailed)
                        }
                    },
                    fail = {
                        Timber.e(it)
                        eventLiveData.postValue(UnblockingAllUserFailed)
                    }
                )
            }
        }
    }

    sealed class CommunityBlacklistScreenEvent {
        object BlacklistLoadingStarted : CommunityBlacklistScreenEvent()
        object BlacklistLoadingFailed : CommunityBlacklistScreenEvent()
        class BlacklistLoadingSuccess(val uiModel: List<BlacklistedMemberUIModel>) : CommunityBlacklistScreenEvent()

        object UnblockingUserStarted : CommunityBlacklistScreenEvent()
        object UnblockingUserFailed : CommunityBlacklistScreenEvent()
        class UnblockingUserSuccess(val userId: Long) : CommunityBlacklistScreenEvent()

        object UnblockingAllUserFailed : CommunityBlacklistScreenEvent()
        object UnblockingAllUserSuccess : CommunityBlacklistScreenEvent()
    }
}
