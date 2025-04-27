package com.numplates.nomera3.modules.moments.settings.hidefrom.presentation

import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.settings.presentation.MOMENT_SETTINGS_REFRESH_NETWORK
import com.numplates.nomera3.modules.moments.settings.presentation.REFRESH_RESULT_EMPTY_KEY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraSettingsUserSearchFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel

class MeeraMomentSettingsHideFromAddUserFragment : MeeraSettingsUserSearchFragment() {

    private val viewModel by viewModels<MomentSettingsHideFromAddUserViewModel> { App.component.getViewModelFactory() }

    override fun getViewModel(): BaseSettingsUserSearchViewModel {
        return viewModel
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        getString(R.string.moments_carousel_add),
        true
    )

    override fun onDestroy() {
        super.onDestroy()
        setFragmentResult(
            MOMENT_SETTINGS_REFRESH_NETWORK,
            bundleOf(REFRESH_RESULT_EMPTY_KEY to String.empty())
        )
    }
}
