package com.numplates.nomera3.modules.moments.settings.notshow.presentation

import androidx.fragment.app.viewModels
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel

class MeeraMomentSettingsNotShowAddUserFragment : MeeraSettingsUserSearchFragment() {

    private val viewModel by viewModels<MomentSettingsNotShowAddUserViewModel> { App.component.getViewModelFactory() }

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return viewModel
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        screenTitle = getString(R.string.moments_settings_not_show_title),
        isRequestGetUsers = true
    )
}
