package com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel

class SubscriptionsNotificationAddUsersFragment : SettingsUserSearchFragmentImpl() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return SubscriptionsNotificationAddUsersViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.notification_settings_sources),
            true,
            isShowEmptyResultPlaceholder = true
    )
}