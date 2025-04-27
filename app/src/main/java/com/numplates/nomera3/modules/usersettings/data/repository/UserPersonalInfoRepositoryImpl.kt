package com.numplates.nomera3.modules.usersettings.data.repository

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.usersettings.domain.repository.UserPersonalInfoRepository
import javax.inject.Inject

class UserPersonalInfoRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings
) : UserPersonalInfoRepository {

    override fun setPreferencesBirthdayDate(birthday: Int) {
        appSettings.birthdayFlag = birthday.toLong()
    }
}
