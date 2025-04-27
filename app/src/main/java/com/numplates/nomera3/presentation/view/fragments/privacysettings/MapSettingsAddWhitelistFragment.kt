package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model.MapVisibilitySettingsListType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistChangeCountParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistInitParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistLogDataParams
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.SettingsUserSearchFragmentImpl
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsAddWhitelistViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent

class MapSettingsAddWhitelistFragment : SettingsUserSearchFragmentImpl(), IOnBackPressed {

    private val viewModel by viewModels<MapSettingsAddWhitelistViewModel>()
    private var origin: MapVisibilitySettingsOrigin? = null

    /** Setup logging logic before parent's logic to get success event before exit **/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (arguments?.getSerializable(MapVisibilitySettingsOrigin.ARG) as? MapVisibilitySettingsOrigin)?.let {
            origin = it
            viewModel.mapVisibilityAnalyticsSettingsInitUseCase.execute(
                MapVisibilityBlacklistInitParams(origin, MapVisibilitySettingsListType.WHITELIST)
            )
        }
        viewModel.liveViewEvent.observe(viewLifecycleOwner) { event ->
            if (event is ListUsersSearchViewEvent.OnAddUsersDone) {
                viewModel.mapVisibilitySettingsAnalyticsChangeCountUseCase.execute(
                    MapVisibilityBlacklistChangeCountParams(addCount = event.checkedCount)
                )
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getViewModel(): BaseSettingsUserSearchViewModel = viewModel

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        getString(R.string.general_add),
        true
    )

    override fun onBackPressed(): Boolean {
        origin?.let {
            viewModel.mapVisibilitySettingsAnalyticsLogDataUseCase.execute(
                MapVisibilityBlacklistLogDataParams(false)
            )
        }
        return false
    }
}
