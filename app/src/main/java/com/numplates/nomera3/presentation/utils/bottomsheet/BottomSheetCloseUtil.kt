package com.numplates.nomera3.presentation.utils.bottomsheet

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyBottomSheetCloseMethod

class BottomSheetCloseUtil(
    private val listener: Listener
) {

    interface Listener {
        fun bottomSheetClosed(method: BottomSheetCloseMethod)
    }

    enum class BottomSheetCloseMethod {
        SWIPE, TAP_OUTSIDE, CLOSE_BUTTON, BACK_BUTTON
    }

    private data class DialogState(
        val state: Int? = null,
        val cancelled: Boolean = false,
        val closeButtonPressed: Boolean = false,
        val backButtonPressed: Boolean = false,
    )

    private fun DialogState.buttonPressed(): Boolean {
        return closeButtonPressed || backButtonPressed
    }

    private var dialogState: DialogState = DialogState()

    fun reset() {
        dialogState = DialogState()
    }

    fun onCloseButtonPressed() {
        dialogState = dialogState.copy(closeButtonPressed = true)
    }

    fun onBackButtonPressed() {
        dialogState = dialogState.copy(backButtonPressed = true)
    }

    fun onCancel() {
        dialogState = dialogState.copy(cancelled = true)
    }

    fun onStateChanged(newState: Int) {
        dialogState = dialogState.copy(state = newState)
    }

    fun onDismiss() {
        val closeMethod = when {
            !dialogState.buttonPressed() && dialogState.cancelled && dialogState.state == BottomSheetBehavior.STATE_HIDDEN -> {
                BottomSheetCloseMethod.SWIPE
            }
            !dialogState.buttonPressed() && dialogState.cancelled -> {
                BottomSheetCloseMethod.TAP_OUTSIDE
            }
            dialogState.closeButtonPressed -> {
                BottomSheetCloseMethod.CLOSE_BUTTON
            }
            dialogState.backButtonPressed -> {
                BottomSheetCloseMethod.BACK_BUTTON
            }
            else -> {
                null
            }
        }
        closeMethod?.let(listener::bottomSheetClosed)
    }
}

fun BottomSheetCloseUtil.BottomSheetCloseMethod.toAmplitudePropertyHow(): AmplitudePropertyBottomSheetCloseMethod {
    return when (this) {
        BottomSheetCloseUtil.BottomSheetCloseMethod.SWIPE -> AmplitudePropertyBottomSheetCloseMethod.CLOSE_SWIPE
        BottomSheetCloseUtil.BottomSheetCloseMethod.TAP_OUTSIDE -> AmplitudePropertyBottomSheetCloseMethod.TAP_ON_SPACE
        BottomSheetCloseUtil.BottomSheetCloseMethod.CLOSE_BUTTON -> AmplitudePropertyBottomSheetCloseMethod.CLOSE
        BottomSheetCloseUtil.BottomSheetCloseMethod.BACK_BUTTON -> AmplitudePropertyBottomSheetCloseMethod.BACK
    }
}
