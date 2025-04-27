package com.numplates.nomera3.modules.services.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.DeleteFriendCancelSubscriptionUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.RemoveRelatedUserUseCase
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersUseCase
import com.numplates.nomera3.modules.services.domain.usecase.GetCommunitiesSuspendUseCase
import com.numplates.nomera3.modules.services.domain.usecase.GetRecentUsersUseCase
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecentUsersUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesRecommendedPeopleUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiAction
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiEffect
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUiModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesUserUiModel
import com.numplates.nomera3.modules.services.ui.mapper.MeeraServicesCommunitiesMapper
import com.numplates.nomera3.modules.services.ui.mapper.MeeraServicesContentMapper
import com.numplates.nomera3.modules.services.ui.mapper.MeeraServicesRecentsMapper
import com.numplates.nomera3.modules.services.ui.mapper.MeeraServicesUserProfileMapper
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.BlockSuggestionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EmitSuggestionRemovedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val DEFAULT_OFFSET = 0

private const val CLEAR_RECENT_DELAY_SEC = 5
private const val CLEAR_RECENT_DELAY_MS = CLEAR_RECENT_DELAY_SEC * 1000L

class MeeraServicesViewModel @Inject constructor(
    private val getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase,
    private val getRecentUsersUseCase: GetRecentUsersUseCase,
    private val searchCleanRecentUseCase: SearchCleanRecentUsersUseCase,
    private val getRelatedUsersUseCase: GetRelatedUsersUseCase,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val removeFriendRequestUseCase: DeleteFriendCancelSubscriptionUseCase,
    private val getCommunitiesSuspendUseCase: GetCommunitiesSuspendUseCase,
    private val profileMapper: MeeraServicesUserProfileMapper,
    private val communitiesMapper: MeeraServicesCommunitiesMapper,
    private val recentsMapper: MeeraServicesRecentsMapper,
    private val contentMapper: MeeraServicesContentMapper,
    private val pushSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val getRelatedUsersAndCacheUseCase: GetRelatedUsersAndCacheUseCase,
    private val blockSuggestionUseCase: BlockSuggestionUseCase,
    private val emitSuggestionRemovedUseCase: EmitSuggestionRemovedUseCase,
    private val removeRelatedUserUseCase: RemoveRelatedUserUseCase,
    private val networkStatusProvider: NetworkStatusProvider,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
) : ViewModel() {

    private val _servicesContentState = MutableStateFlow<List<MeeraServicesUiModel>>(emptyList())
    val servicesContentState: StateFlow<List<MeeraServicesUiModel>> = _servicesContentState

    private val _servicesUiEffect = MutableSharedFlow<MeeraServicesUiEffect>()
    val servicesUiEffect: SharedFlow<MeeraServicesUiEffect> = _servicesUiEffect

    private var clearRecentJob: Job? = null
    private var recentList: List<RecentUserUiModel> = emptyList()

    var isRecommendationLoading = false
        private set
    var isRecommendationLast = false
        private set

    var isCommunitiesLoading = false
        private set
    var isCommunitiesLast = false
        private set

    init {
        observeFriendStatusChanged()
    }

    fun handleUiAction(action: MeeraServicesUiAction) {
        when (action) {
            is MeeraServicesUiAction.LoadNextRecommendedUsers -> handleLoadNextRecommendedUsers(
                action.offsetCount,
                action.rootAdapterPosition
            )

            is MeeraServicesUiAction.LoadNextCommunities -> handleLoadNextCommunities(
                action.offset,
                action.rootAdapterPosition
            )

            is MeeraServicesUiAction.AddRecommendedUserToFriendsClick -> handleAddToFriendClick(action.user)
            is MeeraServicesUiAction.RemoveRecommendedUserFromFriendsClick -> handleRemoveFromFriendsAction(action.user.userId)
            is MeeraServicesUiAction.RemoveRecommendedUserClick -> handleHideRelatedUserAction(action.user.userId)
            is MeeraServicesUiAction.PeoplesClick -> handlePeoplesClick()
            is MeeraServicesUiAction.ClearRecentUsersClick -> handleClearRecentsClick()
            is MeeraServicesUiAction.CancelClearingRecentUsersClick -> handleCancelClearRecentsClick()
            is MeeraServicesUiAction.SettingsClick -> handleSettingsClick()
            is MeeraServicesUiAction.RecommendedUserClick -> handleUserClick(action.user.userId)
            is MeeraServicesUiAction.CommunitiesClick -> handleCommunitiesClick()
            is MeeraServicesUiAction.CommunityClick -> handleCommunityClick(action.communityId)
            is MeeraServicesUiAction.EventsClick -> handleEventsClick()
            is MeeraServicesUiAction.UserClick -> handleUserClick(action.userId)
            is MeeraServicesUiAction.UserMomentClick -> {
                emitUiEffect(MeeraServicesUiEffect.NavigateToMoments(action.userId))
                emitUiState(servicesContentState.value.map { item ->
                    if (item is MeeraServicesUserUiModel) item.apply {
                        storiesStateEnum = UserpicStoriesStateEnum.VIEWED
                    } else item
                })
            }
        }
    }

    private fun handleEventsClick() {
        emitUiEffect(MeeraServicesUiEffect.NavigateToEvents)
    }

    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach(::handleFriendStatusChanged)
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(effect: UserSettingsEffect) {
        when (effect) {
            is UserSettingsEffect.UserFriendStatusChanged -> {
                loadPageContent()
            }

            is UserSettingsEffect.UserBlockStatusChanged -> {
                loadPageContent()
            }

            is UserSettingsEffect.SuggestionRemoved -> {
                removeRelatedUserById(effect.userId)
            }

            else -> Unit
        }
    }

    private fun removeRelatedUserById(userId: Long) {
        val recommendationType = _servicesContentState.value
            .filterIsInstance<MeeraServicesRecommendedPeopleUiModel>()
            .firstOrNull()
        val currentRecommendations = recommendationType?.users ?: emptyList()
        if (currentRecommendations.isEmpty()) return
        val newRecommendations = currentRecommendations.toMutableList()
        newRecommendations.removeAll { user ->
            user.userId == userId
        }
        val newRecommendationsListEntity = MeeraServicesRecommendedPeopleUiModel(newRecommendations)
        val listResult = _servicesContentState.value.map { entity ->
            if (entity is MeeraServicesRecommendedPeopleUiModel) newRecommendationsListEntity else entity
        }.filterNot { entity ->
            entity is MeeraServicesRecommendedPeopleUiModel && entity.users.isEmpty()
        }
        emitUiState(listResult)
    }

    private fun handleUserClick(userId: Long) {
        emitUiEffect(MeeraServicesUiEffect.NavigateToUserProfile(userId))
    }

    private fun handlePeoplesClick() {
        emitUiEffect(MeeraServicesUiEffect.NavigateToPeoples)
    }

    private fun handleSettingsClick() {
        emitUiEffect(MeeraServicesUiEffect.NavigateToSettings)
    }

    private fun handleCommunitiesClick() {
        emitUiEffect(MeeraServicesUiEffect.NavigateToCommunities)
    }

    private fun handleCommunityClick(communityId: Int) {
        emitUiEffect(MeeraServicesUiEffect.NavigateToCommunity(communityId))
    }

    private fun handleCancelClearRecentsClick() {
        clearRecentJob?.cancel()
        handleRecentsClearingCancelled()
    }

    private fun handleClearRecentsClick(force: Boolean = false) {
        handleRecentsCleared()
        if (!force) {
            emitUiEffect(MeeraServicesUiEffect.ShowClearRecentsToast(CLEAR_RECENT_DELAY_SEC))
        }

        clearRecentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!force) {
                delay(CLEAR_RECENT_DELAY_MS)
            }

            searchCleanRecentUseCase.execute(
                params = SearchCleanRecentUsersParams(),
                success = {
                    handleRecentsCleared()
                },
                fail = { exception ->
                    if (exception is CancellationException) return@execute
                    emitUiEffect(MeeraServicesUiEffect.ShowErrorToast(R.string.error_try_later))
                    Timber.e(exception)
                }
            )
        }
    }

    private fun handleRecentsCleared() {
        val currentList = _servicesContentState.value
        emitUiState(currentList.filterNot { it is MeeraServicesRecentUsersUiModel })
    }

    private fun handleRecentsClearingCancelled() {
        if (recentList.isEmpty()) return
        val currentList = _servicesContentState.value
        val currentListWithRecents = currentList.toMutableList()
        currentListWithRecents.add(2, MeeraServicesRecentUsersUiModel(recentList))
        emitUiState(currentListWithRecents)
    }

    private fun handleLoadNextCommunities(
        offset: Int,
        rootAdapterPosition: Int
    ) {
        isCommunitiesLoading = true
        viewModelScope.launch {
            runCatching {
                getCommunities(offset).communities
            }.onSuccess { communities ->
                isCommunitiesLoading = false
                val currentList = _servicesContentState.value
                if (rootAdapterPosition != -1) {
                    val listResult = contentMapper.mapPaginationCommunitiesListToUiList(
                        currentList = currentList,
                        newList = communities,
                        rootListPosition = rootAdapterPosition
                    )
                    emitUiState(listResult)
                }
                isCommunitiesLast = communities.isEmpty() || communities.size < DEFAULT_PAGE_LIMIT
            }.onFailure { e ->
                isCommunitiesLoading = false
                isCommunitiesLast = false
                Timber.e(e)
            }
        }
    }

    private fun handleLoadNextRecommendedUsers(
        offsetCount: Int,
        rootAdapterPosition: Int
    ) {
        getNextRelatedUsers(
            offset = offsetCount,
            rootListPosition = rootAdapterPosition
        )
    }

    private fun handleHideRelatedUserAction(userId: Long) {
        viewModelScope.launch {
            runCatching {
                blockSuggestionUseCase.invoke(userId)
                removeRelatedUserUseCase.invoke(userId)
            }.onSuccess {
                emitSuggestionRemovedUseCase.invoke(userId)
            }.onFailure { e ->
                Timber.e(e)
                handleError()
            }
        }
    }

    private fun handleRemoveFromFriendsAction(userId: Long) {
        viewModelScope.launch {
            runCatching { removeFriendRequestUseCase.invoke(userId) }
                .onSuccess {
                    pushSubscriptionChange(
                        userId = userId,
                        isSubscribed = false
                    )
                    handleFriendStatusResponse(
                        userId = userId,
                        isAddToFriendRequest = false
                    )
                    emitUiEffect(
                        MeeraServicesUiEffect.ShowSuccessToast(R.string.friends_cancel_friend_request_and_unsubscribe_success)
                    )
                }.onFailure { e ->
                    Timber.e(e)
                }
        }
    }

    private fun handleAddToFriendClick(entity: RecommendedPeopleUiEntity) {
        val userId = entity.userId
        viewModelScope.launch {
            runCatching { addUserToFriendUseCase.invoke(userId) }
                .onSuccess {
                    pushSubscriptionChange(
                        userId = userId,
                        isSubscribed = true
                    )
                    handleFriendStatusResponse(
                        userId = userId,
                        isAddToFriendRequest = true
                    )
                    if (entity.subscriptionOn.toBoolean()) {
                        emitUiEffect(MeeraServicesUiEffect.ShowSuccessToast(R.string.request_sent))
                    } else {
                        emitUiEffect(MeeraServicesUiEffect.ShowSuccessToast(R.string.request_friend_sent_notif_on))
                    }
                }.onFailure { e ->
                    Timber.e(e)
                }
        }
    }

    private suspend fun pushSubscriptionChange(
        userId: Long,
        isSubscribed: Boolean
    ) {
        pushSubscribeUserUseCase.execute(
            UpdateSubscriptionUserParams(
                userId = userId,
                isSubscribed = isSubscribed,
                needToHideFollowButton = false,
                isBlocked = false
            ), {}, {})
    }

    private fun handleFriendStatusResponse(
        userId: Long,
        isAddToFriendRequest: Boolean
    ) {
        val result = contentMapper.getUpdatedUserFriendStatusListById(
            currentList = _servicesContentState.value,
            selectedUserId = userId,
            isUserSubscribed = isAddToFriendRequest
        )
        emitUiState(result)
        requestRelatedUsersAndCache()
    }

    private fun requestRelatedUsersAndCache() {
        viewModelScope.launch {
            runCatching {
                getRelatedUsersAndCacheUseCase.invoke(
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = DEFAULT_OFFSET
                )
            }.onFailure { e ->
                Timber.d(e)
            }
        }
    }

    private fun getNextRelatedUsers(
        offset: Int = DEFAULT_OFFSET,
        rootListPosition: Int = -1
    ) {
        isRecommendationLoading = true
        viewModelScope.launch {
            runCatching {
                getRelatedUsersUseCase.invoke(
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = offset
                )
            }.onSuccess { relatedUsers ->
                isRecommendationLoading = false
                val currentList = _servicesContentState.value
                if (rootListPosition != -1) {
                    val listResult = contentMapper.mapPaginationRecommendedListToUiList(
                        currentList = currentList,
                        newList = relatedUsers,
                        rootListPosition = rootListPosition
                    )
                    emitUiState(listResult)
                }
                isRecommendationLast = relatedUsers.isEmpty() || relatedUsers.size < DEFAULT_PAGE_LIMIT
            }.onFailure { e ->
                isRecommendationLoading = false
                isRecommendationLast = false
                Timber.e(e)
            }
        }
    }

    fun loadPageContent() {
        viewModelScope.launch {
            runCatching {
                val userProfileAsync = async { getOwnLocalProfileUseCase.invoke() }
                recentList = async {
                    runCatching {
                        getRecentUsersUseCase.invoke().map(recentsMapper::mapRecents)
                    }.getOrElse { emptyList() }
                }.await()
                val recommendedPeople = async {
                    runCatching {
                        getRelatedUsersUseCase.invoke(DEFAULT_PAGE_LIMIT, DEFAULT_OFFSET)
                    }.getOrElse { emptyList() }
                }
                val communities = async {
                    runCatching {
                        getCommunities()
                    }.getOrElse { MeeraServicesCommunitiesUiModel(0, emptyList()) }
                }
                val userProfile = userProfileAsync.await()
                val userProfileModel = if (userProfile != null) profileMapper.mapUserProfile(userProfile) else null

                createAndShowContent(
                    userProfileModel = userProfileModel,
                    recents = recentList,
                    recommendedPeople = recommendedPeople.await(),
                    communities = communities.await()
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private suspend fun getCommunities(offset: Int = DEFAULT_OFFSET): MeeraServicesCommunitiesUiModel {
        val communities = getCommunitiesSuspendUseCase.invoke(DEFAULT_PAGE_LIMIT, offset)
        return communitiesMapper.mapCommunities(communities)
    }

    private fun createAndShowContent(
        userProfileModel: MeeraServicesUserUiModel?,
        recents: List<RecentUserUiModel>,
        recommendedPeople: List<PeopleRelatedUserModel>,
        communities: MeeraServicesCommunitiesUiModel,
    ) {
        val content = contentMapper.mapContent(
            userProfileModel,
            recents,
            recommendedPeople,
            communities
        )
        emitUiState(content)
    }

    private fun handleError() {
        if (!networkStatusProvider.isInternetConnected()) {
            emitUiEffect(MeeraServicesUiEffect.ShowErrorToast(R.string.no_internet))
        } else {
            emitUiEffect(MeeraServicesUiEffect.ShowErrorToast(R.string.error_message_went_wrong))
        }
    }

    private fun emitUiState(state: List<MeeraServicesUiModel>) {
        viewModelScope.launch {
            _servicesContentState.emit(state)
        }
    }

    private fun emitUiEffect(effect: MeeraServicesUiEffect) {
        viewModelScope.launch {
            _servicesUiEffect.emit(effect)
        }
    }

    companion object {
        const val DEFAULT_PAGE_LIMIT = 20
    }

}
