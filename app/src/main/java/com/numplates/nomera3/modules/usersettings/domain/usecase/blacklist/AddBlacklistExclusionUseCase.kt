package com.numplates.nomera3.modules.usersettings.domain.usecase.blacklist

import com.meera.core.extensions.toInt
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.privacysettings.data.blacklist.BlacklistRepository
import javax.inject.Inject

class AddBlacklistExclusionUseCase @Inject constructor(
    private val blacklistRepository: BlacklistRepository,
    private val roomDataRepository: RoomDataRepository,
    private val momentsRepository: MomentsRepository
) {

    suspend fun invoke(userIds: List<Long>) {
        val isSuccess = blacklistRepository.addToBlackList(userIds)
        if (isSuccess) {
            userIds.forEach { userId ->
                hideChatRequest(userId)
                modifyMomentBlockState(userId)
            }
        }
    }

    private suspend fun hideChatRequest(companionId: Long) {
        val roomData = roomDataRepository.getRoomDataByCompanionId(companionId)
        roomData?.isHidden = true
        roomData?.companion?.blacklistedByMe = true.toInt()
        if (roomData != null) {
            roomDataRepository.updateDialog(roomData)
        }
    }

    private fun modifyMomentBlockState(remoteUserId: Long) {
        momentsRepository.updateUserBlockStatus(remoteUserId = remoteUserId, isBlockedByMe = true)
    }
}
