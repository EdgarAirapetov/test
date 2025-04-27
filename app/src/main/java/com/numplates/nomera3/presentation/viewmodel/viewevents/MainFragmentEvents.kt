package com.numplates.nomera3.presentation.viewmodel.viewevents

import com.numplates.nomera3.modules.appInfo.ui.entity.ForceUpdateDialogEntity

sealed class MainFragmentEvents {
    object AppSettingsRequestFinished : MainFragmentEvents()

    object RegisterInternetObserver : MainFragmentEvents()

    object UnregisterInternetObserver : MainFragmentEvents()

    data class UpdateScreenEvent(
        var infos: List<String>?,
        var appVerName: String?
    ) : MainFragmentEvents()

    data class ForceUpdateEvent(
        val data: ForceUpdateDialogEntity?
    ) : MainFragmentEvents()
}
