package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsAddBlacklistViewModel

class MeeraOnlineSettingsAddBlacklistFragment: MeeraSettingsUserSearchFragment() {

    override fun getViewModel() = OnlineSettingsAddBlacklistViewModel()

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.general_add),
            true
    )
}
