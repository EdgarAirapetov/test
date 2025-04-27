package com.numplates.nomera3.modules.services.ui.entity

import androidx.annotation.StringRes

sealed class MeeraServicesUiEffect {

    data class NavigateToMoments(val userId: Long) : MeeraServicesUiEffect()

    data object NavigateToEvents : MeeraServicesUiEffect()

    data object NavigateToPeoples : MeeraServicesUiEffect()

    data class NavigateToUserProfile(val userId: Long) : MeeraServicesUiEffect()

    data object NavigateToSettings : MeeraServicesUiEffect()

    data object NavigateToCommunities : MeeraServicesUiEffect()

    data class NavigateToCommunity(val communityId: Int) : MeeraServicesUiEffect()

    data class ShowSuccessToast(@StringRes val stringId: Int) : MeeraServicesUiEffect()

    data class ShowErrorToast(@StringRes val stringId: Int) : MeeraServicesUiEffect()

    data class ShowClearRecentsToast(val delaySeconds: Int) : MeeraServicesUiEffect()
}
