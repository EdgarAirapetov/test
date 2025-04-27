package com.numplates.nomera3.modules.chat.helpers.resendmessage

import com.meera.core.utils.files.FileManager
import com.meera.db.models.message.MessageEntity
import timber.log.Timber

class ResendVideoMessageTask(
    dependencies: TaskDependencies,
    private val message: MessageEntity,
    resultCallback: IResendResultCallback,
    fileManager: FileManager
): BaseResendTask(dependencies, resultCallback, fileManager), IResendTask {

    override suspend fun execute() {
        Timber.e("RESEND Video message LONG... LONG... LONG...")
        resendMessageCommon(message)
    }
}
