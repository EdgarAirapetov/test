package com.numplates.nomera3.modules.appDialogs.domain

import com.numplates.nomera3.modules.appDialogs.DialogEntity
import com.numplates.nomera3.modules.appDialogs.DialogType
import com.numplates.nomera3.modules.appDialogs.data.DialogQueueStorage
import javax.inject.Inject

class DialogQueueUseCase @Inject constructor(
    private var dialogQueueStorage: DialogQueueStorage
) {

    suspend fun getDialogToShow(): DialogEntity {
        return dialogQueueStorage.getDialogToShow()
    }

    suspend fun setDialogNotCompleted(dialogType: DialogType) {
        dialogQueueStorage.dialogNotCompleted(dialogType)
    }

    suspend fun setDialogShown(dialogType: DialogType) {
        dialogQueueStorage.dialogShown(dialogType)
    }
}