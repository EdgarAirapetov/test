package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsAddBlacklistViewModel

class MeeraCallSettingsAddBlacklistFragment: MeeraSettingsUserSearchFragment() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return CallSettingsAddBlacklistViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.general_add),
            true
    )
}
