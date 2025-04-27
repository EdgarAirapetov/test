package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesAddBlackListViewModel

class PersonalMessageAddBlackListFragment: SettingsUserSearchFragmentImpl() {

    override fun getViewModel() = PersonalMessagesAddBlackListViewModel()

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            getString(R.string.general_add),
            true
    )
}