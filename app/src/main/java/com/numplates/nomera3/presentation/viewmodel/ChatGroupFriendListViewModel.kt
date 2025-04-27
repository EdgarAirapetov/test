package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.network.websocket.WebSocketResponseException
import com.meera.db.models.userprofile.UserRole
import com.numplates.nomera3.data.newmessenger.FriendEntity
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.data.newmessenger.USER_TYPE_MEMBER
import com.numplates.nomera3.data.newmessenger.response.ErrorMessage
import com.numplates.nomera3.data.newmessenger.response.Friends
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveResult
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertySearchType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunitySearch
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereFriendsSearch
import com.numplates.nomera3.modules.chat.domain.GroupChatRepository
import com.numplates.nomera3.modules.chat.domain.interactors.ChatInteractorImlp
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupEffect
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.properties.Delegates

class ChatGroupFriendListViewModel @Inject constructor(
    private val analyticsHelper: AnalyticsInteractor,
    private val webSocketMain: WebSocketMainChannel,
    private val gson: Gson,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val groupChatRepository: GroupChatRepository,
    private val chatInteractor: ChatInteractorImlp,
) : BaseViewModel() {

    private val disposables = CompositeDisposable()

    private val _liveListUsers = MutableLiveData<List<FriendEntity>>().apply {
        observeForever { resultsList ->
            if (searchInput.isNotBlank()) {
                logFriendsSearchResults(resultsList.isNotEmpty())
            }
        }
    }

    val liveListUsers = _liveListUsers as LiveData<List<FriendEntity>>

    private val _liveViewEvents = MutableLiveData<ChatGroupViewEvent>()
    val liveViewEvents = _liveViewEvents as LiveData<ChatGroupViewEvent>

    val effect: SharedFlow<ChatGroupEffect?>
        get() = _effect.asSharedFlow()
    private val _effect = MutableSharedFlow<ChatGroupEffect>()

    // Friend list search
    private val users = mutableListOf<FriendEntity>()
    private val foundUsers = mutableListOf<FriendEntity>()
    private var searchInput: String by Delegates.observable(initialValue = "") { _, _, newQuery ->
        if (newQuery.isNotEmpty()) {
            searchUsers(newQuery)
        } else {
            showLoadedUsers()
        }
    }

    override fun onCleared() {
        disposables.dispose()
    }

    fun onNewQuery(query: String) {
        searchInput = query
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun getFriends(roomId: Long?, isRoomAdmin: Boolean, userId: Long?, limit: Int, offset: Int) {
        if (users.isNotEmpty()) return

        val payloadUserId = hashMapOf<String, Any>()
        userId?.let { id ->
            payloadUserId["user_id"] = id
            payloadUserId["limit"] = limit
            payloadUserId["offset"] = offset
            payloadUserId["page"] = 1

        }

        val payloadRoomId = hashMapOf<String, Any>()
        roomId?.let {
            payloadRoomId["room_id"] = it
            payloadRoomId["user_type"] = "UserChat"
        }

        Timber.d("GET_FRIENDS_LOG socket get_friends Payload-2:$payloadUserId")
        disposables.add(
                Observables.combineLatest(
                        webSocketMain.pushGetFriends(payloadUserId),
                        webSocketMain.pushGetGroupMembers(payloadRoomId))
                { responseFriends, allMembers ->
                     Timber.d("RESPONSE Friends WebSock: ${responseFriends.payload}")
                     Timber.d("RESPONSE Members WebSock: ${allMembers.payload}")

                    // Set checked if members exists
                    val friendsResponse = gson.fromJson<ResponseWrapperWebSock<Friends>>(responseFriends.payload)
                    val listFriends = friendsResponse.response!!.friends
                    users.addAll(listFriends.filter { it.userRole != UserRole.ANNOUNCE_USER && it.userRole != UserRole.SUPPORT_USER })

                    val adminMembersIds = mutableListOf<Long>()
                    val members = chatInteractor.getAllMembers(roomId)
                    members.forEach { member ->
                        if (isRoomAdmin
                            && (member.type != USER_TYPE_CREATOR
                                && member.type != USER_TYPE_ADMIN)) {
                            adminMembersIds.add(member.user.userId)
                        } else if (!isRoomAdmin
                            && (member.type == USER_TYPE_CREATOR
                                || member.type == USER_TYPE_ADMIN
                                || member.type == USER_TYPE_MEMBER)) {
                            adminMembersIds.add(member.user.userId)
                        }
                    }

                    listFriends.forEach { user ->
                        if (isRoomAdmin) {
                            // Show users who ATTACHED to group chat (Add admin screen)
                            if (!adminMembersIds.contains(user.id)) {
                                users.remove(user)
                            }
                        } else {
                            // Show users who NOT ATTACHED to group chat (Invite new users to chat)
                            if (adminMembersIds.contains(user.id)) {
                                users.remove(user)
                            }
                        }
                    }

                    users
                }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ listUsers ->
                            Timber.d("Success get users!!! $listUsers")
                            listUsers?.let { users ->
                                _liveListUsers.value = users
                            }
                        }, { Timber.e(it) })
        )
    }

    // TODO: Необходимо, чтобы сервер возвращал список пользователей для записи в БД (Reactive UPDATE)
    fun addNewAdmins(roomId: Long?, userIds: List<Long>) {
        Timber.d("ADD Admins in progress: (roomId:$roomId) UserIds($userIds)")
        if (roomId == null || roomId == 0L) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    chatInteractor.changeChatMembersType(membersIds = userIds, type = USER_TYPE_ADMIN)
                    groupChatRepository.setAdminsToCache(userIds)
                }
                _liveViewEvents.value = ChatGroupViewEvent.AdminsAdded
            }
        } else {
            val payload = hashMapOf(
                "user_ids" to userIds,
                "room_id" to roomId,
            )
            disposables.add(
                webSocketMain.pushAddAdmins(payload)
                    .doOnNext { chatInteractor.changeChatMembersType(membersIds = userIds, type = USER_TYPE_ADMIN) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Successfully ADD Admin: ${gson.toJson(response.payload)}")
                        _liveViewEvents.value = ChatGroupViewEvent.AdminsAdded
                    }, { error ->
                        Timber.e("ERROR: Add admins failure: $error")
                        handleError(error)
                    })
            )
        }
    }

    // TODO: Необходимо, чтобы сервер возвращал список пользователей для записи в БД (Reactive UPDATE)
    fun addNewUsers(roomId: Long?, userIds: List<Long>) {
        Timber.d("ADD User in progress: (roomId:$roomId) UserIds($userIds)")
        roomId?.let { id ->
            val payload = hashMapOf(
                    "room_id" to id,
                    "user_ids" to userIds
            )
            disposables.add(
                    webSocketMain.pushAddUsers(payload)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ response ->
                                Timber.d("Successfully added NEW USERS: ${gson.toJson(response.payload)}")
                                _liveViewEvents.value = ChatGroupViewEvent.MembersAdded
                            }, { error ->
                                Timber.e(error)
                                handleError(error)
                            })
            )
        }
    }

    fun selectFriend(friend: FriendEntity) {
        val changedUsers = if (isSearchMode().not()) {
            users.map { it.setFriendChecked(friend) }
        } else {
            foundUsers.map { it.setFriendChecked(friend) }
        }
        _liveListUsers.value = changedUsers
        if (isSearchMode().not()) {
            addChangedUsers(users, changedUsers)
        } else {
            addChangedUsers(foundUsers, changedUsers)
        }
    }

    private fun addChangedUsers(
        existUsers: MutableList<FriendEntity>,
        changedUsers: List<FriendEntity>
    ) {
        existUsers.clear()
        existUsers.addAll(changedUsers)
    }

    fun gotoEditGroupScreen(roomId: Long?) {
        val isUserChecked = if (isSearchMode()) isExistsUsersChecked(foundUsers) else isExistsUsersChecked(users)
        if (isUserChecked) {
            if (roomId == null || roomId == 0L) {
                groupChatRepository.clearCachedUsers()
                val cachedUsers = if (isSearchMode()) filterCheckedUsers(foundUsers) else filterCheckedUsers(users)
                groupChatRepository.addUsersToCache(cachedUsers)
                _liveViewEvents.value = ChatGroupViewEvent.EditChatUsers()
                emitEffect(ChatGroupEffect.EditChatUsers())
            } else {
                val userIds = if (isSearchMode()) getListFriends(foundUsers) else getListFriends(users)
                addNewUsers(roomId, userIds.toList())
            }
        } else {
            _liveViewEvents.value = ChatGroupViewEvent.ErrorChooseFriends
        }
    }

    private fun emitEffect(effect: ChatGroupEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun searchUsers(query: String) {
        for (usr in users) {
            if (usr.name != null && usr.name?.contains(query, ignoreCase = true) == true) {
                if (foundUsers.contains(usr)) {
                    foundUsers.remove(usr)
                }
                foundUsers.add(usr)
            } else {
                foundUsers.remove(usr)
            }
        }
        _liveListUsers.value = foundUsers
    }

    private fun showLoadedUsers() {
        _liveListUsers.value = users
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
            whereFriendsSearch = AmplitudePropertyWhereFriendsSearch.GROUP_CHAT_CREATE
        )
    }

    private fun handleError(error: Throwable) {
        val err = error as WebSocketResponseException
        val msg = gson.fromJson<ResponseWrapperWebSock<ErrorMessage>>(gson.toJson(err.getPayload()))

        val event = ChatGroupViewEvent.ErrorUserMessage()
        event.mesage = msg.response?.error

        _liveViewEvents.postValue(event)
    }

    private fun isSearchMode() = foundUsers.isNotEmpty()

    private fun isExistsUsersChecked(users: List<FriendEntity>) = users.any { it.isChecked }

    fun getListFriends() = users.filter { it.isChecked }.map { it.id }.toLongArray()

    private fun getListFriends(users: List<FriendEntity>) = users.filter { it.isChecked }.map { it.id }.toLongArray()

    private fun FriendEntity.setFriendChecked(selectFriend: FriendEntity) =
        copy(isChecked = if (this.id == selectFriend.id) !selectFriend.isChecked else this.isChecked)

    private fun filterCheckedUsers(users: List<FriendEntity>) = users.filter { it.isChecked }
}
