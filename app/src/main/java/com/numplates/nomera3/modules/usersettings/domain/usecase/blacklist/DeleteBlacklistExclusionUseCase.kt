package com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist

import com.meera.core.extensions.toInt
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepository
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import com.numplates.nomera3.modules.privacysettings.data.blacklist.BlacklistRepository
import javax.inject.Inject

class DeleteBlacklistExclusionUseCase @Inject constructor(
    private val blacklistRepository: BlacklistRepository,
    private val chatRequestRepository: ChatRequestRepository,
    private val roomDataRepository: RoomDataRepository,
    private val momentsRepository: MomentsRepository
) {

    suspend fun invoke(userIds: List<Long>) {
        val isSuccess = blacklistRepository.deleteFromBlacklist(userIds)
        if (isSuccess) {
            for (userId in userIds) {
                chatRequestRepository.getDialogByCompanionId(userId).forEach { dialogData ->
                    unhideChatRequest(dialogData.companionUid)
                }
                modifyMomentBlockState(userId)
            }
        }
    }

    private suspend fun unhideChatRequest(companionId: Long) {
        val roomData = roomDataRepository.getRoomDataByCompanionId(companionId)
        roomData?.isHidden = false
        roomData?.companion?.blacklistedByMe = false.toInt()
        if (roomData != null) {
            roomDataRepository.updateDialog(roomData)
        }
    }

    private fun modifyMomentBlockState(remoteUserId: Long) {
        momentsRepository.updateUserBlockStatus(remoteUserId = remoteUserId, isBlockedByMe = false)
    }
}
