package com.numplates.nomera3.presentation.view.fragments.notificationsettings.message

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel


class MessageNotificationsAddUsersFragment : SettingsUserSearchFragmentImpl() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return MessageNotificationsAddUsersViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.settings_add_exclusion_title),
            true,
            isShowEmptyResultPlaceholder = true
    )
}