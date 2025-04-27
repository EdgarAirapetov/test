package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.CallSettingsAddWhitelistViewModel

class CallSettingsAddWhitelistFragment : SettingsUserSearchFragmentImpl() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return CallSettingsAddWhitelistViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.general_add),
            true
    )
}