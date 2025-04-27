package com.numplates.nomera3.modules.peoples.ui.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.permission.ReadContactsPermissionProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.data.workers.SYNC_COUNT
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.DeleteFriendCancelSubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.GetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.ObserveSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.SetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.StartSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.UnsubscribeUserUseCaseNew
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleContentCardProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideSectionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsActionTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsSuccessActionTypeProperty
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.usecase.GetApprovedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetNeedShowSyncContactsDialogUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetPeopleAllSavedContentUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetPeopleOnboardingShownUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetTopUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.NeedShowPeopleBadgeUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.RemoveRelatedUserUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetNeedShowSyncContactsDialogUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetPeopleBadgeShownUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetPeopleOnboardingShownUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetSelectCommunityTooltipShownUseCase
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggersPlaceHolderUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.delegate.PeopleAnalyticDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiStates
import com.numplates.nomera3.modules.peoples.ui.mapper.MeeraPeopleContentUiMapper
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.BlockSuggestionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EmitSuggestionRemovedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_OFFSET = 0
private const val REFRESH_SWIPE_STATE = true
private const val TOP_PEOPLE_CONTENT_POSITION = 0
private const val DELAY_NAVIGATION_BAR_CLICK_REQUEST = 1500L
private const val DELAY_PEOPLE_WELCOME = 100L
private const val DELAY_NAVIGATING_TO_USER_FROM_PUSH = 2000L

