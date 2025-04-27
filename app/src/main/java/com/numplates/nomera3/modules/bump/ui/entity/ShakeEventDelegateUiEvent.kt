package com.numplates.nomera3.modules.bump.ui.entity

import androidx.annotation.StringRes

sealed interface ShakeEventDelegateUiEvent {

    data class ShowShakeDialog(val showDialogByShake: Boolean) : ShakeEventDelegateUiEvent

    object ShowGeoEnabledDialog : ShakeEventDelegateUiEvent

    data class ShowErrorToast(@StringRes val errorMessageRes: Int) : ShakeEventDelegateUiEvent

    object ShowShakeFriendRequests : ShakeEventDelegateUiEvent
}
