package com.numplates.nomera3.modules.registration.domain

import com.meera.db.models.usersettings.PrivacySettingDto
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.data.repository.RegistrationUserDataRepository
import javax.inject.Inject

class UploadUserDataUseCase @Inject constructor(
    private val repository: RegistrationUserDataRepository
) : BaseUseCaseCoroutine<UploadUserDataParams, UserProfileNew> {
    override suspend fun execute(
        params: UploadUserDataParams,
        success: (UserProfileNew) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.uploadUserData(
            userData = params.userData,
            settings = params.settings,
            success = success,
            fail = fail
        )
    }
}

data class UploadUserDataParams(
    val userData: RegistrationUserData,
    val settings: List<PrivacySettingDto>? = null
) : DefParams()
