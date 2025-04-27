package com.numplates.nomera3.modules.appDialogs.data

import com.numplates.nomera3.modules.appDialogs.DialogEntity
import com.numplates.nomera3.modules.appDialogs.DialogState
import com.numplates.nomera3.modules.appDialogs.DialogType
import javax.inject.Inject

class DialogQueueStorage @Inject constructor(
    private val dialogStateStorage: DialogStateStorage
) {


    fun getDialogToShow(): DialogEntity {
        if (!dialogStateStorage.isFirstDialogShown) {
            return DialogEntity(getFirstDialogType())
        }

        val notCompleted = dialogStateStorage.notCompletedDialog
        if (notCompleted != DialogType.NONE) {
            return DialogEntity(notCompleted).apply {
                state = DialogState.NOT_COMPLETED_LAST_STEP
            }
        }

        val previousDialog = dialogStateStorage.previousShownDialog
        return DialogEntity(getNextDialog(previousDialog)).apply {
            state = DialogState.NOT_SHOWN
        }
    }

    fun dialogNotCompleted(dialogType: DialogType) {
        dialogStateStorage.notCompletedDialog = dialogType
    }

    fun dialogShown(dialogType: DialogType) {
        if (dialogType == getFirstDialogType() && !dialogStateStorage.isFirstDialogShown) {
            dialogStateStorage.isFirstDialogShown = true
        }
        dialogStateStorage.previousShownDialog = dialogType
    }

    private fun getFirstDialogType(): DialogType {
        return DialogType.ONBOARDING
    }

    private fun getNextDialog(dialogType: DialogType): DialogType {
        return when (dialogType) {
            DialogType.ONBOARDING -> DialogType.ENABLE_CALLS
            else -> DialogType.NONE
        }
    }
}