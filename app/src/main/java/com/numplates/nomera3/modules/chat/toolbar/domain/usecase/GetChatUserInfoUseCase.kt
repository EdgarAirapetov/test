package com.numplates.nomera3.modules.chat.toolbar.domain.usecase


import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepository
import javax.inject.Inject


class GetChatUserInfoUseCase @Inject constructor(private val repository: ChatToolbarRepository) {

    suspend operator fun invoke(companionId: Long): UserChat? = invoke(listOf(companionId)).firstOrNull()

    suspend operator fun invoke(userIds: List<Long>): List<UserChat> = repository.getUserInfo(userIds)

    suspend fun invokeRest(userId: Long): List<UserChat> = repository.getUserInfo(userId)

}
