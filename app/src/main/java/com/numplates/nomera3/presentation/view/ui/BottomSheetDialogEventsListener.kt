package com.numplates.nomera3.presentation.view.ui

interface BottomSheetDialogEventsListener {
    fun onDismissDialog(closeTypes: CloseTypes? = null) {}
    fun onCreateDialog() {}
}

data class CloseTypes(
    val isClickedBack: Boolean? = null,
    val isResultReceived: Boolean? = null
)
