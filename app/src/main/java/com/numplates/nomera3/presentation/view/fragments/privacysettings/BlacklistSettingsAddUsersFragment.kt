package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BlacklistSettingsAddUsersViewModel

/**
 * Search and Add users to Blacklist
 */
class BlacklistSettingsAddUsersFragment : SettingsUserSearchFragmentImpl() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return BlacklistSettingsAddUsersViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        screenTitle = getString(R.string.general_block),
        isRequestGetUsers = true,
    )
}
