package com.numplates.nomera3.modules.search.ui.viewmodel.user

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
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
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFullness
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyTransportType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleContentCardProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideSectionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsActionTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsSuccessActionTypeProperty
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.peoples.domain.models.PeopleApprovedUserModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleModel
import com.numplates.nomera3.modules.peoples.domain.models.PeopleRelatedUserModel
import com.numplates.nomera3.modules.peoples.domain.usecase.GetApprovedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetNeedShowSyncContactsDialogUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetPeopleAllSavedContentUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetTopUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.NeedShowPeopleBadgeUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.RemoveRelatedUserUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetNeedShowSyncContactsDialogUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetPeopleBadgeShownUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.SetSelectCommunityTooltipShownUseCase
import com.numplates.nomera3.modules.peoples.ui.content.action.FriendsContentActions
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.BloggerMediaContentListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeopleInfoUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUserUiModel
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecentUsersUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleListUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.TitleSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultShimmerUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.UserSearchResultUiEntity
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity
import com.numplates.nomera3.modules.peoples.ui.delegate.PeopleAnalyticDelegate
import com.numplates.nomera3.modules.peoples.ui.entity.PeopleUiEffect
import com.numplates.nomera3.modules.peoples.ui.mapper.MeeraPeopleContentUiMapper
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.search.domain.mapper.recent.SearchRecentUsersMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.FriendStatusButtonMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.SearchUserResultMapper
import com.numplates.nomera3.modules.search.domain.usecase.GetNumberSearchParamsFlowUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCaseParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentUserParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentUsersUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchUsersParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchUsersUseCase
import com.numplates.nomera3.modules.search.ui.entity.state.UserSearchViewState
import com.numplates.nomera3.modules.search.ui.util.PagingProperties
import com.numplates.nomera3.modules.search.ui.viewmodel.base.CLEAR_RECENT_DELAY_MS
import com.numplates.nomera3.modules.search.ui.viewmodel.base.CLEAR_RECENT_DELAY_SEC
import com.numplates.nomera3.modules.search.ui.viewmodel.base.DEFAULT_SEARCH_RESULT_PAGE_SIZE
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendObserverParams
import com.numplates.nomera3.modules.user.domain.usecase.BlockSuggestionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EmitSuggestionRemovedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeParams
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeUseCase
import com.numplates.nomera3.modules.user.domain.usecase.SubscribeUserParams
import com.numplates.nomera3.modules.user.domain.usecase.SubscribeUserUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserStateObserverUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterGender
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterResult
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.isSomethingChanged
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.widgets.numberplateview.validateNumberForSearch
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_PAGE_LIMIT = 20
private const val DEFAULT_OFFSET = 0
const val TOP_PEOPLE_CONTENT_POSITION = 0
private const val DELAY_NAVIGATION_BAR_CLICK_REQUEST = 1500L
private const val SEARCH_SHIMMER_COUNT = 12

