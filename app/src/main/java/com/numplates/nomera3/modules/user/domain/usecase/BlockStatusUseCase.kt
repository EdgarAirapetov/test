package com.numplates.nomera3.modules.user.domain.usecase

import com.meera.core.extensions.toInt
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class BlockStatusUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val roomDataRepository: RoomDataRepository
) {

    @Deprecated("Use invoke() function instead.", ReplaceWith("this.invoke(params)"))
    suspend fun execute(
        params: DefBlockParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            userRepository.blockUser(
                userId = params.userId,
                remoteUserId = params.remoteUserId,
                isBlocked = params.isBlocked,
            )
            changeChatRequestVisibility(companionId = params.remoteUserId, isBlocked = params.isBlocked)
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    suspend fun invoke(params: DefBlockParams) {
        userRepository.blockUser(
            userId = params.userId,
            remoteUserId = params.remoteUserId,
            isBlocked = params.isBlocked,
        )
        changeChatRequestVisibility(
            companionId = params.remoteUserId,
            isBlocked = params.isBlocked,
        )
    }

    suspend fun invoke(userId: Long, companionId: Long, isBlocked: Boolean) {
        userRepository.blockUser(
            userId = userId,
            remoteUserId = companionId,
            isBlocked = isBlocked,
        )
        changeChatRequestVisibility(companionId = companionId, isBlocked = isBlocked)
    }

    private suspend fun changeChatRequestVisibility(companionId: Long, isBlocked: Boolean) {
        val roomData = roomDataRepository.getRoomDataByCompanionId(companionId)
        roomData?.isHidden = isBlocked
        roomData?.companion?.blacklistedByMe = isBlocked.toInt()
        if (roomData != null) {
            roomDataRepository.updateDialog(roomData)
        }
    }
}

class DefBlockParams(
    val userId: Long,
    val remoteUserId: Long,
    val isBlocked: Boolean
) : DefParams()
