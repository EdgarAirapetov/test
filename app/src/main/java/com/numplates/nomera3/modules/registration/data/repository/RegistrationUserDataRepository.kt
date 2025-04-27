package com.numplates.nomera3.modules.registration.data.repository

import com.meera.db.models.usersettings.PrivacySettingDto
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import java.lang.Exception

interface RegistrationUserDataRepository {

    suspend fun uploadUserData(
      userData: RegistrationUserData,
      settings: List<PrivacySettingDto>?,
      success: (userProfile: UserProfileNew) -> Unit,
      fail: (e: Exception) -> Unit
    )
}
