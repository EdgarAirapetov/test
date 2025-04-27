package com.numplates.nomera3.modules.appDialogs.ui

import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class DismissDialogType {
    CALL_ENABLE, HOLIDAY, HOLIDAY_CALENDAR, FRIENDS_FOLLOWERS_PRIVACY, PEOPLE_ONBOARDING, SHAKE
}

class DialogDismissListener @Inject constructor() {

    private val _sharedFlow = MutableSharedFlow<DismissDialogType>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    suspend fun dialogDismissed(dialogType: DismissDialogType) =
        _sharedFlow.emit(dialogType)
}
