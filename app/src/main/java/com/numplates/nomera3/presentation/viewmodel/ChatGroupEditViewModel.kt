package com.numplates.nomera3.presentation.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.network.websocket.WebSocketResponseException
import com.meera.core.utils.files.FileManager
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.LastMessage
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.data.newmessenger.USER_TYPE_MEMBER
import com.numplates.nomera3.data.newmessenger.response.ErrorMessage
import com.numplates.nomera3.data.newmessenger.response.GroupChatMembers
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.data.newmessenger.toUserEntity
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.UploadGroupChatAvatarUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveDescription
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHavePhoto
import com.numplates.nomera3.modules.chat.domain.GroupChatRepository
import com.numplates.nomera3.modules.chat.domain.interactors.ChatInteractorImlp
import com.numplates.nomera3.modules.chatrooms.domain.interactors.RoomsInteractor
import com.numplates.nomera3.modules.userprofile.domain.maper.toUserEntity
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

const val NOT_YET_CREATED_ROOM_ID = 0L

data class ChatMembers(
    val members: List<ChatMember>,
    val admins: List<ChatMember>
)

class ChatGroupEditViewModel @Inject constructor(
    private val webSocketMainChannel: WebSocketMainChannel,
    private val gson: Gson,
    private val uploadAvatar: UploadGroupChatAvatarUseCase,
    private val analyticsInteractor: AnalyticsInteractor,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val roomsInteractor: RoomsInteractor,
    private val chatInteractor: ChatInteractorImlp,
    private val ownUserProfileUseCase: GetOwnLocalProfileUseCase,
    private val fileManager: FileManager,
    private val groupChatRepository: GroupChatRepository,
) : BaseViewModel() {

    val disposables = CompositeDisposable()

    val liveGroupEditEvents = MutableLiveData<ChatGroupViewEvent>()

    val liveViewEventsOnce = MutableLiveData<ChatGroupViewEvent>()

    // Not yet created chat
    val liveSelectedMembers = MutableLiveData<List<ChatMember>>()
    val liveSelectedAdmin = MutableLiveData<List<ChatMember>>()

    val liveSelectedChatMembers = MutableLiveData<ChatMembers>()

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun createImageFile() = fileManager.createImageFile()

    fun isGooglePhoto(uri: Uri?) = fileManager.isGooglePhoto(uri)

    fun saveImageFromGoogleDrives(uri: Uri?) = fileManager.saveImageFromGoogleDrives(uri)

    fun saveBitmapInFile(bitmap: Bitmap, absolutePath: String) = fileManager.saveBitmapInFile(bitmap, absolutePath)

    fun clearCacheAdmins() {
        groupChatRepository.clearCachedAdmins()
    }

    fun createGroupChat(
        chatName: String,
        description: String,
        avatarPath: String?,
    ) {
        viewModelScope.launch {
            val members = mutableListOf<Long>()
            val admins = mutableListOf<Long>()
            withContext(Dispatchers.IO) {
                chatInteractor.getAllMembers(roomId = 0).forEach { user ->
                    members.add(user.userId)
                    if (user.type == USER_TYPE_CREATOR || user.type == USER_TYPE_ADMIN) {
                        admins.add(user.userId)
                    }
                }
            }

            val payload = hashMapOf(
                "members" to members,
                "type" to "group",
                "title" to chatName,
                "description" to description,
                "admins" to admins,
                "user_type" to "UserChat"
            )

            disposables.add(
                webSocketMainChannel.pushCreateRoom(payload)
                    .flatMap { response ->
                        Observable.fromCallable {
                            Timber.d("ROOM create server RESPONSE: ${response.payload}")

                            val json = gson.toJson(response.payload)
                            val dialogResponse =
                                gson.fromJson<ResponseWrapperWebSock<DialogEntity>>(json)

                            // Save to Db
                            dialogResponse.response?.let { dialog ->
                                dialog.creator.userId?.let { uid ->
                                    dialog.creatorUid = uid
                                    dialog.companionUid = uid
                                    dialog.lastMessage = LastMessage()
                                    chatInteractor.insertDialog(dialog)
                                }
                            }

                            dialogResponse.response
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ dialog ->
                        Timber.d("ROOM Successfully created: DB and Server: roomId: ${dialog?.roomId}")
                        groupChatRepository.clearCachedAdmins()
                        logAmplitudeGroupChatCreated(description, avatarPath)

                        // Set room id view event
                        val event = ChatGroupViewEvent.GroupChatCreated()
                        event.dialog = dialog
                        liveGroupEditEvents.value = event                   // Goto group chat
                    }, { error ->
                        Timber.e("ERROR: Create room: $error")
                        showErrorMessage(error)
                    })
            )
        }
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    /**
     * Get all chat members by room id from Db
     */
    fun getChatMembers(roomId: Long?) =
        chatInteractor.getAllMembersLive(roomId)


    /**
     * For loaded limit chat members (ONLY First members)
     */
    fun showChatMembers(roomId: Long?) {
        Timber.e("Show chat members: $roomId")
        roomId?.let { id ->
            val payload = hashMapOf<String, Any>(
                "room_id" to id,
                "user_type" to "UserChat"
                // .. limit, offset - optionally
            )

            disposables.add(
                webSocketMainChannel.pushGetMembers(payload)
                    .flatMap { response ->
                        Observable.fromCallable {
                            val responseObj =
                                gson.fromJson<ResponseWrapperWebSock<GroupChatMembers>>(
                                    gson.toJson(response.payload)
                                )
                            responseObj.response?.members?.let {
                                it.forEach { memb ->
                                    memb.roomId = id
                                    memb.userId = memb.user.userId

                                    // Timber.e("MemB: ${memb.user.name} roomId: ${memb.roomId} userId: ${memb.userId}")
                                }

                                // в бд могут остаться удаленные участники группового чата,
                                // если участник был удален администратором группы и приложение
                                // было выключено у кого-либо из других участников
                                chatInteractor.clearDb()

                                chatInteractor.insertChatMembers(it)
                            }
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("SUCCESS Save members to Db: $response")
                        /*val responseObj = gson.fromJson<ResponseWrapperWebSock<GroupChatMembers>>(
                                gson.toJson(response.payload))
                        liveDialogMembers.value = responseObj.response?.members*/

                    }, { error ->
                        Timber.e(error)
                        //showErrorMessage(error)
                    })
            )
        }
    }

    /**
     * Show group chat members if chat not yet created
     */
    fun showNotYetCreatedMembers() {
        viewModelScope.launch {
            runCatching {
                val ownProfile = ownUserProfileUseCase.invoke()
                ownProfile?.let { im ->
                    val allMembers = getChatMembersFromCache().toMutableList()

                    val imAdmin = ChatMember(
                        userId = im.userId,
                        roomId = NOT_YET_CREATED_ROOM_ID,
                        type = USER_TYPE_CREATOR,
                        user = im.toUserEntity(),
                    )
                    allMembers.add(imAdmin)

                    val allAdmins = getAllAdmins(imAdmin)

                    clearChatMembersIntoDb(
                        creatorUid = im.userId,
                        members = allMembers,
                        admins = allAdmins
                    )
                    return@let ChatMembers(
                        members = allMembers,
                        admins = allAdmins
                    )
                }
            }.onSuccess { chatMembers ->
                chatMembers?.let { liveSelectedChatMembers.postValue(it) }
            }.onFailure {
                Timber.e("FAIL extracting chat members:$it")
            }
        }
    }

    private fun getChatMembersFromCache(): List<ChatMember> {
        return groupChatRepository.getUsersFromCache().map {
            ChatMember(
                userId = it.id,
                roomId = NOT_YET_CREATED_ROOM_ID,
                type = USER_TYPE_MEMBER,
                user = it.toUserEntity()
            )
        }
    }

    private fun getAllAdmins(chatCreator: ChatMember): List<ChatMember> {
        val admins = groupChatRepository.getAdminsFromCache().map {
            ChatMember(
                userId = it.id,
                roomId = NOT_YET_CREATED_ROOM_ID,
                type = USER_TYPE_ADMIN,
                user = it.toUserEntity()
            )
        }.toMutableList()
        admins.add(chatCreator)
        return admins
    }

    // в бд могут остаться удаленные участники группового чата,
    // если участник был удален администратором группы и приложение
    // было выключено у кого-либо из других участников
    private fun clearChatMembersIntoDb(
        creatorUid: Long,
        members: List<ChatMember>,
        admins: List<ChatMember>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedMembers = filterMembersByType(creatorUid, members, admins)
            chatInteractor.clearDb()
            chatInteractor.insertChatMembers(updatedMembers)
        }
    }

    /**
     * Получаем всех участников и присваиваем им статусы админов
     * если были добавлены новые
     */
    private fun filterMembersByType(
        creatorUid: Long,
        members: List<ChatMember>,
        admins: List<ChatMember>
    ): List<ChatMember> {
        return members.map { member ->
            val found = admins.firstOrNull { it.userId == member.userId }
            return@map if (found != null && found.userId != creatorUid) {
                member.copy(type = USER_TYPE_ADMIN)
            } else {
                member
            }
        }
    }


    /**
     * Show group chat members if chat not yet created
     */
    @Deprecated("Used only old not redesigned screen")
    fun showNotYetCreatedChatMembers() {
        disposables.add(
            chatInteractor.getUserProfileRx()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ im ->
                    val members = mutableListOf<ChatMember>()
                    groupChatRepository.getUsersFromCache().forEach {
                        members.add(
                            ChatMember(
                                userId = it.id,
                                roomId = NOT_YET_CREATED_ROOM_ID,
                                type = USER_TYPE_MEMBER,
                                user = it.toUserEntity()
                            )
                        )
                    }

                    val imAdmin = ChatMember(
                        userId = im.userId,
                        roomId = NOT_YET_CREATED_ROOM_ID,
                        type = USER_TYPE_CREATOR,
                        user = im.toUserEntity(),
                    )
                    members.add(imAdmin)

                    viewModelScope.launch(Dispatchers.IO) {
                        // в бд могут остаться удаленные участники группового чата,
                        // если участник был удален администратором группы и приложение
                        // было выключено у кого-либо из других участников
                        chatInteractor.clearDb()
                        chatInteractor.insertChatMembers(members)
                    }

                    liveSelectedMembers.value = members
                    liveSelectedAdmin.value = mutableListOf(imAdmin)

                }, { error -> Timber.e("ERROR: Get user profile from Db: $error") })
        )
    }

    // About GROUP Fragment ----------------------

    // Live data
    fun getGroupData(roomId: Long?) = chatInteractor.getDialogByRoomIdLive(roomId)

    /**
     * Add new user to group chat
     */
    fun addNewUsers(roomId: Long?, members: List<Long>) {
        roomId?.let { id ->
            val payload = hashMapOf(
                "room_id" to id,
                "user_ids" to members
            )
            disposables.add(
                webSocketMainChannel.pushAddUsers(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Successfully added NEW USERS: ${gson.toJson(response.payload)}")
                    }, { error ->
                        Timber.e(error)
                        showErrorMessage(error)
                    })
            )
        }
    }


    // pushChangeTitle
    fun changeChatTitle(roomId: Long?, title: String, enableViewEvent: Boolean = true) {
        analyticsInteractor.logGroupTitleChange()
        roomId?.let { id ->
            val payload = hashMapOf(
                "room_id" to id,
                "title" to title
            )
            disposables.add(
                webSocketMainChannel.pushChangeTitle(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Successfully changed TITLE Group chat: ${gson.toJson(response.payload)}")
                        if (enableViewEvent) {
                            liveViewEventsOnce.value = ChatGroupViewEvent.SuccessTitleChanged
                        }
                    }, { showErrorMessage(it) })
            )
        }
    }


    fun changeChatDescription(roomId: Long?, description: String, enableViewEvent: Boolean = true) {
        analyticsInteractor.logGroupDescriptionChange()
        roomId?.let { id ->
            val payload = hashMapOf(
                "room_id" to id,
                "description" to description
            )
            disposables.add(
                webSocketMainChannel.pushChangeDescription(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d(
                            "Successfully changed DESCRIPTION group chat: ${
                                gson.toJson(
                                    response.payload
                                )
                            }"
                        )
                        if (enableViewEvent) {
                            liveViewEventsOnce.value = ChatGroupViewEvent.SuccessDescriptionChanged
                        }
                    }, { showErrorMessage(it) })
            )
        }
    }

    @Deprecated("Don't use method with VM callbacks!")
    fun changeChatAvatar(
        roomId: Long?,
        imagePath: String,
        successCallback: () -> Any,
        errorCallback: () -> Any
    ) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    uploadAvatar.uploadGroupChatAvatar(id, imagePath)
                    launch(Dispatchers.Main) { successCallback.invoke() }
                } catch (e: Exception) {
                    Timber.e("ERROR: Upload Group avatar ${e.message}")
                    e.printStackTrace()
                    launch(Dispatchers.Main) { errorCallback.invoke() }
                }
            }
        }
    }

    fun changeAvatarWhenEditChat(
        roomId: Long?,
        imagePath: String,
        name: String,
        description: String
    ) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    uploadAvatar.uploadGroupChatAvatar(id, imagePath)
                    liveGroupEditEvents.postValue(ChatGroupViewEvent.CompleteAvatarChangedWhenEdit(
                        roomId = roomId,
                        name = name,
                        description = description
                    ))
                } catch (e: Exception) {
                    Timber.e("ERROR: Upload Group avatar ${e.message}")
                    liveGroupEditEvents.postValue(ChatGroupViewEvent.CompleteAvatarChangedWhenEdit(
                        roomId = roomId,
                        name = name,
                        description = description
                    ))
                    e.printStackTrace()
                }
            }
        }
    }

    fun changeAvatarWhenCreateChat(
        roomId: Long?,
        imagePath: String,
        room: DialogEntity?
    ) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    uploadAvatar.uploadGroupChatAvatar(id, imagePath)
                    liveGroupEditEvents.postValue(ChatGroupViewEvent.SuccessAvatarChangedWhenCreateChat(room))
                } catch (e: Exception) {
                    Timber.e("ERROR: Upload Group avatar ${e.message}")
                    liveGroupEditEvents.postValue(ChatGroupViewEvent.ErrorAvatarChangedWhenCreateChat(room))
                    e.printStackTrace()
                }
            }
        }
    }


    // Only creator can delete group chat
    fun deleteGroupChat(roomId: Long?, isBoth: Boolean) {
        roomId?.let { id ->
            val payload = hashMapOf(
                "room_id" to id,
                "both" to isBoth
            )

            disposables.add(
                webSocketMainChannel.pushDeleteRoom(payload)
                    .flatMap { response ->
                        Observable.fromCallable {
                            chatInteractor.deleteDialogByRoomId(roomId)
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        logAmplitudeGroupChatDeleted()
                        liveGroupEditEvents.value = ChatGroupViewEvent.GroupChatDeleted
                    },
                        { liveGroupEditEvents.value = ChatGroupViewEvent.ErrorChatDeleted })
            )
        }
    }


    fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsyncViewModel({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    Timber.d("Temp image file extension: $extension")
                    if (extension != ".gif")
                        return@doAsyncViewModel fileManager.deleteFile(it)
                    else
                        return@doAsyncViewModel false
                } catch (e: Exception) {
                    Timber.e(e)
                    return@doAsyncViewModel false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
            })
        }
    }


    /**
     * Load actual data about room
     */
    fun reloadDialogs(roomId: Long?) {
        roomId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                val dialog = roomsInteractor.getRoom()
                liveViewEventsOnce.postValue(
                    ChatGroupViewEvent.OnSuccessReloadDialogs(dialog)
                )
            }
        }

    }

    private fun showErrorMessage(error: Throwable) {
        val err = error as WebSocketResponseException
        if (err.isValid()) {
            val msg = gson.fromJson<ResponseWrapperWebSock<ErrorMessage>>(
                gson.toJson(err.getPayload())
            )

            val event = ChatGroupViewEvent.ErrorUserMessage()
            event.mesage = msg.response?.error
            liveGroupEditEvents.value = event
        } else {
            val event = ChatGroupViewEvent.ErrorUserMessage()
            liveGroupEditEvents.value = event
        }
    }

    private fun logAmplitudeGroupChatCreated(description: String, avatarPath: String?) {
        val havePhoto = avatarPath?.let { AmplitudePropertyHavePhoto.YES }
            ?: kotlin.run { AmplitudePropertyHavePhoto.NO }
        val haveDescription = if (description.isNotEmpty()) AmplitudePropertyHaveDescription.YES
        else AmplitudePropertyHaveDescription.NO
        analyticsInteractor.logGroupChatCreate(havePhoto, haveDescription)
    }

    private fun logAmplitudeGroupChatDeleted() {
        analyticsInteractor.logGroupChatDelete()
    }

}
