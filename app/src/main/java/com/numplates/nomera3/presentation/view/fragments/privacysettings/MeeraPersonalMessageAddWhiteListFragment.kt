package com.numplates.nomera3.presentation.view.fragments.privacysettings

import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesAddWhiteListViewModel

class MeeraPersonalMessageAddWhiteListFragment : MeeraSettingsUserSearchFragment() {

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
       return PersonalMessagesAddWhiteListViewModel()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        screenTitle =  getString(R.string.general_add),
        isRequestGetUsers = true,
        isShowConfirmBottomButton = true
    )
}
