package com.numplates.nomera3.modules.appDialogs.data

import com.meera.core.preferences.PrefManager
import com.numplates.nomera3.modules.appDialogs.DialogType

class DialogStateStorage(
    private val prefManager: PrefManager
) {

    var isFirstDialogShown: Boolean
        get() {
            return prefManager.getBoolean(KEY_DIALOG_QUEUE_FIRST_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_DIALOG_QUEUE_FIRST_SHOWN, value)
        }

    var previousShownDialog: DialogType
        get() {
            return getDialogTypeFromKey(
                prefManager.getString(
                    KEY_DIALOG_QUEUE_PREVIOUS_SHOWN,
                    DialogType.NONE.name
                )
            )
        }
        set(value) {
            prefManager.putValue(KEY_DIALOG_QUEUE_PREVIOUS_SHOWN, value.name)
        }

    var notCompletedDialog: DialogType
        get() {
            return getDialogTypeFromKey(
                prefManager.getString(
                    KEY_DIALOG_QUEUE_NOT_COMPLETED,
                    DialogType.NONE.name
                )
            )
        }
        set(value) {
            prefManager.putValue(KEY_DIALOG_QUEUE_NOT_COMPLETED, value.name)
        }


    private fun getDialogTypeFromKey(key: String?): DialogType {
        return when (key) {
            DialogType.ONBOARDING.name -> DialogType.ONBOARDING
            DialogType.ENABLE_CALLS.name -> DialogType.ENABLE_CALLS
            else -> DialogType.NONE
        }
    }

    companion object {
        private const val KEY_DIALOG_QUEUE_FIRST_SHOWN = "KEY_DIALOG_QUEUE_LAST_SHOWN"
        private const val KEY_DIALOG_QUEUE_PREVIOUS_SHOWN = "KEY_DIALOG_QUEUE_PREVIOUS_SHOWN"
        private const val KEY_DIALOG_QUEUE_NOT_COMPLETED = "KEY_DIALOG_QUEUE_NOT_COMPLETED"
    }
}
