package com.numplates.nomera3.modules.registration.ui.gender

import androidx.lifecycle.MutableLiveData
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationGender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationGenderHide
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.registration.ui.RegistrationBaseViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_GENDER
import javax.inject.Inject

class RegistrationGenderViewModel : RegistrationBaseViewModel() {

    val liveData = MutableLiveData<RegistrationGenderViewEvent>()

    @Inject
    lateinit var userUseCase: UserDataUseCase

    @Inject
    lateinit var uploadUserDataUseCase: UploadUserDataUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    init {
        App.getRegistrationComponent().inject(this)
    }

    fun setGender(gender: Int) {
        userUseCase.userData?.gender = gender
        event(RegistrationGenderViewEvent.SetContinueButtonAvailable(userUseCase.userData?.gender != null))
    }

    fun setHideGender(hide: Boolean) {
        userUseCase.userData?.hideGender = hide
        val settingsValue = if (userUseCase.userData?.hideGender == true) 0 else 1
        setSettings(PrivacySettingDto(SHOW_GENDER.key, settingsValue))
    }

    fun continueClicked() =
        uploadUserData(RegistrationUserData(gender = userUseCase.userData?.gender))

    fun clearAll() {
        event(RegistrationGenderViewEvent.None)
    }

    private fun event(event: RegistrationGenderViewEvent) {
        liveData.postValue(event)
    }

    override val userDataUseCase: UserDataUseCase
        get() = userUseCase

    override val uploadUserUseCase: UploadUserDataUseCase
        get() = uploadUserDataUseCase

    override fun userDataInitialized() {
        event(
            RegistrationGenderViewEvent.Gender(
                gender = userUseCase.userData?.gender,
                hideGender = userUseCase.userData?.hideGender == true,
                hiddenAgeAndGender = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled
            )
        )
    }

    override fun uploadSuccess(userProfile: UserProfileNew) {
        val genderSelected = if (userUseCase.userData?.gender == RegistrationUserData.GENDER_MALE) {
            AmplitudePropertyRegistrationGender.MALE
        } else {
            AmplitudePropertyRegistrationGender.FEMALE
        }
        val hide = if (userUseCase.userData?.hideGender == true) {
            AmplitudePropertyRegistrationGenderHide.TRUE
        } else {
            AmplitudePropertyRegistrationGenderHide.FALSE
        }
        amplitudeHelper.logRegistrationGenderSelected(genderSelected, hide)
        event(RegistrationGenderViewEvent.GoToNextStep)
    }
}
