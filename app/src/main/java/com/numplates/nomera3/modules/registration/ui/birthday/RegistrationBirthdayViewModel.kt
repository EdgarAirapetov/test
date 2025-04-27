package com.numplates.nomera3.modules.registration.ui.birthday

import androidx.lifecycle.MutableLiveData
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationBirthdayHide
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.registration.ui.RegistrationBaseViewModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum.SHOW_BIRTHDAY
import javax.inject.Inject

class RegistrationBirthdayViewModel : RegistrationBaseViewModel() {

    val eventLiveData = MutableLiveData<RegistrationBirthdayViewEvent>()

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

    fun isHiddenAge() = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    fun setBirthday(birthday: String?) {
        userUseCase.userData?.birthday = birthday
    }

    fun setHideAge(hide: Boolean) {
        userUseCase.userData?.hideAge = hide
        val settingsValue = if (hide) 0 else 1
        setSettings(PrivacySettingDto(SHOW_BIRTHDAY.key, settingsValue))
    }

    fun continueClicked() =
        uploadUserData(RegistrationUserData(birthday = userUseCase.userData?.birthday))

    fun clearAll() {
        event(RegistrationBirthdayViewEvent.None)
    }

    private fun event(event: RegistrationBirthdayViewEvent) {
        eventLiveData.postValue(event)
    }

    override val userDataUseCase: UserDataUseCase
        get() = userUseCase

    override val uploadUserUseCase: UploadUserDataUseCase
        get() = uploadUserDataUseCase

    override fun userDataInitialized() {
        event(
            RegistrationBirthdayViewEvent.BirthdayData(
                userUseCase.userData?.birthday,
                userUseCase.userData?.hideAge == true
            )
        )
    }

    override fun uploadSuccess(userProfile: UserProfileNew) {
        userUseCase.userData?.birthday?.let { birthday ->
            val hide =
                if (userUseCase.userData?.hideAge == true) AmplitudePropertyRegistrationBirthdayHide.TRUE
                else AmplitudePropertyRegistrationBirthdayHide.FALSE
            amplitudeHelper.logRegistrationBirthdayEntered(birthday.getValidAge(), hide)
        }
        event(RegistrationBirthdayViewEvent.GoToNextStep)
    }
}
