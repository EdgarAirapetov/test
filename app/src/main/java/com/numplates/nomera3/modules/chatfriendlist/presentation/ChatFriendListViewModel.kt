package com.numplates.nomera3.modules.chatfriendlist.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereFriendsSearch
import com.numplates.nomera3.modules.chat.domain.interactors.ChatInteractorImlp
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import com.numplates.nomera3.modules.chatfriendlist.domain.GetFriendlistUsecase
import com.numplates.nomera3.modules.chatfriendlist.presentation.paging.FriendsDataCallback
import com.numplates.nomera3.modules.chatfriendlist.presentation.paging.FriendsDataSourceFactory
import com.numplates.nomera3.modules.userprofile.domain.maper.toChatInitUserProfile
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

class ChatFriendListViewModel @Inject constructor(
    private val friendlistUsecase: GetFriendlistUsecase,
    private val chatInteractor: ChatInteractorImlp,
    private val userinfoUsecase: GetProfileUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val analyticsHelper: AnalyticsInteractor
) : BaseViewModel() {

    private val _viewEventsLiveData = MutableLiveData<ChatFriendListViewEvent>(ChatFriendListViewEvent.Empty)
    val viewEventsLiveData: LiveData<ChatFriendListViewEvent> = _viewEventsLiveData

    val effect: SharedFlow<ChatFriendListEffect?>
        get() = _effect.asSharedFlow()
    private val _effect = MutableSharedFlow<ChatFriendListEffect>()

    private val friendsDataCallback = object : FriendsDataCallback {
        override fun getData(nameQuery: String?, startingFrom: Int, howMuch: Int) =
            runBlocking {
                try {
                    friendlistUsecase.invoke(
                        nameQuery = nameQuery,
                        startingFrom = startingFrom,
                        howMuch = howMuch
                    )
                } catch (e: Exception) {
                    Timber.e(e)
                    _viewEventsLiveData.postValue(ChatFriendListViewEvent.FailedToLoadFriendList)
                    null
                }
            }
    }
    private val myFriendsDataSource = FriendsDataSourceFactory(friendsDataCallback)

    private var searchInput: String by Delegates.observable(initialValue = "") { _, _, newQuery ->
        loadFriends(newQuery)
    }

    val friendList: LiveData<PagedList<UserSimple>> = myFriendsDataSource.toLiveData(
        Config(
            pageSize = DEFAULT_PAGING_PAGE_SIZE,
            prefetchDistance = DEFAULT_PAGING_PREFETCH_DISTANCE,
            enablePlaceholders = DEFAULT_PAGING_ARE_PLACEHOLDERS_ENABLED)
    ).apply {
        observeForever(::onSearchResult)
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }

    fun handleAction(action: ChatFriendListAction) {
        when (action) {
            ChatFriendListAction.OpenNewChatScreen -> handleOpenNewFragmentAction()
        }
    }

    fun onNewSearchQuery(query: String) {
        searchInput = query
    }

    fun reset() {
        _viewEventsLiveData.postValue(ChatFriendListViewEvent.Empty)
    }

    suspend fun getChatInitProfileSettings(friendInfo: UserSimple): ChatInitProfileData? {
        return try {
            val userProfile = getUserProfile(friendInfo.userId)
            return cacheCompanionUserUseCase.invoke(userProfile.toChatInitUserProfile())
        } catch (e: Exception) {
            Timber.e(e)
            _viewEventsLiveData.postValue(ChatFriendListViewEvent.FailedToLoadFriendProfile)
            null
        }
    }

    private fun handleOpenNewFragmentAction() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { chatInteractor.clearDb() }
            _viewEventsLiveData.postValue(ChatFriendListViewEvent.OpenNewChatScreen)
            _effect.emit(ChatFriendListEffect.OpenNewChatScreen)
        }
    }

    private fun loadFriends(query: String) {
        myFriendsDataSource.newNameQuery(query)
        myFriendsDataSource.invalidateData()
        val isNewGroupChatButtonVisible = query.isEmpty()
        _viewEventsLiveData.postValue(
            ChatFriendListViewEvent.ChangeNewGroupChatButtonVisibility(isNewGroupChatButtonVisible)
        )
    }

    private fun onSearchResult(friendList: List<UserSimple>) {
        val isSearchingAllFriends = myFriendsDataSource.isSearchingAllFriends()
        val hasNoFriends = friendList.isEmpty() && isSearchingAllFriends
        val searchResultsEmpty = friendList.isEmpty() && !isSearchingAllFriends
        val newViewState = when {
            hasNoFriends -> ChatFriendListViewEvent.NoFriends
            searchResultsEmpty -> ChatFriendListViewEvent.EmptySearchResult
            else -> ChatFriendListViewEvent.FriendList
        }
        _viewEventsLiveData.postValue(newViewState)
        if (searchInput.isNotBlank()) {
            logFriendsSearchResults(friendList.isNotEmpty())
        }
    }

    private fun logFriendsSearchResults(isResultNotEmpty: Boolean) {
        val haveResult = if (isResultNotEmpty) {
            AmplitudePropertyHaveResult.YES
        } else {
            AmplitudePropertyHaveResult.NO
        }
        analyticsHelper.logSearchInput(
            type = AmplitudePropertySearchType.FRIENDS,
            haveResult = haveResult,
            whereCommunitySearch = AmplitudePropertyWhereCommunitySearch.NONE,
            whereFriendsSearch = AmplitudePropertyWhereFriendsSearch.NEW_MESSAGE
        )
    }

    private suspend fun getUserProfile(userId: Long) = userinfoUsecase.invoke(userId)

    companion object {
        private const val DEFAULT_PAGING_PAGE_SIZE = 20
        private const val DEFAULT_PAGING_PREFETCH_DISTANCE = 20
        private const val DEFAULT_PAGING_ARE_PLACEHOLDERS_ENABLED = false
    }
}
