package com.numplates.nomera3.modules.registration.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.UploadUserDataParams
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class RegistrationBaseViewModel : BaseViewModel() {

    val progressLiveData = MutableLiveData<Boolean>()

    abstract val userDataUseCase: UserDataUseCase
    abstract val uploadUserUseCase: UploadUserDataUseCase
    private val settings = mutableListOf<PrivacySettingDto>()

    protected fun setSettings(privacySettings: PrivacySettingDto) {
        settings.removeAll { it.key == privacySettings.key }
        settings.add(privacySettings)
    }

    protected fun progress(inProgress: Boolean) {
        progressLiveData.postValue(inProgress)
    }

    fun initUserData() {
        progress(true)
        viewModelScope.launch(Dispatchers.IO) {
            userDataUseCase.execute(
                params = DefParams(),
                success = {
                    userDataInitialized()
                    progress(false)
                },
                fail = {
                    progress(false)
                    Timber.e(it)
                }
            )
        }
    }

    abstract fun userDataInitialized()

    fun uploadUserData(user: RegistrationUserData, fail: ((Throwable) -> Unit)? = null) {
        progress(true)
        viewModelScope.launch(Dispatchers.IO) {
            uploadUserUseCase.execute(
                params = UploadUserDataParams(user, settings),
                success = {
                    uploadSuccess(it)
                    progress(false)
                },
                fail = {
                    fail?.invoke(it)
                    progress(false)
                    Timber.e(it)
                }
            )
        }
    }

    abstract fun uploadSuccess(userProfile: UserProfileNew)
}
