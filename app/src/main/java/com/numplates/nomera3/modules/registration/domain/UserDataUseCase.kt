package com.numplates.nomera3.modules.registration.domain

import com.meera.db.models.userprofile.Country
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.Avatar
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.ui.birthday.toRegistrationBirthdayString
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import timber.log.Timber

class UserDataUseCase(
    private val profileRepository: ProfileRepository
) : BaseUseCaseCoroutine<DefParams, RegistrationUserData> {

    var userData: RegistrationUserData? = null

    fun clear() {
        userData = null
    }

    override suspend fun execute(
        params: DefParams,
        success: (RegistrationUserData) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        if (userData == null) {
            val profile = try {
                profileRepository.requestOwnProfileSynch()
            } catch (e: Exception) {
                Timber.e(e)
                fail.invoke(e)
                return
            }

            userData = RegistrationUserData(
                name = profile.name,
                birthday = profile.birthday?.toRegistrationBirthdayString(),
                hideAge = profile.hideBirthday == 1,
                gender = profile.gender,
                hideGender = profile.hideGender == 1,
                country = profile.country?.map(),
                city = profile.city?.map(profile.country?.id?.toInt() ?: 0),
                uniqueName = profile.uniquename,
                avatar = Avatar(
                    big = profile.avatarBig,
                    small = profile.avatarSmall,
                    animation = profile.avatarAnimation
                ),
                avatarGender = profile.gender
            )
            userData?.let { success(it) }
        } else {
            userData?.let { success(it) }
        }
    }

    private fun Country.map(): com.numplates.nomera3.data.network.Country {
        return com.numplates.nomera3.data.network.Country(
            name = this.name,
            countryId = this.id?.toInt()
        )
    }

    private fun com.meera.db.models.userprofile.City.map(countryId: Int): City {
        return City(
            title_ = this.name,
            name = this.name,
            countryId = countryId,
            cityId = this.id?.toInt() ?: 0
        )
    }
}
