package com.numplates.nomera3.modules.chat.domain

import androidx.lifecycle.LiveData
import com.meera.core.network.websocket.ConnectionStatus
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import io.reactivex.subjects.BehaviorSubject

interface ChatRepository {

    fun getAllMembersLive(roomId: Long?): LiveData<List<ChatMember>>

    fun getAllMembers(roomId: Long?): List<ChatMember>

    suspend fun removeChatUser(roomId: Long, userId: Long)

    suspend fun deleteChatMemberInDb(userId: Long, roomId: Long)

    suspend fun subscribeRoom(roomId: Long): Boolean

    suspend fun changeRoomMuteState(roomId: Long, isMuted: Boolean)

    fun isWebSocketConnected(): Boolean

    fun listenSocketStatus(): BehaviorSubject<ConnectionStatus>

    fun cacheCompanionUserForChatInit(user: ChatInitProfileData?): ChatInitProfileData?
}