class SearchUserViewModel @Inject constructor(
    private val searchRecentUseCase: SearchRecentUsersUseCase,
    private val searchCleanRecentUseCase: SearchCleanRecentUsersUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val searchUsersByNumberUseCase: SearchByNumberUseCase,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val removeUserFromFriendAndUnsubscribeUseCase: RemoveUserFromFriendAndUnsubscribeUseCase,
    private val subscribeUserUserCase: SubscribeUserUseCase,
    private val unsubscribeUserUseCase: UnsubscribeUserUseCaseNew,
    private val userStateObserverUseCase: UserStateObserverUseCase,
    val amplitudeHelper: AnalyticsInteractor,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private val appSettings: AppSettings,
    private val amplitudeFindFriendsHelper: AmplitudeFindFriends,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val searchUserResultMapper: SearchUserResultMapper,
    private val amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic,
    private val peopleContentUiMapper: MeeraPeopleContentUiMapper,
    private val networkStatusProvider: NetworkStatusProvider,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val removeFriendRequestUseCase: DeleteFriendCancelSubscriptionUseCase,
    private val setSelectCommunityTooltipShownUseCase: SetSelectCommunityTooltipShownUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
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
    private val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase,
    private val getNumberSearchParamsFlowUseCase: GetNumberSearchParamsFlowUseCase
) : ViewModel() {

    private var isKeyboardOpened: Boolean = false
    private var lastRecentItems: List<RecentUserUiModel>? = null
    private var clearRecentJob: Job? = null

    private var filterResult: FilterResult? = null
    private var previousFilterResult: FilterResult? = null
    private var filterNumbers: NumberSearchParameters? = null

    private val disposables = CompositeDisposable()
    private val searchResultPagingProperties = PagingProperties()
    private var query: String = String.empty()

    private val _searchUserState = MutableLiveData<UserSearchViewState>()
    val searchUserState: LiveData<UserSearchViewState> = _searchUserState

    private val _peoplesContentEvent = MutableSharedFlow<PeopleUiEffect>()
    val peoplesContentEvent: SharedFlow<PeopleUiEffect> = _peoplesContentEvent

    private var showPeoples: Boolean = true
    private var isNeedUpdate: Boolean = true
    private var topUsersOffset: Int = DEFAULT_OFFSET
    private var isOpenedFromSyncContactsWelcome: Boolean = false
    private var peopleModel: PeopleModel? = null
    var isRecommendationLoading = false
        private set
    var isRecommendationLast = false
        private set
    var isTopContentLoading = false
        private set
    var isTopContentLast = false
        private set

    fun init(openedFromPeoples: Boolean) {
        this.showPeoples = !openedFromPeoples
        showEmpty()
        observeAddingUserToFriend()
        initObservers()
        getSavedContent()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun loadMore() {
        if (filterNumbers == null) {
            loadResult(
                query = query,
                offset = getPagingProperties().offset
            )
        } else {
            searchUserByNumber(filterNumbers, offset = getPagingProperties().offset)
        }
    }

    fun getPagingProperties(): PagingProperties = searchResultPagingProperties

    fun isFilterChanged(): Boolean {
        return if (previousFilterResult != null && filterResult == null) {
            previousFilterResult = null
            true
        } else {
            previousFilterResult.isSomethingChanged(filterResult)
        }
    }

    fun logOnFindFriendsPressed(fromWhere: AmplitudeFindFriendsWhereProperty) {
        amplitudeFindFriendsHelper.onFindFriendsPressed(fromWhere)
    }

    fun setFilterResult(result: FilterResult?) {
        previousFilterResult = filterResult?.copy()
        filterResult = result
    }

    fun setNumberFilter(params: NumberSearchParameters?) {
        filterNumbers = params
    }

    fun resetPaging() {
        getPagingProperties().reset()
    }

    fun getFilterResult() = filterResult

    fun forceClearRecent() {
        clearRecentJob?.cancel()
        clearRecent(true)
    }

    fun reload() {
        query = String.empty()
        showDefaultScreen()
        loadRecent()
        getSavedContent()
    }

    fun undoClearRecent() {
        clearRecentJob?.cancel()
        showDefaultScreen()
    }

    fun isClearingRecent(): Boolean {
        return clearRecentJob != null && clearRecentJob?.isActive == true
    }

    private fun clearRecent(force: Boolean = false) {
        showEmpty()

        if (!force) {
            emitViewEvent(PeopleUiEffect.ShowClearRecentSnackBar(CLEAR_RECENT_DELAY_SEC))
        }

        clearRecentJob = viewModelScope.launch(Dispatchers.IO) {
            if (!force) {
                delay(CLEAR_RECENT_DELAY_MS)
            }

            searchCleanRecentUseCase.execute(
                params = SearchCleanRecentUsersParams(),
                success = {
                    lastRecentItems = emptyList()
                    showEmpty()
                },
                fail = { exception ->
                    if (exception is CancellationException) return@execute
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))
                    Timber.e(exception)
                }
            )
        }
    }

    private fun selectRecentItem(recentUser: RecentUserUiModel) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = recentUser.uid,
                where = AmplitudePropertyWhere.YOU_VISITED
            )
        )
    }

    private fun openUserAddDialog(user: UserSearchResultUiEntity) {
        emitViewEvent(PeopleUiEffect.AddUserFromSearch(user))
    }

    private fun selectUserItem(user: UserSearchResultUiEntity) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = user.uid,
                where = AmplitudePropertyWhere.SEARCH
            )
        )
    }

    private fun openUserMoments(user: UserSearchResultUiEntity, fromView: View) {
        emitViewEvent(
            PeopleUiEffect.OpenMomentsProfile(
                userId = user.uid,
                view = fromView,
                hasNewMoments = user.hasNewMoments)
        )
    }

    private fun showEmpty() {
        val list = if (peopleModel == null) {
            peopleContentUiMapper.createDefaultContent(
                allowSyncContacts = getSyncContactsPrivacyUseCase.invoke(),
                showPeoples = showPeoples && !isKeyboardOpened,
                createForSearch = true
            )
        } else {
            peopleContentUiMapper.createPeopleContent(
                peopleApprovedUserModels = peopleModel!!.approvedUsers,
                peopleRelatedUserModels = peopleModel!!.relatedUsers,
                myUserId = getUserUidUseCase.invoke(),
                allowSyncContacts = getSyncContactsPrivacyUseCase.invoke(),
                showPeoples = showPeoples && !isKeyboardOpened,
                createForSearch = true
            )
        }
        setUiState(
            UserSearchViewState(
                contentList = list,
                showPlaceholder = (list.size == 1 && list.any { it is RecentUsersUiEntity }) || list.isEmpty(),
                showProgressBar = false
            )
        )
    }

    private fun showDefaultScreen() {
        if (clearRecentJob?.isActive == true) {
            showEmpty()
            return
        }
        if (lastRecentItems == null) return
        val list = if (peopleModel == null) {
            peopleContentUiMapper.createDefaultContent(
                allowSyncContacts = getSyncContactsPrivacyUseCase.invoke(),
                lastRecentItems = lastRecentItems,
                showLastRecentItems = !isKeyboardOpened,
                showPeoples = showPeoples && !isKeyboardOpened,
                createForSearch = true
            )
        } else {
            peopleContentUiMapper.createPeopleContent(
                peopleApprovedUserModels = peopleModel!!.approvedUsers,
                peopleRelatedUserModels = peopleModel!!.relatedUsers,
                myUserId = getUserUidUseCase.invoke(),
                allowSyncContacts = getSyncContactsPrivacyUseCase.invoke(),
                recentUsers = lastRecentItems,
                showLastRecentItems = !isKeyboardOpened,
                showPeoples = showPeoples && !isKeyboardOpened,
                createForSearch = true
            )
        }
        setUiState(
            UserSearchViewState(
                contentList = list,
                showPlaceholder = (list.size == 1 && list.any { it is RecentUsersUiEntity }) || list.isEmpty(),
                showProgressBar = false
            )
        )
    }

    private fun loadRecent(showDefaultState: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            searchRecentUseCase.execute(
                params = SearchRecentUserParams(),
                success = { response ->
                    lastRecentItems = SearchRecentUsersMapper().mapForPeoples(response)

                    if (showDefaultState) {
                        showDefaultScreen()
                    }
                },
                fail = { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))
                    Timber.e(exception)

                }
            )
        }
    }

    fun clearRecentGlobalIfExists() {
        if (isClearingRecent()) {
            GlobalScope.launch(Dispatchers.IO) {
                searchCleanRecentUseCase.execute(
                    params = SearchCleanRecentUsersParams(),
                    success = {
                        Timber.i("Success delete recents:${it}")
                        cancel()
                    },
                    fail = { exception ->
                        Timber.e(exception)
                        cancel()
                    }
                )
            }
        }
    }

    fun addUserToFriend(user: UserSearchResultUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                addUserToFriendUseCase.invoke(user.uid)
            }.onSuccess {
                val influencerProperty = createInfluencerAmplitudeProperty(
                    topContentMaker = user.topContentMaker.toBoolean(),
                    approved = user.approved.toBoolean()
                )
                pushAmplitudeAddFriend(
                    fromId = getUserUidUseCase.invoke(),
                    toId = user.uid,
                    influencer = influencerProperty
                )
                val newUser = user.copy(
                    buttonState = UserSearchResultUiEntity.ButtonState.Hide
                )

                findUserItemAndUpdate(newUser)
                emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.request_sent))
            }.onFailure { exception ->
                emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))

                Timber.e(exception)
            }
        }
    }

    fun acceptUserFriendRequest(user: UserSearchResultUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { addUserToFriendUseCase.invoke(user.uid) }
                .onSuccess {
                    val newUser = user.copy(
                        buttonState = UserSearchResultUiEntity.ButtonState.Hide
                    )

                    findUserItemAndUpdate(newUser)

                    findUserItemAndUpdate(user.uid, FRIEND_STATUS_CONFIRMED)
                }.onFailure { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))

                    Timber.e(exception)
                }
        }
    }

    fun declineUserFriendRequest(user: UserSearchResultUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            removeUserFromFriendAndUnsubscribeUseCase.execute(
                params = RemoveUserFromFriendAndUnsubscribeParams(user.uid),
                success = {
                    val newUser = user.copy(
                        buttonState = UserSearchResultUiEntity.ButtonState.ShowAdd
                    )

                    findUserItemAndUpdate(newUser)
                    emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.request_rejected))
                },
                fail = { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))

                    Timber.e(exception)
                }
            )
        }
    }

    fun subscribeUser(user: UserSearchResultUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeUserUserCase.execute(
                params = SubscribeUserParams(user.uid),
                success = {
                    val newUser = user.copy(
                        isSubscribed = true
                    )
                    val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
                        topContentMaker = user.topContentMaker.toBoolean(),
                        approved = user.approved.toBoolean()
                    )
                    amplitudeFollowButton.followAction(
                        fromId = appSettings.readUID(),
                        toId = user.uid,
                        where = AmplitudeFollowButtonPropertyWhere.SEARCH,
                        type = AmplitudePropertyType.OTHER,
                        amplitudeInfluencerProperty = amplitudeInfluencerProperty
                    )
                    findUserItemAndUpdate(newUser)
                    emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.meera_search_subscribed_notification))
                },
                fail = { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))

                    Timber.e(exception)
                }
            )
        }
    }

    fun getSearchQuery(): String {
        return query
    }

    fun unsubscribeUser(user: UserSearchResultUiEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { unsubscribeUserUseCase.invoke(user.uid) }
                .onSuccess {
                    val newUser = user.copy(
                        isSubscribed = false
                    )
                    val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
                        topContentMaker = user.topContentMaker.toBoolean(),
                        approved = user.approved.toBoolean()
                    )
                    amplitudeFollowButton.logUnfollowAction(
                        fromId = getUserUidUseCase.invoke(),
                        toId = user.uid,
                        where = AmplitudeFollowButtonPropertyWhere.SEARCH,
                        type = AmplitudePropertyType.OTHER,
                        amplitudeInfluencerProperty = amplitudeInfluencerProperty
                    )
                    findUserItemAndUpdate(newUser)
                    emitViewEvent(PeopleUiEffect.ShowSuccessToast(R.string.meera_search_unsubscribed_notification))
                }.onFailure { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))

                    Timber.e(exception)
                }
        }
    }

    fun search(query: String) {
        this.query = query
        resetPaging()
        loadResult(this.query)
    }

    fun searchUserByNumber(params: NumberSearchParameters?, offset: Int) {
        getPagingProperties().isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            searchUsersByNumberUseCase.execute(
                params = SearchByNumberUseCaseParams(
                    number = params?.number ?: "",
                    countryId = params?.countryId?.toInt() ?: 0,
                    typeId = params?.vehicleTypeId ?: 0
                ),
                success = { users ->
                    val mappedNewItems = searchUserResultMapper.mapToPeoplesUiEntity(users, getUserUidUseCase.invoke())
                    logVehicleSearch(params, offset, users.size)
                    setUiState(
                        UserSearchViewState(
                            contentList = getNewSearchItems(mappedNewItems),
                            showPlaceholder = mappedNewItems.isEmpty()
                        )
                    )
                    getPagingProperties().isLoading = false
                    getPagingProperties().isLastPage = users.isEmpty()
                    getPagingProperties().offset = offset + mappedNewItems.size
                },
                fail = {
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))
                    Timber.d(it)
                    getPagingProperties().isLoading = false
                }
            )
        }
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

            is FriendsContentActions.AddUserSearchResultUiAction -> {
                openUserAddDialog(contentActions.user)
            }

            is FriendsContentActions.ClearRecentUsersUiAction -> {
                clearRecent()
            }

            is FriendsContentActions.SelectRecentItemUiAction -> {
                selectRecentItem(contentActions.recentUser)
            }

            is FriendsContentActions.SelectUserSearchResultUiAction -> {
                selectUserItem(contentActions.user)
            }

            is FriendsContentActions.OpenUserMomentsAction -> {
                openUserMoments(contentActions.user, contentActions.fromView)
            }

            is FriendsContentActions.KeyboardVisibilityChanged -> {
                handleKeyboardVisibilityChange(contentActions.isKeyboardOpened)
            }
            else -> Unit
        }
    }

    private fun logCommunitySection() {
        peopleAnalyticDelegate.logCommunitySection()
    }

    private fun handleKeyboardVisibilityChange(isKeyboardOpened: Boolean) {
        this.isKeyboardOpened = isKeyboardOpened
        if (query.isNotEmpty() || filterResult != null) return
        showDefaultScreen()
    }

    private fun getSavedContent() {
        viewModelScope.launch {
            runCatching {
                getPeopleAllSavedContentUseCase.invoke()
            }.onSuccess { peopleModel ->
                this@SearchUserViewModel.peopleModel = peopleModel
                if (peopleModel.approvedUsers.isEmpty() && peopleModel.relatedUsers.isEmpty()) {
                    refreshPeopleContent()
                    return@onSuccess
                }
                resetPaginationState()
                resetRecommendedPeopleListPage()
                resetTopUsersOffset()
                setTopPeopleOffset(peopleModel.approvedUsers.size)
                showDefaultScreen()
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
    private fun refreshPeopleContent(showDefaultState: Boolean = true) {
        viewModelScope.launch {
            val topUsers = getFirstPageTopUsers()
            val relatedUsers = getFirstPageRelatedUsers()
            if (topUsers == null || relatedUsers == null) {
                handleError()
            } else {
                this@SearchUserViewModel.peopleModel = PeopleModel(topUsers, relatedUsers)
            }
            Timber.d("PeopleRequest: Top users: $topUsers Related users: $relatedUsers")
            resetPaginationState()
            resetTopUsersOffset()
            resetRecommendedPeopleListPage()
            loadRecent(showDefaultState)
            if (!showDefaultState) {
                _searchUserState.value?.let {
                    _searchUserState.value = it.copy(isRefreshing = false)
                }
            }
        }
    }

    private fun setTooltipShown() {
        setSelectCommunityTooltipShownUseCase.invoke()
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
                val currentList = _searchUserState.value?.contentList?.toMutableList() ?: mutableListOf()
                if (rootListPosition != -1) {
                    val listResult = peopleContentUiMapper.mapPaginationRecommendedListToUiList(
                        currentList = currentList,
                        newList = relatedUsers,
                        rootListPosition = rootListPosition
                    )
                    setUiState(
                        UserSearchViewState(
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
                isTopContentLoading = false
                peopleModel?.approvedUsers = peopleModel?.approvedUsers?.plus(approvedUsers) ?: emptyList()
                showDefaultScreen()
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
            UserSearchViewState(
                contentList = getContentListWithSubscribeStatusChanged(userId, isUserSubscribed),
                isRefreshing = _searchUserState.value?.isRefreshing ?: false
            )
        )
        requestTopUsersAndCache()
    }

    private fun getContentListWithSubscribeStatusChanged(
        userId: Long,
        isUserSubscribed: Boolean
    ): List<PeoplesContentUiEntity> = _searchUserState.value?.contentList
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
        val listResult = _searchUserState.value?.contentList?.toMutableList()
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
            currentList = _searchUserState.value?.contentList ?: return,
            selectedUserId = userId,
            isUserSubscribed = isAddToFriendRequest,
            showPossibleFriendsText = false
        )
        setUiState(
            UserSearchViewState(
                contentList = result,
                isRefreshing = _searchUserState.value?.isRefreshing ?: false
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

    private fun emitViewEvent(event: PeopleUiEffect) {
        viewModelScope.launch {
            _peoplesContentEvent.emit(event)
        }
    }

    /**
     * Реактивно слушаем изменения, если юзер переходит в другой профиль подписывается и отписывается
     * от юзера
     */
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

    private fun setUiState(newState: UserSearchViewState) {
        viewModelScope.launch(Dispatchers.Main) {
            _searchUserState.value = newState
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

    private suspend fun getFirstPageRelatedUsers(): List<PeopleRelatedUserModel>? {
        val response = runCatching {
            getRelatedUsersUseCase.invoke(
                limit = DEFAULT_PAGE_LIMIT,
                offset = DEFAULT_OFFSET
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
        val currentList = _searchUserState.value?.contentList ?: return
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
            type = FriendAddAction.ADD_FRIENDS_SEARCH
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
            where = AmplitudeFollowButtonPropertyWhere.ADVICE_TO_FOLLOW_SEARCH
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
            where = AmplitudeFollowButtonPropertyWhere.ADVICE_TO_FOLLOW_SEARCH
        )
        unSubscribeUser(user.userId)
    }

    private fun handleContentActionRefreshed() {
        if (!isNeedUpdate) return
        refreshPeopleContent()
        isNeedUpdate = false
        doDelayed(DELAY_NAVIGATION_BAR_CLICK_REQUEST) { isNeedUpdate = true }
        emitViewEvent(PeopleUiEffect.ScrollToPositionEffect(TOP_PEOPLE_CONTENT_POSITION))
    }

    private fun handleActionBySwipeToRefresh() {
        refreshPeopleContent(showDefaultState = false)
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
            emitViewEvent(PeopleUiEffect.OpenMomentsProfile(
                userId = entity.userId,
                view = view,
                hasNewMoments = entity.hasNewMoments
            ))

        } else {
            emitViewEvent(
                PeopleUiEffect.OpenUserProfile(
                    userId = entity.userId,
                    where = AmplitudePropertyWhere.ADVICE_TO_FOLLOW_SEARCH
                )
            )
        }
    }

    private fun handleUserClickAction(entity: PeopleInfoUiEntity) {
        emitViewEvent(
            PeopleUiEffect.OpenUserProfile(
                userId = entity.userId,
                where = AmplitudePropertyWhere.ADVICE_TO_FOLLOW_SEARCH
            )
        )
    }

    private fun handleRemoveFromFriendsAction(userId: Long) {
        removeFromFriends(userId)
    }

    private fun handleVideoPostClick(entity: BloggerMediaContentUiEntity.BloggerVideoContentUiEntity) {
        handleMediaContentClicked(
            userId = entity.rootUser.userId,
            postId = entity.postId,
            where = AmplitudePropertyWhere.CONTENT_ADVICE_TO_FOLLOW_SEARCH
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
            where = AmplitudePropertyWhere.CONTENT_ADVICE_TO_FOLLOW_SEARCH
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
            where = AmplitudePropertyWhere.PEOPLE_VIEW_ALL_SEARCH
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
            cardWhereAction = AmplitudePeopleContentCardProperty.SEARCH,
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
                where = AmplitudePropertyWhere.ADD_FRIENDS_SEARCH
            )
        )
    }

    private fun handleShowBumpClicked() {
        peopleAnalyticDelegate.logShakeOpenedByButton(AmplitudeShakeWhereProperty.SEARCH)
        emitViewEvent(PeopleUiEffect.ShowShakeDialog)
    }

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
        handleSyncContactsWelcome(needToShowWelcome)
    }

    private fun handleNeedToShowBadge(isCalledFromBottomNav: Boolean) {
        if (isCalledFromBottomNav && needShowPeopleBadgeUseCase.invoke()) {
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
        setNeedShowSyncContactsDialogUseCase.invoke(false)
        emitViewEvent(PeopleUiEffect.ShowSyncDialogPermissionDenied)
    }

    private fun showSyncContactsByClick() {
        isOpenedFromSyncContactsWelcome = true
        emitViewEvent(PeopleUiEffect.ShowSyncContactsDialogUiEffect)
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

    private fun handleContactPermissionDenied(deniedAndNoRationaleNeededAfterRequest: Boolean) {
        if (deniedAndNoRationaleNeededAfterRequest) {
            emitViewEvent(PeopleUiEffect.ShowSyncDialogPermissionDenied)
        }
    }

    private fun initObservers() {
        observeFriendStatusChanged()
        observeSyncContactsWorker()
        observeMomentsEvents()
        observeNumberSearchParams()
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

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event -> handleMomentsEvents(event) }.launchIn(viewModelScope)
    }

    private fun observeNumberSearchParams() {
        getNumberSearchParamsFlowUseCase.invoke()
            .filterNotNull()
            .onEach(::handleNumberParams)
            .catch { Timber.e(it) }
            .launchIn(viewModelScope)
    }

    private fun handleNumberParams(numberSearchParams: NumberSearchParameters) {
        setFilterResult(null)
        setNumberFilter(numberSearchParams)
        resetPaging()
        emitViewEvent(PeopleUiEffect.ApplyNumberSearchParams(numberSearchParams))
    }

    private fun handleMomentsEvents(event: MomentRepositoryEvent) {
        when (event) {
            is MomentRepositoryEvent.UserMomentsStateUpdated -> {
                updateUserMomentsState(event.userMomentsStateUpdate)
            }
            else -> Unit
        }
    }

    private fun updateUserMomentsState(update: UserMomentsStateUpdateModel) {
        val newList = getCurrentSearchItems()?.map {
            if (it is RecentUsersUiEntity) {
                val updatedRecentUsers = getUpdateRecentUsersMomentsState(
                    recentUsers = it.users,
                    update = update
                )
                return@map it.copy(users = updatedRecentUsers)
            }
            if (it is UserSearchResultUiEntity && it.getUserId() == update.userId) {
                return@map it.copy(hasMoments = update.hasMoments, hasNewMoments = update.hasNewMoments)
            }
            return@map it
        }
        setUiState(UserSearchViewState(contentList = newList))
    }

    private fun getUpdateRecentUsersMomentsState(recentUsers: List<RecentUserUiModel>, update: UserMomentsStateUpdateModel): List<RecentUserUiModel> {
        return recentUsers.map {
            if (it.uid == update.userId) {
                it.copy(hasMoments = update.hasMoments, hasNewMoments = update.hasNewMoments)
            } else {
                it
            }
        }
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
        val currentList = _searchUserState.value?.contentList?.toMutableList() ?: return
        if (isFinishedSuccess) {
            currentList.find { entity ->
                entity.getPeoplesActionType() == PeoplesContentType.CONTACT_SYNC_TYPE
            }?.let {
                currentList.remove(it)
            }
        }
        setUiState(
            UserSearchViewState(
                contentList = currentList,
                isRefreshing = _searchUserState.value?.isRefreshing ?: false,
                showProgressBar = !isFinished
            )
        )
    }

    private fun removeRelatedUserById(userId: Long) {
        val recommendationType = _searchUserState.value
            ?.contentList
            ?.filterIsInstance<RecommendedPeopleListUiEntity>()
            ?.firstOrNull()
        val currentRecommendations = recommendationType?.recommendedPeopleList ?: emptyList()
        if (currentRecommendations.isEmpty()) return
        val newRecommendations = currentRecommendations.toMutableList()
        newRecommendations.removeAll { user ->
            user.userId == userId
        }
        val newRecommendationsListEntity = RecommendedPeopleListUiEntity(newRecommendations, false)
        val listResult = _searchUserState.value?.contentList?.map { entity ->
            if (entity is RecommendedPeopleListUiEntity) newRecommendationsListEntity else entity
        }?.filterNot { entity ->
            entity is RecommendedPeopleListUiEntity && entity.recommendedPeopleList.isEmpty()
        } ?: emptyList()
        setUiState(
            UserSearchViewState(
                contentList = listResult,
                isRefreshing = _searchUserState.value?.isRefreshing ?: false,
                showProgressBar = _searchUserState.value?.showProgressBar ?: false
            )
        )
    }

    private fun handleHideRelatedUserAction(userId: Long) {
        peopleAnalyticDelegate.logUserCardHide(userId, AmplitudeUserCardHideSectionProperty.SEARCH)
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

    private fun getPeopleSyncContactsWhereProperty(allowSyncContactsWelcome: Boolean) =
        if (allowSyncContactsWelcome) AmplitudeSyncContactsWhereProperty.SUGGEST else
            AmplitudeSyncContactsWhereProperty.SEARCH

    private fun updateUsersByFriendsStatusChanged(userId: Long) {
        viewModelScope.launch {
            val listResult = _searchUserState.value?.contentList
                ?.filterIsInstance<RecommendedPeopleListUiEntity>()
                ?.firstOrNull()
                ?.let { it.recommendedPeopleList.find { related -> related.userId == userId } }
                ?.let { model -> getRemovedListIfUnsubscribed(model) }
                ?: getCurrentList()
            setUiState(
                UserSearchViewState(
                    contentList = listResult,
                    isRefreshing = _searchUserState.value?.isRefreshing ?: false,
                    showProgressBar = _searchUserState.value?.showProgressBar ?: false
                )
            )
        }
    }

    private fun getRemovedListIfUnsubscribed(
        user: RecommendedPeopleUiEntity
    ): List<PeoplesContentUiEntity> {
        val currentList = _searchUserState.value?.contentList?.toMutableList() ?: emptyList()
        return currentList.map { model ->
            if (model is RecommendedPeopleListUiEntity) {
                val users = model.recommendedPeopleList.toMutableList()
                users.remove(user)
                RecommendedPeopleListUiEntity(users, false)
            } else {
                model
            }
        }
    }

    private fun getCurrentList(): List<PeoplesContentUiEntity> =
        _searchUserState.value?.contentList ?: emptyList()

    private fun pushAmplitudeAddFriend(
        fromId: Long,
        toId: Long,
        influencer: AmplitudeInfluencerProperty
    ) {
        amplitudeAddFriendAnalytic.logAddFriend(
            fromId = fromId,
            toId = toId,
            type = FriendAddAction.SEARCH,
            influencer = influencer
        )
    }

    private fun showLoading() {
        setUiState(createSearchResultShimmerState())
    }

    private fun createSearchResultShimmerState(): UserSearchViewState {
        val shimmerList = mutableListOf<PeoplesContentUiEntity>()
        shimmerList.add(TitleSearchResultUiEntity(R.string.search_result_list_title))
        repeat(SEARCH_SHIMMER_COUNT) {
            shimmerList.add(UserSearchResultShimmerUiEntity)
        }
        return UserSearchViewState(
            contentList = shimmerList
        )
    }

    private fun getNewSearchItems(users: List<UserSearchResultUiEntity>?): List<PeoplesContentUiEntity> {
        return if (users.isNullOrEmpty()) {
            emptyList()
        } else {
            val list = mutableListOf<PeoplesContentUiEntity>()
            list.add(TitleSearchResultUiEntity(R.string.search_result_list_title))
            list.addAll(users)
            list
        }
    }

    private fun addSearchItems(newItems: List<UserSearchResultUiEntity>): List<PeoplesContentUiEntity> {
        val list = mutableListOf<PeoplesContentUiEntity>()
        list.addAll(getCurrentSearchItems() ?: emptyList())
        list.addAll(newItems)
        return list
    }

    private fun logVehicleSearch(
        params: NumberSearchParameters?,
        offset: Int,
        resultsCount: Int
    ) {
        if (offset != 0) return
        params?.let {
            val transportType = if (params.vehicleTypeId == 1) {
                AmplitudePropertyTransportType.CAR
            } else {
                AmplitudePropertyTransportType.MOTO
            }
            val country = params.countryName ?: ""
            val charCount = params.number.length
            val haveResult = if (resultsCount > 0) {
                AmplitudePropertyHaveResult.YES
            } else {
                AmplitudePropertyHaveResult.NO
            }
            val fullness = if (validateNumberForSearch(params.vehicleTypeId, params.number, params.countryId)) {
                AmplitudePropertyFullness.FULLY
            } else {
                AmplitudePropertyFullness.PARTICALLY
            }
            amplitudeHelper.logNumberSearch(
                transportType = transportType,
                country = country,
                fullness = fullness,
                charCount = charCount.toString(),
                haveResult = haveResult
            )
        }
    }

    private fun observeAddingUserToFriend() {
        disposables.add(
            userStateObserverUseCase
                .execute(AddUserToFriendObserverParams())
                .subscribeOn(Schedulers.io())
                .subscribe({ userState ->
                    updateUserStatus(userState)
                }, { Timber.e(it) })
        )
    }

    private fun updateUserStatus(userState: UserState) {
        when (userState) {
            is UserState.AddUserToFriendSuccess -> {
                findUserItemAndUpdate(userState.userId, userState.friendStatus)
            }

            is UserState.CancelFriendRequest -> {
                findUserItemAndUpdate(userState.userId, userState.friendStatus)
            }

            is UserState.BlockStatusUserChanged -> {
                if (userState.isBlocked) {
                    findUserItemAndRemove(userState.userId)
                }
            }
        }
    }

    private fun findUserItemAndRemove(userId: Long) {
        setUiState(UserSearchViewState(
            contentList = getCurrentSearchItems()?.filterNot { it.getUserId() == userId }
        ))
    }

    private fun findUserItemAndUpdate(userId: Long, friendStatus: Int) {
        val newList = getCurrentSearchItems()?.map {
            if (it is UserSearchResultUiEntity && it.getUserId() == userId) {
                return@map it.copy(buttonState = FriendStatusButtonMapper().mapForPeoples(friendStatus))
            }
            return@map it
        }
        setUiState(UserSearchViewState(contentList = newList))
    }

    private fun findUserItemAndUpdate(user: UserSearchResultUiEntity) {
        val newList = getCurrentSearchItems()?.map {
            if (it is UserSearchResultUiEntity && it.getUserId() == user.uid) {
                return@map user
            }
            return@map it
        }
        setUiState(UserSearchViewState(contentList = newList))
    }

    private fun loadResult(
        query: String = String.empty(),
        offset: Int = 0,
        limit: Int = DEFAULT_SEARCH_RESULT_PAGE_SIZE,
    ) {

        getPagingProperties().isLoading = true
        val isPagingLoad = offset != 0
        if (!isPagingLoad) {
            showLoading()
        }

        viewModelScope.launch(Dispatchers.IO) {
            val gender = when (filterResult?.gender) {
                FilterGender.MALE -> 1
                FilterGender.FEMALE -> 0
                else -> null
            }
            val ageFrom = filterResult?.age?.start
            val ageTo = filterResult?.age?.end
            val cityIds = mutableListOf<Int>()
            filterResult?.cities?.forEach { cityIds.add(it.cityId) }
            val countryIds = mutableListOf<Int>()
            filterResult?.countries?.forEach {
                it.id?.let { id -> countryIds.add(id) }
            }

            searchUsersUseCase.execute(
                params = SearchUsersParams(
                    query = query, limit = limit, offset = offset,
                    gender = gender, ageFrom = ageFrom, ageTo = ageTo, cityIds = cityIds,
                    countryIds = countryIds
                ),
                success = { users ->
                    val mappedNewItems = searchUserResultMapper.mapToPeoplesUiEntity(users, getUserUidUseCase.invoke())
                    val resultList = if (isPagingLoad.not()) {
                        getNewSearchItems(mappedNewItems)
                    } else {
                        addSearchItems(mappedNewItems)
                    }

                    if (isPagingLoad.not()) {
                        analyticLogInputSearch(resultList.isNotEmpty())
                    }
                    setUiState(
                        UserSearchViewState(
                            contentList = resultList,
                            showPlaceholder = resultList.isEmpty()
                        )
                    )

                    getPagingProperties().isLastPage = users.isEmpty()
                    getPagingProperties().isLoading = false
                    getPagingProperties().offset = offset + mappedNewItems.size
                },
                fail = { exception ->
                    emitViewEvent(PeopleUiEffect.ShowErrorToast(R.string.error_try_later))
                    getPagingProperties().isLoading = false

                    Timber.d(exception)
                }
            )
        }
    }

    private fun analyticLogInputSearch(haveResult: Boolean) {
        val haveResultProperty = if (haveResult) AmplitudePropertyHaveResult.YES else AmplitudePropertyHaveResult.NO
        amplitudeHelper.logSearchInput(
            type = AmplitudePropertySearchType.PEOPLE,
            haveResult = haveResultProperty,
            whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.NONE
        )
    }

    private fun getCurrentSearchItems(): List<PeoplesContentUiEntity>? =
        _searchUserState.value?.contentList

}
