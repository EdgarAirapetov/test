package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectPrivacyShowFriends
import com.numplates.nomera3.modules.user.domain.usecase.SetPreferencesFriendsFollowersPrivacyUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams.PrivacySettingsParams
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.model.enums.toAmplitudePropertySettingVisibility
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ShowFriendsSubscribersPrivacyViewModel @Inject constructor(
    private val setUserPrivacyUseCase: SetSettingsUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setPreferencesFriendsFollowersPrivacyUseCase: SetPreferencesFriendsFollowersPrivacyUseCase,
    private val amplitudeHelper: AmplitudeSelectPrivacyShowFriends
) : BaseViewModel() {

    fun setSetting(key: SettingsKeyEnum, value: SettingsUserTypeEnum, shouldUpdate: Boolean) {
        Timber.e("SEND setting to server: key:$key  value:$value")
        setUserPrivacyUseCase.invoke(
            PrivacySettingsParams(key, value)
        ).invokeOnCompletion { error ->
            if (error == null && shouldUpdate) requestSettings()
        }
    }

    fun requestSettings() {
        Timber.e("Request settings from SERVER")
        viewModelScope.launch(SupervisorJob()) {
            runCatching {
                getSettingsUseCase.invoke()
            }.onFailure {
                Timber.e("Error load privacy settings")
            }
        }
    }

    fun logAmplitudeFriendsPrivacySelected(privacyType: SettingsUserTypeEnum) {
        val amplitudeActionType = privacyType.toAmplitudePropertySettingVisibility()
        amplitudeHelper.onShowMutualFriendsPrivacySelected(amplitudeActionType)
    }

    fun setPreferencesPrivacy(settingsUserType: SettingsUserTypeEnum) {
        setPreferencesFriendsFollowersPrivacyUseCase.invoke(settingsUserType.key)
    }
}
