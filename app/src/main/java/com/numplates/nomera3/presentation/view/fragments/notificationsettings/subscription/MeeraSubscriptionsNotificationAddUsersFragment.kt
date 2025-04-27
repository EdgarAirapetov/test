package com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel

class MeeraSubscriptionsNotificationAddUsersFragment : MeeraSettingsUserSearchFragment() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return SubscriptionsNotificationAddUsersViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.settings_add),
            true,
            isShowEmptyResultPlaceholder = true
    )
}
