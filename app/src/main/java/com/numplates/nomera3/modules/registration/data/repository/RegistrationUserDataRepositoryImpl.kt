package com.numplates.nomera3.modules.registration.data.repository

import com.meera.db.models.userprofile.UserProfileNew
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.modules.registration.data.NameContainsProfanityException
import com.numplates.nomera3.modules.registration.data.RegistrationApi
import com.numplates.nomera3.modules.registration.data.RegistrationData
import com.numplates.nomera3.modules.registration.data.RegistrationRequest
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.data.UserDataNotUploadedException
import com.numplates.nomera3.modules.registration.ui.birthday.parsedDate
import com.numplates.nomera3.modules.registration.ui.birthday.toSeconds
import retrofit2.HttpException

private const val CODE_NAME_ERROR = 422

class RegistrationUserDataRepositoryImpl(
    private val api: RegistrationApi
): RegistrationUserDataRepository {
    override suspend fun uploadUserData(
        userData: RegistrationUserData,
        settings: List<PrivacySettingDto>?,
        success: (userProfile: UserProfileNew) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            val user = api.uploadUserData(RegistrationRequest(userData.toRequestBody(), settings)).data
            if (user != null) success(user)
            else fail(UserDataNotUploadedException())
        } catch (e: HttpException) {
            if (e.code() == CODE_NAME_ERROR) {
                fail(NameContainsProfanityException())
            } else {
                fail(e)
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    private fun RegistrationUserData.toRequestBody(): RegistrationData {
        return RegistrationData(
            name = this.name,
            birthday = this.birthday?.parsedDate()?.time?.toSeconds(),
            gender = this.gender,
            countryId = this.country?.countryId,
            cityId = this.city?.cityId,
            uniqueName = this.uniqueName,
            avatar = this.avatar
        )
    }
}
