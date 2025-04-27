package com.numplates.nomera3.presentation.view.fragments.notificationsettings.message

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel


class MeeraMessageNotificationsAddUsersFragment : MeeraSettingsUserSearchFragment() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return MessageNotificationsAddUsersViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        screenTitle = getString(R.string.meeera_settings_add_exclusion_title),
        isRequestGetUsers = true,
        isShowEmptyResultPlaceholder = true
    )
}
