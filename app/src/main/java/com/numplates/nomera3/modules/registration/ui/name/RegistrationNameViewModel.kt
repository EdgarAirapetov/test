package com.numplates.nomera3.modules.registration.ui.name

import androidx.lifecycle.MutableLiveData
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.registration.data.NameContainsProfanityException
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.registration.ui.RegistrationBaseViewModel
import javax.inject.Inject

class RegistrationNameViewModel: RegistrationBaseViewModel() {

    val liveData = MutableLiveData<RegistrationNameViewEvent>()

    @Inject
    lateinit var userUseCase: UserDataUseCase

    @Inject
    lateinit var uploadUserDataUseCase: UploadUserDataUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    init {
        App.getRegistrationComponent().inject(this)
    }

    fun setName(name: String?) {
        userUseCase.userData?.name = name
        if (!userUseCase.userData?.name.isNullOrEmpty()) event(RegistrationNameViewEvent.NameAccepted)
        else event(RegistrationNameViewEvent.NameNotAccepted)
    }

    fun continueClicked() = uploadUserData(RegistrationUserData(name = userUseCase.userData?.name)) { throwable ->
        if (throwable is NameContainsProfanityException) {
            event(RegistrationNameViewEvent.NameError(R.string.meera_registration_profanity_in_name_error))
        }
    }

    fun clearAll() {
        event(RegistrationNameViewEvent.None)
    }

    private fun event(event: RegistrationNameViewEvent) {
        liveData.postValue(event)
    }

    override val userDataUseCase: UserDataUseCase
        get() = userUseCase

    override val uploadUserUseCase: UploadUserDataUseCase
        get() = uploadUserDataUseCase

    override fun userDataInitialized() {
        event(RegistrationNameViewEvent.Name(userUseCase.userData?.name))
    }

    override fun uploadSuccess(userProfile: UserProfileNew) {
        amplitudeHelper.logRegistrationNameEntered()
        event(RegistrationNameViewEvent.GoToNextStep)
    }
}
