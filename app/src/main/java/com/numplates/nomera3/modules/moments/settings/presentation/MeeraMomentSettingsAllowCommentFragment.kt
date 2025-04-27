package com.numplates.nomera3.modules.moments.settings.presentation

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.meera.core.extensions.empty
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserTypeFragment

class MeeraMomentSettingsAllowCommentFragment : MeeraBaseSettingsUserTypeFragment() {
    private val viewModel by viewModels<MomentSettingsViewModel> { App.component.getViewModelFactory() }

    override fun screenTitle(): String = getString(R.string.moment_settings_allow_comments)

    override fun settingTypeTitle(): String = String.empty()

    override fun actionDescription(): String = getString(R.string.moment_settings_allow_comment_description)

    override fun actionTransitBlacklist(userCount: Int?) = Unit

    override fun actionTransitWhitelist(userCount: Int?) = Unit

    override fun sendSettingUserType(typeEnum: SettingsUserTypeEnum) {
        viewModel.setSetting(SettingsKeyEnum.MOMENTS_ALLOW_COMMENT, typeEnum)
    }

    override fun refreshCounters() = Unit

    override fun hasBlackWhiteLists(): Boolean = false

    companion object {
        const val MOMENT_SETTINGS_ALLOW_COMMENT_BOTTOM_DIALOG_TAG = "momentSettingsAllowCommentBottomDialog"

        @JvmStatic
        fun show(fragmentManager: FragmentManager, args: Bundle): MeeraMomentSettingsAllowCommentFragment {
            val instance = MeeraMomentSettingsAllowCommentFragment()
            instance.arguments = args
            instance.show(fragmentManager, MOMENT_SETTINGS_ALLOW_COMMENT_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