class MeeraPeoplesViewModel @Inject constructor(
    private val peopleContentUiMapper: MeeraPeopleContentUiMapper,
    private val networkStatusProvider: NetworkStatusProvider,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val unsubscribeUserUseCase: UnsubscribeUserUseCaseNew,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val removeFriendRequestUseCase: DeleteFriendCancelSubscriptionUseCase,
    private val setSelectCommunityTooltipShownUseCase: SetSelectCommunityTooltipShownUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val setPeopleOnboardingShownUseCase: SetPeopleOnboardingShownUseCase,
    private val getPeopleOnboardingShownUseCase: GetPeopleOnboardingShownUseCase,
    private val dialogDismissListener: DialogDismissListener,
    private val peopleAnalyticDelegate: PeopleAnalyticDelegate,
    private val getApprovedUsersUseCase: GetApprovedUsersUseCase,
    private val getRelatedUsersUseCase: GetRelatedUsersUseCase,
    private val getPeopleAllSavedContentUseCase: GetPeopleAllSavedContentUseCase,
    private val pushSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val getRelatedUsersAndCacheUseCase: GetRelatedUsersAndCacheUseCase,
    private val getTopUsersAndCacheUseCase: GetTopUsersAndCacheUseCase,
    private val readContactsPermissionProvider: ReadContactsPermissionProvider,
    private val observeSyncContactsUseCase: ObserveSyncContactsUseCase,
    private val startSyncContactsUseCase: StartSyncContactsUseCase,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val getSyncContactsPrivacyUseCase: GetSyncContactsPrivacyUseCase,
    private val setSyncContactsPrivacyUseCase: SetSyncContactsPrivacyUseCase,
    private val blockSuggestionUseCase: BlockSuggestionUseCase,
    private val emitSuggestionRemovedUseCase: EmitSuggestionRemovedUseCase,
    private val setNeedShowSyncContactsDialogUseCase: SetNeedShowSyncContactsDialogUseCase,
    private val getNeedShowSyncContactsDialogUseCase: GetNeedShowSyncContactsDialogUseCase,
    private val removeRelatedUserUseCase: RemoveRelatedUserUseCase,
    private val setPeopleBadgeShownUseCase: SetPeopleBadgeShownUseCase,
    private val needShowPeopleBadgeUseCase: NeedShowPeopleBadgeUseCase,
    private val friendInviteTapAnalytics: FriendInviteTapAnalytics,
    private val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase
) : ViewModel() {

    private val _peoplesContentState = MutableLiveData<PeopleUiStates>()
    val peoplesContentState: LiveData<PeopleUiStates> = _peoplesContentState

    private val _peoplesContentEvent = MutableSharedFlow<PeopleUiEffect>()
    val peoplesContentEvent: SharedFlow<PeopleUiEffect> = _peoplesContentEvent

    var isRecommendationLoading = false
        private set
    var isRecommendationLast = false
        private set
    var isTopContentLoading = false
        private set
    var isTopContentLast = false
        private set
    private var isNeedUpdate: Boolean = true
    private var topUsersOffset: Int = DEFAULT_OFFSET
    private var isOpenedFromSyncContactsWelcome: Boolean = false
    private var isOnboardingAlreadyShown: Boolean = false

    fun init(userIdFromPush: Long? = null) {
        handleNavigatingToUserFromPush(userIdFromPush)
        initShimmerContent()
        initObservers()
        initDialogDismissListener()
        getSavedContent()
        observeMomentsEvents()
        userIdFromPush ?: return
        refreshPeopleContent(userIdFromPush = userIdFromPush)
    }

    fun handleContentAction(contentActions: FriendsContentActions) {
        when (contentActions) {
            FriendsContentActions.FindFriendsContentUiActions -> {
                handleMainSearchOpenClicked()
            }
            FriendsContentActions.OnReferralClicked -> {
                handleReferralScreenClicked()
            }
            FriendsContentActions.OnRefreshContentByTabBarAction -> {
                handleContentActionRefreshed()
            }
            FriendsContentActions.OnRefreshContentBySwipe -> {
                handleActionBySwipeToRefresh()
            }
            FriendsContentActions.GetNextTopUsersAction -> {
                handleGetNextTopUsersAction()
            }
            FriendsContentActions.SetCommunityTooltipShownAction -> {
                handleSetCommunityTooltipAction()
            }
            FriendsContentActions.LogInviteFriendAction -> {
                logInviteFriend()
            }
            is FriendsContentActions.OnDialogSyncContactsPositiveButtonClickedUiAction -> {
                handlePositiveButtonSyncContactsAction(contentActions.showSyncContactsWelcome)
            }
            FriendsContentActions.ReadContactsPermissionGrantedUiAction -> {
                handleReadContactsPermissionGrantedAction()
            }
            is FriendsContentActions.OnSyncContactsUiAction -> {
                handleSyncContactsClicked()
            }
            is FriendsContentActions.OnBloggerSubscribeClicked -> {
                handleBloggerSubscribeClick(contentActions.user)
            }
            is FriendsContentActions.OnBloggerUnSubscribeClicked -> {
                handleBloggerUnsubscribeClick(contentActions.user)
            }
            is FriendsContentActions.OnUserAvatarClicked -> {
                handleUserAvatarClickAction(contentActions.entity, contentActions.view)
            }
            is FriendsContentActions.OnUserClicked -> {
                handleUserClickAction(contentActions.entity)
            }
            is FriendsContentActions.OnRelatedUserClicked -> {
                handleRelatedUserClick(contentActions.entity)
            }
            is FriendsContentActions.OnVideoPostClicked -> {
                handleVideoPostClick(contentActions.entity)
            }
            is FriendsContentActions.OnImagePostClicked -> {
                handleImagePostClick(contentActions.entity)
            }
            is FriendsContentActions.OnShowBumpClicked -> {
                handleShowBumpClicked()
            }
            is FriendsContentActions.OnMediaPlaceholderClicked -> {
                handleMediaPlaceholderClick(
                    postId = contentActions.postId,
                    userId = contentActions.userId
                )
            }
            is FriendsContentActions.OnRecommendedUserRemoveFromFriendsClicked -> {
                handleRemoveFromFriendsAction(
                    userId = contentActions.userId
                )
            }
            is FriendsContentActions.OnRecommendedUserAddToFriendClicked -> {
                handleAddToFriendClick(
                    entity = contentActions.entity
                )
            }
            is FriendsContentActions.ShowOnboardingAction -> {
                handleShowOnboardingAction()
            }
            is FriendsContentActions.GetNextRelatedUsers -> {
                handleGetNextRelatedUsersAction(
                    rootAdapterPosition = contentActions.rootAdapterPosition,
                    offsetCount = contentActions.offsetCount
                )
            }
            is FriendsContentActions.LogCommunitySectionUiAction -> {
                logCommunitySection()
            }
            is FriendsContentActions.InitPeopleWelcomeUiAction -> {
                handlePeopleWelcome(
                    needToShowWelcome = contentActions.needToShowWelcome,
                    isCalledFromBottomNav = contentActions.isCalledFromBottomNav
                )
            }
            is FriendsContentActions.ContactsPermissionDeniedUiAction -> {
                handleContactPermissionDenied(contentActions.deniedAndNoRationaleNeededAfterRequest)
            }
            is FriendsContentActions.OnHideRelatedUserUiAction -> {
                handleHideRelatedUserAction(contentActions.userId)
            }
            is FriendsContentActions.OnSuccessSyncContactsClosedUiAction -> {
                handleSuccessSyncContactsByClosed(contentActions.syncCount)
            }

            is FriendsContentActions.OnSuccessSyncContactsClosedButtonUiAction -> {
                handleSuccessSyncContactsClosedByButton(contentActions.syncCount)
            }
            is FriendsContentActions.LogSyncContactsGoToSettings -> {
                logSyncContactsGoToSettings(contentActions.showSyncContactsWelcome)
            }
            is FriendsContentActions.LogSyncContactsDialogClosedUiAction -> {
                logSyncContactsDialogClosed(contentActions.showSyncContactsWelcome)
            }
            is FriendsContentActions.LogSyncContactsGoToSettingsClosedUiAction -> {
                logSyncContactsGoToSettingsClose(contentActions.showSyncContactsWelcome)
            }
            is FriendsContentActions.CheckIfNeedToScrollToUserFromPush -> {
                checkIfNeedToScrollToUserFromPush(contentActions.userId, contentActions.currentList)
            }
            else -> Unit
        }
    }

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event -> handleMomentsEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleMomentsEvents(event: MomentRepositoryEvent) {
        when (event) {
            is MomentRepositoryEvent.UserMomentsStateUpdated -> {
                updateUserMomentsState(event.userMomentsStateUpdate)
            }

            else -> {
                Timber.e("$event")
            }
        }
    }

    private fun updateUserMomentsState(userMomentsStateUpdate: UserMomentsStateUpdateModel) {
        val postUpdate = userMomentsStateUpdate.toUIPostUpdate()
        val result = _peoplesContentState.value?.contentList?.toMutableList() ?: return
        for (i in 0 until result.size) {
            val post = result[i] as? PeopleInfoUiEntity
            if (post?.getUserId() == postUpdate.userId) {
                val user = post.copy(
                    hasMoments = postUpdate.hasMoments,
                    hasNewMoments = postUpdate.hasNewMoments
                )

                replaceItemInPosts(result, position = i, user = user)
            }
        }
        setUiState(
            PeopleUiStates.PeoplesContentUiState(
                contentList = result,
            )
        )
    }

    private fun replaceItemInPosts(
        result: MutableList<PeoplesContentUiEntity>,
        position: Int,
        user: PeopleInfoUiEntity
    ): MutableList<PeoplesContentUiEntity> {
        result.removeAt(position)
        result.add(position, user)
        return result
    }

    private fun logCommunitySection() {
        peopleAnalyticDelegate.logCommunitySection()
    }

    private fun getSavedContent() {
        viewModelScope.launch {
            runCatching {
                getPeopleAllSavedContentUseCase.invoke()
            }.onSuccess { peopleModel ->
                if (peopleModel.approvedUsers.isEmpty() && peopleModel.relatedUsers.isEmpty()) {
                    refreshPeopleContent()
                    return@onSuccess
                }
                resetPaginationState()
                resetRecommendedPeopleListPage()
                resetTopUsersOffset()
                setTopPeopleOffset(peopleModel.approvedUsers.size)
                val listResult = peopleContentUiMapper.createPeopleContent(
                    peopleRelatedUserModels = peopleModel.relatedUsers,
                    peopleApprovedUserModels = peopleModel.approvedUsers,
                    myUserId = getUserUidUseCase.invoke(),
                    allowSyncContacts = getSyncContactsPrivacyUseCase.invoke()
                )
                setUiState(
                    PeopleUiStates.PeoplesContentUiState(
                        contentList = listResult,
                        isRefreshing = false
                    )
                )
            }.onFailure { e ->
                Timber.d(e)
            }
        }
    }

    /**
     * В данном методе делается самый первый запрос на возможный контент
     * И сетится результат разных ui entity с контентом
     * [com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity]
     */
    private fun refreshPeopleContent(isRefresh: Boolean = false, userIdFromPush: Long? = null) {
        setRefreshingState(isRefresh)
        viewModelScope.launch {
            val topUsers = getFirstPageTopUsers()
            val relatedUsers = getFirstPageRelatedUsers(userIdFromPush)
            if (topUsers == null || relatedUsers == null) {
                handleError()
            }
            Timber.d("PeopleRequest: Top users: $topUsers Related users: $relatedUsers")
            resetPaginationState()
            resetRecommendedPeopleListPage()
            val contentListResult = peopleContentUiMapper.createPeopleContent(
                peopleApprovedUserModels = topUsers ?: emptyList(),
                peopleRelatedUserModels = relatedUsers ?: emptyList(),
                myUserId = getUserUidUseCase.invoke(),
                allowSyncContacts = getSyncContactsPrivacyUseCase.invoke()
            )
            setUiState(
                PeopleUiStates.PeoplesContentUiState(
                    contentList = contentListResult,
                    isRefreshing = false
                )
            )
        }
    }

    private fun setTooltipShown() {
        setSelectCommunityTooltipShownUseCase.invoke()
    }

    private fun setPeopleOnboardingShown() {
        setPeopleOnboardingShownUseCase.invoke(true)
    }

    private fun setPeopleBadgeShown() {
        setPeopleBadgeShownUseCase.invoke()
    }

    private fun emitPeopleOnboardingShowIfNeed() {
        val isOnboardingShown = getPeopleOnboardingShownUseCase.invoke() || isOnboardingAlreadyShown
        if (!isOnboardingShown) {
            isOnboardingAlreadyShown = true
            emitViewEvent(PeopleUiEffect.ShowOnboardingEffect(true))
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
                val currentList = _peoplesContentState.value?.contentList?.toMutableList() ?: mutableListOf()
                if (rootListPosition != -1) {
                    val listResult = peopleContentUiMapper.mapPaginationRecommendedListToUiList(
                        currentList = currentList,
                        newList = relatedUsers,
                        rootListPosition = rootListPosition
                    )
                    setUiState(
                        PeopleUiStates.PeoplesContentUiState(
                            contentList = listResult,
                            isRefreshing = false
                        )
                    )
                }
                isRecommendationLast = relatedUsers.isEmpty()
            }.onFailure { e ->
                isRecommendationLoading = false
                isRecommendationLast = false
                Timber.e(e)
            }
        }
    }

    private fun getNextTopUsers() {
        if (topUsersOffset == DEFAULT_OFFSET) return
        isTopContentLoading = true
        viewModelScope.launch {
            runCatching {
                getApprovedUsersUseCase.invoke(
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = topUsersOffset
                )
            }.onSuccess { approvedUsers ->
                Timber.d("Approved users list: $approvedUsers")
                isTopContentLoading = false
                val uiList = peopleContentUiMapper.mapFromApprovedUsers(
                    approvedUsers = approvedUsers,
                    myUserId = getUserUidUseCase.invoke()
                )
                val currentContentList = _peoplesContentState.value?.contentList?.toMutableList() ?: mutableListOf()
                if (currentContentList.any { it is BloggersPlaceHolderUiEntity } && uiList.isNotEmpty()) {
                    currentContentList.removeIf { it is BloggersPlaceHolderUiEntity }
                }
                val listResult = currentContentList.plus(uiList)
                setUiState(
                    PeopleUiStates.PeoplesContentUiState(
                        contentList = listResult,
                        isRefreshing = false
                    )
                )
                setTopPeopleOffset(approvedUsers.size)
                isTopContentLast = approvedUsers.isEmpty()
            }.onFailure { e ->
                isTopContentLoading = false
                isTopContentLast = false
                Timber.e(e)
            }
        }
    }

    private fun subscribeUser(userId: Long) {
        viewModelScope.launch {
            runCatching { subscribeUserUseCase.invoke(userId) }
                .onSuccess {
                    pushSubscriptionChange(
                        userId = userId,
                        isSubscribed = true
                    )
                    handleUserSubscribe(
                        userId = userId,
                        isUserSubscribed = true
                    )
                    emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.subscribed_on_user_notif_on))
                }.onFailure { e ->
                    Timber.e(e)
                    handleError()
                }
        }
    }

    private fun unSubscribeUser(userId: Long) {
        viewModelScope.launch {
            runCatching { unsubscribeUserUseCase.invoke(userId) }
                .onSuccess {
                    pushSubscriptionChange(
                        userId = userId,
                        isSubscribed = false
                    )
                    handleUserSubscribe(
                        userId = userId,
                        isUserSubscribed = false
                    )
                    emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.disabled_new_post_notif))
                }.onFailure { e ->
                    Timber.e(e)
                    handleError()
                }
        }
    }

    private fun handleUserSubscribe(
        userId: Long,
        isUserSubscribed: Boolean,
    ) {
        setUiState(
            PeopleUiStates.PeoplesContentUiState(
                contentList = getContentListWithSubscribeStatusChanged(userId, isUserSubscribed),
                isRefreshing = _peoplesContentState.value?.isRefreshing ?: false
            )
        )
        requestTopUsersAndCache()
    }

    private fun getContentListWithSubscribeStatusChanged(
        userId: Long,
        isUserSubscribed: Boolean
    ): List<PeoplesContentUiEntity> = _peoplesContentState.value?.contentList
        ?.filterIsInstance<BloggerMediaContentListUiEntity>()
        ?.find { it.getUserId() == userId }?.bloggerPostList
        ?.map { post -> checkViewTypeAndUpdateFriendStatus(post, isUserSubscribed) }
        ?.let { posts -> getNewBloggerMediaContentListUiEntity(userId, posts) }
        ?.let { newItem -> getUpdatedFriendStatusList(userId, isUserSubscribed, newItem) } ?: listOf()

    private fun getUpdatedFriendStatusList(
        userId: Long,
        isUserSubscribed: Boolean,
        newItem: BloggerMediaContentListUiEntity
    ): List<PeoplesContentUiEntity>? {
        val listResult = _peoplesContentState.value?.contentList?.toMutableList()
        return listResult?.map { model ->
            if (model is PeopleInfoUiEntity && model.userId == userId) {
                model.copy(
                    isUserSubscribed = isUserSubscribed
                )
            } else if (model is BloggerMediaContentListUiEntity && model.getUserId() == userId) {
                newItem
            } else {
                model
            }
        }
    }

    private fun getNewBloggerMediaContentListUiEntity(
        userId: Long,
        posts: List<BloggerMediaContentUiEntity>
    ) = BloggerMediaContentListUiEntity(
        userId = userId,
        bloggerPostList = posts
    )

    private fun checkViewTypeAndUpdateFriendStatus(
        post: BloggerMediaContentUiEntity,
        isUserSubscribed: Boolean
    ) = if (post is BloggerMediaContentUiEntity.BloggerContentPlaceholderUiEntity) {
        post.copy(user = post.user.copy(isUserSubscribed = isUserSubscribed))
    } else {
        post
    }

    private fun addToFriendSocket(selectedItem: RecommendedPeopleUiEntity) {
        val userId = selectedItem.userId
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
                    if (selectedItem.subscriptionOn.toBoolean()) {
                        emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.request_sent))
                    } else {
                        emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.request_friend_sent_notif_on))
                    }
                }.onFailure { e ->
                    Timber.e(e)
                }
        }
    }

    private fun removeFromFriends(
        userId: Long
    ) {
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
                    emitViewEvent(
                        PeopleUiEffect.ShowSuccessToast(
                            R.string.friends_cancel_friend_request_and_unsubscribe_success
                        )
                    )
                }.onFailure { e ->
                    Timber.e(e)
                }
        }
    }

    private fun handleFriendStatusResponse(
        userId: Long,
        isAddToFriendRequest: Boolean
    ) {
        val result = peopleContentUiMapper.getUpdatedUserFriendStatusListById(
            currentList = _peoplesContentState.value?.contentList ?: return,
            selectedUserId = userId,
            isUserSubscribed = isAddToFriendRequest,
            showPossibleFriendsText = true
        )
        setUiState(
            PeopleUiStates.PeoplesContentUiState(
                contentList = result,
                isRefreshing = _peoplesContentState.value?.isRefreshing ?: false
            )
        )
        requestRelatedUsersAndCache()
    }

    private fun handleError() {
        if (!networkStatusProvider.isInternetConnected()) {
            emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.no_internet))
        } else {
            emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_message_went_wrong))
        }
    }

    private fun setRefreshingState(isRefresh: Boolean) {
        when (_peoplesContentState.value) {
            is PeopleUiStates.PeoplesContentUiState -> {
                setUiState(
                    (_peoplesContentState.value as PeopleUiStates.PeoplesContentUiState).copy(
                        isRefreshing = isRefresh
                    )
                )
            }
            is PeopleUiStates.LoadingState -> {
                setUiState(
                    (_peoplesContentState.value as PeopleUiStates.LoadingState).copy(
                        isRefreshing = isRefresh
                    )
                )
            }
            else -> Unit
        }
    }

    private fun initShimmerContent() {
        val allowSyncContacts = getSyncContactsPrivacyUseCase.invoke()
        setUiState(PeopleUiStates.LoadingState(peopleContentUiMapper.createDefaultContent(allowSyncContacts)))
    }

    private fun emitViewEvent(event: PeopleUiEffect) {
        viewModelScope.launch {
            _peoplesContentEvent.emit(event)
        }
    }

    /**
     * Реактивно слушаем изменения, если юзер переходит в другой профиль подписывается и отписывается
     * от юзера
     */
    // TODO: BR-22996 Было б удобнее обсервить через сокет
    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach(::handleFriendStatusChanged)
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(effect: UserSettingsEffect) {
        when (effect) {
            is UserSettingsEffect.UserFriendStatusChanged -> {
                if (effect.isSubscribe) {
                    refreshPeopleContent()
                } else {
                    updateUsersByFriendsStatusChanged(effect.userId)
                }
            }
            is UserSettingsEffect.UserBlockStatusChanged -> {
                refreshPeopleContent()
            }
            is UserSettingsEffect.SuggestionRemoved -> {
                removeRelatedUserById(effect.userId)
            }
            else -> Unit
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

    private fun setUiState(newState: PeopleUiStates) {
        _peoplesContentState.postValue(newState)
    }

    private fun initDialogDismissListener() {
        viewModelScope.launch {
            dialogDismissListener.sharedFlow.collect { dialogType ->
                when (dialogType) {
                    DismissDialogType.PEOPLE_ONBOARDING -> {
                        checkNeedRequestSyncContactsWelcome()
                        setPeopleOnboardingShown()
                        setPeopleBadgeShown()
                    }
                    else -> Unit
                }
            }
        }
    }

    private suspend fun getFirstPageTopUsers(): List<PeopleApprovedUserModel>? {
        val response = runCatching {
            getApprovedUsersUseCase.invoke(
                limit = DEFAULT_PAGE_LIMIT,
                offset = DEFAULT_OFFSET
            )
        }.onFailure { e ->
            Timber.d(e)
        }
        return if (response.getOrNull() != null) {
            resetTopUsersOffset()
            val usersSize = response.getOrNull()?.size ?: DEFAULT_OFFSET
            setTopPeopleOffset(usersSize)
            response.getOrNull()
        } else {
            null
        }
    }

    private suspend fun getFirstPageRelatedUsers(userIdFromPush: Long?): List<PeopleRelatedUserModel>? {
        val response = runCatching {
            getRelatedUsersUseCase.invoke(
                limit = DEFAULT_PAGE_LIMIT,
                offset = DEFAULT_OFFSET,
                selectedUserId = userIdFromPush
            )
        }.onFailure { e ->
            Timber.d(e)
        }
        return if (response.getOrNull() == null) {
            null
        } else {
            response.getOrNull()
        }
    }

    private fun resetTopUsersOffset() {
        topUsersOffset = 0
    }

    private fun setTopPeopleOffset(newOffset: Int) {
        topUsersOffset += newOffset
    }

    private fun resetPaginationState() {
        isRecommendationLast = false
        isRecommendationLoading = false
        isTopContentLast = false
        isTopContentLoading = false
    }

    private fun resetRecommendedPeopleListPage() {
        val currentList = _peoplesContentState.value?.contentList ?: return
        currentList.find { entity ->
            entity.getPeoplesActionType() == PeoplesContentType.RECOMMENDED_PEOPLE
        }?.let { result ->
            val recommendedListIndex = currentList.indexOf(result)
            emitViewEvent(PeopleUiEffect.ClearRelatedUserPageUiEffect(recommendedListIndex))
        }
    }

    private fun handleMainSearchOpenClicked() {
        peopleAnalyticDelegate.logSearchOpen()
        emitViewEvent(
            PeopleUiEffect.OpenSearch
        )
    }

    private fun handleReferralScreenClicked() {
        peopleAnalyticDelegate.logInvitePeople()
        emitViewEvent(
            PeopleUiEffect.OpenReferralScreen
        )
    }

    private fun handleAddToFriendClick(entity: RecommendedPeopleUiEntity) {
        val influencerProperty = createInfluencerAmplitudeProperty(
            approved = entity.isAccountApproved,
            topContentMaker = entity.topContentMaker
        )
        peopleAnalyticDelegate.logAddToFriendAmplitude(
            userId = entity.userId,
            influencer = influencerProperty,
            type = FriendAddAction.ADD_FRIENDS_PEOPLE
        )
        addToFriendSocket(selectedItem = entity)
    }

    private fun handleMediaContentClicked(
        userId: Long,
        postId: Long,
        where: AmplitudePropertyWhere
    ) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = userId,
                postId = postId,
                where = where
            )
        )
    }

    private fun handleBloggerSubscribeClick(user: PeopleInfoUiEntity) {
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = user.isInterestingAuthor,
            approved = user.isApprovedAccount
        )
        peopleAnalyticDelegate.logFollowActionAmplitude(
            userId = user.userId,
            amplitudeInfluencerProperty = influencerProperty,
            where = AmplitudeFollowButtonPropertyWhere.ADVICE_TO_FOLLOW_PEOPLE
        )
        subscribeUser(user.userId)
    }

    private fun handleBloggerUnsubscribeClick(user: PeopleInfoUiEntity) {
        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = user.isInterestingAuthor,
            approved = user.isApprovedAccount
        )
        peopleAnalyticDelegate.logUnfollowAmplitude(
            userId = user.userId,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty,
            where = AmplitudeFollowButtonPropertyWhere.ADVICE_TO_FOLLOW_PEOPLE
        )
        unSubscribeUser(user.userId)
    }

    private fun handleContentActionRefreshed() {
        if (!isNeedUpdate) return
        refreshPeopleContent(REFRESH_SWIPE_STATE)
        isNeedUpdate = false
        doDelayed(DELAY_NAVIGATION_BAR_CLICK_REQUEST) { isNeedUpdate = true }
        emitViewEvent(PeopleUiEffect.ScrollToPositionEffect(TOP_PEOPLE_CONTENT_POSITION))
    }

    private fun handleActionBySwipeToRefresh() {
        refreshPeopleContent(true)
    }

    private fun handleGetNextTopUsersAction() {
        getNextTopUsers()
    }

    private fun handleGetNextRelatedUsersAction(
        rootAdapterPosition: Int,
        offsetCount: Int
    ) {
        getNextRelatedUsers(
            offset = offsetCount,
            rootListPosition = rootAdapterPosition
        )
    }

    private fun handleSetCommunityTooltipAction() {
        setTooltipShown()
    }

    private fun handleUserAvatarClickAction(entity: PeopleInfoUiEntity, view: View?) {
        if (entity.hasMoments) {
            emitViewEvent(
                PeopleUiEffect.OpenMomentsProfile(
                    userId = entity.userId,
                    view = view,
                    hasNewMoments = entity.hasNewMoments
                )
            )
        } else {
            emitViewEvent(
                PeopleUiEffect.OpenUserProfile(
                    userId = entity.userId,
                    where = AmplitudePropertyWhere.ADVICE_TO_FOLLOW_PEOPLE
                )
            )
        }
    }

    private fun handleUserClickAction(entity: PeopleInfoUiEntity) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = entity.userId,
                where = AmplitudePropertyWhere.ADVICE_TO_FOLLOW_PEOPLE
            )
        )
    }

    private fun handleShowOnboardingAction() {
        emitViewEvent(PeopleUiEffect.ShowOnboardingEffect(false))
    }

    private fun handleRemoveFromFriendsAction(userId: Long) {
        removeFromFriends(userId)
    }

    private fun handleVideoPostClick(entity: BloggerMediaContentUiEntity.BloggerVideoContentUiEntity) {
        handleMediaContentClicked(
            userId = entity.rootUser.userId,
            postId = entity.postId,
            where = AmplitudePropertyWhere.CONTENT_ADVICE_TO_FOLLOW_PEOPLE
        )
        logMediaPostClick(
            haveVideo = true,
            haveImage = false,
            postId = entity.postId,
            authorId = entity.rootUser.userId
        )
    }

    private fun handleImagePostClick(entity: BloggerMediaContentUiEntity.BloggerImageContentUiEntity) {
        handleMediaContentClicked(
            userId = entity.rootUser.userId,
            postId = entity.postId,
            where = AmplitudePropertyWhere.CONTENT_ADVICE_TO_FOLLOW_PEOPLE
        )
        logMediaPostClick(
            haveVideo = false,
            haveImage = true,
            postId = entity.postId,
            authorId = entity.rootUser.userId
        )
    }

    private fun handleMediaPlaceholderClick(
        postId: Long,
        userId: Long
    ) {
        handleMediaContentClicked(
            userId = userId,
            postId = postId,
            where = AmplitudePropertyWhere.PEOPLE_VIEW_ALL_PEOPLE
        )
    }

    private fun logMediaPostClick(
        haveVideo: Boolean,
        haveImage: Boolean,
        postId: Long,
        authorId: Long
    ) {
        val myUserId = getUserUidUseCase.invoke()
        peopleAnalyticDelegate.logContentCardAmplitude(
            cardWhereAction = AmplitudePeopleContentCardProperty.PEOPLE,
            havePhoto = haveImage,
            haveVideo = haveVideo,
            userId = myUserId,
            postId = postId,
            authorId = authorId
        )
    }

    private fun handleRelatedUserClick(entity: RecommendedPeopleUiEntity) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = entity.userId,
                where = AmplitudePropertyWhere.ADD_FRIENDS_PEOPLE
            )
        )
    }

    private fun handleShowBumpClicked() {
        peopleAnalyticDelegate.logShakeOpenedByButton(AmplitudeShakeWhereProperty.PEOPLE)
        emitViewEvent(PeopleUiEffect.ShowShakeDialog)
    }

    // TODO: BR-21313 Не самый лучший способ постоянно дергать запрос на новый контент после подписки..
    private fun requestTopUsersAndCache() {
        viewModelScope.launch {
            runCatching {
                getTopUsersAndCacheUseCase.invoke(
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = DEFAULT_OFFSET
                )
            }.onFailure { e ->
                Timber.d(e)
            }
        }
    }

    // TODO: BR-21313 Не самый лучший способ постоянно дергать запрос на новый контент после добавления в друзья
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

    private fun handleSyncContactsClicked() {
        peopleAnalyticDelegate.logStartSyncContacts()
        emitViewEvent(PeopleUiEffect.ShowSyncContactsDialogUiEffect)
    }

    private fun handlePositiveButtonSyncContactsAction(showSyncContactsWelcome: Boolean) {
        logSyncContactsAction(showSyncContactsWelcome)
        syncContactOrShowPermission()
    }

    private fun handleReadContactsPermissionGrantedAction() {
        setSyncContactsSettingTrue()
    }

    private fun handlePeopleWelcome(
        needToShowWelcome: Boolean,
        isCalledFromBottomNav: Boolean
    ) {
        handleNeedToShowBadge(isCalledFromBottomNav)
        doDelayed(DELAY_PEOPLE_WELCOME) {
            handleSyncContactsWelcome(needToShowWelcome)
            emitPeopleOnboardingShowIfNeed()
        }
    }

    private fun handleNeedToShowBadge(isCalledFromBottomNav: Boolean) {
        if (isCalledFromBottomNav && needShowPeopleBadgeUseCase.invoke() && getPeopleOnboardingShownUseCase.invoke()) {
            setPeopleBadgeShownUseCase.invoke()
        }
    }

    private fun handleSyncContactsWelcome(needToShowWelcome: Boolean) {
        when {
            needToShowWelcome && isOpenedFromSyncContactsWelcome.not() -> showSyncContactsByClick()
            getNeedShowSyncContactsDialogUseCase.invoke() && !readContactsPermissionProvider.hasContactsPermission()
                && getSyncContactsPrivacyUseCase.invoke() -> emitShowSyncContactsGoToSettings()
        }
    }

    private fun emitShowSyncContactsGoToSettings() {
        if (isPeopleOnboardingShown()) {
            setNeedShowSyncContactsDialogUseCase.invoke(false)
            emitViewEvent(PeopleUiEffect.ShowSyncDialogPermissionDenied)
        }
    }

    private fun showSyncContactsByClick() {
        isOpenedFromSyncContactsWelcome = true
        if (isPeopleOnboardingShown()) {
            emitViewEvent(PeopleUiEffect.ShowSyncContactsDialogUiEffect)
        }
    }

    private fun syncContactOrShowPermission() {
        if (readContactsPermissionProvider.hasContactsPermission().not()) {
            emitViewEvent(PeopleUiEffect.RequestReadContactsPermissionUiEffect)
        } else {
            setSyncContactsSettingTrue()
        }
    }

    private fun setSyncContactsSettingTrue() {
        setSettingsUseCase.invoke(
            params = SettingsParams.CommonSettingsParams(
                key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                value = true.toInt()
            )
        ).invokeOnCompletion { error ->
            if (error == null) {
                startSyncContacts()
            } else {
                Timber.e(error)
                handleError()
            }
        }
    }

    private fun checkNeedRequestSyncContactsWelcome() {
        if (isOpenedFromSyncContactsWelcome && !getPeopleOnboardingShownUseCase.invoke()) {
            emitViewEvent(PeopleUiEffect.ShowSyncContactsDialogUiEffect)
        }
    }

    private fun handleContactPermissionDenied(deniedAndNoRationaleNeededAfterRequest: Boolean) {
        if (deniedAndNoRationaleNeededAfterRequest) {
            emitViewEvent(PeopleUiEffect.ShowSyncDialogPermissionDenied)
        }
    }

    private fun initObservers() {
        observeFriendStatusChanged()
        observeSyncContactsWorker()
    }

    private fun observeSyncContactsWorker() {
        observeSyncContactsUseCase.invoke()
            .distinctUntilChanged()
            .onEach(::handleSyncWork)
            .catch { e ->
                Timber.e(e)
            }
            .launchIn(viewModelScope)
    }

    private fun handleSyncWork(workInfo: WorkInfo?) {
        val isFinishedSucceed = workInfo?.state == WorkInfo.State.SUCCEEDED
        if (isFinishedSucceed) {
            if (getSyncContactsPrivacyUseCase.invoke().not()) {
                val syncCount = workInfo?.outputData?.getInt(SYNC_COUNT, 0) ?: 0
                emitViewEvent(PeopleUiEffect.ShowContactsHasBeenSyncDialogUiEffect(syncCount))
            }
            viewModelScope.launch {
                setSyncContactsPrivacyUseCase.invoke(true)
            }
        }
        if (workInfo?.state == WorkInfo.State.FAILED) {
            handleError()
        }
        setProgressBySyncContactsState(workInfo)
    }

    private fun startSyncContacts() {
        viewModelScope.launch { startSyncContactsUseCase.invoke() }
    }

    private fun setProgressBySyncContactsState(workInfo: WorkInfo?) {
        val isFinishedSuccess = workInfo?.state == WorkInfo.State.SUCCEEDED
        val isFinished = workInfo?.state?.isFinished ?: false
        val currentList = _peoplesContentState.value?.contentList?.toMutableList() ?: return
        if (isFinishedSuccess) {
            currentList.find { entity ->
                entity.getPeoplesActionType() == PeoplesContentType.CONTACT_SYNC_TYPE
            }?.let {
                currentList.remove(it)
            }
        }
        setUiState(
            PeopleUiStates.PeoplesContentUiState(
                contentList = currentList,
                isRefreshing = _peoplesContentState.value?.isRefreshing ?: false,
                showProgressBar = !isFinished
            )
        )
    }

    private fun removeRelatedUserById(userId: Long) {
        val recommendationType = _peoplesContentState.value
            ?.contentList
            ?.filterIsInstance<RecommendedPeopleListUiEntity>()
            ?.firstOrNull()
        val currentRecommendations = recommendationType?.recommendedPeopleList ?: emptyList()
        if (currentRecommendations.isEmpty()) return
        val newRecommendations = currentRecommendations.toMutableList()
        newRecommendations.removeAll { user ->
            user.userId == userId
        }
        val newRecommendationsListEntity = RecommendedPeopleListUiEntity(newRecommendations, true)
        val listResult = _peoplesContentState.value?.contentList?.map { entity ->
            if (entity is RecommendedPeopleListUiEntity) newRecommendationsListEntity else entity
        }?.filterNot { entity ->
            entity is RecommendedPeopleListUiEntity && entity.recommendedPeopleList.isEmpty()
        } ?: emptyList()
        setUiState(
            PeopleUiStates.PeoplesContentUiState(
                contentList = listResult,
                isRefreshing = _peoplesContentState.value?.isRefreshing ?: false,
                showProgressBar = _peoplesContentState.value?.showProgressBar ?: false
            )
        )
    }

    private fun handleHideRelatedUserAction(userId: Long) {
        peopleAnalyticDelegate.logUserCardHide(userId, AmplitudeUserCardHideSectionProperty.PEOPLE)
        blockSuggestionById(userId)
    }

    private fun blockSuggestionById(userId: Long) {
        viewModelScope.launch {
            runCatching {
                blockSuggestionUseCase.invoke(userId)
                removeRelatedUserByIdDb(userId)
            }.onSuccess {
                emitSuggestionRemovedUseCase.invoke(userId)
            }.onFailure { e ->
                Timber.e(e)
                handleError()
            }
        }
    }

    private suspend fun removeRelatedUserByIdDb(userId: Long) {
        runCatching { removeRelatedUserUseCase.invoke(userId) }
            .onFailure { e -> Timber.e(e) }
    }

    private fun isPeopleOnboardingShown(): Boolean = getPeopleOnboardingShownUseCase.invoke()

    private fun handleSuccessSyncContactsByClosed(syncCount: Int) {
        peopleAnalyticDelegate.logSyncContactsFinished(SyncContactsSuccessActionTypeProperty.CLOSE, syncCount)
    }

    private fun handleSuccessSyncContactsClosedByButton(syncCount: Int) {
        peopleAnalyticDelegate.logSyncContactsFinished(SyncContactsSuccessActionTypeProperty.GREAT, syncCount)
    }

    private fun logSyncContactsAction(showSyncContactsWelcome: Boolean) {
        val propertyWhere = getPeopleSyncContactsWhereProperty(showSyncContactsWelcome)
        peopleAnalyticDelegate.logSyncContactsAction(
            where = propertyWhere,
            typeProperty = AmplitudeSyncContactsActionTypeProperty.ALLOW,
            numberOfPopup = SyncContactsConst.ALLOW_OR_LATTER
        )
    }

    private fun logSyncContactsGoToSettings(showSyncContactsWelcome: Boolean) {
        val peopleWhere = getPeopleSyncContactsWhereProperty(showSyncContactsWelcome)
        peopleAnalyticDelegate.logSyncContactsAction(
            where = peopleWhere,
            numberOfPopup = SyncContactsConst.GO_TO_SETTINGS_OR_LATTER,
            typeProperty = AmplitudeSyncContactsActionTypeProperty.GO_TO_SETTINGS
        )
    }

    private fun logSyncContactsDialogClosed(showSyncContactsWelcome: Boolean) {
        val where = getPeopleSyncContactsWhereProperty(showSyncContactsWelcome)
        peopleAnalyticDelegate.logSyncContactsAction(
            where = where,
            numberOfPopup = SyncContactsConst.ALLOW_OR_LATTER,
            typeProperty = AmplitudeSyncContactsActionTypeProperty.CLOSE
        )
    }

    private fun logSyncContactsGoToSettingsClose(showSyncContactsWelcome: Boolean) {
        val where = getPeopleSyncContactsWhereProperty(showSyncContactsWelcome)
        peopleAnalyticDelegate.logSyncContactsAction(
            where = where,
            numberOfPopup = SyncContactsConst.GO_TO_SETTINGS_OR_LATTER,
            typeProperty = AmplitudeSyncContactsActionTypeProperty.CLOSE
        )
    }

    private fun checkIfNeedToScrollToUserFromPush(userId: Long?, currentList: List<PeoplesContentUiEntity>?) {
        val userId = userId ?: return
        if (userId <= 0) return
        peopleAnalyticDelegate.logPeopleOpened(AmplitudePeopleWhereProperty.USER_PROFILE)
        if (currentList.isNullOrEmpty()) return
        val position = currentList.indexOfFirst { it.getPeoplesActionType() == PeoplesContentType.RECOMMENDED_PEOPLE }
        if (position < 0) return
        doDelayed(200L) {
            emitViewEvent(PeopleUiEffect.ScrollToRecommendedUser(userId, position))
        }
    }

    private fun getPeopleSyncContactsWhereProperty(allowSyncContactsWelcome: Boolean) =
        if (allowSyncContactsWelcome) AmplitudeSyncContactsWhereProperty.SUGGEST else
            AmplitudeSyncContactsWhereProperty.PEOPLE

    private fun updateUsersByFriendsStatusChanged(userId: Long) {
        viewModelScope.launch {
            val listResult = _peoplesContentState.value?.contentList
                ?.filterIsInstance<RecommendedPeopleListUiEntity>()
                ?.firstOrNull()
                ?.let { it.recommendedPeopleList.find { related -> related.userId == userId } }
                ?.let { model -> getRemovedListIfUnsubscribed(model) }
                ?: getCurrentList()
            setUiState(
                PeopleUiStates.PeoplesContentUiState(
                    contentList = listResult,
                    isRefreshing = _peoplesContentState.value?.isRefreshing ?: false,
                    showProgressBar = _peoplesContentState.value?.showProgressBar ?: false
                )
            )
        }
    }

    private fun handleNavigatingToUserFromPush(userId: Long?) {
        if (userId != null && userId != 0L) {
            doDelayed(DELAY_NAVIGATING_TO_USER_FROM_PUSH) {
                emitViewEvent(
                    PeopleUiEffect.OpenUserProfile(
                        userId = userId,
                        where = AmplitudePropertyWhere.PUSH
                    )
                )
            }
        }
    }

    private fun getRemovedListIfUnsubscribed(
        user: RecommendedPeopleUiEntity
    ): List<PeoplesContentUiEntity> {
        val currentList = _peoplesContentState.value?.contentList?.toMutableList() ?: emptyList()
        return currentList.map { model ->
            if (model is RecommendedPeopleListUiEntity) {
                val users = model.recommendedPeopleList.toMutableList()
                users.remove(user)
                RecommendedPeopleListUiEntity(users, true)
            } else {
                model
            }
        }
    }

    private fun logInviteFriend() {
        friendInviteTapAnalytics.logFiendInviteTap(FriendInviteTapProperty.PEOPLE)
    }

    private fun getCurrentList(): List<PeoplesContentUiEntity> =
        _peoplesContentState.value?.contentList ?: emptyList()

    companion object {
        const val DEFAULT_PAGE_LIMIT = 20
    }
}
