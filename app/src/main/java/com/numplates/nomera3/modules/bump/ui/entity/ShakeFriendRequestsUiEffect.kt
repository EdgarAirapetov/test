package com.numplates.nomera3.modules.bump.ui.entity

import androidx.annotation.StringRes

sealed class ShakeFriendRequestsUiEffect {

    data class ShowSuccessToast(@StringRes val messageRes: Int) : ShakeFriendRequestsUiEffect()

    data class ShowErrorToast(@StringRes val errorMessageRes: Int) : ShakeFriendRequestsUiEffect()

    data class NavigateToUserFragment(val userId: Long) : ShakeFriendRequestsUiEffect()

    object CloseShakeFriendRequests : ShakeFriendRequestsUiEffect()

    object AnimateNextUserUiEffect : ShakeFriendRequestsUiEffect()

    object AnimateSkipUserUiEffect : ShakeFriendRequestsUiEffect()

    object AnimateVisibleAppearWithButtonsEffect : ShakeFriendRequestsUiEffect()

    object AnimateVisibleAppearWithoutButtonsEffect : ShakeFriendRequestsUiEffect()
}
