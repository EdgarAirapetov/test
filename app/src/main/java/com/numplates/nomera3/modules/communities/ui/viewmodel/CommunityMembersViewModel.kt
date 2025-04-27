package com.numplates.nomera3.modules.communities.ui.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.domain.usecase.BlockCommunityMemberUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.BlockCommunityMemberUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityUsersUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.MeeraGetCommunityUsersUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.MeeraGetCommunityUsersUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.RemoveCommunityMemberUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.RemoveCommunityMemberUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.UnblockCommunityMemberUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.admin.AddCommunityAdminUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.admin.CommunityAdminUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.admin.RemoveCommunityAdminUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.approvedecline.ApproveMembershipRequestUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.approvedecline.DeclineMembershipRequestUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.approvedecline.MembershipRequestUseCaseParams
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val USER_TYPE = "UserSimpleWithGroup"

class CommunityMembersViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var getCommunityUsersUseCase: GetCommunityUsersUseCase

    @Inject
    lateinit var getMeeraCommunityUsersUseCase: MeeraGetCommunityUsersUseCase

    @Inject
    lateinit var blockCommunityMemberUseCase: BlockCommunityMemberUseCase

    @Inject
    lateinit var unblockCommunityMemberUseCase: UnblockCommunityMemberUseCase

    @Inject
    lateinit var addMemberToAdminsUseCase: AddCommunityAdminUseCase

    @Inject
    lateinit var removeMemberFromAdminsUseCase: RemoveCommunityAdminUseCase

    @Inject
    lateinit var approveMembershipRequestUseCase: ApproveMembershipRequestUseCase

    @Inject
    lateinit var declineMembershipRequestUseCase: DeclineMembershipRequestUseCase

    @Inject
    lateinit var removeCommunityMemberUseCase: RemoveCommunityMemberUseCase

    @Inject
    lateinit var getCommunityInformationUseCase: GetCommunityInformationUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    val liveViewEvent = MutableLiveData<CommunityMembersViewEvent>()

    var isLoading = false
    var isLastPage = false

    private var searchQuery: String? = null
    private var groupId: Int? = null
    private var userRole: Int = CommunityUserRole.REGULAR
    private var lastUnapprovedCount = 0

    init {
        App.component.inject(this)
    }

    fun logScreenForFragment(screenName: String) = fbAnalytic.logScreenForFragment(screenName)

    fun getUserUid() = getUserUidUseCase.invoke()

    fun isHiddenAgeAndGender() = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    fun bind(groupId: Int?, userRole: Int = CommunityUserRole.REGULAR) {
        if (this.groupId != groupId) {
            this.groupId = groupId
        }
        this.userRole = userRole
    }

    fun getUserRole() = userRole

    fun getData(
        quantity: Int = DEFAULT_USERS_QUANTITY,
        userState: Int
    ) {
        clearMembers(userState)
        isLastPage = false
        getCommunityUsers(INITIAL_START_INDEX, quantity, userState, null)
    }

    fun getSearchQuery() = searchQuery

    fun search(searchQuery: String) {
        isLastPage = false
        getCommunityUsers(
            INITIAL_START_INDEX,
            DEFAULT_USERS_QUANTITY,
            CommunityMemberState.APPROVED,
            searchQuery
        )
    }

    fun newSearch(userState: Int, searchQuery: String) {
        isLastPage = false
        getCommunityUsers(
            INITIAL_START_INDEX,
            DEFAULT_USERS_QUANTITY,
            userState,
            searchQuery
        )
    }

    fun loadMore(startIndex: Int, userState: Int) {
        getCommunityUsers(startIndex, DEFAULT_USERS_QUANTITY, userState, null)
    }

    private fun getCommunityUsers(startIndex: Int, quantity: Int, userState: Int, searchQuery: String?) {
        Timber.d("Get community USERS: grId: $groupId")
        this.searchQuery = searchQuery
        eventMembersProgress(userState, true)
        isLoading = true
        groupId?.let { id ->
            viewModelScope.launch {
                runCatching {
                    getMeeraCommunityUsersUseCase.invoke(
                        params = MeeraGetCommunityUsersUseCaseParams(
                            groupId = id,
                            userType = USER_TYPE,
                            startIndex = startIndex,
                            quantity = quantity,
                            userState = userState,
                            query = searchQuery
                        )
                    )
                }.onSuccess { usersModel ->
                    Timber.d("RESPONSE Get community USERS: ${Gson().toJson(usersModel)}")
                    eventMembersProgress(userState, false)
                    isLoading = false
                    isLastPage = usersModel.data.users?.isEmpty() ?: true
                    when {
                        !searchQuery.isNullOrEmpty() -> {
                            liveViewEvent.value =
                                CommunityMembersViewEvent.SuccessSearchMembers(
                                    members = usersModel.data,
                                    userState = userState,
                                    isLoadMore = startIndex > INITIAL_START_INDEX
                                )
                        }

                        searchQuery.isNullOrEmpty() && userState == CommunityMemberState.APPROVED -> {
                            liveViewEvent.value =
                                CommunityMembersViewEvent.SuccessGetApprovedMembers(
                                    members = usersModel.data,
                                    isLoadMore = startIndex > INITIAL_START_INDEX
                                )
                        }

                        searchQuery.isNullOrEmpty() && userState == CommunityMemberState.NOT_APPROVED -> {
                            liveViewEvent.value =
                                CommunityMembersViewEvent
                                    .SuccessGetNotApprovedMembers(
                                        members = usersModel.data,
                                        isLoadMore = startIndex > INITIAL_START_INDEX
                                    )
                        }
                    }

                    if (userState == CommunityMemberState.NOT_APPROVED && searchQuery.isNullOrEmpty()) {
                        lastUnapprovedCount = usersModel.data.totalCount ?: 0
                        liveViewEvent.value = CommunityMembersViewEvent
                            .SuccessGetJoinRequestsCount(usersModel.data.totalCount ?: 0)
                    }
                }.onFailure {
                    Timber.e(it)
                    eventMembersProgress(userState, false)
                    liveViewEvent.value = CommunityMembersViewEvent.FailGetMembers
                    isLoading = false
                }
            }
        }
    }

    fun blockMember(userId: Long, position: Int, userState: Int) {
        Timber.d("Community BLOCK USER: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                blockCommunityMemberUseCase.execute(
                    params = BlockCommunityMemberUseCaseParams(groupId, userId),
                    success = {
                        Timber.d("Community BLOCK USER: SUCCESS")
                        when (userState) {
                            CommunityMemberState.APPROVED -> liveViewEvent.value =
                                CommunityMembersViewEvent.SuccessfullyBlockedApprovedMember(position)

                            CommunityMemberState.NOT_APPROVED -> liveViewEvent.value =
                                CommunityMembersViewEvent
                                    .SuccessfullyBlockedNotApprovedMember(position)
                        }
                    },
                    fail = {
                        liveViewEvent.value = CommunityMembersViewEvent.FailedBlockingMember
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun setMemberAdminStatus(
        userId: Long,
        position: Int,
        userState: Int
    ) {
        Timber.d("Community ADD USER TO ADMINS: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                addMemberToAdminsUseCase.execute(
                    params = CommunityAdminUseCaseParams(groupId, userId),
                    success = {
                        Timber.d("Community ADD USER TO ADMINS: SUCCESS")
                        when (userState) {
                            CommunityMemberState.APPROVED -> liveViewEvent.value =
                                CommunityMembersViewEvent
                                    .SuccessfullyAddedApprovedMemberToAdmins(position)

                            CommunityMemberState.NOT_APPROVED -> liveViewEvent.value =
                                CommunityMembersViewEvent
                                    .SuccessfullyAddedNotApprovedMemberToAdmins(position)
                        }
                    },
                    fail = {
                        liveViewEvent.value = CommunityMembersViewEvent.FailedAddingMemberToAdmins
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun removeMemberAdminStatus(userId: Long, position: Int) {
        Timber.d("Community REMOVE USER FROM ADMINS: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                removeMemberFromAdminsUseCase.execute(
                    params = CommunityAdminUseCaseParams(groupId, userId),
                    success = {
                        Timber.d("Community REMOVE USER FROM ADMINS: SUCCESS")
                        liveViewEvent.value =
                            CommunityMembersViewEvent.SuccessfullyRemovedMemberFromAdmins(position)
                    },
                    fail = {
                        liveViewEvent.value =
                            CommunityMembersViewEvent.FailedRemovingMemberFromAdmins
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun approveMembershipRequest(
        userId: Long,
        position: Int,
        setAsAdmin: Boolean
    ) {
        Timber.d("Community APPROVE MEMBERSHIP REQUEST: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                approveMembershipRequestUseCase.execute(
                    params = MembershipRequestUseCaseParams(groupId, userId),
                    success = { success ->
                        Timber.d("Community APPROVE MEMBERSHIP REQUEST: $success")
                        when {
                            success && setAsAdmin -> setMemberAdminStatus(
                                userId,
                                position,
                                CommunityMemberState.NOT_APPROVED
                            )

                            success -> liveViewEvent.value = CommunityMembersViewEvent
                                .SuccessfullyApprovedMembershipRequest(position)

                            else -> liveViewEvent.value = CommunityMembersViewEvent
                                .FailedApprovingMembershipRequest
                        }
                        decrementJoinRequestsCount()
                    },
                    fail = {
                        liveViewEvent.value =
                            CommunityMembersViewEvent.FailedApprovingMembershipRequest
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun declineMembershipRequest(
        userId: Long,
        position: Int,
        setBlocked: Boolean = false
    ) {
        Timber.d("Community DECLINE MEMBERSHIP REQUEST: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                declineMembershipRequestUseCase.execute(
                    params = MembershipRequestUseCaseParams(groupId, userId),
                    success = { success ->
                        Timber.d("Community DECLINE MEMBERSHIP REQUEST: $success")
                        when {
                            success && setBlocked -> blockMember(
                                userId,
                                position,
                                CommunityMemberState.NOT_APPROVED
                            )

                            success && !setBlocked -> liveViewEvent.value =
                                CommunityMembersViewEvent
                                    .SuccessfullyDeclinedMembershipRequest(position)

                            else -> liveViewEvent.value =
                                CommunityMembersViewEvent.FailedDecliningMembershipRequest
                        }
                        decrementJoinRequestsCount()
                    },
                    fail = {
                        liveViewEvent.value =
                            CommunityMembersViewEvent.FailedDecliningMembershipRequest
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun removeCommunityMember(
        userId: Long,
        position: Int
    ) {
        Timber.d("Community REMOVE MEMBER REQUEST: grId: $groupId")
        groupId?.let { groupId ->
            viewModelScope.launch {
                removeCommunityMemberUseCase.execute(
                    params = RemoveCommunityMemberUseCaseParams(groupId, userId),
                    success = { success ->
                        Timber.d("Community REMOVE MEMBER REQUEST: $success")
                        if (success) {
                            liveViewEvent.value =
                                CommunityMembersViewEvent.SuccessfullyRemovedMember(position)
                        }
                    },
                    fail = {
                        liveViewEvent.value = CommunityMembersViewEvent.FailedRemovingMember
                        Timber.e(it)
                    }
                )
            }
        }
    }

    fun refreshApprovedMembers(userState: Int) {
        liveViewEvent.value = when (userState) {
            CommunityMemberState.NOT_APPROVED -> CommunityMembersViewEvent.RefreshNotApprovedMembers
            else -> CommunityMembersViewEvent.RefreshApprovedMembers
        }
    }

    fun clearSearchMembers(userState: Int) {
        liveViewEvent.value = when (userState) {
            CommunityMemberState.NOT_APPROVED -> CommunityMembersViewEvent.ClearSearchNoApprovedMembers
            else -> CommunityMembersViewEvent.ClearSearchApprovedMembers
        }
    }

    private fun decrementJoinRequestsCount() {
        lastUnapprovedCount -= 1
        liveViewEvent.value = CommunityMembersViewEvent
            .SuccessGetJoinRequestsCount(lastUnapprovedCount)
    }

    private fun clearMembers(userState: Int) {
        liveViewEvent.value = when (userState) {
            CommunityMemberState.NOT_APPROVED -> CommunityMembersViewEvent.ClearNotApprovedMembers
            else -> CommunityMembersViewEvent.ClearApprovedMembers
        }
    }

    private fun eventMembersProgress(userState: Int, inProgress: Boolean) {
        liveViewEvent.value = when (userState) {
            CommunityMemberState.NOT_APPROVED ->
                CommunityMembersViewEvent.ProgressGetNotApprovedMembers(inProgress)

            else -> CommunityMembersViewEvent.ProgressGetApprovedMembers(inProgress)
        }
    }

    fun updateGroupInfo() {
        groupId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                getCommunityInformationUseCase.execute(
                    params = GetCommunityInformationUseCaseParams(id),
                    success = { community ->
                        viewModelScope.launch(Dispatchers.Main) {
                            updateUserRoleWithCommunity(community)
                        }
                    },
                    fail = {
                        Timber.e(it)
                    }
                )
            }
        }
    }

    private fun updateUserRoleWithCommunity(community: Community?) {
        community?.let {
            userRole = when {
                it.community?.isAuthor == 1 -> CommunityUserRole.AUTHOR
                it.community?.isModerator == 1 -> CommunityUserRole.MODERATOR
                else -> CommunityUserRole.REGULAR
            }
        }
    }

    companion object {
        const val INITIAL_START_INDEX = 0
        const val COMMUNITY_DETAILS_BOTTOM_SHEET_USERS_QUANTITY = 5
        const val DEFAULT_USERS_QUANTITY = 20
    }
}
