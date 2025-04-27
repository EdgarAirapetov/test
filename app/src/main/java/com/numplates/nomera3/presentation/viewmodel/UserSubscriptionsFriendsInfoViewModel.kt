package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetUserFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserSubscribersUseCase
import com.numplates.nomera3.domain.interactornew.GetUserSubscriptionsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.util.UserSubscriptionsUiMapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeHowSelectedMutualFriendsProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeMutualFriendsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectedMutualFriendsTabProperty
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_FRIENDS
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_SUBSCRIBERS
import com.numplates.nomera3.presentation.view.fragments.MODE_SHOW_USER_SUBSCRIPTIONS
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendsFollowersUiState
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserSubscriptionsFriendsInfoViewModel @Inject constructor(
    private val getUserFriendsUseCase: GetUserFriendsUseCase,
    private val getUserSubscribersUseCase: GetUserSubscribersUseCase,
    private val getUserSubscriptionsUseCase: GetUserSubscriptionsUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val amplitudeMutualFriendsAnalytic: AmplitudeMutualFriendsAnalytic,
    private val appSettings: AppSettings
) : ViewModel() {

    private val _userSubscriptionsState = MutableLiveData<UserFriendsFollowersUiState>()
    val userSubscriptionsState: LiveData<UserFriendsFollowersUiState> = _userSubscriptionsState
    private val _userSubscriptionViewEvent = MutableSharedFlow<UserSubscriptionViewEvent>()
    val userSubscriptionViewEvent: SharedFlow<UserSubscriptionViewEvent> =
        _userSubscriptionViewEvent
    private val mapper = UserSubscriptionsUiMapper()
    private val userSearchState = UserSearchState()

    /**
     * Данный arrayList будет хранить весь имеющийся список
     * Устанавливаются данные в 2 случаях:
     * 1.Отрабатывает пагинация
     * 2.Самый первый запрос
     */
    private var currentList = mutableListOf<FriendsFollowersUiModel>()

    // Pagination
    private var isLoadingFriends = false
    private var isLoadingSubscribers = false
    private var isLoadingSubscriptions = false
    private var isLastFriend = false
    private var isLastSubscriber = false
    private var isLastSubscription = false

    init {
        App.component.inject(this)
        observeFriendStatusChanged()
    }

    fun isLastPage(page: Int?) = when (page) {
        MODE_SHOW_USER_FRIENDS -> isLastFriend
        MODE_SHOW_USER_SUBSCRIBERS -> isLastSubscriber
        MODE_SHOW_USER_SUBSCRIPTIONS -> isLastSubscription
        else -> false
    }

    fun isLoading(page: Int?) = when (page) {
        MODE_SHOW_USER_FRIENDS -> isLoadingFriends
        MODE_SHOW_USER_SUBSCRIBERS -> isLoadingSubscribers
        MODE_SHOW_USER_SUBSCRIPTIONS -> isLoadingSubscriptions
        else -> false
    }

    /**
     * Получаем список юзеров в зависомости от состояния экрана
     * @param userId - id юзера, которого хотим получить список
     * @param actionMode - Переменная, которая помогает определить тип экрана "Друзья/Подписчики/Подписки"
     * @param isRefreshing - Происходит ли запрос через SwipeRefreshLayout
     * @param querySearch - Поисковая строка
     * @param clickedItem - Модель, если юзер кликнул на состояние иконки
     */
    fun getUserList(
        userId: Long?,
        actionMode: Int?,
        isRefreshing: Boolean = false,
        querySearch: String? = null,
        clickedItem: FriendsFollowersUiModel? = null,
        offset: Int = DEFAULT_OFFSET
    ) {
        actionMode?.let { mode ->
            Timber.d("User subscribers mode: $mode")
            userSearchState.saveSearchState(querySearch)
            if (isRefreshing) {
                pushUiStateList(isRefreshing = true)
            }
            when (mode) {
                MODE_SHOW_USER_FRIENDS -> {
                    getUserFriends(
                        userid = userId ?: 0,
                        offset = offset,
                        querySearch = querySearch,
                        clickedItem = clickedItem
                    )
                }
                MODE_SHOW_USER_SUBSCRIBERS -> {
                    getUserSubscribers(
                        userId = userId ?: 0,
                        offset = offset,
                        querySearch = querySearch,
                        clickedItem = clickedItem
                    )
                }
                MODE_SHOW_USER_SUBSCRIPTIONS -> {
                    getUserSubscriptions(
                        userId = userId ?: 0,
                        offset = offset,
                        querySearch = querySearch,
                        clickedItem = clickedItem
                    )
                }
            }
        }
    }

    fun isSearch() = userSearchState.isSearch

    fun getSearchInput() = userSearchState.searchInputText

    fun clearSearchList() = userSearchState.resetSearchList()

    fun replaceSearchListToCurrentList() {
        handleUiState()
    }

    fun searchOpenStateChanged(isSearchOpen: Boolean) {
        if (isSearchOpen) userSearchState.searchOpen() else userSearchState.resetSearch()
    }

    fun logMutualFriendsTabChanged(actionMode: Int?) {
        when (actionMode) {
            MODE_SHOW_USER_FRIENDS -> {
                logMutualFriendsTabChangedByState(AmplitudeSelectedMutualFriendsTabProperty.FRIENDS)
            }
            MODE_SHOW_USER_SUBSCRIBERS -> {
                logMutualFriendsTabChangedByState(
                    AmplitudeSelectedMutualFriendsTabProperty.FOLLOWERS
                )
            }
            MODE_SHOW_USER_SUBSCRIPTIONS -> {
                logMutualFriendsTabChangedByState(
                    AmplitudeSelectedMutualFriendsTabProperty.FOLLOWS
                )
            }
        }
    }

    fun isMe(userId: Long?) = appSettings.readUID() == userId

    private fun logMutualFriendsTabChangedByState(
        currentState: AmplitudeSelectedMutualFriendsTabProperty
    ) {
        amplitudeMutualFriendsAnalytic.logMutualFriendsTabSelected(
            friendTabSelected = currentState,
            typeSelected = AmplitudeHowSelectedMutualFriendsProperty.TABS_INSIDE_BLOCK
        )
    }

    private fun getUserFriends(
        userid: Long,
        offset: Int,
        querySearch: String? = null,
        clickedItem: FriendsFollowersUiModel? = null
    ) {
        isLoadingFriends = true
        viewModelScope.launch {
            runCatching {
                getUserFriendsUseCase.invoke(
                    userId = userid,
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = offset,
                    querySearch = querySearch
                )
            }.onSuccess { friendsList ->
                isLoadingFriends = false
                handleListResponse(
                    ownUserId = getUserUidUseCase.invoke(),
                    friendsList = friendsList?.friends,
                    offset = offset,
                    clickedItem = clickedItem
                )
                isLastFriend = friendsList?.friends?.isEmpty() == true
            }.onFailure { e ->
                isLastFriend = false
                isLoadingFriends = false
                handleErrorResponse(e)
            }
        }
    }

    private fun getUserSubscribers(
        userId: Long,
        offset: Int,
        querySearch: String? = null,
        clickedItem: FriendsFollowersUiModel?
    ) {
        isLoadingSubscribers = true
        viewModelScope.launch {
            runCatching {
                getUserSubscribersUseCase.invoke(
                    userId = userId,
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = offset,
                    querySearch = querySearch
                )
            }.onSuccess { subscribersList ->
                isLoadingSubscribers = false
                handleListResponse(
                    ownUserId = getUserUidUseCase.invoke(),
                    friendsList = subscribersList?.subscriptions,
                    offset = offset,
                    clickedItem = clickedItem
                )
                isLastSubscriber = subscribersList?.subscriptions?.isEmpty() == true
            }.onFailure { e ->
                isLoadingSubscribers = false
                isLastSubscriber = false
                handleErrorResponse(e)
            }
        }
    }

    private fun getUserSubscriptions(
        userId: Long,
        offset: Int,
        querySearch: String?,
        clickedItem: FriendsFollowersUiModel?
    ) {
        isLoadingSubscriptions = true
        viewModelScope.launch {
            runCatching {
                getUserSubscriptionsUseCase.invoke(
                    userId = userId,
                    limit = DEFAULT_PAGE_LIMIT,
                    offset = offset,
                    querySearch = querySearch
                )
            }.onSuccess { subscriptionList ->
                isLoadingSubscriptions = false
                handleListResponse(
                    ownUserId = getUserUidUseCase.invoke(),
                    friendsList = subscriptionList?.subscriptions,
                    offset = offset,
                    clickedItem = clickedItem
                )
                isLastSubscription = subscriptionList?.subscriptions?.isEmpty() == true
            }.onFailure { e ->
                isLoadingSubscriptions = false
                isLastSubscription = false
                handleErrorResponse(e)
            }
        }
    }

    private fun handleListResponse(
        ownUserId: Long,
        friendsList: List<UserSimple?>?,
        offset: Int,
        clickedItem: FriendsFollowersUiModel?
    ) {
        friendsList?.let { friendsListNotNull ->
            val newList = mapper.mapFromUserSimpleListToUiModel(
                responseList = friendsListNotNull,
                myUserId = ownUserId
            )
            if (offset == DEFAULT_OFFSET) {
                createNewList(newList, clickedItem)
            } else {
                addAllList(newList, clickedItem)
            }
            handleUiState()
        }
    }

    private fun handleUiState() {
        when {
            currentList.isEmpty() || (userSearchState.isSearch
                && userSearchState.searchList.isEmpty()) -> pushPlaceholderState()
            else -> pushUiStateList(false)
        }
    }

    /**
     * Определяем, что идет ли сейчас поиск.
     * Если данные пришли вне поиска, то устанавливаем значения в currentList
     */
    private fun createNewList(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        if (userSearchState.isSearch) {
            userSearchState.searchList = newList.toMutableList()
            checkIsNeedReplaceIndex(newList, clickedItem)
        } else currentList = newList.toMutableList()
    }

    /**
     * Устанавливаются значения, если отрабатывает пагинация
     */
    private fun addAllList(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        if (userSearchState.isSearch) {
            userSearchState.searchList.addAll(newList)
            checkIsNeedReplaceIndex(newList, clickedItem)
        } else currentList.addAll(newList)
    }

    /**
     * В данном методе мы изменяем index основого списка currentList
     * Кейс в том, что если происходит поиск и мы изменяем состояние иконки, то будет обновлен только
     * searchList. Основной список тоже будет обновлен.
     */
    private fun checkIsNeedReplaceIndex(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        clickedItem?.let { item ->
            val index = currentList.indexOf(item)
            newList.find { currentModel ->
                currentModel.userSimple?.userId == clickedItem.userSimple?.userId
            }.let { uiModel ->
                currentList[index] = uiModel ?: return
            }
        }
    }

    private fun handleErrorResponse(t: Throwable) {
        Timber.e(t)
        pushUiStateList(isRefreshing = false)
    }

    private fun pushUiStateList(isRefreshing: Boolean = false) {
        val newList = mutableListOf<FriendsFollowersUiModel>()
        if (userSearchState.isSearch) newList.addAll(userSearchState.searchList) else newList.addAll(
            currentList
        )
        _userSubscriptionsState.postValue(
            UserFriendsFollowersUiState.SuccessGetList(
                isShowProgress = false,
                friendsList = newList,
                isRefreshing = isRefreshing
            )
        )
    }

    private fun pushPlaceholderState() {
        _userSubscriptionsState.postValue(
            UserFriendsFollowersUiState.ListEmpty(userSearchState.isSearch)
        )
    }

    private fun emitViewEvent(typeEvent: UserSubscriptionViewEvent) {
        viewModelScope.launch {
            _userSubscriptionViewEvent.emit(typeEvent)
        }
    }

    /**
     * Тут мы будем слушать изменения, когда юзер меняет состояние друга:
     * 1.Подписывается
     * 2.Отписывается
     * 3.Добавляет в друзья
     * 4.Удаляет из друзей
     */
    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach(::handleFriendStatusChanged)
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(event: UserSettingsEffect) {
        when (event) {
            is UserSettingsEffect.UserFriendStatusChanged -> {
                val itemSelected = findItemByIdFromCurrentList(event.userId)
                itemSelected?.let { selectedItemNotNull ->
                    emitViewEvent(UserSubscriptionViewEvent.RefreshUserList(selectedItemNotNull))
                }
            }
            is UserSettingsEffect.UserBlockStatusChanged -> {
                Timber.d("Is user blocked me: ${event.isUserBlocked}")
                val itemSelected = findItemByIdFromCurrentList(event.userId)
                itemSelected?.let { selectedItemNotNull ->
                    emitViewEvent(UserSubscriptionViewEvent.RefreshUserList(selectedItemNotNull))
                }
            }
            else -> Unit
        }
    }

    private fun findItemByIdFromCurrentList(
        userId: Long
    ): FriendsFollowersUiModel? {
        return currentList.find { item ->
            userId == item.userSimple?.userId
        }
    }

    /**
     * Данный класс будет хранить состояние поиска
     */
    private inner class UserSearchState {
        var isSearch = false
        var searchInputText: String? = null

        /**
         * Данный список хранит весь поисковый массив. Устанавливаются значения в случае, когда идет поиск
         * И очищается, когда юзер нажал кнопку "Назад"
         */
        var searchList = mutableListOf<FriendsFollowersUiModel>()

        fun resetSearch() {
            isSearch = false
            searchInputText = null
        }

        fun searchOpen() {
            isSearch = true
        }

        fun saveSearchState(searchInput: String?) {
            searchInputText = searchInput
        }

        fun resetSearchList() = searchList.clear()
    }

    companion object {
        private const val DEFAULT_PAGE_LIMIT = 100
        private const val DEFAULT_OFFSET = 0
    }
}
