package com.numplates.nomera3.modules.chat.domain.interactors

import androidx.lifecycle.LiveData
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.domain.usecases.DeleteDialogByIdUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetAllMembersLiveUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetAllMembersUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetDialogByIdLiveUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetUserProfileRxUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.InsertDialogUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.ChangeChatMembersTypeUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.ClearChatMembersUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SendTypingUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.SubscribeRoomUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.RemoveChatMembersUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateChatMembersUseCase
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import io.reactivex.Single
import javax.inject.Inject

interface ChatInteractor {
    fun getUserProfileRx(): Single<UserProfileModel>
    fun clearDb()
    fun insertChatMembers(members: List<ChatMember>)
    fun insertDialog(dialog: DialogEntity)
    fun getAllMembersLive(roomId: Long?): LiveData<List<ChatMember>>
    fun getAllMembers(roomId: Long?): List<ChatMember>
    fun getDialogByRoomIdLive(roomId: Long?): LiveData<DialogEntity>
    fun deleteDialogByRoomId(roomId: Long)
    fun deleteChatMembers(membersIds: List<Long>)
    fun changeChatMembersType(membersIds: List<Long>, type: String)

    suspend fun sendTypingUseCase(roomId: Long, type: String): Boolean

    suspend fun subscribeRoom(roomId: Long): Boolean
}

class ChatInteractorImlp @Inject constructor(
    private val removeChatMembersUseCase: RemoveChatMembersUseCase,
    private val clearChatMembersUseCase: ClearChatMembersUseCase,
    private val updateChatMembersUseCase: UpdateChatMembersUseCase,
    private val changeChatMembersTypeUseCase: ChangeChatMembersTypeUseCase,
    private val deleteDialogByIdUseCase: DeleteDialogByIdUseCase,
    private val getDialogByIdLiveUseCase: GetDialogByIdLiveUseCase,
    private val getUserProfileRxUseCase: GetUserProfileRxUseCase,
    private val getAllMembersLiveUseCase: GetAllMembersLiveUseCase,
    private val getAllMembersUseCase: GetAllMembersUseCase,
    private val insertDialogUseCase: InsertDialogUseCase,
    private val sendTypingUseCase: SendTypingUseCase,
    private val subscribeRoomUseCase: SubscribeRoomUseCase
) : ChatInteractor {

    override fun getUserProfileRx(): Single<UserProfileModel> =
        getUserProfileRxUseCase.invoke()

    override fun clearDb() =
        clearChatMembersUseCase.invoke()

    override fun insertChatMembers(members: List<ChatMember>) =
        updateChatMembersUseCase.invoke(members)

    override fun insertDialog(dialog: DialogEntity) =
        insertDialogUseCase.invoke(dialog)

    override fun getAllMembersLive(roomId: Long?) =
        getAllMembersLiveUseCase.invoke(roomId)

    override fun getAllMembers(roomId: Long?): List<ChatMember> =
        getAllMembersUseCase.invoke(roomId)

    override fun getDialogByRoomIdLive(roomId: Long?) =
        getDialogByIdLiveUseCase.invoke(roomId)

    override fun deleteDialogByRoomId(roomId: Long) =
        deleteDialogByIdUseCase.invoke(roomId)

    override fun deleteChatMembers(membersIds: List<Long>) =
        removeChatMembersUseCase.invoke(membersIds)

    override fun changeChatMembersType(membersIds: List<Long>, type: String) =
        changeChatMembersTypeUseCase.invoke(membersIds, type)

    override suspend fun sendTypingUseCase(roomId: Long, type: String): Boolean {
        return sendTypingUseCase.invoke(roomId, type)
    }

    override suspend fun subscribeRoom(roomId: Long): Boolean {
        return subscribeRoomUseCase.invoke(roomId)
    }
}
