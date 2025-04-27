package com.numplates.nomera3.modules.search.ui.viewmodel.user

import android.view.View
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyFullness
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyTransportType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriends
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.search.domain.mapper.action.ReplaceUserMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.FriendStatusButtonMapper
import com.numplates.nomera3.modules.search.domain.mapper.result.SearchUserResultMapper
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCaseParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchCleanRecentUsersUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchRecentUsersUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchUsersParams
import com.numplates.nomera3.modules.search.domain.usecase.SearchUsersUseCase
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.numplates.nomera3.modules.search.ui.entity.event.SearchMessageViewEvent
import com.numplates.nomera3.modules.search.ui.entity.event.UserSearchViewEvent
import com.numplates.nomera3.modules.search.ui.entity.state.SearchResultViewState
import com.numplates.nomera3.modules.search.ui.viewmodel.base.DEFAULT_SEARCH_RESULT_PAGE_SIZE
import com.numplates.nomera3.modules.search.ui.viewmodel.base.SearchResultScreenBaseViewModel
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendObserverParams
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendParams
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeParams
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeUseCase
import com.numplates.nomera3.modules.user.domain.usecase.SubscribeUserParams
import com.numplates.nomera3.modules.user.domain.usecase.SubscribeUserUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UnsubscribeUserParams
import com.numplates.nomera3.modules.user.domain.usecase.UnsubscribeUserUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UserStateObserverUseCase
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterGender
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterResult
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.isSomethingChanged
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.widgets.numberplateview.validateNumberForSearch
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Deprecated("use SearchUserViewModel")
class SearchUserResultViewModel : SearchResultScreenBaseViewModel() {

    @Inject
    lateinit var searchUsersUseCase: SearchUsersUseCase

    @Inject
    lateinit var searchUsersByNumberUseCase: SearchByNumberUseCase

    @Inject
    lateinit var searchRecentUseCase: SearchRecentUsersUseCase

    @Inject
    lateinit var searchCleanRecentUseCase: SearchCleanRecentUsersUseCase

    @Inject
    lateinit var addUserToFriendUseCase: AddUserToFriendUseCase

    @Inject
    lateinit var removeUserFromFriendAndUnsubscribeUseCase: RemoveUserFromFriendAndUnsubscribeUseCase

    @Inject
    lateinit var subscribeUserUserCase: SubscribeUserUseCase

    @Inject
    lateinit var unsubscribeUserUseCase: UnsubscribeUserUseCase

    @Inject
    lateinit var userStateObserverUseCase: UserStateObserverUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var amplitudeFollowButton: AmplitudeFollowButton

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var amplitudeFindFriendsHelper: AmplitudeFindFriends

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase

    @Inject
    lateinit var searchUserResultMapper: SearchUserResultMapper

    @Inject
    lateinit var amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic

    private var filterResult: FilterResult? = null
    private var previousFilterResult: FilterResult? = null
    private var filterNumbers: NumberSearchParameters? = null

