package com.numplates.nomera3.modules.chat.domain.usecases.messages.voice

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class DownloadVoiceMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepositoryImpl
) {

    fun invoke(
        message: MessageEntity,
        externalFilesDir: File?
    ): Flow<Int> {
        return repository.downloadVoiceMessage(message, externalFilesDir)
    }

    fun invoke(
        url: String,
        roomId: Long,
        externalFilesDir: File?
    ): Flow<Int> = repository.downloadVoiceMessage(url, roomId, externalFilesDir)

}