    init {
        App.component.inject(this)
        addUserToFriendObserver()
        observeMomentsEvents()
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

    override fun isFilterChanged(): Boolean {
        return if (previousFilterResult != null && filterResult == null) {
            previousFilterResult = null
            true
        } else {
            previousFilterResult.isSomethingChanged(filterResult)
        }
    }

    override fun loadMore() {
        if (filterNumbers == null) {
            loadResult(
                query = query,
                offset = getPagingProperties().offset
            )
        } else {
            searchUserByNumber(filterNumbers, offset = getPagingProperties().offset)
        }
    }

    fun selectRecentItem(recentUser: SearchItem.RecentBlock.RecentBaseItem.RecentUser) {
        publishEvent(
            UserSearchViewEvent.SelectUser(
                userId = recentUser.uid,
                isRecent = true,
                approved = recentUser.approved,
                topContentMaker = recentUser.topContentMaker
            )
        )
    }

    fun selectUserItem(user: SearchItem.User) {
        publishEvent(
            UserSearchViewEvent.SelectUser(
                userId = user.uid,
                isRecent = false,
                approved = user.approved.toBoolean(),
                topContentMaker = user.topContentMaker.toBoolean()
            )
        )
    }

    fun openUserMoments(user: SearchItem.User, view: View?) {
        publishEvent(UserSearchViewEvent.OpenUserMoments(
            userId = user.uid,
            view = view,
            hasNewMoments = user.hasNewMoments
        ))
    }

    fun openUserAddDialog(user: SearchItem.User) {
        publishEvent(UserSearchViewEvent.AddUser(user))
    }

    fun addUserToFriend(user: SearchItem.User) {
        viewModelScope.launch(Dispatchers.IO) {
            addUserToFriendUseCase.execute(
                params = AddUserToFriendParams(user.uid),
                success = {
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
                        buttonState = SearchItem.User.ButtonState.Hide
                    )

                    showListWithNewUser(newUser)

                    val message = if (user.isSubscribed) {
                        SearchMessageViewEvent.UserAddToFriendWhileSubscribed
                    } else {
                        SearchMessageViewEvent.UserAddToFriendWhileNoSubscribed
                    }

                    publishMessage(message)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }
    }

    fun acceptUserFriendRequest(user: SearchItem.User) {
        viewModelScope.launch(Dispatchers.IO) {
            addUserToFriendUseCase.execute(
                params = AddUserToFriendParams(user.uid),
                success = {
                    val newUser = user.copy(
                        buttonState = SearchItem.User.ButtonState.Hide
                    )

                    showListWithNewUser(newUser)
                    publishMessage(SearchMessageViewEvent.UserAcceptedIncomingFriendRequest)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }

    }

    fun declineUserFriendRequest(user: SearchItem.User) {
        viewModelScope.launch(Dispatchers.IO) {
            removeUserFromFriendAndUnsubscribeUseCase.execute(
                params = RemoveUserFromFriendAndUnsubscribeParams(user.uid),
                success = {
                    val newUser = user.copy(
                        buttonState = SearchItem.User.ButtonState.ShowAdd
                    )

                    showListWithNewUser(newUser)
                    publishMessage(SearchMessageViewEvent.UserDeclinedIncomingFriendRequest)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }
    }

    fun subscribeUser(user: SearchItem.User) {
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
                    showListWithNewUser(newUser)
                    publishMessage(SearchMessageViewEvent.UserSubscribed)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }
    }

    fun unsubscribeUser(user: SearchItem.User) {
        viewModelScope.launch(Dispatchers.IO) {
            unsubscribeUserUseCase.execute(
                params = UnsubscribeUserParams(user.uid),
                success = {
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
                    showListWithNewUser(newUser)
                    publishMessage(SearchMessageViewEvent.UserUnsubscribed)
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)

                    Timber.e(exception)
                }
            )
        }
    }

    override fun search(query: String) {
        super.search(query)

        loadResult(this.query)
    }

    override fun searchUserByNumber(params: NumberSearchParameters?, offset: Int) {
        getPagingProperties().isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            searchUsersByNumberUseCase.execute(
                params = SearchByNumberUseCaseParams(
                    number = params?.number ?: "",
                    countryId = params?.countryId?.toInt() ?: 0,
                    typeId = params?.vehicleTypeId ?: 0
                ),
                success = { users ->
                    val mappedNewItems = searchUserResultMapper.mapToSearchItem(users, getUserUidUseCase.invoke())
                    // no need to show "results" label if there is no results
                    logVehicleSearch(params, offset, users.size)
                    val result = if (users.isEmpty()) {
                        SearchResultViewState.SearchResult(mappedNewItems, params?.number.isNullOrEmpty())
                    } else {
                        SearchResultViewState.SearchResult(
                            newSearchItems(
                                mappedNewItems
                            ), params?.number.isNullOrEmpty()
                        )
                    }
                    resultState.postValue(result)
                    getPagingProperties().isLoading = false
                    getPagingProperties().isLastPage = users.isEmpty()
                    getPagingProperties().offset = offset + result.value.size
                },
                fail = {
                    publishMessage(SearchMessageViewEvent.Error)
                    Timber.d(it)
                    getPagingProperties().isLoading = false
                }
            )
        }
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

    fun showEmpty() {
        resultState.postValue(SearchResultViewState.DefaultResult(emptyList()))
    }

    private fun showListWithNewUser(newUser: SearchItem.User) {
        ReplaceUserMapper(getCurrentRenderData()).map(newUser).let { newData ->
            resultState.postValue(newData)
        }
    }

    private fun showListWithUpdatedUser(newUser: SearchItem.User) {
        ReplaceUserMapper(getCurrentRenderData()).map(newUser).let { newData ->
            resultState.postValue(SearchResultViewState.UpdateSearchResultItem(
                value = newData.value,
                updatedUser = newUser
            ))
        }
    }

    private fun loadResult(
        query: String = String.empty(),
        offset: Int = 0,
        limit: Int = DEFAULT_SEARCH_RESULT_PAGE_SIZE,
    ) {
        showLoading()

        getPagingProperties().isLoading = true
        val isPagingLoad = offset != 0

        if (!isPagingLoad) {
            publishList(SearchResultViewState.SearchStart)
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
                    val mappedNewItems = searchUserResultMapper.mapToSearchItem(users, getUserUidUseCase.invoke())
                    val resultList = if (isPagingLoad.not()) {
                        newSearchItems(mappedNewItems)
                    } else {
                        addSearchItems(mappedNewItems)
                    }

                    if (isPagingLoad.not()) {
                        resultState.postValue(SearchResultViewState.SearchResult(resultList))
                    } else {
                        resultState.postValue(SearchResultViewState.Data(resultList))
                    }

                    getPagingProperties().isLastPage = users.isEmpty()
                    getPagingProperties().isLoading = false
                    getPagingProperties().offset = offset + mappedNewItems.size
                },
                fail = { exception ->
                    publishMessage(SearchMessageViewEvent.Error)
                    getPagingProperties().isLoading = false

                    Timber.d(exception)
                }
            )
        }
    }

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

    private fun newSearchItems(newItems: List<SearchItem>): List<SearchItem> {
        return if (newItems.isNotEmpty()) {
            val title = SearchItem.Title(R.string.search_result_list_title)
            mutableListOf<SearchItem>(title).apply {
                addAll(newItems)
            }
        } else {
            newItems
        }
    }

    private fun addSearchItems(newItems: List<SearchItem>): List<SearchItem> {
        val currentItems = getAllSearchItems()
        return mutableListOf<SearchItem>().apply {
            addAll(currentItems)
            addAll(newItems)
        }
    }

    private fun getCurrentSearchItems(): List<SearchItem> {
        return getAllSearchItems().filterIsInstance<SearchItem.User>()
    }

    fun addUserToFriendObserver() {
        disposables.add(
            userStateObserverUseCase
                .execute(AddUserToFriendObserverParams())
                .subscribeOn(Schedulers.io())
                .subscribe({ userState ->
                    updateUserStatus(userState)
                }, { Timber.e(it) })
        )
    }

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event -> handleMomentsEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleMomentsEvents(event: MomentRepositoryEvent) {
        when (event) {
            is MomentRepositoryEvent.UserMomentsStateUpdated ->
                findUserItemAndUpdate(
                    userId = event.userMomentsStateUpdate.userId,
                    hasMoments = event.userMomentsStateUpdate.hasMoments,
                    hasNewMoments = event.userMomentsStateUpdate.hasNewMoments
                )
            else -> Unit
        }
    }

    private fun updateUserStatus(userState: UserState) {
        when (userState) {
            is UserState.AddUserToFriendSuccess -> {
                findUserItemAndUpdate(userId = userState.userId, friendStatus = userState.friendStatus)
            }
            is UserState.CancelFriendRequest -> {
                findUserItemAndUpdate(userId = userState.userId, friendStatus = userState.friendStatus)
            }
            is UserState.BlockStatusUserChanged -> {
                if (userState.isBlocked) {
                    findUserItemAndRemove(userState.userId)
                }
            }
        }
    }

    private fun findUserItemAndRemove(userId: Long) {
        val foundItem = getCurrentSearchItems().find { searchItem ->
            if (searchItem is SearchItem.User) return@find searchItem.uid == userId
            else return@find false
        }
        val newList = mutableListOf<SearchItem>()
        newList.addAll(getCurrentSearchItems())
        newList.remove(foundItem)
        publishList(SearchResultViewState.Data(newList))
    }

    private fun findUserItemAndUpdate(userId: Long, friendStatus: Int? = null, hasMoments: Boolean? = null, hasNewMoments: Boolean? = null) {
        val foundItem = getCurrentSearchItems().find { searchItem ->
            if (searchItem is SearchItem.User) {
                return@find searchItem.uid == userId
            } else {
                return@find false
            }
        }
        if (foundItem is SearchItem.User) {
            val buttonState = if (friendStatus != null) FriendStatusButtonMapper().map(friendStatus) else foundItem.buttonState
            val hasMomentsValue = hasMoments ?: foundItem.hasMoments
            val hasNewMomentsValue = hasNewMoments ?: foundItem.hasNewMoments
            showListWithUpdatedUser(
                foundItem.copy(
                    buttonState = buttonState,
                    hasMoments = hasMomentsValue,
                    hasNewMoments = hasNewMomentsValue
                )
            )
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
